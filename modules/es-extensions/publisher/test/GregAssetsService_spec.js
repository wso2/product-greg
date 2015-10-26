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
 * Groups TEST cases for Service-POST
 */
describe('Assets POST - Publisher API', function() {
    /*
     * Endpoint: /publisher/apis/assets?type=service
     * Method: POST
     * Response: created asset
     * test: check for a return-id
     */
    it('Test add service', function() {
        var url = utils.server_url + '/assets?type=soapservice';
        var asset = {
            'overview_name': 'TestService',
            'overview_version': '1.2.3',
            'overview_namespace': 'org.wso2.test',
            'overview_description': 'test description',
            'interface_wsdlUrl': 'https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules/integration/registry/tests-metadata/src/test/resources/artifacts/GREG/wsdl/sample.wsdl',
            'docLinks_documentType': 'pdf',
            'docLinks_url': 'http://www.jetbrains.com/idea/docs/IntelliJIDEA_ReferenceCard.pdf',
            'docLinks_documentComment': 'This is a pdf'
        };
        var header = utils.obtainAuthorizedHeaderForAPICall();
        var response;
        try {
            response = post(url, asset, header, 'json');
        } catch (e) {
            log.error(e);
        } finally {            
            // utils.deleteAssetWithID(response.data.id, "soapservice");
            var deleteUrl = utils.server_url + '/assets/' + response.data.id + '?type=soapservice';
            var deleted = del(deleteUrl, {}, header, 'json');
            // for (var index in response.data.data.dependencies) {
            //     utils.deleteAssetWithID(response.data.data.dependencies[index].associationUUID, response.data.data.dependencies[index].associationType);
            // }
            // for (var index in response.data.data.dependents) {
            //     utils.deleteAssetWithID(response.data.data.dependents[index].associationUUID, response.data.data.dependencies[index].associationType);
            // }
            utils.logoutAuthorizedUser(header);
            expect(response.data).not.toBe(undefined);
            expect(response.data.name).toEqual(asset.overview_name);
            expect(response.data.attributes.overview_version).toEqual(asset.overview_version);
            expect(response.data.attributes.overview_namespace).toEqual(asset.overview_namespace);
            expect(response.data.attributes.overview_description).toEqual(asset.overview_description);
            expect(response.data.attributes.interface_wsdlUrl).toEqual(asset.interface_wsdlUrl);
            //expect(response.data.wsdl_url).toEqual("/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/1.2.3/TestService.wsdl");
            //expect(response.data.attributes.endpoints_entry).toEqual(":http://footballpool.dataaccess.eu/data/info.wso");
            expect(response.data.lifecycle).toEqual("ServiceLifeCycle");
            var genericArtifactManager = createGenericArtifactManager();
            var artifact = createArtifact(genericArtifactManager);
            expect(response.data.name).toEqual(String((artifact.getQName().getLocalPart())));
            expect(response.data.attributes.overview_namespace).toEqual(String((artifact.getQName().getNamespaceURI())));
            expect(response.data.lifecycle).toEqual(String((artifact.getLifecycleName())));
            removeArtifact(genericArtifactManager, artifact);
        }
    });
});
/**
 * Groups TEST cases for Asset-GET
 */
describe('Assets GET - Publisher API', function() {
    /*
     * Endpoint: /publisher/apis/assets?type=service
     * Method: GET
     * Response: asset
     * test: asset name
     */
    it('Test get service by id', function() {
        var assetId = getAsset("SampleService").id;
        var url = utils.server_url + '/assets/' + assetId + '?type=soapservice';
        url = encodeURI(url);
        var header = utils.obtainAuthorizedHeaderForAPICall();
        try {
            header.Accept = "*/*";
            var response = get(url, {}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            // utils.deleteAssetWithID(assetId, "service");
            // for (var index in response.data.data.dependencies) {
            //     utils.deleteAssetWithID(response.data.data.dependencies[index].associationUUID, response.data.data.dependencies[index].associationType);
            // }
            // for (var index in response.data.data.dependents) {
            //     utils.deleteAssetWithID(response.data.data.dependents[index].associationUUID, response.data.data.dependencies[index].associationType);
            // }
            var deleted = del(url, {}, header, 'json');
            
            utils.logoutAuthorizedUser(header);

            expect(response.data).not.toBe(undefined);
            expect(response.data.attributes.overview_name).toEqual('SampleService');
            var genericArtifactManager = createGenericArtifactManager();
            var rawArtifact = createSampleArtifact(genericArtifactManager);
            var artifact = genericArtifactManager.getGenericArtifact(rawArtifact.getId());
            expect(response.data.name).toEqual(String((artifact.getQName().getLocalPart())));
            expect(response.data.attributes.overview_namespace).toEqual(String((artifact.getQName().getNamespaceURI())));
            expect(response.data.lifecycle).toEqual(String((artifact.getLifecycleName())));
            removeArtifact(genericArtifactManager, artifact);
        }
    });
});
/**
 * Groups TEST cases for Asset-DELETE
 */
describe('Assets DELETE - Publisher API', function() {
    /*
     * Endpoint: /publisher/apis/assets?type=service
     * Method: DELETE
     * Response: asset
     * test: asset name
     */
    it('Test delete service by id', function() {
        var asset = getAsset("AnotherService");
        var assetId = asset.id;
        var url = utils.server_url + '/assets/' + assetId + '?type=soapservice';
        var header = utils.obtainAuthorizedHeaderForAPICall();
        try {
            header.Accept = "*/*";
            var response = del(url, {}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            expect(response.data.message).toEqual('Asset Deleted Successfully');
            // for (var index in asset.dependencies) {
            //     utils.deleteAssetWithID(asset.dependencies[index].associationUUID, asset.dependencies[index].associationType);
            // }
            // for (var index in asset.dependents) {
            //     utils.deleteAssetWithID(asset.dependents[index].associationUUID, asset.dependencies[index].associationType);
            // }
            utils.logoutAuthorizedUser(header);
        }
    });
});

var createGenericArtifactManager = function() {
    var carbon = require('carbon');
    var host = "https://localhost:10343/admin";
    var server = new carbon.server.Server(host);
    var options = {
        username: 'admin',
        domain: 'carbon.super',
        tenantId: -1234
    };
    var registry = new carbon.registry.Registry(server, options);
    Packages.org.wso2.carbon.governance.api.util.GovernanceUtils.loadGovernanceArtifacts(registry.registry);
    var genericArtifactManager = new Packages.org.wso2.carbon.governance.api.generic.GenericArtifactManager(registry.registry.getChrootedRegistry("/_system/governance"), "service");
    return genericArtifactManager;
};
var createArtifact = function(genericArtifactManager) {
    var serviceArtifacts = require("/test/test-utils/test_artifacts.json");
    var omContent = serviceArtifacts.ServiceCOnfigurations.metadata;
    var artifact = genericArtifactManager.newGovernanceArtifact(omContent);
    genericArtifactManager.addGenericArtifact(artifact);
    return artifact;
};
var createSampleArtifact = function(genericArtifactManager) {
    var serviceArtifacts = require("/test/test-utils/test_artifacts.json");
    var omContent = serviceArtifacts.ServiceSampleConfigurations.metadata;
    var artifact = genericArtifactManager.newGovernanceArtifact(omContent);
    genericArtifactManager.addGenericArtifact(artifact);
    return artifact;
};
var removeArtifact = function(genericArtifactManager, artifact) {
    var dependencyArtifacts = artifact.getDependencies();
    var dependentArtifacts = artifact.getDependents();
    genericArtifactManager.removeGenericArtifact(artifact.getId());
    removeAssociatingArtifacts(dependencyArtifacts, genericArtifactManager);
    removeAssociatingArtifacts(dependentArtifacts, genericArtifactManager);
};
var removeAssociatingArtifacts = function(associatingArtifacts, genericArtifactManager) {
    if (associatingArtifacts != null) {
        for (var index in associatingArtifacts) {
            genericArtifactManager.removeGenericArtifact(associatingArtifacts[index].getId());
        }
    }
};
/**
 * To add a asset and return the retrieved id of newly added asset
 * @return uuid
 */
var getAsset = function(name) {
    var url = utils.server_url + '/assets?type=soapservice';
    var asset = {
        'overview_name': name,
        'overview_version': '1.2.3',
        'overview_namespace': 'org.wso2.test',
        'overview_description': 'test description',
        'interface_wsdlUrl': 'https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules/integration/registry/tests-metadata/src/test/resources/artifacts/GREG/wsdl/sample.wsdl',
        'docLinks_documentType': 'pdf',
        'docLinks_url': 'http://www.jetbrains.com/idea/docs/IntelliJIDEA_ReferenceCard.pdf',
        'docLinks_documentComment': 'This is a pdf'
    };
    var header = utils.obtainAuthorizedHeaderForAPICall();
    
    var response;
    try {
        response = post(url, asset, header, 'json');
    } catch (e) {
        log.error(e);
    } finally {
        assetId = response.data.id;
        utils.logoutAuthorizedUser(header);
        expect(response.data).not.toBe(undefined);
    }

    return response.data;
};