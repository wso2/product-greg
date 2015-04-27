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
// 	return {
// 		get:function(id){
// 			log.info('overridden get method in the GREG default extension');
// 			return this._super.get.call(this,id);
// 		}
// 	}
// };
	
asset.renderer = function(ctx) {
	return {
		pageDecorators:{
            sidebarPopulator: function(page) {
                log.info('current page: '+page.meta.pageName);
                if (page.meta.pageName === 'details') {
                    page.isSidebarEnabled = true;
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
                deletableStates: [],
		defaultLifecycleEnabled:false,
                publishedStates: ['Published']
            },
            grouping: {
                groupingEnabled: false,
                groupingAttributes: ['overview_name']
            }
        }
    };
};

// asset.configure = function(){
// 	return {
// 		meta:{
// 			lifecycle:{
// 				lifecycleViewEnabled:false,
// 				lifecycleEnabled:false
// 			},
// 			grouping:{
// 				groupingEnabled:true
// 			}
// 		}
// 	};
// };