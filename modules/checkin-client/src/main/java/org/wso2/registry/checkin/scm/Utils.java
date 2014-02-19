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
package org.wso2.registry.checkin.scm;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

public class Utils {

    private static final String PASSWORD_ARG = "--password";
    private static final String USERNAME_ARG = "--user";
    private static final String LOCATION_ARG = "--location";

    public static Commandline getCommandLine(ScmProviderRepository repository, String command,
                                             ScmFileSet scmFileSet) {
        Commandline cl = new Commandline();
        String carbonHome = System.getenv("CARBON_HOME");
        String executableName;
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            executableName = "checkin-client.bat";
        } else {
            executableName = "checkin-client.sh";
        }
        if (carbonHome != null) {
            if (!carbonHome.endsWith(File.separator)) {
                carbonHome += File.separator;
            }
            cl.setExecutable(carbonHome + executableName);
        } else {
            cl.setExecutable(executableName);
        }
        cl.addArguments(new String[]{command,
                ((ScmProviderRepositoryWithHost)repository).getHost(), USERNAME_ARG,
                repository.getUser(), PASSWORD_ARG, repository.getPassword(), LOCATION_ARG,
                scmFileSet.getBasedir().getAbsolutePath()});

        return cl;
    }

    public static String getCommandLineAsString(Commandline cl) {
        String clString = cl.toString();

        int pos = clString.indexOf(PASSWORD_ARG);

        if (pos > 0) {
            String beforePassword = clString.substring(0, pos + PASSWORD_ARG.length() + 1);
            String afterPassword = clString.substring(pos + PASSWORD_ARG.length() + 1);
            afterPassword = afterPassword.substring(afterPassword.indexOf(' '));
            if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                return beforePassword + "********" + afterPassword;
            } else {
                return beforePassword + "'********'" + afterPassword;
            }
        }

        return clString;
    }

}
