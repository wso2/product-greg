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
// asset.manager = function(ctx) {
//  return {
//      get:function(id){
//          log.info('overridden get method in the GREG default extension');
//          return this._super.get.call(this,id);
//      }
//  }
// };
asset.renderer = function(ctx) {
    var gregAPI = require('/modules/greg-publisher-api.js').gregAPI;
    var rxt = require('rxt');
    var allowedPagesForSidebar = ['list', 'details', 'lifecycle', 'update', 'associations', 'copy', 'delete', 'create'];
    var assetManager = function(session, type) {
        var am = rxt.asset.createUserAssetManager(session, type);
        return am;
    };
    var getAssetNoteManager = function (ctx) {
        var rxt = require('rxt');
        var am = rxt.asset.createUserAssetManager(ctx.session, 'note');
        return am;
    };
    return {
        pageDecorators: {
            sidebarPopulator: function(page) {
                if(allowedPagesForSidebar.indexOf(page.meta.pageName)>-1){
                    page.isNotificationbarEnabled = true;
                }

                if (page.meta.pageName === 'details') {
                    page.isSidebarEnabled = true;
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
                if (allowedPagesForSidebar.indexOf(page.meta.pageName)>-1) {
                    var am = assetManager(ctx.session,ctx.assetType);
                    page.notificationsCount = gregAPI.notifications.count(am);
                }
            },
            notificationListPopulator: function(page) {
                if (allowedPagesForSidebar.indexOf(page.meta.pageName)>-1) {
                    var am = assetManager(ctx.session,ctx.assetType);
                    page.notifications = gregAPI.notifications.list(am);
                }
            },
            notePopulator: function(page) {
                if (page.meta.pageName === 'details') {}
            },
            associationMetaDataPopulator: function(page, util) {
                var ptr = page.leftNav || [];
                var entry;
                var allowedPages = ['details', 'lifecycle', 'update', 'associations', 'permissions', 'copy', 'delete'];
                log.debug('Association populator ' + page.meta.pageName);
                //if (((page.meta.pageName !== 'associations') && (page.meta.pageName !== 'list')) &&(page.meta.pageName !== 'create')) {
                // Fix REGISTRY-3926, only render association page if user has permission
                /**if(allowedPages.indexOf(page.meta.pageName)>-1){
                    log.debug('adding link');
                    entry = {};
                    entry.id = 'Associations';
                    entry.name = 'Associations';
                    entry.iconClass = 'btn-association';
                    entry.url = this.buildAppPageUrl('associations') + '/' + page.assets.type + '/' + page.assets.id
                    ptr.push(entry);
                    if (ptr[5] != null && ptr[4] != null) {
                        var temp = ptr[5];
                        ptr[5] = ptr[4];
                        ptr[4] = temp;
                    }
                }**/
            },
            permissionMetaDataPopulator: function(page, util) {
                var ptr = page.leftNav || [];
                var am = assetManager(ctx.session,ctx.assetType);
                var entry;
                var allowedPages = ['details','lifecycle','update','associations','permissions', 'copy', 'delete'];
                log.debug('Permission populator ' + page.meta.pageName);
                if(allowedPages.indexOf(page.meta.pageName)>-1){
                    var permissionList = gregAPI.permissions.list(am, page.assets.id);
                    if(permissionList){
                        if(permissionList.isAuthorizeAllowed && !permissionList.isVersionView){
                            log.debug('adding link');
                            entry = {};
                            entry.name = 'Permissions';
                            entry.iconClass = 'btn-permission';
                            entry.url = this.buildAppPageUrl('permissions') + '/' + page.assets.type + '/'
                                + page.assets.id;
                            ptr.push(entry);
                        }
                    }
                }
            },
            notes: function (page) {
                if (!page.assets.id) {
                    return;
                }
                var q = {};
                q.overview_resourcepath = page.assets.path;
                var items = getAssetNoteManager(ctx).search(q);
                page.notes = items;
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
            },
            decoratingAssetListing: function(page) {
                // Following is to remove the statistics button in the list page
                for(index in page.leftNav) {
                    var button = page.leftNav[index];

                    if(button.iconClass === "btn-stats") {
                        page.leftNav.splice(index, 1);
                    }
                }
            },
            checkDependents:function(page) {
                if(page.assets){
                    var dependencies  = page.assets.dependencies || [];
                    var isDependentsPresent =  ( dependencies.length > 0 );
                    page.assets.isDependentsPresent = isDependentsPresent;
                }
            },
            sorting: function (page) {
                require('/modules/page-decorators.js').pageDecorators.sorting(ctx, page, [
                    {name: "overview_name", label: "Name"},
                    {name: "overview_version", label: "Version"},
                    {name: "createdDate", label: "Date/Time"}]);
            },
            checkSubscriptionMenuItems: function (page) {
                page.isContentType = false;
                if (page.rxt && page.rxt.fileExtension) {
                    page.isContentType = true;
                }
            }
        }
    };
};
asset.configure = function() {
    return {
        table: {
            overview: {
                fields: {
                    provider: {
                        auto: false
                    }
                }
            }
        },
        meta: {
            lifecycle: {
                commentRequired: false,
                defaultAction: '',
                deletableStates: ['*'],
                defaultLifecycleEnabled: false,
                publishedStates: ['Published']
            },
            providerAttribute:'',
            ui:{
                icon:'fw fw-resource'
            },
            grouping: {
                groupingEnabled: false,
                groupingAttributes: ['overview_name']
            },
            notifications:{
                enabled:false
            }
        }
    };
};

