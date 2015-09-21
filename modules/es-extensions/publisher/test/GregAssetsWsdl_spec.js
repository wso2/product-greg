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
    it('Test add wsdl', function() {
        var url = utils.server_url + '/assets?type=wsdl';
        var asset = {
            'overview_url': 'https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules/integration/registry/tests-metadata/src/test/resources/artifacts/GREG/wsdl/sample.wsdl',
            'overview_name': 'sample.wsdl',
            'overview_version': '1.2.3'
        };
        var header = utils.obtainAuthorizedHeaderForAPICall();
        var result;
        try {
            result = post(url, asset, header, 'json');
        } catch (e) {
            log.error(e);
        } finally {
            // deleteArtifacts();
            utils.logoutAuthorizedUser(header);

            expect(result.data).not.toBe(undefined);
            expect(result.data.overview_name).toEqual(asset.overview_name);
            expect(result.data.overview_version).toEqual(asset.overview_version);
            expect(result.data.overview_url).toEqual(asset.overview_url);
            expect(result.data.type).toEqual("wsdl");
        }
    });
});
/*
Creates a WSDLManager, to be able to get the
ID of the wsdl created.
 */

var createWsdlManager = function() {
    var carbon = require('carbon');
    var host = "https://localhost:10343/admin";
    var server = new carbon.server.Server(host);
    var options = {
        username: 'admin',
        domain: 'carbon.super',
        tenantId: -1234
    };
    var registry = new carbon.registry.Registry(server, options);
    var wsdlManager = new Packages.org.wso2.carbon.governance.api.wsdls.WsdlManager(registry.registry.getChrootedRegistry("/_system/governance"));
    return wsdlManager;
};
var deleteArtifacts = function() {
    var wsdlManager = createWsdlManager();
    var wsdls = wsdlManager.getAllWsdls();
    var wsdl;
    for (var index in wsdls) {
        if (wsdls[index].getPath().contains("sample.wsdl")) {
            wsdl = wsdls[index];
        }
    }
    var dependencies = wsdl.getDependencies();
    var dependents = wsdl.getDependents();
    for (var index in dependents) {
        var path = dependents[index].getPath();
        var subPaths = path.split('/');
        var associationTypePlural = subPaths[2];
        var associationType = associationTypePlural.substring(0, associationTypePlural.lastIndexOf('s'));
        utils.deleteAssetWithID(dependents[index].getId(), associationType);
    }
    utils.deleteAssetWithID(wsdl.getId(), "wsdl");
    for (var index in dependencies) {
        var path = dependencies[index].getPath();
        var subPaths = path.split('/');
        var associationTypePlural = subPaths[2];
        var associationType = associationTypePlural.substring(0, associationTypePlural.lastIndexOf('s'));
        utils.deleteAssetWithID(dependencies[index].getId(), associationType);
    }
};