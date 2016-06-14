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
            var tenantAPI = require('/modules/tenant-api.js').api;
            var tenantDetails = tenantAPI.tenantContext(session);
            var tenantId = tenantDetails.urlTenantId; //carbon.server.superTenant.tenantId;
            am = rxt.asset.createAnonAssetManager(ctx.session, type, tenantId);
        }
        return am;
    };
	return {
        pageDecorators: {
            recentAssets: function (page) {
                return;
            },
            myAssets: function (page) {
                return;
            },
            embedLinks: function (page, meta) {
                return;
            },
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
                    var am = assetManager(ctx.session,ctx.assetType);
                    page.notificationsCount = gregAPI.notifications.count(am);
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
        	},
            downloadPopulator:function(page){
                //Populate the links for downloading content RXTs
                if(page.meta.pageName === 'details'){
                    var isDownloadable = ctx.rxtManager.isDownloadable(page.assets.type);
                    if(!isDownloadable){
                        return;
                    }
                    var config = require('/config/store.js').config();
                    var pluralType = page.rxt.pluralLabel.toLowerCase();
                    var domain = require('carbon').server.tenantDomain({tenantId:ctx.tenantId});
                    page.downloadMetaData = {}; 
                    page.downloadMetaData.downloadFileType = page.rxt.singularLabel;
                    page.downloadMetaData.enabled = isDownloadable;
                    page.downloadMetaData.url = config.server.https+'/governance/'+pluralType+'/'+page.assets.id+'/content?tenant='+domain;
                }
            },
            versions: function (page) {
                if (page.meta.pageName !== 'details') {
                    return;
                }

                var type = page.assets.type;
                page.assetVersions = gregAPI.getAssetVersions(ctx.session, ctx.assetType, page.assets.path, page.assets.name);
                var single_version = false;
                if(page.assetVersions.length === 1){
                    single_version = true;
                }
                page.single_version = single_version;
            }
        }
    }
};

asset.configure = function() {
    return {
        meta: {
            'isDependencyShown': true,
            'isDiffViewShown': true,
            sorting: {
                attributes: [
                    {name: "overview_name", label: "Name"},
                    {name: "createdDate", label: "Date/Time"}
                ]
            }
        }
    }
};