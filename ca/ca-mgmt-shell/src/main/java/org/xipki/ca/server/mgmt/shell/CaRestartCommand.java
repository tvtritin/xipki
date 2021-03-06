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

package org.xipki.ca.server.mgmt.shell;

import java.util.HashSet;
import java.util.Set;

import org.apache.karaf.shell.commands.Command;

/**
 * @author Lijun Liao
 */

@Command(scope = "xipki-ca", name = "ca-restart", description="Restart CA system")
public class CaRestartCommand extends CaCommand
{

    @Override
    protected Object doExecute()
    throws Exception
    {
        boolean successfull = caManager.restartCaSystem();
        if(successfull == false)
        {
            err("Could not restart CA system");
            return null;
        }

        StringBuilder sb = new StringBuilder("Restarted CA system");
        Set<String> names = new HashSet<>(caManager.getCaNames());

        if(names.size() > 0)
        {
            sb.append(" with following CAs: ");
            Set<String> caAliasNames = caManager.getCaAliasNames();
            for(String aliasName : caAliasNames)
            {
                String name = caManager.getCaName(aliasName);
                names.remove(name);

                sb.append(name).append(" (alias ").append(aliasName).append(")").append(", ");
            }

            for(String name : names)
            {
                sb.append(name).append(", ");
            }

            int len = sb.length();
            sb.delete(len-2, len);
        }
        else
        {
            sb.append(": no CA is configured");
        }

        out(sb.toString());
        return null;
    }
}
