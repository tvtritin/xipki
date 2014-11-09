/*
 *
 * This file is part of the XiPKI project.
 * Copyright (c) 2014 Lijun Liao
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

package org.xipki.security.shell;

import iaik.pkcs.pkcs11.Session;
import iaik.pkcs.pkcs11.objects.Object;
import iaik.pkcs.pkcs11.objects.X509PublicKeyCertificate;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.bouncycastle.util.encoders.Hex;
import org.xipki.security.api.SecurityFactory;
import org.xipki.security.api.SignerException;
import org.xipki.security.api.p11.P11SlotIdentifier;
import org.xipki.security.p11.iaik.IaikExtendedModule;
import org.xipki.security.p11.iaik.IaikExtendedSlot;

/**
 * @author Lijun Liao
 */

@Command(scope = "keytool", name = "rm-cert", description="Remove certificate from PKCS#11 device")
public class P11CertDeleteCommand extends SecurityCommand
{
    @Option(name = "-slot",
            required = true, description = "Required. Slot index")
    protected Integer slotIndex;

    @Option(name = "-key-id",
            required = true, description = "Required. Id of the certificate in the PKCS#11 device")
    protected String keyId;

    @Option(name = "-module",
            required = false, description = "Name of the PKCS#11 module.")
    protected String moduleName = SecurityFactory.DEFAULT_P11MODULE_NAME;

    @Override
    protected Object doExecute()
    throws Exception
    {
        IaikExtendedModule module = getIaikModule(moduleName);

        IaikExtendedSlot slot = null;
        try
        {
            slot = module.getSlot(new P11SlotIdentifier(slotIndex, null));
        }catch(SignerException e)
        {
            err("ERROR:  " + e.getMessage());
            return null;
        }

        X509PublicKeyCertificate[] existingCerts = slot.getCertificateObjects(
                Hex.decode(keyId), null);

        if(existingCerts == null || existingCerts.length == 0)
        {
            err("Could not find certificates with id " + keyId);
            return null;
        }

        Session session = slot.borrowWritableSession();
        try
        {
            for(X509PublicKeyCertificate cert : existingCerts)
            {
                session.destroyObject(cert);
            }
        }finally
        {
            slot.returnWritableSession(session);
        }

        securityFactory.getP11CryptService(moduleName).refresh();
        int n = existingCerts.length;
        out("Deleted " + n + " certificate" + (n > 1 ? "s" : ""));
        return null;
    }

}
