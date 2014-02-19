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

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.wso2.carbon.registry.synchronization.SynchronizationConstants;
import org.wso2.registry.checkin.scm.checkin.CheckInCommand;
import org.wso2.registry.checkin.scm.checkout.CheckOutCommand;
import org.wso2.registry.checkin.scm.repository.ProviderRepository;
import org.wso2.registry.checkin.scm.update.UpdateCommand;

public class Provider extends AbstractScmProvider {

    public String getScmType() {
        return "wso2";
    }

    public String getScmSpecificFilename() {
        return SynchronizationConstants.META_DIRECTORY;
    }

    public ScmProviderRepository makeProviderScmRepository(String scmSpecificUrl, char delimiter)
            throws ScmRepositoryException {

        return new ProviderRepository(scmSpecificUrl);
    }

    public CheckOutScmResult checkout(ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters) throws ScmException {
        CheckOutCommand command = new CheckOutCommand();
        command.setLogger(getLogger());
        return (CheckOutScmResult) command.execute(repository, fileSet, parameters);
    }

    public CheckInScmResult checkin(ScmProviderRepository repository, ScmFileSet fileSet,
                                    CommandParameters parameters) throws ScmException {
        CheckInCommand command = new CheckInCommand();
        command.setLogger(getLogger());
        return (CheckInScmResult) command.execute(repository, fileSet, parameters);
    }

    public UpdateScmResult update(ScmProviderRepository repository, ScmFileSet fileSet,
                                  CommandParameters parameters) throws ScmException {
        UpdateCommand command = new UpdateCommand();
        command.setLogger(getLogger());
        return (UpdateScmResult) command.execute(repository, fileSet, parameters);
    }
}
