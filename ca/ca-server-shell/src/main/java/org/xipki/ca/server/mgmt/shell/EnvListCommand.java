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

package org.xipki.ca.server.mgmt.shell;

import java.util.Set;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.xipki.security.common.EnvironmentParameterResolver;

@Command(scope = "ca", name = "env-list", description="List environment parameters")
public class EnvListCommand extends CaCommand {

	@Option(name = "-name",
            description = "Parameter Name",
            required = false, multiValued = false)
	protected String name;

    @Override
    protected Object doExecute() throws Exception {
		EnvironmentParameterResolver envParameterResolver = caManager.getEnvParameterResolver();
		
		StringBuilder sb = new StringBuilder();
		
		if(name == null)
		{
			Set<String> paramNames = envParameterResolver.getAllParameterNames();
			int n = paramNames.size();
			
			sb.append(n + " Enviroment paramters are configured:\n");		
			for(String paramName : paramNames)
			{
				sb.append("\t").append(paramName).append("\n");
			}
		}
		else
		{
			String paramValue = envParameterResolver.getParameterValue(name);
			sb.append(name).append("\n\t").append(paramValue);
		}
		
		System.out.println(sb.toString());
		
        return null;
    }
}