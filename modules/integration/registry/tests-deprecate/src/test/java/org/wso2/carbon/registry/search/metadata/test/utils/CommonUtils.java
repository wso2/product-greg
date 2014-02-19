package org.wso2.carbon.registry.search.metadata.test.utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.LogEntry;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.governance.api.test.TestUtils;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;

import java.rmi.RemoteException;
import java.util.Calendar;

public class CommonUtils {

    private static Log log = LogFactory.getLog(CommonUtils.class) ;

    public static void cleanUpResource(Registry registry) throws RegistryException {
        TestUtils.cleanupResources(registry);
        LogEntry logEntries[] = registry.getLogs(null,LogEntry.ADD, FrameworkSettings.USER_NAME,null,null,true);

        if(logEntries != null){
            for (LogEntry logEntry : logEntries) {
                if (registry.resourceExists(logEntry.getResourcePath())) {
                    registry.delete(logEntry.getResourcePath());
                }
            }
        }

    }

    public static AdvancedSearchResultsBean getSearchResult(SearchAdminServiceClient searchAdminService,
                                                            CustomSearchParameterBean parameterBean)
            throws RemoteException, InterruptedException, SearchAdminServiceRegistryExceptionException {
        AdvancedSearchResultsBean resultsBean = null;
        Calendar startTime = Calendar.getInstance();
        while (((Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) < 180000) {
            log.info("Searching ..... ");
            try {
                resultsBean = searchAdminService.getAdvancedSearchResults(parameterBean);
            } catch (Exception e) {
                log.error("Error while searching ... ", e);
                return resultsBean;
            }
            if (resultsBean != null) {
                return resultsBean;
            }
            Thread.sleep(5000);
        }
        return resultsBean;
    }
}
