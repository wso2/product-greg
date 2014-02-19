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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryClientUtils;

import java.io.*;

import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry file system Import Export
 */
public class FileSystemImportExportTestCase {
    public RemoteRegistry registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() {
        InitializeAPI initializeAPI = new InitializeAPI();
        registry = initializeAPI.getRegistry(FrameworkSettings.CARBON_HOME, FrameworkSettings.HTTPS_PORT, FrameworkSettings.HTTP_PORT);
    }

    @Test(groups = {"wso2.greg"})
    public void FileImportTest() throws RegistryException {

        String filePath = FrameworkSettings.CARBON_HOME + "/dbscripts";
        File file = new File(filePath);
        RegistryClientUtils.importToRegistry(file, "/framework", registry);
        assertTrue(registry.resourceExists("/framework/dbscripts/db2.sql"), "Resource not found.");
        assertTrue(registry.resourceExists("/framework/dbscripts/mysql.sql"), "Resource not found.");
        assertTrue(registry.resourceExists("/framework/dbscripts/mssql.sql"), "Resource not found.");
        assertTrue(registry.resourceExists("/framework/dbscripts/oracle.sql"), "Resource not found.");
        assertTrue(registry.resourceExists("/framework/dbscripts/oracle_rac.sql"), "Resource not found.");
        assertTrue(registry.resourceExists("/framework/dbscripts/postgresql.sql"), "Resource not found.");

        Resource r1;
        r1 = registry.get("/framework/dbscripts/mysql.sql");
        r1.getContent();
        String contain = new String((byte[]) r1.getContent());
        assertTrue(contain.contains("CREATE"), "Resource contain not found");

        r1 = registry.get("/framework/dbscripts/mssql.sql");
        r1.getContent();
        String containUm = new String((byte[]) r1.getContent());
        assertTrue(containUm.contains("CREATE"), "Resource contain not found");


        r1.discard();

        RegistryClientUtils.importToRegistry(file, "/framework", registry);

        Resource r2;
        r2 = registry.get("/framework/dbscripts/mysql.sql");
        r2.getContent();
        String contain2 = new String((byte[]) r2.getContent());
        assertTrue(contain2.contains("CREATE"), "Resource contain not found");

        r2 = registry.get("/framework/dbscripts/mssql.sql");
        r2.getContent();
        String containUm2 = new String((byte[]) r2.getContent());
        assertTrue(containUm2.contains("CREATE"), "Resource contain not found");

        r2.discard();

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "FileImportTest")
    public void FileExportTest() throws RegistryException, FileNotFoundException {

        Runtime runTime = Runtime.getRuntime();
        String fileExportPath = FrameworkSettings.CARBON_HOME + "/export";

        File file = new File(FrameworkSettings.CARBON_HOME + "/export");
        String osName = "";
        try {
            osName = System.getProperty("os.name");
        } catch (Exception e) {
            System.out.println("Exception caught =" + e.getMessage());
        }

        if (osName.startsWith("Windows")) {
            try {
                runTime.exec("cmd.exe /C" + "" + "mkdir" + "" + fileExportPath, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                runTime.exec("mkdir" + " " + fileExportPath, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        RegistryClientUtils.exportFromRegistry(file, "/framework/", registry);

        File f = new File(FrameworkSettings.CARBON_HOME + "/export/dbscripts/h2.sql");
        assertTrue(f.exists(), "File doesn't exist at the location");

        File f1 = new File(FrameworkSettings.CARBON_HOME + "/export/dbscripts/mysql.sql");
        assertTrue(f1.exists(), "File doesn't exist at the location");

        assertTrue(fileContainString(FrameworkSettings.CARBON_HOME + "/export/dbscripts/oracle.sql", "CREATE"), "Resource contain not found");
        assertTrue(fileContainString(FrameworkSettings.CARBON_HOME + "/export/dbscripts/h2.sql", "CREATE"), "Resource contain not found");

        if (osName.startsWith("Windows")) {
            try {
                runTime.exec("cmd.exe /C" + "" + "del" + "" + fileExportPath, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                runTime.exec("rm -rf" + " " + fileExportPath, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "FileExportTest")
    public void JarFileExportTest() throws RegistryException, FileNotFoundException {
        String jarFileName = "jcr-2.0.jar";
        String filePath = FrameworkSettings.CARBON_HOME + "/repository/lib/" + jarFileName;
        File file = new File(filePath);
        RegistryClientUtils.importToRegistry(file, "/framework", registry);

        assertTrue(registry.resourceExists("/framework/" + jarFileName), "Resource not found.");

        Runtime runTime = Runtime.getRuntime();
        String fileExportPath = FrameworkSettings.CARBON_HOME + "/export";
        String osName = "";
        if (osName.startsWith("Windows")) {
            try {
                 runTime.exec("cmd.exe /C" + "" + "mkdir" + "" + fileExportPath, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                runTime.exec("chmod +w " +FrameworkSettings.CARBON_HOME);
                runTime.exec("mkdir" + " " + fileExportPath, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file2 = new File(FrameworkSettings.CARBON_HOME + "/export");
        RegistryClientUtils.exportFromRegistry(file2, "/framework", registry);

        File f1 = new File(FrameworkSettings.CARBON_HOME + "/export/" + jarFileName);
        assertTrue(f1.exists(), "File doesn't exist at the location");

        if (osName.startsWith("Windows")) {
            try {
                runTime.exec("cmd.exe /C" + "" + "del" + "" + fileExportPath, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                runTime.exec("rm -rf" + " " + fileExportPath, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static boolean versionCount(String r1Versions[]) {
        boolean versionCount = false;
        if (r1Versions.length >= 1) {
            versionCount = true;
        }
        return versionCount;
    }

    public static String slurp(InputStream in) throws IOException {
        StringBuilder out = new StringBuilder();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

    public static boolean fileContainString(String path, String pattern) throws FileNotFoundException {
        String st = null;
        boolean valuefile = false;
        InputStream is = new BufferedInputStream(new FileInputStream(path));
        try {
            st = slurp(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (st != null) {
            if (st.contains(pattern)) {
                valuefile = true;
            }
        }
        return valuefile;
    }



    @AfterClass
    public void destroy() {
        try {
            registry.delete("/framework");
        } catch (RegistryException ignore) {

        }
    }
}
