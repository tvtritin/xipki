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

package org.xipki.ca.api.publisher;

import java.security.cert.CertificateEncodingException;

import org.xipki.ca.common.RequestorInfo;
import org.xipki.ca.common.X509CertificateWithMetaInfo;
import org.xipki.security.common.ParamChecker;

public class CertificateInfo
{
	private final byte[] subjectPublicKey;
	private final X509CertificateWithMetaInfo cert;
	private final X509CertificateWithMetaInfo issuerCert;
	private final String profileName;
	private final String caName;
	
	private RequestorInfo requestor;
	private String user;
	
	private String warningMessage;
	
	public CertificateInfo(X509CertificateWithMetaInfo cert, 
			X509CertificateWithMetaInfo issuerCert,
			byte[] subjectPublicKey,
			String profileName,
			String caName)
		throws CertificateEncodingException
	{
		super();
		ParamChecker.assertNotNull("cert", cert);
		ParamChecker.assertNotNull("issuerCert", issuerCert);
		ParamChecker.assertNotEmpty("profileName", profileName);
		ParamChecker.assertNotEmpty("caName", caName);
		ParamChecker.assertNotNull("subjectPublicKey", subjectPublicKey);
		
		this.cert = cert;
		this.subjectPublicKey = subjectPublicKey;
		
		this.issuerCert = issuerCert;
		this.profileName = profileName;
		this.caName = caName;
	}

	public byte[] getSubjectPublicKey()
	{
		return subjectPublicKey;
	}
	
	public X509CertificateWithMetaInfo getCert() 
	{
		return cert;
	}

	public X509CertificateWithMetaInfo getIssuerCert() 
	{
		return issuerCert;
	}

	public String getProfileName() 
	{
		return profileName;
	}

	public String getWarningMessage() {
		return warningMessage;
	}

	public void setWarningMessage(String warningMessage) {
		this.warningMessage = warningMessage;
	}

	public String getCaName() {
		return caName;
	}

	public RequestorInfo getRequestor() {
		return requestor;
	}

	public void setRequestor(RequestorInfo requestor) {
		this.requestor = requestor;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}	
	
}