package org.greg.es.test.executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.Map;

//org.greg.es.test.executors.PromoteNotificationExecutor
public class PromoteNotificationExecutor implements Execution {

    private static final Log log = LogFactory.getLog(PromoteNotificationExecutor.class);

    @Override
    public void init(Map map) {
        log.info("@@@@@@@@@@@@@@@@@@@@@@ PromoteNotificationExecutor initilized! ---------------------------");
    }

    @Override
    public boolean execute(RequestContext requestContext, String s, String s2) {

        log.info("@@@@@@@@@@@@@@@@@@@@@@ PromoteNotificationExecutor ACTION executed! ---------------------------");
        return true;
    }
}
