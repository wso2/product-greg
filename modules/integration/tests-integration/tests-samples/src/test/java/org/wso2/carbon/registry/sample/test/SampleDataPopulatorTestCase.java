/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.registry.sample.test;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.sample.test.utils.CustomFileFilter;
import org.wso2.carbon.registry.sample.test.utils.SuffixFilter;
import org.wso2.carbon.registry.sample.test.utils.TypeFilter;
import org.wso2.carbon.registry.utils.TestUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;

import java.io.*;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SampleDataPopulatorTestCase extends GREGIntegrationBaseTest {

    private String location = FrameworkPathUtil.getSystemResourceLocation() + File.separator + ".." + File.separator + ".." +
            File.separator + ".." + File.separator + "src" + File.separator + "test" + File.separator + "java" +
            File.separator + "resources" + File.separator + "populator";

    private Registry registry;
    WSRegistryServiceClient wsRegistry;

    @BeforeClass(alwaysRun = true, groups = {"wso2.greg"})
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = new LoginLogoutClient(automationContext).login();
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        log.debug("Running SuccessCase");
        wsRegistry = registryProviderUtil.getWSRegistry(automationContext);
        registry = registryProviderUtil.getGovernanceRegistry(wsRegistry, automationContext);
        TestUtils.cleanupResources(registry);
        // create directory if not exist
        File file = new File(CarbonUtils.getCarbonHome() + File.separator + "samples" + File.separator +
                "asset-models" +
                File.separator + "FooModel");
        if(!file.exists())
            file.mkdirs();
        FileManipulator.copyDir(new File(FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "populate" + File.separator + "FooModel"),
                new File(CarbonUtils.getCarbonHome() + File.separator + "samples" + File.separator + "asset-models" +
                File.separator + "FooModel"));

        FileManipulator.copyFileToDir(new File(FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "populate" + File.separator +
                "build.xml"), new File(CarbonUtils.getCarbonHome() + File.separator + "samples" + File.separator +
                "asset-models" + File.separator + "Populator"));

        List<File> files = getAllSvnFiles(new File(CarbonUtils.getCarbonHome() + File.separator + "samples" +
                File.separator + "asset-models" + File.separator + "Populator"));

        List<File> filesFoo = getAllSvnFiles(new File(CarbonUtils.getCarbonHome() + File.separator +
                "samples" + File.separator + "asset-models" + File.separator + "FooModel"));

        removeFiles(files);
        removeFiles(filesFoo);

        runAnt(CarbonUtils.getCarbonHome() + File.separator + "samples" + File.separator + "asset-models" +
                File.separator + "Populator" + File.separator + "build.xml", "run-foo");
    }

    private void removeFiles(List<File> files) throws IOException {
        if(files.size() > 0) {
            for(File file : files) {
                FileUtils.deleteDirectory(file);
            }
        }
    }

    private String printOutput(Process p) throws IOException {
        StringBuilder builder = new StringBuilder("");
        if(p != null) {
            for(InputStream stream : new InputStream[]{p.getInputStream(), p.getErrorStream()}) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                while((line = reader.readLine()) != null) {
                    builder.append(line).append(System.getProperty("line.separator"));
                }
                reader.close();
            }
        }
        return builder.toString();
    }

    private void runAnt(String location, String command) {
        Runtime runTime = Runtime.getRuntime();
        String osName = "";
        try {
            osName = System.getProperty("os.name");
        } catch(Exception e) {
            System.out.println("Exception caught =" + e.getMessage());
        }
        try {
            Process p;
            if(osName.startsWith("Windows")) {
                p = runTime.exec("cmd.exe /C ant -f " + location + (command != null ? " " + command : ""), null);
            } else {
                p = runTime.exec("ant -f " + location + (command != null ? " " + command : ""), null);
            }
            // Print ant output.
            System.out.println(printOutput(p));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = {"wso2.greg"})
    public void dataUploadTest() throws RegistryException {
        assertTrue(registry.resourceExists("/trunk/wsdls/net/webservicex/www/1.0.0/globalweather.asmx.wsdl"),
                "Resource not found.");
        assertTrue(registry.resourceExists("/trunk/services/foo/1.0.0/service"), "Service not added.");
    }

    @Test(groups = {"wso2.greg"})
    public void lifecycleOperationTest() throws RegistryException {
        assertTrue(registry.resourceExists("/trunk/soapservices/net/webservicex/www/1.0.0/GlobalWeather"),
                "Resource not found.");
        assertEquals(registry.get("/trunk/soapservices/net/webservicex/www/1.0.0/GlobalWeather")
                .getProperty("registry.LC.name"), "ServiceLifeCycle", "Lifecycle not attached.");
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws RegistryException {
        TestUtils.cleanupResources(registry);
    }

    public static List<File> getAllSvnFiles(File directory) {
        if(directory.exists()) {
            return CustomFileFilter.getFilesRecursive(directory, new SuffixFilter(TypeFilter.DIR, ".svn"));
        }
        return null;
    }
}
