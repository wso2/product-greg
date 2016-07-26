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
    gregAPI.notifications = {};
    gregAPI.subscriptions = {};
    gregAPI.notes = {};
    gregAPI.associations = {};
    var formatResultSet = function(output) {
        var results = {};
        var entry;
        var resultEntry;
        results.StoreLifeCycleStateChanged = {};
        results.StoreLifeCycleStateChanged.email = {};
        results.StoreLifeCycleStateChanged.work = {};
        results.StoreLifeCycleStateChanged.email.checked = false;
        results.StoreLifeCycleStateChanged.work.checked = false;
        results.StoreResourceUpdated = {};
        results.StoreResourceUpdated.email = {};
        results.StoreResourceUpdated.work = {};
        results.StoreResourceUpdated.email.checked = false;
        results.StoreResourceUpdated.work.checked = false;

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
        var subcriptions = SubscriptionPopulator.populate(userRegistry.registry, registryPath).getSubscriptionInstances();
        var length = subcriptions.length;
        var result = [];
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
                var workList = {};
                var row =  rows[i];
                var presentSub = String(row.getPresentationSubject());
                var pathValue = presentSub.substring(presentSub.indexOf("/"));
                //This code is done since there are different messages are received for lifecycle and information update notification
                pathValue = pathValue.replace("was updated", "");
                if (endsWith('.',pathValue)){
                    pathValue = pathValue.substr(0,pathValue.length-1);
                }
                pathValue = pathValue.trim();
                if (am.registry.registry.resourceExists(pathValue) &&
                    am.registry.registry.get(pathValue).getMediaType() != null &&
                    getTimeFromRow(row.getCreatedTime()) > getTimeFromDate(am.registry.registry.get(pathValue).getCreatedTime())) {
                    count ++;
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
                var presentSub = String(row.getPresentationSubject());
                var pathValue = presentSub.substring(presentSub.indexOf("/"));

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

    gregAPI.notifications.remove = function(registry, notificationId) {};
    gregAPI.notes.reply = function() {};
    gregAPI.notes.replies = function(parentNoteId) {};
    gregAPI.userRegistry = function(session) {};
    gregAPI.assetManager = function(session, type) {};
    var assetManager = function(session, type) {
        var tenantAPI = require('/modules/tenant-api.js').api;
        var options = {'type':type};
        var tenantResources = tenantAPI.createTenantAwareAssetResources(session, options);
        return tenantResources.am;
    };
    /*Need assetManager for getAssetVersions*/
    gregAPI.getAssetVersions = function (session, type, path, name) { /* TODO: Instead of path accept asset itself*/
        var am = assetManager(session,type);
        var asset = {};
        asset.attributes = {};
        asset.attributes.overview_name =  name;

        var resources = am.getAssetGroup(asset);
        var filtered_resources = resources.filter(
            function(version){
                return version.path !== path;
            }
        );
        return filtered_resources;
    };
}(gregAPI));