/*
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */

package org.wso2.carbon.registry.metadata.test.util;

public final class RegistryConsts {

    /*constant declarations for media types*/
    public static final String POLICY_XML = "application/policy+xml";
    public static final String TEXT_XML = "text/xml";
    public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";
    public static final String APPLICATION_XML = "application/xml";
    public static final String APPLICATION_WSDL_XML = "application/wsdl+xml";
    public static final String APPLICATION_X_XSD_XML = "application/x-xsd+xml";
    public static final String APPLICATION_WSO2_GOVERNANCE_ARCHIVE = "application/vnd.wso2.governance-archive";

    private RegistryConsts() {
        throw new AssertionError();
    }
}
