/*
 * Copyright (c) 2014 Lijun Liao
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

package org.xipki.ca.server.mgmt.shell;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.xipki.security.common.CRLReason;
import org.xipki.security.common.CertRevocationInfo;

/**
 * @author Lijun Liao
 */

@Command(scope = "ca", name = "ca-revoke", description="Revoke CA")
public class CaRevokeCommand extends CaCommand
{
    public static List<CRLReason> permitted_reasons = Collections.unmodifiableList(
            Arrays.asList(    new CRLReason[]
            {
                CRLReason.UNSPECIFIED, CRLReason.KEY_COMPROMISE, CRLReason.CA_COMPROMISE,
                CRLReason.AFFILIATION_CHANGED, CRLReason.SUPERSEDED, CRLReason.CESSATION_OF_OPERATION,
                CRLReason.CERTIFICATE_HOLD,    CRLReason.PRIVILEGE_WITHDRAWN}));

    @Option(name = "-name",
            description = "Required, CA name",
            required = true)
    protected String           caName;

    @Option(name = "-reason",
            required = true,
            description = "Required. Reason, valid values are \n" +
                    "0: unspecified\n" +
                    "1: keyCompromise\n" +
                    "2: cACompromise\n" +
                    "3: affiliationChanged\n" +
                    "4: superseded\n" +
                    "5: cessationOfOperation\n" +
                    "6: certificateHold\n" +
                    "9: privilegeWithdrawn")
    protected String           reason;

    @Override
    protected Object doExecute()
    throws Exception
    {

        CRLReason crlReason = CRLReason.getInstance(reason);
        if(crlReason == null)
        {
            System.out.println("invalid reason " + reason);
            return null;
        }

        if(permitted_reasons.contains(crlReason) == false)
        {
            System.err.println("reason " + reason + " is not permitted");
            return null;
        }

        if(caManager.getCANames().contains(caName) == false)
        {
            System.out.println("invalid CA name " + caName);
            return null;
        }

        CertRevocationInfo revInfo = new CertRevocationInfo(crlReason);
        caManager.revokeCa(caName, revInfo);

        return null;
    }
}
