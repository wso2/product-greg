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

import org.apache.maven.scm.*;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.wso2.registry.checkin.scm.Utils;
import org.wso2.registry.checkin.scm.repository.ProviderRepository;

import java.util.Date;

public class UpdateCommand extends AbstractUpdateCommand {

    protected UpdateScmResult executeUpdateCommand(ScmProviderRepository scmProviderRepository,
                                                       ScmFileSet scmFileSet, ScmVersion scmVersion)
            throws ScmException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("executing update command...");
        }

        ProviderRepository repository = (ProviderRepository) scmProviderRepository;
        Commandline cl = Utils.getCommandLine(repository, "up", scmFileSet);

        UpdateConsumer consumer = new UpdateConsumer(getLogger());

        CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Executing: " + cl.getWorkingDirectory().getAbsolutePath() + ">>" +
                    Utils.getCommandLineAsString(cl));
        }

        try {
            exitCode = CommandLineUtils.executeCommandLine(cl, consumer, err);
            if (exitCode != 0) {
                // print out the writable copy for manual handling
                if (getLogger().isWarnEnabled()) {
                    getLogger().warn(err.getOutput());
                }
            }
        } catch (CommandLineException e) {
            getLogger().error("An error occurred while performing update", e);
        }

        return new UpdateScmResult(cl.toString(), consumer.getUpdatedFiles());
    }

    protected ChangeLogCommand getChangeLogCommand() {
        throw new UnsupportedOperationException("This operation is not supported");
    }
}
