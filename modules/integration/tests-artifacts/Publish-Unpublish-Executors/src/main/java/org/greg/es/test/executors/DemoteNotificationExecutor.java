package org.greg.es.test.executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.Map;

//org.greg.es.test.executors.DemoteNotificationExecutor
public class DemoteNotificationExecutor implements Execution {

    private static final Log log = LogFactory.getLog(DemoteNotificationExecutor.class);

    @Override
    public void init(Map map) {
        log.info("###################### DemoteNotificationExecutor initilized! ---------------------------");
    }

    @Override
    public boolean execute(RequestContext requestContext, String s, String s2) {

        log.info("###################### DemoteNotificationExecutor ACTION executed! ---------------------------");
        return true;
    }
}
