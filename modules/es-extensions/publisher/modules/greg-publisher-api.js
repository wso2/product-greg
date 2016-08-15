var gregAPI = {};
(function(gregAPI) {
    var InfoUtil = Packages.org.wso2.carbon.registry.info.services.utils.InfoUtil;
    var populator = Packages.org.wso2.carbon.registry.info.services.utils.SubscriptionBeanPopulator;
    var SubscriptionPopulator = Packages.org.wso2.carbon.registry.info.services.utils.SubscriptionBeanPopulator;
    var GovernanceUtils = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils;
    var CommonUtil = Packages.org.wso2.carbon.governance.registry.extensions.utils.CommonUtil;
    var TSimpleQueryInput = org.wso2.carbon.humantask.client.api.types.TSimpleQueryInput;

    var carbon = require('carbon');
    var time = require('utils').time;
    var constants = require('rxt').constants;
    var responseProcessor = require('utils').response;
    var rxtModule = require('rxt');
    var taskOperationService = carbon.server.osgiService('org.wso2.carbon.humantask.core.TaskOperationService');

    var REGISTRY_PERMISSION_CHECK = "registry.permissionCheck";

    gregAPI.notifications = {};
    gregAPI.subscriptions = {};
    gregAPI.notes = {};
    gregAPI.associations = {};
    gregAPI.serviceDiscovery = {};
    gregAPI.password = {};
    gregAPI.permissions = {};
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

    /**
     * This method retrieve notifications from humantask core TaskOperationService
     */
    var getRefreshedNotificationRows = function () {

        var queryInput = new org.wso2.carbon.humantask.client.api.types.TSimpleQueryInput();
        queryInput.setPageNumber(0);
        queryInput.setSimpleQueryCategory(org.wso2.carbon.humantask.client.api.types.TSimpleQueryCategory.ASSIGNED_TO_ME);
        var resultSet = taskOperationService.simpleQuery(queryInput);
        var rows = resultSet.getRow();

        var queryInputClaim = new org.wso2.carbon.humantask.client.api.types.TSimpleQueryInput();
        queryInputClaim.setPageNumber(0);
        queryInputClaim.setSimpleQueryCategory(org.wso2.carbon.humantask.client.api.types.TSimpleQueryCategory.CLAIMABLE);
        var resultSetClaim = taskOperationService.simpleQuery(queryInputClaim);
        if (rows != null && resultSetClaim.getRow() != null) {
            rows = org.apache.commons.lang.ArrayUtils.addAll(rows, resultSetClaim.getRow());
        } else if (rows == null && resultSetClaim.getRow() != null) {
            rows = resultSetClaim.getRow();
        }

        return rows;
    };

    /**
     * This method is to clean the notifications
     */
    var getNotificationRows = function () {

        var rows = getRefreshedNotificationRows();

        // Below if block is a workaround to stop showing duplicate notifications.
        if (rows != null) {
            for (var i = 0; i < rows.length; i++) {
                var row = rows[i];
                if (rows.length > (i + 1)) {
                    var row2 = rows[i + 1];
                    if ((String(row.getPresentationSubject()) == String(row2.getPresentationSubject()))
                        && (getTimeFromRow(row.getCreatedTime()) == getTimeFromRow(row2.getCreatedTime()))) {
                        var id = String(row2.getId());
                        var idObj = new org.apache.axis2.databinding.types.URI(id);
                        try {
                            // Deleting duplicate notifications permanently.
                            taskOperationService.start(idObj);
                            taskOperationService.complete(idObj, "<WorkResponse>true</WorkResponse>");
                        } catch (e) {
                            log.warn(e);
                        }
                    }
                }
            }
        }

        var newRows = getRefreshedNotificationRows();

        return newRows;
    };

    gregAPI.notifications.count = function(am) {
        var results = {};
        var count = 0;
        var rows = getNotificationRows();

        if (rows != null) {
            for (var i = 0; i < rows.length; i++) {
                var row = rows[i];
                var presentSub = String(row.getPresentationSubject());
                var pathValue = presentSub.substring(presentSub.indexOf("/"));
                //This code is done since there are different messages are received for lifecycle and information update notification
                pathValue = pathValue.replace("was updated", "");
                if (endsWith('.', pathValue)) {
                    pathValue = pathValue.substr(0, pathValue.length - 1);
                }
                pathValue = pathValue.trim();
                if (am.registry.registry.resourceExists(pathValue) &&
                    am.registry.registry.get(pathValue).getMediaType() != null &&
                    getTimeFromRow(row.getCreatedTime()) > getTimeFromDate(am.registry.registry.get(pathValue).getCreatedTime())) {
                    count++;
                }
            }
        }
        results.count = count;
        return results;
    };
    gregAPI.notifications.list = function(am) {
        var results = {};
        var result = [];
        var rows = getNotificationRows();

        if (rows != null) {
            for (var i = 0; i < rows.length; i++) {
                var workList = {};
                var row =  rows[i];
                workList.id = String(row.getId());
                workList.presentationSubject = String(row.getPresentationSubject());

                var pathValue = workList.presentationSubject.substring(workList.presentationSubject.indexOf("/"));
                //This code is done since there are different messages are received for lifecycle and information update notification
                pathValue = pathValue.replace("was updated", "");
                if (endsWith('.',pathValue)){
                    pathValue = pathValue.substr(0,pathValue.length-1);
                }
                pathValue = pathValue.trim();
                if (am.registry.registry.resourceExists(pathValue) &&
                    am.registry.registry.get(pathValue).getMediaType() != null &&
                    getTimeFromRow(row.getCreatedTime()) > getTimeFromDate(am.registry.registry.get(pathValue).getCreatedTime())) {
                    workList.id = String(row.getId());
                    workList.presentationSubject = String(row.getPresentationSubject());
                    var uuid = am.registry.registry.get(pathValue).getUUID();
                    workList.presentationSubject = workList.presentationSubject.replace(pathValue, "");


                    var attifact = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils.findGovernanceArtifactConfigurationByMediaType(am.registry.registry.get(pathValue).getMediaType(), am.registry.registry);
                    //log.info(attifact.getKey());
                    var key = String(attifact.getKey());
                    workList.uuid = uuid;
                    workList.type = String(attifact.getKey());
                    if (key === 'wsdl' || key === 'wadl' || key === 'policy' || key === 'schema' || key === 'endpoint' || key === 'swagger') {
                        var subPaths = pathValue.split('/');
                        workList.overviewName = subPaths[subPaths.length - 1];
                        workList.overviewVersion = subPaths[subPaths.length - 2];
                    } else {
                        var govAttifact = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils.retrieveGovernanceArtifactByPath(am.registry.registry, pathValue);
                        workList.overviewName = String(govAttifact.getQName().getLocalPart());
                        var rxtManager = rxtModule.core.rxtManager(server.current(session).tenantId);
                        var versionAttribute = rxtManager.getVersionAttribute(key);
                        workList.overviewVersion = govAttifact.getAttribute(versionAttribute);
                        if (!workList.overviewVersion) {
                            workList.overviewVersion = "";
                        }
                    }

                    workList.presentationSubject = workList.presentationSubject.replace("resource at path", workList.overviewName + " " + workList.overviewVersion);
                    workList.presentationSubject = workList.presentationSubject.replace("resource at", workList.overviewName + " " + workList.overviewVersion);
                    //workList.message = workList.overviewName +
                    workList.clickResource = true; //This will be checked in order to show or not 'Click here' link in the notification.
                    workList.presentationName = String(row.getPresentationName());
                    workList.priority = String(row.getPriority());
                    workList.status = String(row.getStatus());
                    workList.time = time.formatTimeAsTimeSince(getDateTime(row.getCreatedTime()));
                    var owner = taskOperationService.loadTask(row.getId()).getActualOwner();
                    if (owner != null) {
                        workList.user = String(owner.getTUser());
                    } else {
                        workList.user = "";
                    }
                    result.push(workList);
                }
            }
        }
        results.list = result;
        return results;
    };

    gregAPI.notifications.clear = function (res) {
        var rows = getNotificationRows();
        if (rows) {
            for (var i = 0; i < rows.length; i++) {
                var row = rows[i];
                var id = String(row.getId());
                var idObj = new org.apache.axis2.databinding.types.URI(id);
                try {
                    taskOperationService.start(idObj);
                    taskOperationService.complete(idObj, "<WorkResponse>true</WorkResponse>");
                } catch (e) {
                    log.warn(e);
                    return responseProcessor.buildErrorResponseDefault(e.code, 'error on clearing notifications', res,
                        'Failed to clear all notifications ', e.message, []);
                }
            }
        }
        message = {
            'status': 'success'
        };
        return responseProcessor.buildSuccessResponseDefault(constants.STATUS_CODES.OK, res, message);
    };

    var getDateTime = function(date) {
        var year = date.get(date.YEAR);
        var month = date.get(date.MONTH);
        var day = date.get(date.DAY_OF_MONTH);
        var hours = date.get(date.HOUR_OF_DAY);
        var minutes = date.get(date.MINUTE);
        var seconds = date.get(date.SECOND);

        return new Date(year,month,day,hours,minutes,seconds);
    };

    var getTimeFromRow = function(date) {
        var year = date.get(date.YEAR);
        var month = date.get(date.MONTH);
        var day = date.get(date.DAY_OF_MONTH);
        var hours = date.get(date.HOUR_OF_DAY);
        var minutes = date.get(date.MINUTE);
        var seconds = date.get(date.SECOND);

        return new Date(year,month,day,hours,minutes,seconds).getTime();
    };

    var getTimeFromDate = function(date) {
        var year = 1900+date.getYear();
        var month = date.getMonth();
        var day = date.getDate();
        var hours = date.getHours();
        var minutes = date.getMinutes();
        var seconds = date.getSeconds();

        return new Date(year,month,day,hours,minutes,seconds).getTime();
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

    /**
     * Used to create the response object.
     *
     * @param code      response code
     * @param message   response message to be shown.
     * @param data      returned data of the response.
     * @return response message object.
     */
    var msg = function (code, message, data) {
        var obj = {};
        obj.code = code;
        obj.message = message;
        obj.data = data;
        return obj;
    };

    /**
     * Used to create the success response message.
     *
     * @param obj      response object
     * @return response success object.
     */
    var successMsg = function (obj) {
        obj.success = true;
        return obj;
    };

    /**
     * Used to create the error response message.
     *
     * @param obj      response object
     * @return response error object.
     */
    var errorMsg = function (obj) {
        obj.success = false;
        return obj;
    };

    /**
     * Validate association already exist before adding.
     *
     * @param session
     * @param type              short name of the asset.
     * @param sourcePath        registry path of the source asset of association.
     * @param destPath          registry path of the destination asset of association.
     * @param associationType   type of the association.
     * @return true if association already exists. Otherwise false.
     */
    var checkExistingAssociations = function (session, type, sourcePath, destPath, associationType) {
        var am = assetManager(session, type);
        var associations = am.registry.associations(sourcePath);
        for (var i = 0; i < associations.length; i++) {
            if (associations[i].dest == destPath && associations[i].type == associationType) {
                return true;
            }
        }
        return false;

    };

    gregAPI.associations.listPossible = function (type, association, id, name) {
        var resultList = new Object();
        resultList.results = [];
        var map = CommonUtil.getAssociationConfig(type);
        if (!map) {
            map = CommonUtil.getAssociationConfig("default");
        }
        var assetsTypes = (map.get(association)).split(",");
        var paging = {
            'start': 0,
            'count': 10,
            'sortOrder': 'ASC',
            'sortBy': 'overview_name',
            'paginationLimit': 10
        };
        for (var i = 0; i < assetsTypes.length; i++) {
            try {
                var rxtManager = rxtModule.core.rxtManager(server.current(session).tenantId);
                var nameAttribute = rxtManager.getNameAttribute(assetsTypes[i]);
                paging.sortBy = nameAttribute ? nameAttribute : paging.sortBy;
                var query = null;
                if (name && name != 'undefined') {
                    query = parse('{"' + nameAttribute + '" : "' + name + '"}');
                }
                var manager = assetManager(session, assetsTypes[i]).am;
                var artifacts = manager.search(query,paging);
                for (var j = 0; j < artifacts.length; j++) {
                    var assetJson = new Object();
                    assetJson.uuid = manager.registry.registry.get(artifacts[j].path).getUUID();
                    if(assetJson.uuid == id ) { continue; }

                    // below script will read the name attribute from rxt if it is defined.
                    if (nameAttribute) {
                        assetJson.text = artifacts[j].attributes[nameAttribute];
                    } else {
                        assetJson.text = artifacts[j].attributes.overview_name;
                    }
                    // below script will read the name attribute from storage path.
                    if(assetJson.text == null){
                        var subPaths =  artifacts[j].path.split('/');
                        assetJson.text = subPaths[subPaths.length - 1]
                    }

                    var version = artifacts[j].attributes.overview_version;
                    if(version == null) {
                        version = artifacts[j].attributes.version;
                    }
                    if(version != null) {
                        assetJson.version = version;
                    }

                    assetJson.type = artifacts[j].mediaType;
                    assetJson.shortName = artifacts[j].type;
                    resultList.results.push(assetJson);
                }
            } catch (e) {
                log.warn('Artifact type ' + assetsTypes[i]
                    + ' defined in the governance.xml is not in registry or unable to find relevant configuration.' + e);
            }

        }
        return resultList;

    };

    gregAPI.associations.add = function(session, sourceType, sourceUUID, destType, destUUID, associationType) {
        var srcam = assetManager(session, sourceType);
        var sourcePath = srcam.get(sourceUUID).path;
        var destam = assetManager(session, destType);
        var destPath = destam.get(destUUID).path;
        var reverseAssociationType = CommonUtil.getReverseAssociationType(sourceType, associationType);
        if (!reverseAssociationType){
            reverseAssociationType = CommonUtil.getReverseAssociationType("default", associationType);
        }
        var destPath = destam.get(destUUID).path;
        var isAssociationExist = checkExistingAssociations(session, sourceType, sourcePath, destPath, associationType);
        if (isAssociationExist) {
            return errorMsg(msg(400, 'Association already exists'));
        } else {
            srcam.registry.registry.addAssociation(sourcePath, destPath, associationType);

        }
        if (reverseAssociationType) {
            srcam.registry.registry.addAssociation(destPath, sourcePath, reverseAssociationType);
        }
        return successMsg(msg(200, 'Association added successfully'));
    }

    gregAPI.associations.list = function(session, type, path) {
        var username = require('store').server.current(session).username;
        var am = assetManager(session, type);
        var resultList = new Object();
        resultList.results = [];
        var results = am.registry.associations(path);
        var artifact,artifactConfig;
        for(var i=0; i < results.length; i++){
            if (results[i].src == path){
                var destPath = results[i].dest
                try {
                    var artifactPath = destPath.replace("/_system/governance", "");
                    var govRegistry = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils.
                        getGovernanceUserRegistry(am.registry.registry, username);
                    artifact = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils.
                        retrieveGovernanceArtifactByPath(govRegistry, artifactPath);
                } catch (e) {
                    log.warn("Association can not be retrieved. Resource does not exist at path " + destPath);
                    continue;
                }


                if (artifact == null) { //if associated artifact is not a resource we are not displaying it in publisher
                    log.info("resource at path /_system/governance " + destPath + " is not a governance artifact!");
                    continue;
                }
                artifactConfig = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils.
                    findGovernanceArtifactConfigurationByMediaType(am.registry.registry.get(destPath).
                        getMediaType(), am.registry.registry);

                var assetJson = new Object();
                var uuid = am.registry.registry.get(destPath).getUUID();
                var key = String(artifactConfig.getKey());

                var uniqueAttributesNames = artifactConfig.getUniqueAttributes();
                var artifactNameAttribute = artifactConfig.getArtifactNameAttribute();

                assetJson.uniqueAttributesValues = [];
                assetJson.uniqueAttributesNames = [];

                for (var j = 0; j < uniqueAttributesNames.size(); j++) {
                    if (artifactNameAttribute == uniqueAttributesNames.get(j)) {
                        continue;
                    }
                    var attributeName = uniqueAttributesNames.get(j);
                    if (key === 'wsdl' || key === 'wadl' || key === 'policy' ||
                        key === 'schema' || key === 'endpoint' || key === 'swagger'){
                        attributeName = attributeName.replace("overview_", "");
                    }
                    if (artifact.getAttributes(attributeName) != null) {
                        assetJson.uniqueAttributesNames[j] = attributeName;
                        assetJson.uniqueAttributesValues[j] = artifact.getAttributes(attributeName)[0];
                    }
                };

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
        return resultList;
    }

    gregAPI.associations.listTypes = function (type) {
        var map = CommonUtil.getAssociationWithIcons(type);
        if (!map) {
            map = CommonUtil.getAssociationWithIcons("default");
        }

        var keySet = map.keySet().toArray();
        var results = [], item;

        for (var i = 0; i < keySet.length; i++) {
            item = {};
            item.key = keySet[i];
            item.value = String(map.get(keySet[i]));
            results.push(item);
        }

        return results;
    }

    gregAPI.associations.remove = function(session, sourceType, sourceUUID, destType, destUUID, associationType) {

        var srcam = assetManager(session, sourceType);
        var sourcePath = srcam.get(sourceUUID).path;
        var destam = assetManager(session, destType);
        var destPath = destam.get(destUUID).path;
        srcam.registry.registry.removeAssociation(sourcePath,destPath,associationType);
        var reverseAssociationType = CommonUtil.getReverseAssociationType(sourceType, associationType);
        if (!reverseAssociationType){
            reverseAssociationType = CommonUtil.getReverseAssociationType("default", associationType);
        }
        if (!reverseAssociationType){
            reverseAssociationType = CommonUtil.getAssociationTypeForRemoveOperation(destType, associationType);
        }
        if (!reverseAssociationType){
            reverseAssociationType = CommonUtil.getAssociationTypeForRemoveOperation("default", associationType);
        }
        var results = srcam.registry.associations(destPath);
        if (reverseAssociationType) {
            for (var i = 0; i < results.length; i++) {
                if (results[i].dest == sourcePath && results[i].type == reverseAssociationType) {
                    srcam.registry.registry.removeAssociation(destPath, sourcePath, reverseAssociationType);
                }
            }
        }
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
        if (registry.resourceExists(path)) {
            resource = registry.get(path);
        } else {
            resource = registry.newCollection();
        }

        // Osgi service used to encrypt password.
        var securityService = carbon.server.
            osgiService('org.wso2.carbon.registry.security.vault.service.RegistrySecurityService');
        var properties = {};
        if (key != null && value != null) {
            var encryptedText = securityService.doEncrypt(value);
            resource.setProperty(key, encryptedText);
            registry.put(path, resource);
        }
        var properties;
        if (registry.resourceExists(path)) {
            var collection = registry.get(path);
            properties = collection.getProperties();
        }
        return properties;
    };

    /***
     * Calls the registry util class to get the role permissions for a particular resource
     * @param am
     * @param assetId
     * @returns {{}}
     */
    gregAPI.permissions.list = function (am, assetId) {
        var registryPath = am.get(assetId).path;
        var userRegistry = am.registry;
        var registry = userRegistry.registry;
        var PermissionUtil = Packages.org.wso2.carbon.registry.resource.services.utils.PermissionUtil;
        var results = {};
        var result = [];
        var permissionCheck;
        var resource = registry.get(registryPath);
        try {
            if (resource) {
                permissionCheck = resource.getProperty(REGISTRY_PERMISSION_CHECK);
            }

            var permissionsBean = PermissionUtil.getPermissions(userRegistry.registry, registryPath);
            if (permissionsBean) {
                var permissions = permissionsBean.getRolePermissions();
                var authorizedRoles = [];

                for (var i = 0; i < permissions.length; i++) {
                    var permissionOptions = {};
                    var permission = permissions[i];

                    if (!permission.isAuthorizeAllow()) {
                        permissionOptions.userName = permission.getUserName();
                        permissionOptions.formattedUserName = renderRoles(permission.getUserName());
                        permissionOptions.readAllow = permission.isReadAllow();
                        permissionOptions.readDeny = permission.isReadDeny();
                        permissionOptions.writeAllow = permission.isWriteAllow();
                        permissionOptions.writeDeny = permission.isWriteDeny();
                        permissionOptions.deleteAllow = permission.isDeleteAllow();
                        permissionOptions.deleteDeny = permission.isDeleteDeny();
                        permissionOptions.notReadOnly = !(permission.getUserName() == "SYSTEM/wso2.anonymous.role");

                        result.push(permissionOptions);
                    } else {
                        authorizedRoles.push(permission.getUserName());
                    }
                }

                results.list = result;
                results.roleNames = results.roleNames = permissionsBean.getRoleNames().filter(function(_item) {
                 return !_item.match(/^Internal\/private/);
                 });
                results.authorizedRoles = authorizedRoles;
                results.pathWithVersion = permissionsBean.getPathWithVersion();
                results.isAuthorizeAllowed = permissionsBean.isAuthorizeAllowed();
                results.isVersionView = permissionsBean.isVersionView();
                results.permissionCheck = permissionCheck;

                return results;
            } else {
                return null;
            }
        } catch (e) {
            log.error(e);
            throw "Unable to retrieve permissions";
        }
    };

    /***
     *
     * Calls the registry util class to add the role permissions for a particular resource
     * @param am
     * @param pathToAuthorize
     * @param roleToAuthorize
     * @param actionToAuthorize
     * @param permissionType
     * @param permissionCheck
     * @returns {boolean}
     */
    gregAPI.permissions.add = function (am, pathToAuthorize,
                                        roleToAuthorize, actionToAuthorize, permissionType, permissionCheck) {
        var userRegistry = am.registry;
        var AddRolePermissionUtil = Packages.org.wso2.carbon.registry.resource.services.utils.AddRolePermissionUtil;
        var registry = userRegistry.registry;
        var resource = registry.get(pathToAuthorize);
        try {
            if (resource) {
                resource.setProperty(REGISTRY_PERMISSION_CHECK, permissionCheck);
                registry.put(pathToAuthorize, resource);
            }

            AddRolePermissionUtil.addRolePermission(userRegistry.registry,
                pathToAuthorize, roleToAuthorize, actionToAuthorize, permissionType);
            return true;
        } catch (e) {
            log.error(e);
            throw "Unable to add role permissions";
        }
    };

    /***
     * Calls the registry util class to modify the role permissions for a particular resource
     * @param am
     * @param resourcePath
     * @param permissionString
     * @param permissionCheck
     * @returns {boolean}
     */
    gregAPI.permissions.modify = function (am, resourcePath, permissionString, permissionCheck) {
        var userRegistry = am.registry;
        var registry = userRegistry.registry;
        var ChangeRolePermissionsUtil =
            Packages.org.wso2.carbon.registry.resource.services.utils.ChangeRolePermissionsUtil;
        var resource = registry.get(resourcePath);
        try {
            if (resource) {
                resource.setProperty(REGISTRY_PERMISSION_CHECK, permissionCheck);
                registry.put(resourcePath, resource);
            }
            ChangeRolePermissionsUtil.changeRolePermissions(registry, resourcePath, permissionString);
            return true;
        } catch (e) {
            log.error(e);
            throw "Unable to change role permissions";
        }
    };

    var renderRoles = function (role) {
        var modifiedRole = "";
        //UM API sometimes returns uppercase letters
        var roleLower = role.toLowerCase();
        if (roleLower == "internal/everyone") {
            modifiedRole = "All tenant users";
        } else if (roleLower == "system/wso2.anonymous.role") {
            modifiedRole = "Public";
        } else if (roleLower.startsWith("internal/")) {
            modifiedRole = capitalize(roleLower.substr(roleLower.indexOf("/")+1));
        } else {
            modifiedRole = capitalize(roleLower);
        }
        return modifiedRole;
    };

    var capitalize = function(role) {
        return role.substr(0, 1).toUpperCase() + role.substr(1);
    };
}(gregAPI));