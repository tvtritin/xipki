/*
 *
 * This file is part of the XiPKI project.
 * Copyright (c) 2014 - 2015 Lijun Liao
 * Author: Lijun Liao
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * THE AUTHOR LIJUN LIAO. LIJUN LIAO DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the XiPKI software without
 * disclosing the source code of your own applications.
 *
 * For more information, please contact Lijun Liao at this
 * address: lijun.liao@gmail.com
 */

package org.xipki.security.shell.p11;

import iaik.pkcs.pkcs11.objects.DSAPublicKey;
import iaik.pkcs.pkcs11.objects.ECDSAPublicKey;
import iaik.pkcs.pkcs11.objects.PrivateKey;
import iaik.pkcs.pkcs11.objects.PublicKey;
import iaik.pkcs.pkcs11.objects.RSAPublicKey;
import iaik.pkcs.pkcs11.objects.X509PublicKeyCertificate;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTNamedCurves;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.teletrust.TeleTrusTNamedCurves;
import org.bouncycastle.asn1.x9.X962NamedCurves;
import org.bouncycastle.util.encoders.Hex;
import org.xipki.common.SecurityUtil;
import org.xipki.security.api.SecurityFactory;
import org.xipki.security.api.p11.P11Identity;
import org.xipki.security.api.p11.P11KeyIdentifier;
import org.xipki.security.api.p11.P11Module;
import org.xipki.security.api.p11.P11SlotIdentifier;
import org.xipki.security.api.p11.P11WritableSlot;
import org.xipki.security.p11.iaik.IaikP11Slot;
import org.xipki.security.p11.keystore.KeystoreP11Slot;
import org.xipki.security.shell.SecurityCommand;

/**
 * @author Lijun Liao
 */

@Command(scope = "xipki-tk", name = "list", description="List objects in PKCS#11 device")
public class P11ListSlotCommand extends SecurityCommand
{
    @Option(name = "-v", aliases="--verbose",
            required = false, description = "Show object information verbosely")
    protected Boolean verbose = Boolean.FALSE;

    @Option(name = "-module",
            required = false, description = "Name of the PKCS#11 module.")
    protected String moduleName = SecurityFactory.DEFAULT_P11MODULE_NAME;

    @Option(name = "-slot",
            required = false, description = "Slot index")
    protected Integer slotIndex;

    @Override
    protected Object doExecute()
    throws Exception
    {
        P11Module module = getP11Module(moduleName);
        if(module == null)
        {
            err("Undefined module " + moduleName);
            return null;
        }

        out("Module: " + moduleName);
        List<P11SlotIdentifier> slots = module.getSlotIdentifiers();
        if(slotIndex == null)
        {
            output(slots);
            return null;
        }

        P11SlotIdentifier slotId = new P11SlotIdentifier(slotIndex, null);
        P11WritableSlot p11slot = module.getSlot(slotId);
        if(p11slot == null)
        {
            err("slot with index " + slotIndex + " does not exist");
            return null;
        }

        if(p11slot instanceof IaikP11Slot)
        {
            IaikP11Slot slot = (IaikP11Slot) p11slot;
            List<PrivateKey> allPrivateObjects = slot.getAllPrivateObjects(null, null);
            int size = allPrivateObjects.size();

            List<ComparableIaikPrivateKey> privateKeys = new ArrayList<>(size);
            for(int i = 0; i < size; i++)
            {
                PrivateKey key = allPrivateObjects.get(i);
                byte[] id = key.getId().getByteArrayValue();
                if(id != null)
                {
                    char[] label = key.getLabel().getCharArrayValue();
                    ComparableIaikPrivateKey privKey = new ComparableIaikPrivateKey(id, label);
                    privateKeys.add(privKey);
                }
            }

            Collections.sort(privateKeys);
            size = privateKeys.size();

            List<X509PublicKeyCertificate> allCertObjects = slot.getAllCertificateObjects();

            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < size; i++)
            {
                ComparableIaikPrivateKey privKey = privateKeys.get(i);
                byte[] keyId = privKey.getKeyId();
                char[] keyLabel = privKey.getKeyLabel();

                PublicKey pubKey = slot.getPublicKeyObject(null, null, keyId, keyLabel);
                sb.append("\t")
                    .append(i + 1)
                    .append(". ")
                    .append(privKey.getKeyLabelAsText())
                    .append(" (").append("id: ")
                    .append(Hex.toHexString(privKey.getKeyId()).toUpperCase())
                    .append(")\n");

                sb.append("\t\tAlgorithm: ")
                    .append(getKeyAlgorithm(pubKey))
                    .append("\n");

                X509PublicKeyCertificate cert = removeCertificateObject(allCertObjects, keyId, keyLabel);
                if(cert == null)
                {
                    sb.append("\t\tCertificate: NONE\n");
                }
                else
                {
                    formatString(sb, cert);
                }
            }

            for(int i = 0; i < allCertObjects.size(); i++)
            {
                X509PublicKeyCertificate certObj = allCertObjects.get(i);
                sb.append("\tCert-")
                    .append(i + 1)
                    .append(". ")
                    .append(certObj.getLabel().getCharArrayValue())
                    .append(" (").append("id: ")
                    .append(Hex.toHexString(certObj.getId().getByteArrayValue()).toUpperCase())
                    .append(")\n");

                formatString(sb, certObj);
            }

            if(sb.length() > 0)
            {
                out(sb.toString());
            }
        } else if(p11slot instanceof KeystoreP11Slot)
        {
            KeystoreP11Slot slot = (KeystoreP11Slot) p11slot;

            List<? extends P11Identity> identities = slot.getP11Identities();

            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < identities.size(); i++)
            {
                P11Identity identity = identities.get(i);
                P11KeyIdentifier p11KeyId = identity.getKeyId();

                sb.append("\t")
                    .append(i + 1)
                    .append(". ")
                    .append(p11KeyId.getKeyLabel())
                    .append(" (").append("id: ")
                    .append(Hex.toHexString(p11KeyId.getKeyId()).toUpperCase())
                    .append(")\n");

                sb.append("\t\tAlgorithm: ")
                    .append(identity.getPublicKey().getAlgorithm())
                    .append("\n");

                formatString(sb, identity.getCertificate());
            }

            if(sb.length() > 0)
            {
                out(sb.toString());
            }
        } else
        {
            err("should not reach here");
        }

        return null;
    }

    private static X509PublicKeyCertificate removeCertificateObject(List<X509PublicKeyCertificate> certificateObjects,
            byte[] keyId, char[] keyLabel)
    {
        X509PublicKeyCertificate cert = null;
        for(X509PublicKeyCertificate certObj : certificateObjects)
        {
            if(keyId != null &&
                    (Arrays.equals(keyId, certObj.getId().getByteArrayValue()) == false))
            {
                continue;
            }

            if(keyLabel != null &&
                    (Arrays.equals(keyLabel, certObj.getLabel().getCharArrayValue()) == false))
            {
                continue;
            }

            cert = certObj;
            break;
        }

        if(cert != null)
        {
            certificateObjects.remove(cert);
        }

        return cert;
    }

    private void formatString(StringBuilder sb, X509PublicKeyCertificate cert)
    {
        byte[] bytes = cert.getSubject().getByteArrayValue();
        String subject;
        try
        {
            X500Principal x500Prin = new X500Principal(bytes);
            subject = SecurityUtil.getRFC4519Name(x500Prin);
        }catch(Exception e)
        {
            subject = new String(bytes);
        }

        if(verbose.booleanValue() == false)
        {
            sb.append("\t\tCertificate: ").append(subject).append("\n");
            return;
        }

        sb.append("\t\tCertificate:\n");
        sb.append("\t\t\tSubject:    ")
            .append(subject)
            .append("\n");

        bytes = cert.getIssuer().getByteArrayValue();
        String issuer;
        try
        {
            X500Principal x500Prin = new X500Principal(bytes);
            issuer = SecurityUtil.getRFC4519Name(x500Prin);
        }catch(Exception e)
        {
            issuer = new String(bytes);
        }
        sb.append("\t\t\tIssuer:     ")
            .append(issuer)
            .append("\n");

        byte[] certBytes = cert.getValue().getByteArrayValue();

        X509Certificate x509Cert = null;
        try
        {
            x509Cert = SecurityUtil.parseCert(certBytes);
        } catch (Exception e)
        {
            sb.append("\t\t\tError: " + e.getMessage());
            return;
        }

        sb.append("\t\t\tSerial:     ")
            .append(x509Cert.getSerialNumber())
            .append("\n");
        sb.append("\t\t\tStart time: ")
            .append(x509Cert.getNotBefore())
            .append("\n");
        sb.append("\t\t\tEnd time:   ")
            .append(x509Cert.getNotAfter())
            .append("\n");
        sb.append("\t\t\tSHA1 Sum:   ")
            .append(SecurityUtil.sha1sum(certBytes))
            .append("\n");
    }

    private void formatString(StringBuilder sb, X509Certificate cert)
    {
        String subject = SecurityUtil.getRFC4519Name(cert.getSubjectX500Principal());

        if(verbose.booleanValue() == false)
        {
            sb.append("\t\tCertificate: ").append(subject).append("\n");
            return;
        }

        sb.append("\t\tCertificate:\n");
        sb.append("\t\t\tSubject:    ")
            .append(subject)
            .append("\n");

        String issuer = SecurityUtil.getRFC4519Name(cert.getIssuerX500Principal());
        sb.append("\t\t\tIssuer:     ")
            .append(issuer)
            .append("\n");

        sb.append("\t\t\tSerial:     ")
            .append(cert.getSerialNumber())
            .append("\n");
        sb.append("\t\t\tStart time: ")
            .append(cert.getNotBefore())
            .append("\n");
        sb.append("\t\t\tEnd time:   ")
            .append(cert.getNotAfter())
            .append("\n");
        sb.append("\t\t\tSHA1 Sum:   ");
        try
        {
            sb.append(SecurityUtil.sha1sum(cert.getEncoded()));
        } catch (CertificateEncodingException e)
        {
            sb.append("ERROR");
        }
        sb.append("\n");
    }

    private static String getKeyAlgorithm(PublicKey key)
    {
        if(key instanceof RSAPublicKey)
        {
            return "RSA";
        }
        else if(key instanceof ECDSAPublicKey)
        {
            byte[] paramBytes = ((ECDSAPublicKey) key).getEcdsaParams().getByteArrayValue();
            if(paramBytes.length < 50)
            {
                try
                {
                    ASN1ObjectIdentifier curveId = (ASN1ObjectIdentifier) ASN1ObjectIdentifier.fromByteArray(paramBytes);
                    String curveName = getCurveName(curveId);
                    return "EC (named curve " + curveName + ")";
                }catch(Exception e)
                {
                    return "EC";
                }
            }
            else
            {
                return "EC (specified curve)";
            }
        }
        else if(key instanceof DSAPublicKey)
        {
            return "DSA";
        }
        else
        {
            return "UNKNOWN";
        }
    }

    private static String getCurveName(ASN1ObjectIdentifier curveId)
    {
        String curveName = X962NamedCurves.getName(curveId);

        if (curveName == null)
        {
            curveName = SECNamedCurves.getName(curveId);
        }

        if (curveName == null)
        {
            curveName = TeleTrusTNamedCurves.getName(curveId);
        }

        if (curveName == null)
        {
            curveName = NISTNamedCurves.getName(curveId);
        }

        return curveName;
    }

    private static class ComparableIaikPrivateKey implements Comparable<ComparableIaikPrivateKey>
    {
        private final byte[] keyId;
        private final char[] keyLabel;

        public ComparableIaikPrivateKey(byte[] keyId, char[] keyLabel)
        {
            this.keyId = keyId;
            this.keyLabel = keyLabel;
        }

        @Override
        public int compareTo(ComparableIaikPrivateKey o)
        {
            if(keyLabel == null)
            {
                if(o.keyLabel == null)
                {
                    return 0;
                }
                else
                {
                    return 1;
                }
            }
            else
            {
                if(o.keyLabel == null)
                {
                    return -1;
                }
                else
                {
                    return new String(keyLabel).compareTo(new String(o.keyLabel));
                }
            }
        }

        public byte[] getKeyId()
        {
            return keyId;
        }

        public char[] getKeyLabel()
        {
            return keyLabel;
        }

        public String getKeyLabelAsText()
        {
            return keyLabel == null ? null : new String(keyLabel);
        }
    }

    private void output(List<P11SlotIdentifier> slots)
    {
        if(slotIndex == null)
        {
            // list all slots
            int n = slots.size();

            if(n == 0 || n == 1)
            {
                out(((n == 0) ? "no" : "1") + " slot is configured");
            }
            else
            {
                out(n + " slots are configured");
            }

            for(P11SlotIdentifier slotId : slots)
            {
                out("\tslot[" + slotId.getSlotIndex() + "]: " + slotId.getSlotId());
            }
        }
    }

}
