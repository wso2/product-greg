/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
app.dependencies=['publisher-common'];
//override publisher-common and enable 'service' & 'wsdl'.
//change the landing page to 'service'.
app.server = function(ctx) {
    return {
    	endpoints:{
	    	pages:[{
		    	title:'Landing page',
	    		url:'gc-landing',
	    		path:'greg-landing.jag',
	    		secured:true  		
	    	},
            {
                title:'Search results',
                url:'search-results',
                path:'search-results.jag',
                secured:true        
            },
            {
                title: 'Password',
                url: 'password',
                path: 'password.jag',
                secured: true
            }]
        },
        configs: {
            landingPage: '/pages/gc-landing',
            disabledAssets: ['ebook','proxy','sequence','service','servicex','uri',
                             'site','provider','gadget','document','endpoint','topic','reply', 'server'],
            uiDisabledAssets: ['note'],
            title : "WSO2 Governance Center - Publisher"
        }
    }
};

app.pageHandlers = function(ctx) {
    return {
        onPageLoad: function() {
            if((ctx.isAnonContext)&&(ctx.endpoint.secured)){
                ctx.res.sendRedirect(ctx.appContext+'/login');
                return false;
            }
            return true;
        }
    };
};

app.renderer = function(ctx){
    return {
        pageDecorators:{
            advanceSearchPatch:function(page){
                for(var index in page.assets) {
                    var asset = page.assets[index] ;
                    var attributes = asset.attributes || {};

                    var hasNameProperty = attributes.hasOwnProperty("overview_name");
                    var hasVersionProperty = attributes.hasOwnProperty("overview_version");

                    var path = asset.path;
                    var subPaths = path.split('/');

                    if(!hasNameProperty) {
                        var name = subPaths[subPaths.length - 1];
                        asset.name = name;
                        asset.attributes.overview_name = name;
                        asset.overview_name = name;
                        asset.attributes.name = name;
                    }

                    if(!hasNameProperty && !hasVersionProperty) {
                        asset.version = subPaths[subPaths.length - 2];
                        asset.attributes.overview_version = asset.version;
                        asset.overview_version = asset.version;
                        asset.attributes.version = asset.version;
                    }
                }
            },
            getStoreUrl: function (page) {
                page.storeUrl = require('/config/publisher.js').config().storeUrl;
            }
        }
    }
};
