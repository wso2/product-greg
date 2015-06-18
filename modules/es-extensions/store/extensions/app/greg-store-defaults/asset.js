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
	return {
        pageDecorators: {
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