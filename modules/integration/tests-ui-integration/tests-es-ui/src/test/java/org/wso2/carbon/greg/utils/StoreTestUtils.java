/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.greg.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.greg.store.exceptions.StoreTestException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;

public class StoreTestUtils {

    protected static Log log = LogFactory.getLog(StoreTestUtils.class);

    /**
     * Executes an ant command given the file location and command
     *
     * @param location location of the ant build file
     * @param command  command to be executed
     */
    public static void runAnt(String location, String command) throws StoreTestException {
        Runtime runTime = Runtime.getRuntime();
        String osName = "";
        try {
            log.info("Executing ant script for getting started samples");
            osName = System.getProperty("os.name");
            Process process;
            if (osName.startsWith("Windows")) {
                process = runTime.exec("cmd.exe /C ant -f " + location + (command != null ? " " + command : ""), null);
            } else {
                process = runTime.exec("ant -f " + location + (command != null ? " " + command : ""), null);
            }
            log.info(printAntShellOutput(process));

        } catch (IOException e) {
            throw new StoreTestException("Could not execute the ant script", e);
        }
        log.info("Finished executing ant script for getting started samples");
        log.info("Thread is sleeping for 10 seconds");
        try {
            Thread.sleep(1000*40);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("Thread is running");
    }

    /**
     * Prints the output of executed script
     *
     * @param process Process
     * @return shell output
     * @throws IOException throws if the shell output is null.
     */
    private static String printAntShellOutput(Process process) throws StoreTestException {
        StringBuilder builder = new StringBuilder("");
        if (process != null) {
            for (InputStream stream : new InputStream[] { process.getInputStream(), process.getErrorStream() }) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        builder.append(line).append(File.separator);
                    }
                    reader.close();
                } catch (IOException e) {
                    throw new StoreTestException("Could not print the output for ant script", e);
                }
            }
        }

        return builder.toString();
    }
}
