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

package org.xipki.ca.qa.certprofile.x509.conf;

import org.xipki.ca.qa.certprofile.x509.jaxb.CertificatePolicyInformationType;
import org.xipki.common.ParamChecker;

/**
 * @author Lijun Liao
 */

public class CertificatePolicyInformationConf
{
    private final String policyId;
    private final PolicyQualifiersConf policyQualifiers;

    public CertificatePolicyInformationConf(CertificatePolicyInformationType jaxb)
    {
        ParamChecker.assertNotNull("jaxb", jaxb);
        this.policyId = jaxb.getPolicyIdentifier().getValue();
        if(jaxb.getPolicyQualifiers() == null)
        {
            this.policyQualifiers = null;
        }else
        {
            this.policyQualifiers = new PolicyQualifiersConf(jaxb.getPolicyQualifiers());
        }
    }

    public String getPolicyId()
    {
        return policyId;
    }

    public PolicyQualifiersConf getPolicyQualifiers()
    {
        return policyQualifiers;
    }

}
