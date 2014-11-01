/*
 * Copyright (c) 2014 Lijun Liao
 *
 * TO-BE-DEFINE
 *
 */

package org.xipki.security.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xipki.common.ParamChecker;
import org.xipki.security.api.SecurityFactory;
import org.xipki.security.api.SignerException;
import org.xipki.security.api.p11.P11CryptService;
import org.xipki.security.api.p11.P11KeyIdentifier;
import org.xipki.security.api.p11.P11SlotIdentifier;

/**
 * @author Lijun Liao
 */

public class XiPKIKeyStoreSpi extends KeyStoreSpi
{
    private static SecurityFactory securityFactory;

    private Date creationDate;

    private static class MyEnumeration<E> implements Enumeration<E>
    {
        private Iterator<E> iter;

        public MyEnumeration(Iterator<E> iter)
        {
            this.iter = iter;
        }

        @Override
        public boolean hasMoreElements()
        {
            return iter.hasNext();
        }

        @Override
        public E nextElement()
        {
            return iter.next();
        }
    }

    private static class KeyCertEntry
    {
        private PrivateKey key;
        private Certificate[] chain;

        public KeyCertEntry(PrivateKey key, Certificate[] chain)
        {
            ParamChecker.assertNotNull("key", key);
            ParamChecker.assertNotNull("chain", chain);
            if(chain.length < 1)
            {
                throw new IllegalArgumentException("chain does not contain any certificate");
            }
            this.key = key;
            this.chain = chain;
        }

        PrivateKey getKey()
        {
            return key;
        }

        Certificate[] getCertificateChain()
        {
            return Arrays.copyOf(chain, chain.length);
        }

        Certificate getCertificate()
        {
            return chain[0];
        }
    }

    private Map<String, KeyCertEntry> keyCerts = new HashMap<>();

    @Override
    public void engineLoad(InputStream stream, char[] password)
    throws IOException, NoSuchAlgorithmException, CertificateException
    {
        this.creationDate = new Date();

        try
        {
            P11CryptService p11Servcie = securityFactory.getP11CryptService(SecurityFactory.DEFAULT_P11MODULE_NAME);
            P11SlotIdentifier[] slotIds = p11Servcie.getSlotIdentifiers();

            Map<P11SlotIdentifier, String[]> keyLabelsMap = new HashMap<>();

            Set<String> allKeyLabels = new HashSet<>();
            Set<String> duplicatedKeyLabels = new HashSet<>();

            for(P11SlotIdentifier slotId: slotIds)
            {
                String[] keyLabels = p11Servcie.getKeyLabels(slotId);
                for(String keyLabel : keyLabels)
                {
                    if(allKeyLabels.contains(keyLabel))
                    {
                        duplicatedKeyLabels.add(keyLabel);
                    }
                    allKeyLabels.add(keyLabel);
                }

                keyLabelsMap.put(slotId, keyLabels);
            }

            for(P11SlotIdentifier slotId: slotIds)
            {
                String[] keyLabels = keyLabelsMap.get(slotId);
                for(String keyLabel : keyLabels)
                {
                    String alias = keyLabel;
                    if(duplicatedKeyLabels.contains(keyLabel))
                    {
                        alias += "-slot" + slotId.getSlotIndex();
                    }

                    P11KeyIdentifier keyId = new P11KeyIdentifier(keyLabel);
                    P11PrivateKey key = new P11PrivateKey(p11Servcie, slotId, keyId);
                    X509Certificate[] chain = p11Servcie.getCertificates(slotId, keyId);

                    KeyCertEntry keyCertEntry = new KeyCertEntry(key, chain);
                    keyCerts.put(alias, keyCertEntry);
                }
            }
        } catch (SignerException | InvalidKeyException e)
        {
            throw new IllegalArgumentException(e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void engineStore(OutputStream stream, char[] password)
    throws IOException, NoSuchAlgorithmException, CertificateException
    {
    }

    @Override
    public Key engineGetKey(String alias, char[] password)
    throws NoSuchAlgorithmException, UnrecoverableKeyException
    {
        if(keyCerts.containsKey(alias) == false)
        {
            return null;
        }

        return keyCerts.get(alias).getKey();
    }

    @Override
    public Certificate[] engineGetCertificateChain(String alias)
    {
        if(keyCerts.containsKey(alias) == false)
        {
            return null;
        }

        return keyCerts.get(alias).getCertificateChain();
    }

    @Override
    public Certificate engineGetCertificate(String alias)
    {
        if(keyCerts.containsKey(alias) == false)
        {
            return null;
        }

        return keyCerts.get(alias).getCertificate();
    }

    @Override
    public Date engineGetCreationDate(String alias)
    {
        if(keyCerts.containsKey(alias) == false)
        {
            return null;
        }
        return creationDate;
    }

    @Override
    public void engineSetKeyEntry(String alias, Key key, char[] password,
            Certificate[] chain)
    throws KeyStoreException
    {
        throw new KeyStoreException("Keystore is read only");
    }

    @Override
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain)
    throws KeyStoreException
    {
        throw new KeyStoreException("Keystore is read only");
    }

    @Override
    public void engineSetCertificateEntry(String alias, Certificate cert)
    throws KeyStoreException
    {
        throw new KeyStoreException("Keystore is read only");
    }

    @Override
    public void engineDeleteEntry(String alias)
    throws KeyStoreException
    {
        throw new KeyStoreException("Keystore is read only");
    }

    @Override
    public Enumeration<String> engineAliases()
    {
        return new MyEnumeration<>(keyCerts.keySet().iterator());
    }

    @Override
    public boolean engineContainsAlias(String alias)
    {
        return keyCerts.containsKey(alias);
    }

    @Override
    public int engineSize()
    {
        return keyCerts.size();
    }

    @Override
    public boolean engineIsKeyEntry(String alias)
    {
        if(keyCerts.containsKey(alias) == false)
        {
            return false;
        }

        return keyCerts.get(alias).key != null;
    }

    @Override
    public boolean engineIsCertificateEntry(String alias)
    {
        if(keyCerts.containsKey(alias) == false)
        {
            return false;
        }

        return keyCerts.get(alias).key == null;
    }

    @Override
    public String engineGetCertificateAlias(Certificate cert)
    {
        for(String alias : keyCerts.keySet())
        {
            if(keyCerts.get(alias).getCertificate().equals(cert))
            {
                return alias;
            }
        }

        return null;
    }

    public static void setSecurityFactory(SecurityFactory pSecurityFactory)
    {
        securityFactory = pSecurityFactory;
    }

}