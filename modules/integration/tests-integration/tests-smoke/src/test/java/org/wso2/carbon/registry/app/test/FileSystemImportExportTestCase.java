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

package org.wso2.carbon.registry.app.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryClientUtils;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry file system Import Export
 */
public class FileSystemImportExportTestCase extends GREGIntegrationBaseTest{
    public RemoteRegistry registry;
    private static final Log log = LogFactory.getLog(FileSystemImportExportTestCase.class);
    private static String EXPORT_PATH = System.getProperty("basedir");

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception{

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        registry = new RegistryProviderUtil().getRemoteRegistry(automationContext);
    }

    @Test(groups = {"wso2.greg"})
    public void FileImportTest() throws RegistryException, MalformedURLException {

        String filePath = getTestArtifactLocation() + "artifacts" + File.separator + "GREG" +
                          File.separator + "apptestresources";

        File file = new File(filePath);
        RegistryClientUtils.importToRegistry(file, "/framework", registry);
        assertTrue(registry.resourceExists("/framework/apptestresources/db2.sql"), "Resource not found.");
        assertTrue(registry.resourceExists("/framework/apptestresources/mysql.sql"), "Resource not found.");
        assertTrue(registry.resourceExists("/framework/apptestresources/mssql.sql"), "Resource not found.");
        assertTrue(registry.resourceExists("/framework/apptestresources/oracle.sql"), "Resource not found.");
        assertTrue(registry.resourceExists("/framework/apptestresources/oracle_rac.sql"), "Resource not found.");
        assertTrue(registry.resourceExists("/framework/apptestresources/postgresql.sql"), "Resource not found.");

        Resource r1 = registry.newResource();
        r1 = registry.get("/framework/apptestresources/mysql.sql");
        r1.getContent();
        String contain = new String((byte[]) r1.getContent());
        assertTrue(containString(contain, "CREATE"), "Resource contain not found");

        r1 = registry.get("/framework/apptestresources/mssql.sql");
        r1.getContent();
        String containUm = new String((byte[]) r1.getContent());
        assertTrue(containString(containUm, "CREATE"), "Resource contain not found");


        r1.discard();

        RegistryClientUtils.importToRegistry(file, "/framework", registry);

        Resource r2 = registry.newResource();
        r2 = registry.get("/framework/apptestresources/mysql.sql");
        r2.getContent();
        String contain2 = new String((byte[]) r2.getContent());
        assertTrue(containString(contain2, "CREATE"), "Resource contain not found");

        r2 = registry.get("/framework/apptestresources/mssql.sql");
        r2.getContent();
        String containUm2 = new String((byte[]) r2.getContent());
        assertTrue(containString(containUm2, "CREATE"), "Resource contain not found");

        r2.discard();

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"FileImportTest"})
    public void FileExportTest() throws RegistryException, FileNotFoundException {
        Process process;
        Runtime runTime = Runtime.getRuntime();
        String fileExportPath = EXPORT_PATH + "/target/export";

        File file = new File(fileExportPath);
        String osName = "";
        try {
            osName = System.getProperty("os.name");
        } catch (Exception e) {
            log.error("Exception caught =" + e.getMessage());
        }

        if (osName.startsWith("Windows")) {
            try {
                process = runTime.exec("cmd.exe /C" + "" + "mkdir" + "" + fileExportPath, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                process = runTime.exec("mkdir" + " " + fileExportPath, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        RegistryClientUtils.exportFromRegistry(file, "/framework/", registry);

        File f = new File(fileExportPath + "/apptestresources/mssql.sql");
        assertTrue(f.exists(), "File doesn't exist at the location");

        File f1 = new File(fileExportPath + "/apptestresources/mysql.sql");
        assertTrue(f1.exists(), "File doesn't exist at the location");

        assertTrue(fileContainString(fileExportPath + "/apptestresources/oracle.sql", "CREATE"), "Resource contain not found");
        assertTrue(fileContainString(fileExportPath + "/apptestresources/mssql.sql", "CREATE"), "Resource contain not found");

        if (osName.startsWith("Windows")) {
            try {
                process = runTime.exec("cmd.exe /C" + "" + "del" + "" + fileExportPath, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                process = runTime.exec("rm -rf" + " " + fileExportPath, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"FileExportTest"})
    public void JarFileExportTest() throws RegistryException, FileNotFoundException {
        String jarFileName = "jcr-2.0.jar";
        String filePath = getTestArtifactLocation() + "artifacts" + File.separator + "GREG"
                + File.separator + "jcr" + File.separator + jarFileName;

        File file = new File(filePath);
        RegistryClientUtils.importToRegistry(file, "/framework", registry);

        assertTrue(registry.resourceExists("/framework/" + jarFileName), "Resource not found.");

        Process process;
        Runtime runTime = Runtime.getRuntime();
        String fileExportPath = EXPORT_PATH + "/target/export/";
        String osName = "";
        if (osName.startsWith("Windows")) {
            try {
                process = runTime.exec("cmd.exe /C" + "" + "mkdir" + "" + fileExportPath, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                process = runTime.exec("mkdir" + " " + fileExportPath, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file2 = new File(fileExportPath);
        RegistryClientUtils.exportFromRegistry(file2, "/framework", registry);

        File f1 = new File(fileExportPath + jarFileName);
        assertTrue(f1.exists(), "File doesn't exist at the location");

        if (osName.startsWith("Windows")) {
            try {
                process = runTime.exec("cmd.exe /C" + "" + "del" + "" + fileExportPath, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                process = runTime.exec("rm -rf" + " " + fileExportPath, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean containString(String str, String pattern) {
        int s = 0;
        int e = 0;
        boolean value = false;

        while ((e = str.indexOf(pattern, s)) >= 0) {
            value = true;
            return value;

        }
        return value;
    }

    public static boolean versionCount(String r1Versions[]) {
        boolean versionCount = false;
        //System.out.println("version length" + r1Versions.length);
        if (r1Versions.length >= 1) {
            versionCount = true;
        }
        return versionCount;
    }

    public static String slurp(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

    public static boolean fileContainString(String path, String pattern)
            throws FileNotFoundException {
        String st = null;
        boolean valuefile = false;
        InputStream is = new BufferedInputStream(new FileInputStream(path));
        try {
            st = slurp(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (containString(st, pattern)) {
            valuefile = true;
        }
        return valuefile;
    }
}
