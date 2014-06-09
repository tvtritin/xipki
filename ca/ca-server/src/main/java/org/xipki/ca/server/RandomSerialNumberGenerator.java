/*
 * Copyright (c) 2014 xipki.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 *
 */

package org.xipki.ca.server;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;

/**
 * Copied from EJBCA.
 *
 * Implements a singleton serial number generator using SecureRandom. This
 * generator generates random 8 octec (64 bits) serial numbers.
 *
 * RFC3280 defines serialNumber be positive INTEGER, and X.690 defines INTEGER
 * consist of one or more octets. X.690 also defines as follows:
 *
 * If the contents octets of an integer value encoding consist of more than one
 * octet, then the bits of the first octet and bit 8 of the second octet: a)
 * shall not all be ones; and b) shall not all be zero.
 *
 * Therefore, minimum 8 octets value is 0080000000000000 and maximum value is
 * 7FFFFFFFFFFFFFFF."
 *
 * Therefore, minimum 4 octets value is 00800000 and maximum value is 7FFFFFFF."
 *
 * X.690:
 *
 * 8.3 Encoding of an integer value
 * 8.3.1 The encoding of an integer value shall be primitive. The contents octets
 *  shall consist of one or more octets.
 * 8.3.2 If the contents octets of an integer value encoding consist of more than
 *  one octet, then the bits of the first octet
 * and bit 8 of the second octet:
 * a) shall not all be ones; and
 * b) shall not all be zero.
 * NOTE – These rules ensure that an integer value is always encoded in the smallest
 *  possible number of octets.
 * 8.3.3 The contents octets shall be a two's complement binary number equal to the
 *  integer value, and consisting of
 * bits 8 to 1 of the first octet, followed by bits 8 to 1 of the second octet,
 *  followed by bits 8 to 1 of each octet in turn up to
 * and including the last octet of the contents octets.
 * NOTE – The value of a two's complement binary number is derived by numbering the
 *  bits in the contents octets, starting with bit
 * 1 of the last octet as bit zero and ending the numbering with bit 8 of the first
 *  octet. Each bit is assigned a numerical value of 2N,
 * where N is its position in the above numbering sequence. The value of the two's
 *  complement binary number is obtained by
 * summing the numerical values assigned to each bit for those bits which are set to
 *  one, excluding bit 8 of the first octet, and then
 * reducing this value by the numerical value assigned to bit 8 of the first octet
 *  if that bit is set to one.
 *
 * Based on EJBCA version: SernoGenerator.java 8373 2009-11-30 14:07:00Z jeklund
 *
 * @version $Id: SernoGeneratorRandom.java 300 2011-02-22 11:42:59Z tomas $
 */

public class RandomSerialNumberGenerator
{

    /** random generator algorithm, default SHA1PRNG */
    private String algorithm = "SHA1PRNG";

    /** number of bytes serial number to generate, default 8 */
    private int noOctets = 8;

    /** random generator */
    private SecureRandom random;

    /** A handle to the unique Singleton instance. */
    private static RandomSerialNumberGenerator instance = null;

    /** lowest possible value we should deliver when getSerno is called */
    private BigInteger lowest = new BigInteger("0080000000000000", 16);

    /** highest possible value we should deliver when getSerno is called */
    private BigInteger highest = new BigInteger("7FFFFFFFFFFFFFFF", 16);

    /**
     * Creates a serial number generator using SecureRandom
     */
    private RandomSerialNumberGenerator()
    {
        init();
    }

    private void init()
    {
        try
        {
            random = SecureRandom.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
        long seed = Math.abs((new Date().getTime()) + this.hashCode());
        random.setSeed(seed);
    }

    public static synchronized RandomSerialNumberGenerator getInstance()
    {
        if (instance == null)
        {
            instance = new RandomSerialNumberGenerator();
        }
        return instance;
    }

    public BigInteger getSerialNumber()
    {
        if (noOctets == 0)
        {
            Random rand = new Random();
            return new java.math.BigInteger(Long.toString(rand.nextInt(4)));
        }

        byte[] sernobytes = new byte[noOctets];
        boolean ok = false;
        BigInteger serno = null;

        while (!ok)
        {
            random.nextBytes(sernobytes);
            serno = (new java.math.BigInteger(sernobytes)).abs();
            // Must be within the range 0080000000000000 - 7FFFFFFFFFFFFFFF
            if ((serno.compareTo(lowest) >= 0) && (serno.compareTo(highest) <= 0))
            {
                ok = true;
            }
        }

        return serno;
    }

    public int getNoSernoBytes()
    {
        return noOctets;
    }

    public void setSeed(final long seed)
    {
        random.setSeed(seed);
    }

    public void setAlgorithm(final String algo)
    throws NoSuchAlgorithmException
    {
        this.algorithm = algo;
        // We must re-init after choosing a new algorithm
        init();
    }

    public void setSernoOctetSize(final int noOctets)
    {
        if (noOctets == 4)
        {
            lowest = new BigInteger("00800000", 16);
            highest = new BigInteger("7FFFFFFF", 16);
        }

        if ((noOctets != 4) && (noOctets != 8) && (noOctets != 0))
        {
            throw new IllegalArgumentException(
                    "SernoOctetSize must be 4 or 8 for this generator.");
        }
        this.noOctets = noOctets;
    }

}
