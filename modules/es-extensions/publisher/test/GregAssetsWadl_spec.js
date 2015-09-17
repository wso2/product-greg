/*
 * Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var utils = require("/test/test-utils/test_util.js");
/**
 * Groups TEST cases for WSDL-POST
 */
describe('Assets POST - Publisher API', function() {
    /*
     * Endpoint: /publisher/apis/assets?type=wsdl
     * Method: POST
     * Response: created asset
     * test: check for a return-id
     */
    it('Test add wadl', function() {
        var url = utils.server_url + '/assets?type=wadl';
        var asset = {
            'overview_url': 'https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/wadl/SearchSearvice.wadl',
            'overview_name': 'SearchSearvice.wadl',
            'overview_version': '1.2.3'
        };
        var header = utils.obtainAuthorizedHeaderForAPICall();
        var result;
        try {
            result = post(url, asset, header, 'json');
        } catch (e) {
            log.error(e);
        } finally {
            utils.logoutAuthorizedUser(header);
            
            expect(result.data).not.toBe(undefined);
            expect(result.data.overview_name).toEqual(asset.overview_name);
            expect(result.data.overview_version).toEqual(asset.overview_version);
            expect(result.data.overview_url).toEqual(asset.overview_url);
            expect(result.data.type).toEqual("wadl");
        }
    });
});
