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
package org.wso2.registry.checkin.scm.checkout;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class CheckOutConsumer extends AbstractConsumer {

    private List<ScmFile> updatedFiles = new LinkedList<ScmFile>();

    public CheckOutConsumer(ScmLogger logger) {
        super(logger);
    }

    public void consumeLine(String line) {
        char statusSymbol = line.charAt(0);
        String file;

        ScmFileStatus status;
        switch (statusSymbol) {
            case 'A':
                status = ScmFileStatus.ADDED;
                file = line.substring(2).trim();
                break;
            case 'O':
                status = ScmFileStatus.UPDATED;
                file = line.substring(3).trim();
                break;
            default:
                // Do nothing
                return;

        }

        if (new File(file).isFile()) {
            updatedFiles.add(new ScmFile(file, status));
        }
    }

    public List<ScmFile> getUpdatedFiles() {
        return updatedFiles;
    }
}
