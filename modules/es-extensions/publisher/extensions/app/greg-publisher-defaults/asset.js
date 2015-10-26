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
            notePopulator: function(page) {
                if (page.meta.pageName === 'details') {}
            },
            associationMetaDataPopulator: function(page, util) {
                var ptr = page.leftNav || [];
                var entry;
                var allowedPages = ['details','lifecycle','update'];
                log.debug('Association populator ' + page.meta.pageName);
                //if (((page.meta.pageName !== 'associations') && (page.meta.pageName !== 'list')) &&(page.meta.pageName !== 'create')) {
                if(allowedPages.indexOf(page.meta.pageName)>-1){
                    log.debug('adding link');
                    entry = {};
                    entry.name = 'Associations';
                    entry.iconClass = 'btn-lifecycle';
                    entry.url = this.buildAppPageUrl('associations') + '/' + page.assets.type + '/' + page.assets.id
                    ptr.push(entry);
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
            }
        }
    };
};
asset.configure = function() {
    return {
        meta: {
            lifecycle: {
                commentRequired: false,
                defaultAction: '',
                deletableStates: ['*'],
                defaultLifecycleEnabled: false,
                publishedStates: ['Published']
            },
            ui:{
                icon:'fw fw-resource'
            },
            grouping: {
                groupingEnabled: false,
                groupingAttributes: ['overview_name']
            }
        }
    };
};

