/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This work is part of XiPKI, owned by Lijun Liao (lijun.liao@gmail.com)
 *
 */

package org.xipki.security.provider;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.spec.AlgorithmParameterSpec;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.NullDigest;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA224Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;

class ECDSASignatureSpi
    extends SignatureSpi
{
    private Digest digest;

    private P11PrivateKey signingKey;
    
    ECDSASignatureSpi(Digest digest)
    {
    	this.digest = digest;
    }

    protected void engineInitVerify(PublicKey publicKey)
        throws InvalidKeyException
    {
    	throw new UnsupportedOperationException("engineInitVerify unsupported");
    }

    protected void engineInitSign(
        PrivateKey privateKey)
        throws InvalidKeyException
    {
    	if(privateKey instanceof P11PrivateKey == false)
    	{
    		throw new InvalidKeyException("privateKey is not instanceof " + P11PrivateKey.class.getName());
    	}
    	
    	String algo = privateKey.getAlgorithm();
    	if(("EC".equals(algo) || "ECDSA".equals(algo)) == false)
    	{
    		throw new InvalidKeyException("privateKey is not a EC private key: " + algo);
    	}
    	
        digest.reset();
        this.signingKey = (P11PrivateKey) signingKey;
    }

    static public class SHA1
        extends ECDSASignatureSpi
    {
        public SHA1()
        {
            super(new SHA1Digest());
        }
    }

    static public class NONE
        extends ECDSASignatureSpi
    {
        public NONE()
        {
            super(new NullDigest());
        }
    }

    static public class SHA224
        extends ECDSASignatureSpi
    {
        public SHA224()
        {
            super(new SHA224Digest());
        }
    }

    static public class SHA256
        extends ECDSASignatureSpi
    {
        public SHA256()
        {
            super(new SHA256Digest());
        }
    }

    static public class SHA384
        extends ECDSASignatureSpi
    {
        public SHA384()
        {
            super(new SHA384Digest());
        }
    }

    static public class SHA512
        extends ECDSASignatureSpi
    {
        public SHA512()
        {
            super(new SHA512Digest());
        }
    }

    static public class RIPEMD160
        extends ECDSASignatureSpi
    {
        public RIPEMD160()
        {
            super(new RIPEMD160Digest());
        }
    }

    protected void engineUpdate(
            byte    b)
            throws SignatureException
        {
            digest.update(b);
        }

        protected void engineUpdate(
            byte[]  b,
            int     off,
            int     len) 
            throws SignatureException
        {
            digest.update(b, off, len);
        }

        protected byte[] engineSign()
            throws SignatureException
        {
            byte[]  hash = new byte[digest.getDigestSize()];

            digest.doFinal(hash, 0);

            try
            {
                return signingKey.CKM_ECDSA(hash);
            }
            catch(SignatureException e)
            {
            	throw e;
            }
            catch (Exception e)
            {
                throw new SignatureException(e.toString());
            }
        }

        protected void engineSetParameter(
            AlgorithmParameterSpec params)
        {
            throw new UnsupportedOperationException("engineSetParameter unsupported");
        }

        /**
         * @deprecated replaced with <a href = "#engineSetParameter(java.security.spec.AlgorithmParameterSpec)">
         */
        protected void engineSetParameter(
            String  param,
            Object  value)
        {
            throw new UnsupportedOperationException("engineSetParameter unsupported");
        }

        /**
         * @deprecated
         */
        protected Object engineGetParameter(
            String      param)
        {
            throw new UnsupportedOperationException("engineSetParameter unsupported");
        }

    protected boolean engineVerify(
            byte[]  sigBytes) 
            throws SignatureException
    {
    	throw new UnsupportedOperationException("engineVerify unsupported");
    }
}