/*
 *  Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.carbon.registry.samples.populator.utils;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.*;

public class Utils {

    /**
     *This method is used to back up existing Files.
     *
     * @param registry      registry instance.
     * @param path          path of the rxt.
     * @param fileName      file name of backed up rxt files.
     * @throws RegistryException
     */
    public static void backUpFiles(Registry registry, String path, String fileName) throws RegistryException {
        Resource resource = registry.get(path);
        try {
            contentToFile(resource.getContentStream(), fileName);
        } catch (FileNotFoundException e) {
            System.out.println("Could not read file content");
        }
    }

    /**
     *This method is used to write file content to text file.
     *
     * @param is        rxt content as a input stream
     * @param fileName  file name of backed up rxt file.
     * @throws FileNotFoundException
     */
    private static void contentToFile(InputStream is, String fileName) throws FileNotFoundException {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }

        } catch (IOException e) {
            System.out.println("Could not read file content");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("Could not close input stream");
                }
            }
        }
        PrintWriter out = new PrintWriter("resources" + File.separator + fileName);
        out.println(sb.toString());
        out.flush();
        out.close();

    }
}
