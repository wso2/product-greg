/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.registry.search.metadata.test.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.governance.HumanTaskAdminClient;
import org.wso2.carbon.automation.api.clients.governance.WorkItem;
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.registry.core.LogEntry;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.subscription.test.util.WorkItemClient;

import java.rmi.RemoteException;
import java.util.Calendar;

public class CommonUtils {

    private static Log log = LogFactory.getLog(CommonUtils.class) ;

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
