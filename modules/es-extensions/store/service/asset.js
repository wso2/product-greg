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

asset.manager = function(ctx){
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
        query.lcState=[publishedStates[0]];
        return query;
    };

    return {
        //due to a bug needed to replicate the 'search' method. JIRA:https://wso2.org/jira/browse/STORE-561
        search: function(query, paging) {
            query=buildPublishedQuery(query);
            var assets = this._super.search.call(this, query, paging);
            return assets;
        },
        get:function(id){
            var asset = this._super.get.call(this,id);
            //check for the wsdl url in the asset json object
            if(asset.attributes.interface_wsdlURL != null){
            var subPaths = asset.attributes.interface_wsdlURL.split('/');  
            var userMod = require('store').user;
            var userRegistry = userMod.userRegistry(ctx.session);
            var ByteArrayInputStream = Packages.java.io.ByteArrayInputStream;
            var resource = userRegistry.registry.get(asset.attributes.interface_wsdlURL);
            var content = resource.getContent();
            var value = '' + new Stream(new ByteArrayInputStream(content));
            asset.content = value;
           }
            return asset;
        }
    };
};