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
package org.wso2.registry.checkin.scm.update;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class UpdateConsumer extends AbstractConsumer {

    private List<ScmFile> updatedFiles = new LinkedList<ScmFile>();

    public UpdateConsumer(ScmLogger logger) {
        super(logger);
    }

    public void consumeLine(String line) {
        char statusSymbol = line.charAt(0);

        ScmFileStatus status;
        switch (statusSymbol) {
            case 'A':
                status = ScmFileStatus.ADDED;
                break;
            case 'D':
                status = ScmFileStatus.DELETED;
                break;
            case 'C':
                status = ScmFileStatus.CONFLICT;
                break;
            case 'U':
                status = ScmFileStatus.UPDATED;
                break;
            default:
                // Do nothing
                return;

        }

        String file = line.substring(2).trim();

        if (new File(file).isFile()) {
            updatedFiles.add(new ScmFile(file, status));
        }
    }

    public List<ScmFile> getUpdatedFiles() {
        return updatedFiles;
    }
}
