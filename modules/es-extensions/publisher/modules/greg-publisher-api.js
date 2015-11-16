var gregAPI = {};
(function(gregAPI) {
    var InfoUtil = Packages.org.wso2.carbon.registry.info.services.utils.InfoUtil;
    var populator = Packages.org.wso2.carbon.registry.info.services.utils.SubscriptionBeanPopulator;
    var SubscriptionPopulator = Packages.org.wso2.carbon.registry.info.services.utils.SubscriptionBeanPopulator;
    var GovernanceUtils = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils;
    var CommonUtil = Packages.org.wso2.carbon.governance.registry.extensions.utils.CommonUtil;

    var carbon = require('carbon');
    var taskOperationService = carbon.server.osgiService('org.wso2.carbon.humantask.core.TaskOperationService');
    gregAPI.notifications = {};
    gregAPI.subscriptions = {};
    gregAPI.notes = {};
    gregAPI.associations = {};
    gregAPI.serviceDiscovery = {};
    gregAPI.password = {};
    var formatResultSet = function(output) {
        var results = {};
        var entry;
        var resultEntry;
        results.PublisherResourceUpdated = {};
        results.PublisherResourceUpdated.email = {};
        results.PublisherResourceUpdated.work = {};
        results.PublisherResourceUpdated.email.checked = false;
        results.PublisherResourceUpdated.work.checked = false;
        results.PublisherLifeCycleStateChanged = {};
        results.PublisherLifeCycleStateChanged.email = {};
        results.PublisherLifeCycleStateChanged.work = {};
        results.PublisherLifeCycleStateChanged.email.checked = false;
        results.PublisherLifeCycleStateChanged.work.checked = false;
        results.PublisherCheckListItemChecked = {};
        results.PublisherCheckListItemChecked.email = {};
        results.PublisherCheckListItemChecked.work = {};
        results.PublisherCheckListItemChecked.email.checked = false;
        results.PublisherCheckListItemChecked.work.checked = false;
        results.PublisherCheckListItemUnchecked = {};
        results.PublisherCheckListItemUnchecked.email = {};
        results.PublisherCheckListItemUnchecked.work = {};
        results.PublisherCheckListItemUnchecked.email.checked = false;
        results.PublisherCheckListItemUnchecked.work.checked = false;

        for(var index = 0; index < output.length; index++){
        	entry = output[index];
        	resultEntry = results[entry.eventName];
        	if(resultEntry){
        		//If the notification method is found then set the state to true
        		resultEntry[entry.notificationMethod].checked = true;
        		resultEntry[entry.notificationMethod].id = entry.id;
        	}
        }
        return results;
    };
    gregAPI.subscriptions.list = function(am, assetId) {
        var assert = am;
        var registryPath = assert.get(assetId).path;
        var userRegistry = am.registry;
        var registryService = am.registry.registry; //carbon.server.osgiService('org.wso2.carbon.registry.core.service.RegistryService');
        var populator = Packages.org.wso2.carbon.registry.info.services.utils.SubscriptionBeanPopulator;
        var SubscriptionPopulator = Packages.org.wso2.carbon.registry.info.services.utils.SubscriptionBeanPopulator;
        var GovernanceUtils = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils; //Used to obtain Asset Types
        var result = [];

        try {
            var subcriptions = SubscriptionPopulator.populate(userRegistry.registry, registryPath).getSubscriptionInstances();
            var length = subcriptions.length;

            for (var i = 0; i < length; i++) {
                var subOptions = {};
                var subcription = subcriptions[i];
                //print(subcription.getEventName() + "\n");
                subOptions.eventName = subcription.getEventName();
                //print(subcription.getTopic() + "\n");
                subOptions.topic = subcription.getTopic();
                //print(subcription.getAddress() + "\n");
                subOptions.address = subcription.getAddress();
                //print(subcription.getDigestType() + "\n");
                subOptions.digestType = subcription.getDigestType();
                //print(subcription.getOwner() + "\n");
                subOptions.owner = subcription.getOwner();
                //print(subcription.getSubManUrl() + "\n");
                subOptions.eventName = subcription.getEventName();
                //print(subcription.getId() + "\n");
                subOptions.id = subcription.getId();
                //print(subcription.getNotificationMethod() + "\n");
                subOptions.notificationMethod = subcription.getNotificationMethod();
                //output +=  subOptions;
                result.push(subOptions);
            }
        } catch(e) {
            log.error(e);
        }

        return formatResultSet(result);
    };
    gregAPI.subscriptions.add = function(am, assetId, username, subscriptionType, notificationMethod) {
        var assert = am; //assetManager(session, options.type);
        var registryPath = assert.get(assetId).path;
        var parms = getParameters(request);
        var notiType = subscriptionType; //parms.notificationType;
        var notiMethod = notificationMethod; //parms.notificationMethod;
        var username = username; //server.current(session).username;
        var realmService = carbon.server.osgiService('org.wso2.carbon.user.core.service.RealmService');
        var realm = realmService.getTenantUserRealm(server.current(session).tenantId);
        var allRoles = realm.getUserStoreManager().getRoleListOfUser(username);
        var emailAdd = realm.getUserStoreManager().getUserClaimValue(username, org.wso2.carbon.user.core.UserCoreConstants.ClaimTypeURIs.EMAIL_ADDRESS, org.wso2.carbon.user.core.UserCoreConstants.DEFAULT_PROFILE);
        var hasAdminRole;
        var userRole;
        for (var i = 0; i < allRoles.length; i++) {
            //log.info(allRoles[i]);
            if (String(allRoles[i]) === 'admin') {
                //log.info('hasAdminRole');
                hasAdminRole = true;
            } else if (allRoles[i].startsWith('Internal') && !allRoles[i].endsWith('everyone')) {
                userRole = allRoles[i];
            }
        };
        //log.info(hasAdminRole);
        if (notiMethod === "work") {
            if (hasAdminRole) {
                notiMethod = notiMethod + "://admin";
            } else if (userRole != null) {
                notiMethod = notiMethod + "://" + userRole;
            } else {
                var message = {
                    'error': 'User Profile doesn\'t have validate profile'
                };
                print(message);
                return;
            }
        } else if (notiMethod === "email") {
            if (emailAdd == null) {
                var message = {
                    'error': 'User Profile doesn\'t have email address'
                };
                print(message);
                return;
            }
            notiMethod = "mailto:" + emailAdd;
        } else {
            var message = {
                'error': 'unsuported method'
            };
            print(message);
            return;
        }
        var result = [];
        var SubscriptionPopulator = Packages.org.wso2.carbon.registry.info.services.utils.SubscriptionBeanPopulator;
        
        try {
            var subcriptions = SubscriptionPopulator.subscribeAndPopulate(userRegistry.registry, registryPath, notiMethod, notiType).getSubscriptionInstances();
            var length = subcriptions.length;
            for (var i = 0; i < length; i++) {
                var subOptions = {};
                var subcription = subcriptions[i];
                subOptions.eventName = subcription.getEventName();
                subOptions.id = subcription.getId();
                subOptions.topic = subcription.getTopic();
                subOptions.address = subcription.getAddress();
                //log.info("A" + subOptions);
                result.push(subOptions);
            }
        } catch (e) {
            log.error(e);
        }
    };
    gregAPI.subscriptions.remove = function(am, assetId) {
        var parms = getParameters(request);
        //log.info(parms.subcriptionid);
        try {
            var assert = assetManager(session, options.type);
            var registryPath = assert.get(options.id).path;
            var InfoUtil = Packages.org.wso2.carbon.registry.info.services.utils.InfoUtil;
            InfoUtil.unsubscribe(userRegistry.registry, registryPath, parms.subcriptionid, null);
        } catch (e) {
            var message = {
                'status': 'error occured'
            };
            print(message);
            log.warn(e);
            return;
        }
        //print('This is a DELETE');
        var message = {
            'status': 'sucess'
        };
        //log.info(message);
    };
    gregAPI.notifications.count = function() {
        var results = {};
        var count = 0;
        var queryInput = new org.wso2.carbon.humantask.client.api.types.TSimpleQueryInput();
        queryInput.setPageNumber(0);
        queryInput.setSimpleQueryCategory(org.wso2.carbon.humantask.client.api.types.TSimpleQueryCategory.ASSIGNED_TO_ME);
        var resultSet = taskOperationService.simpleQuery(queryInput);
        var rows = resultSet.getRow();
        if (rows != null){
            count = rows.length;
        }
        //log.info(parseInt(count));
        results.count = count;
        return results;
    };
    gregAPI.notifications.list = function(am) {
        var results = {};
        var result = [];
        var count = 0;
        var queryInput = new org.wso2.carbon.humantask.client.api.types.TSimpleQueryInput();
        queryInput.setPageNumber(0);
        queryInput.setSimpleQueryCategory(org.wso2.carbon.humantask.client.api.types.TSimpleQueryCategory.ASSIGNED_TO_ME);
        var resultSet = taskOperationService.simpleQuery(queryInput);
        var rows = resultSet.getRow();
        if (rows != null) {
            for (var i = 0; i < rows.length; i++) {
                var workList = {};
                var row =  rows[i];
                workList.id = String(row.getId());
                workList.presentationSubject = String(row.getPresentationSubject());
                
                //Get Assrt information
                var arr = workList.presentationSubject.split(" ");
                var pathValue;
                for (var a = 0; a < arr.length; a++) {
                    if(10 < arr[a].length){
                        pathValue = arr[a];
                    }
                }
                if (endsWith('.',pathValue)){
                    pathValue = pathValue.substr(0,pathValue.length-1);
                }
                if (am.registry.registry.resourceExists(pathValue) && am.registry.registry.get(pathValue).getMediaType() != null) {
                    var uuid = am.registry.registry.get(pathValue).getUUID();
                    workList.presentationSubject = workList.presentationSubject.replace(pathValue, "");


                    var attifact = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils.findGovernanceArtifactConfigurationByMediaType(am.registry.registry.get(pathValue).getMediaType(), am.registry.registry);
                    //log.info(attifact.getKey());
                    var key = String(attifact.getKey());
                    workList.uuid = uuid;
                    workList.type = String(attifact.getKey());
                    if (key === 'wsdl' || key === 'wadl' || key === 'policy' || key === 'schema' || key === 'endpoint') {
                        var subPaths = pathValue.split('/');
                        workList.overviewName = subPaths[subPaths.length - 1];
                    } else {
                        var govAttifact = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils.retrieveGovernanceArtifactByPath(am.registry.registry, pathValue);
                        workList.overviewName = String(govAttifact.getAttribute('overview_name'));
                    }

                    workList.presentationSubject = workList.presentationSubject.replace("resource at path", workList.overviewName);
                    workList.presentationSubject = workList.presentationSubject.replace("resource at", workList.overviewName);
                    //workList.message = workList.overviewName +
                    workList.clickResource = true; //This will be checked in order to show or not 'Click here' link in the notification.
                }
                else {
                    workList.clickResource = false;//If this is false 'Click here' link will not be shown as there is no such resource.
                }
                workList.presentationName = String(row.getPresentationName());
                workList.priority = String(row.getPriority());
                workList.status = String(row.getStatus());
                workList.time = String(row.getCreatedTime().getTime());
                //workList.createdTime = String(row.getCreatedTime());
                workList.user = String(taskOperationService.loadTask(row.getId()).getActualOwner().getTUser());
                result.push(workList);
            }
        }
        results.list = result;
        return results;
    };
    var endsWith = function(suffix, val) {
        return val.indexOf(suffix, val.length - suffix.length) !== -1;
    };

    var startsWith  = function(str, prefix) {
       return str.indexOf(prefix) === 0;
    }

    var assetManager = function(session, type) {
        var rxt = require('rxt');
        var am = rxt.asset.createUserAssetManager(session, type);
        return am;
    };

    gregAPI.associations.listPossible = function (type, association, id) {
        var resultList = new Object();
        resultList.results = [];
        var map = CommonUtil.getAssociationConfig(type);
        if (!map) {
            map = CommonUtil.getAssociationConfig("default");
        }
        var assetsTypes = (map.get(association)).split(",");
        for (var i = 0; i < assetsTypes.length; i++) {
            try {
                var manager = assetManager(session, assetsTypes[i]).am;
                var artifacts = manager.search();
                for (var j = 0; j < artifacts.length; j++) {
                    var assetJson = new Object();
                    assetJson.uuid = manager.registry.registry.get(artifacts[j].path).getUUID();
                    if(assetJson.uuid == id ) { continue; }
                    assetJson.text = artifacts[j].attributes.overview_name;
                    if(assetJson.text == null){
                        var subPaths =  artifacts[j].path.split('/');
                        assetJson.text = subPaths[subPaths.length - 1]
                    }
                    assetJson.type = artifacts[j].mediaType;
                    assetJson.shortName = artifacts[j].type;
                    resultList.results.push(assetJson);
                }
            } catch (e) {
                log.warn('Artifact type ' + assetsTypes[i]
                + ' defined in the association-config.xml is not in registry or unable to find relevant configuration.' + e);
            }

        }
        return resultList;

    }

    gregAPI.associations.add = function(session, sourceType, sourceUUID, destType, destUUID, associationType) {
        var srcam = assetManager(session, sourceType);
        var sourcePath = srcam.get(sourceUUID).path;
        var destam = assetManager(session, destType);
        var destPath = destam.get(destUUID).path;
        srcam.registry.registry.addAssociation(sourcePath,destPath,associationType);
    }

    gregAPI.associations.list = function(session, type, path) {
        var am = assetManager(session, type);
        var resultList = new Object();
        resultList.results = [];
        var results = am.registry.associations(path);
        var artifact;
        for(var i=0; i < results.length; i++){
            if (results[i].src == path){
                var destPath = results[i].dest
                try {
                    artifact = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils.findGovernanceArtifactConfigurationByMediaType(am.registry.registry.get(destPath).getMediaType(), am.registry.registry);
                } catch (e){
                    log.warn("Association can not be retrieved. Resource does not exist at path "+destPath);
                    continue;
                }

                var assetJson = new Object();
                var uuid = am.registry.registry.get(destPath).getUUID();
                var key = String(artifact.getKey());
                if (key === 'wsdl' || key === 'wadl' || key === 'policy' || key === 'schema' || key === 'endpoint' || key === 'swagger'){
                    var subPaths = destPath.split('/');
                    assetJson.text = subPaths[subPaths.length - 1];
                } else {
                    var govAttifact = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils.retrieveGovernanceArtifactByPath(am.registry.registry,destPath);
                    var name = (govAttifact.getAttribute("overview_name"));
                    if (name === null || name.length === 0) {
                        assetJson.text = govAttifact.getQName().getLocalPart();
                    } else {
                        assetJson.text = String(name);
                    }
                }
                assetJson.type = am.registry.registry.get(destPath).getMediaType();
                assetJson.associationType = results[i].type;
                assetJson.uuid = uuid;
                assetJson.shortName = key;
                resultList.results.push(assetJson);
            }
        }
        return resultList

    }
    gregAPI.associations.listTypes = function(type) {
        var map = CommonUtil.getAssociationConfig(type);
        if(!map){
            map = CommonUtil.getAssociationConfig("default");
        }
        var results = map.keySet().toArray();
        return results;

    }
    gregAPI.associations.remove = function(session, sourceType, sourceUUID, destType, destUUID, associationType) {

        var srcam = assetManager(session, sourceType);
        var sourcePath = srcam.get(sourceUUID).path;
        var destam = assetManager(session, destType);
        var destPath = destam.get(destUUID).path;
        srcam.registry.registry.removeAssociation(sourcePath,destPath,associationType);
    }


    gregAPI.notifications.remove = function(registry, notificationId) {};
    gregAPI.notes.reply = function() {};
    gregAPI.notes.replies = function(parentNoteId) {};
    gregAPI.userRegistry = function(session) {};
    gregAPI.assetManager = function(session, type) {};

    gregAPI.getAssetVersions = function (session, type, path, name) {
        var am = assetManager(session, type);
        var resource = am.registry.registry.get(path);
        var params = path.split("/" + name);
        var version_left_index = params[0];
        var collection_path = version_left_index.substring(0, version_left_index.lastIndexOf("/"));
        var base_version = version_left_index.substring(version_left_index.lastIndexOf("/") + 1, version_left_index.length);

        var resource = am.registry.get(collection_path);
        var children;
        var collection;
        if (resource.collection) {
            collection = resource;
        }

        if (!resource.collection) {
            throw 'Provided resource is not a collection';
        }

        children = am.registry.content(collection.path);

        var versions = [];
        for (var i = 0; i < children.length; i++) {
            var version = {};
            version.version = children[i].substring(children[i].lastIndexOf("/") + 1, children[i].length());
            if (base_version != version.version) {
                version.path = collection_path + "/" + version.version + "/" + name;
                if (am.registry.registry.resourceExists(version.path)) {
                    versions.push(version);
                }
            }
        }

        return versions;
    };

    gregAPI.serviceDiscovery.discovery = function (session, type, id) {
        var rxt = require('rxt');
        var assetManager = rxt.asset.createUserAssetManager(session, type);
        var genericArtifact = assetManager.am.manager.getGenericArtifact(id);
        var ServerDiscoveryService = Packages.org.wso2.carbon.governance.registry.extensions.discoveryagents.
            ServerDiscoveryService;
        var serverDiscoveryService = new ServerDiscoveryService();
        try {
            return serverDiscoveryService.discoverArtifacts(genericArtifact);
        } catch (e) {
            log.error('Message - ' + e.message);
            log.error('File name - ' + e.fileName);
            log.error('Line number - ' + e.lineNumber);
            throw 'Discovery agent is not configured properly.';
        }
    };

    gregAPI.serviceDiscovery.save = function (session, type, serverId, discoveryServicesData, existArtifactStrategy,
                                              orphanArtifactStrategy) {
        var HashMap = java.util.HashMap;
        var ArrayList = java.util.ArrayList;
        var discoveryServiceDataMap = new HashMap();
        for (var key in discoveryServicesData) {
            var detachedGenericArtifactList = new ArrayList();
            for (var key2 in discoveryServicesData[key].data) {
                var DetachedGenericArtifact = Packages.org.wso2.carbon.governance.api.generic.dataobjects
                    .DetachedGenericArtifactImpl;
                var Gson = Packages.com.google.gson.Gson;
                var gson = new Gson();
                var result = gson.fromJson(stringify(discoveryServicesData[key].data[key2]), DetachedGenericArtifact);
                detachedGenericArtifactList.add(result);
            }
            discoveryServiceDataMap.put(discoveryServicesData[key].serviceType, detachedGenericArtifactList);
        }

        var ServerDiscoveryService = Packages.org.wso2.carbon.governance.registry.extensions.discoveryagents.
            ServerDiscoveryService;
        var serverDiscoveryService = new ServerDiscoveryService();

        var rxt = require('rxt');
        var assetManager = rxt.asset.createUserAssetManager(session, type);
        var serverArtifact = assetManager.am.manager.getGenericArtifact(serverId);
        return serverDiscoveryService.persistArtifacts(discoveryServiceDataMap, serverArtifact, existArtifactStrategy,
            orphanArtifactStrategy);
    };

    gregAPI.serviceDiscovery.getDiscoveryEnumData = function () {
        var discoveryEnumData = {};
        var ExistArtifactStrategy = org.wso2.carbon.governance.registry.extensions.discoveryagents.
            ExistArtifactStrategy;
        discoveryEnumData.existArtifactStrategy = [];
        for(var index = 0; index < ExistArtifactStrategy.values().length; index++){
            discoveryEnumData.existArtifactStrategy.push(ExistArtifactStrategy.values()[index].name());
        }

        var OrphanArtifactStrategy = org.wso2.carbon.governance.registry.extensions.discoveryagents.
            OrphanArtifactStrategy;
        discoveryEnumData.orphanArtifactStrategy = [];
        for(var index = 0; index < OrphanArtifactStrategy.values().length; index++){
            discoveryEnumData.orphanArtifactStrategy.push(OrphanArtifactStrategy.values()[index].name());
        }
        return discoveryEnumData;
    };

    gregAPI.password.addNewPassword = function (session, type, key, value) {
        var am = assetManager(session, type);
        var registry = am.registry.registry;
        // Collection path used to store key and encrypted password value.
        var path = "/_system/config/repository/components/secure-vault";
        var resource;

        if(registry.resourceExists(path)){
            resource = registry.get(path);
        }
        else {
            resource = registry.newCollection();
        }

        // Osgi service used to encrypt password.
        var securityService =  carbon.server.osgiService('org.wso2.carbon.registry.security.vault.service.RegistrySecurityService');
        var properties = [];
        properties[1] = "";
        if (key != null && value != null){
            var encryptedText = securityService.doEncrypt(value);
            resource.setProperty(key, encryptedText);
            registry.beginTransaction();
            registry.put(path, resource);
            registry.commitTransaction();
            properties[1] = "Password Saved Successfully";
        }

        var properties;
        if(registry.resourceExists(path)){
            var collection = registry.get(path);
            properties[0] = collection.getProperties();
        }

        return properties;
    }
}(gregAPI));