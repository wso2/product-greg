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
                    if (key === 'wsdl' || key === 'wadl' || key === 'policy' || key === 'schema' || key === 'endpoint' || key === 'swagger') {
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

    gregAPI.notifications.remove = function(registry, notificationId) {};
    gregAPI.notes.reply = function() {};
    gregAPI.notes.replies = function(parentNoteId) {};
    gregAPI.userRegistry = function(session) {};
    gregAPI.assetManager = function(session, type) {};
}(gregAPI));