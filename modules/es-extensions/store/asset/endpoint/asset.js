/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    var tenantAPI = require('/modules/tenant-api.js').api;
    /**
     * The function augments the provided query to include published state information
     * @param  {[type]} query [description]
     * @return {[type]}       The provided query object
     */
    var buildPublishedQuery = function(query) {
        //Get all of the published assets
        var publishedStates = ctx.rxtManager.getPublishedStates(ctx.assetType) || [];
        //Determine if there are any published states
        if (publishedStates.length == 0) {
            return query;
        }
        //If there is no query then build a new one
        if (!query) {
            query = {};
        }
        //TODO: Even though an array is sent in only the first search value is accepted
        query.lcState = [publishedStates[0]];
        return query;
    };
    var getRegistry = function(cSession) {
        var tenantDetails = tenantAPI.createTenantAwareAssetResources(cSession,{type:ctx.assetType});
        if((!tenantDetails)&&(!tenantDetails.am)) {
            log.error('The tenant-api was unable to create a registry instance by resolving tenant details');
            throw 'The tenant-api  was unable to create a registry instance by resolving tenant details';
        }
        return tenantDetails.am.registry;
    };
    return {
        //due to a bug needed to replicate the 'search' method. JIRA:https://wso2.org/jira/browse/STORE-561
        search: function(query, paging) {
            //query = buildPublishedQuery(query);--commented this inorder to let anystate 
            //query=buildPublishedQuery(query);
            var assets = this._super.search.call(this, query, paging);
            return assets;
        },
        get: function(id) {
            var asset = this._super.get.call(this, id);
            return asset;
        }
    };
};