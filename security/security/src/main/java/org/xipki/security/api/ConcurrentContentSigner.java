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

package org.xipki.security.api;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.ContentSigner;

/**
 * @author Lijun Liao
 */

public interface ConcurrentContentSigner
{

    AlgorithmIdentifier getAlgorithmIdentifier();

    /**
     *
     * @return the private key if possible. {@code null} may be returned.
     */
    PrivateKey getPrivateKey();

    X509Certificate getCertificate();

    X509CertificateHolder getCertificateAsBCObject();

    void setCertificateChain(X509Certificate[] certchain);

    X509Certificate[] getCertificateChain();

    X509CertificateHolder[] getCertificateChainAsBCObjects();

    void initialize(String conf, PasswordResolver passwordResolver)
    throws SignerException;

    public ContentSigner borrowContentSigner()
    throws NoIdleSignerException;

    /**
     *
     * @param timeout timeout in milliseconds, 0 for infinitely
     * @return
     * @throws InterruptedException
     */
    public ContentSigner borrowContentSigner(int timeout)
    throws NoIdleSignerException;

    public void returnContentSigner(ContentSigner signer);

    public boolean isHealthy();
}
