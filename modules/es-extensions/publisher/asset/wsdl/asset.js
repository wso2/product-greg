
/*
* Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

asset.manager = function(ctx){
	var getRegistry = function(cSession){
			var userMod = require('store').user;
			var userRegistry = userMod.userRegistry(cSession);

			return userRegistry;
	};
	return {
		importAssetFromHttpRequest: function(options){
			log.info('Importing asset from request');
			return options;
		},
		combineWithRxt:function(asset){
			return asset;
		},
		create:function(options){
			var url = options.overview_url;
			var name = options.overview_name;
			var version = options.overview_version;

			var userRegistry = getRegistry(ctx.session);

			var utils = Packages.org.wso2.carbon.registry.resource.services.utils.ImportResourceUtil;

			var parentPath = "/_system/governance/trunk/wsdls/".concat(version);
			var mediaType = "application/wsdl+xml";

			var javaArray = Packages.java.lang.reflect.Array;
			var properties = javaArray.newInstance(java.lang.String, 1, 2);

			properties[0][0] = 'version';
			properties[0][1] = version;

			utils.importResource(parentPath, name, mediaType, '', url, '', userRegistry.registry, properties);
		},
		get:function(id){
			var item = this._super.get.call(this,id);
			var subPaths = item.path.split('/');	

			item.name = subPaths[subPaths.length - 1];
			item.version = subPaths[subPaths.length - 2];

			var userRegistry = getRegistry(ctx.session);

			var ByteArrayInputStream = Packages.java.io.ByteArrayInputStream;
			var resource = userRegistry.registry.get(item.path);

			item.authorUserName = resource.getAuthorUserName();

			var content = resource.getContent();

		    var value = '' + new Stream(new ByteArrayInputStream(content));
			item.content = value;

			return item;
		},
		list:function(paging){
			var items = this._super.list.call(this,paging);
			return items;
		},
		search:function(q,paging){
			var results = this._super.search.call(this,q,paging);

			for(var index = 0; index < results.length; index++){
				var result = results[index];
				var path = result.path;
				var subPaths = path.split('/');
				var name = subPaths[subPaths.length - 1];

				result.name = name;
				result.version = subPaths[subPaths.length - 2];
			}

			return results;
		},
		getName:function(asset){
			return asset.name;
		}
	};
};

asset.renderer =  function (ctx){
	return {
		details:function(page,meta){
			return page;
		}
	};
};