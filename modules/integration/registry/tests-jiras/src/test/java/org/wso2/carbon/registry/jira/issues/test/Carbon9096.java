/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.registry.jira.issues.test;


import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertEquals;

public class Carbon9096 {

    @Test(groups = "wso2.greg", description = "Check Version is set in XmlSchema")
    public void addSchema() throws RegistryException, IOException {
        InputStream inputStream = null;
        String schemaFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                                File.separator + "GREG" + File.separator + "schema" + File.separator + "version.xsd";
        try {
            inputStream = new FileInputStream(schemaFilePath);
            XmlSchemaCollection schema1 = new XmlSchemaCollection();
            XmlSchema schema = schema1.read(new StreamSource(inputStream), null);
            assertEquals("2.0", schema.getVersion());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

    }
}
