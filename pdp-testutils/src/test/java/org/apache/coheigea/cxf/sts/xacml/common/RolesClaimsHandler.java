/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.coheigea.cxf.sts.xacml.common;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.rt.security.claims.Claim;
import org.apache.cxf.rt.security.claims.ClaimCollection;
import org.apache.cxf.sts.claims.ClaimsHandler;
import org.apache.cxf.sts.claims.ClaimsParameters;
import org.apache.cxf.sts.claims.ProcessedClaim;
import org.apache.cxf.sts.claims.ProcessedClaimCollection;

/**
 * A ClaimsHandler implementation that works with Roles.
 */
public class RolesClaimsHandler implements ClaimsHandler {

    public static final URI ROLE = 
            URI.create("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role");  
    
    public ProcessedClaimCollection retrieveClaimValues(
            ClaimCollection claims, ClaimsParameters parameters) {
      
        if (claims != null && claims.size() > 0) {
            ProcessedClaimCollection claimCollection = new ProcessedClaimCollection();
            for (Claim requestClaim : claims) {
                ProcessedClaim claim = new ProcessedClaim();
                claim.setClaimType(requestClaim.getClaimType());
                if (ROLE.equals(requestClaim.getClaimType())) {
                    claim.setIssuer("STS");
                    if ("alice".equals(parameters.getPrincipal().getName())) {
                        claim.addValue("boss");
                        claim.addValue("employee");
                    } else if ("bob".equals(parameters.getPrincipal().getName())) {
                        claim.addValue("employee");
                    }
                }
                claimCollection.add(claim);
            }
            return claimCollection;
        }
        return null;
    }

    public List<URI> getSupportedClaimTypes() {
        List<URI> list = new ArrayList<URI>();
        list.add(ROLE);
        return list;
    }

}
