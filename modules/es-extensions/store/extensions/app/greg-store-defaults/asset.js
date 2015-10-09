/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
asset.manager = function(ctx) {    
    return {
        search: function(query, paging) {
            var assets = this._super.search.call(this, query, paging);
            return assets;
        }
    };
};

asset.renderer = function(ctx) {
    var gregAPI = require('/modules/greg-publisher-api.js').gregAPI;
    var rxt = require('rxt');
    var server = require('store').server;

    var assetManager = function(session, type) {
        var am;
        var user = server.current(ctx.session);
        //Create asset manager depend on whether there is a loggedIn user or not.
        if (user) {
            am = rxt.asset.createUserAssetManager(ctx.session, type);
        } else {
            var carbon = require('carbon');
            var tenantId = carbon.server.superTenant.tenantId;
            am = rxt.asset.createAnonAssetManager(ctx.session, type, tenantId);
        }
        return am;
    };
	return {
        pageDecorators: {
            sidebarPopulator: function(page) {
                if (page.meta.pageName === 'details') {
                    page.isSidebarEnabled = true;
                    page.isNotificationbarEnabled = true;
                }
                if (page.meta.pageName === 'list') {
                    page.isNotificationbarEnabled = true;
                }
            },
            subscriptionPopulator: function(page) {
                if (page.meta.pageName === 'details') {
                    var am = assetManager(ctx.session,ctx.assetType);
                    log.debug('### obtaining subscriptions ###');
                    page.subscriptions = gregAPI.subscriptions.list(am,page.assets.id);
                    log.debug('### done ###');
                }
            },
            notificationPopulator: function(page) {
                if (page.meta.pageName === 'list' || page.meta.pageName === 'details') {
                    page.notificationsCount = gregAPI.notifications.count();
                }
            },
            notificationListPopulator: function(page) {
                if (page.meta.pageName === 'list' || page.meta.pageName === 'details') {
                    var am = assetManager(ctx.session,ctx.assetType);
                    page.notifications = gregAPI.notifications.list(am);
                }
            },
        	checkDependents:function(page) {
        		if(page.assets){
        			var dependencies  = page.assets.dependencies || [];
        			var dependents = page.assets.dependents || [];
        			var isDependentsPresent =  ( dependencies.length > 0 ) || (dependents.length > 0 );
        			page.assets.isDependentsPresent = isDependentsPresent;
        		}
        	}
        }
    }
}
