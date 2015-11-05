/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.es.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import java.util.Map;

public class ESTestCommonUtils {
    private static final Log log = LogFactory.getLog(ESTestCommonUtils.class);
    private String cookieHeader;
    private GenericRestClient genericRestClient;
    private Map<String, String> headerMap;
    private String publisherUrl;

    public ESTestCommonUtils(GenericRestClient genericRestClient, String publisherUrl, Map<String, String> headerMap) {
        this.genericRestClient = genericRestClient;
        this.publisherUrl = publisherUrl;
        this.headerMap = headerMap;
    }

    public ClientResponse searchAssetByQuery(Map<String, String> queryParamMap) throws JSONException {
        ClientResponse clientResponse;
        JSONObject obj;
        double time1 = System.currentTimeMillis();
        int count = 0;
        do {
            clientResponse = genericRestClient.geneticRestRequestGet(publisherUrl + "/assets", queryParamMap,
                    headerMap, cookieHeader);
            Assert.assertNotNull(clientResponse, "Client Response for search rest service cannot be null");
            Assert.assertTrue((clientResponse.getStatusCode() == 200), "Wrong status code ,Expected 200 OK " +
                    clientResponse.getStatusCode());
            String response = clientResponse.getEntity(String.class);
            obj = new JSONObject(response);
            double time2 = System.currentTimeMillis();
            if ((time2 - time1) > 240000) {
                log.error("Timeout while searching for assets | time waited: " + (time2 - time1));
                break;
            }
            count = count + 1;
        } while ((Double)obj.get("count") <= 0);
        double time3 = System.currentTimeMillis();
        System.out.println("Time for query the results: " +(time3 - time1));
        System.out.println("search for the rest service...." + count);
        return clientResponse;
    }

    public ClientResponse getAssetById(String id , Map<String, String> queryParamMap)
    {
        return genericRestClient.geneticRestRequestGet(publisherUrl+"/assets/"+id, queryParamMap,
                headerMap, cookieHeader);
    }

    public void setCookieHeader(String cookieHeader) {
        this.cookieHeader = cookieHeader;
    }
}
