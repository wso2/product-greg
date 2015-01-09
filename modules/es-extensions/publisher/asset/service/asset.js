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

asset.manager=function(ctx){
    var getRegistry = function(cSession){
            var userMod = require('store').user;
            var userRegistry = userMod.userRegistry(cSession);

            return userRegistry;
    };
    var setAttributes = function(artifact, attributes) {
        for (name in attributes) {
            if (attributes.hasOwnProperty(name)) {
                attribute = attributes[name];
                if (attribute instanceof Array) {
                    artifact.setAttributes(name, attribute);
                } else {
                    artifact.setAttribute(name, attribute);
                }
            }
        }
    }

    var setId = function(artifact, id) {
        if (id) {
            artifact.id = id;
        }    
    }

    var setContent = function(artifact, content) {
        if (content) {
            if (content instanceof Stream) {
                artifact.setContent(IOUtils.toByteArray(content.getStream()));
            } else {
                artifact.setContent(new java.lang.String(content).getBytes());
            }
        }  
    }

    var createOMContent = function(attributes) {
        var omContent = "<metadata xmlns=\"http://www.wso2.org/governance/metadata\"><overview><name>";
        omContent += attributes.overview_name;
        omContent += "</name><namespace>";

        omContent += attributes.overview_namespace;

        omContent += "</namespace><version>";
        omContent += attributes.overview_version;
        omContent += "</version></overview><interface>";

        if(attributes.interface_wsdlURL || attributes.interface_wsdlUrl) {
            omContent += "<wsdlURL>";
            if(attributes.interface_wsdlUrl) {
                omContent += attributes.interface_wsdlUrl;
            } else {
                omContent += attributes.interface_wsdlURL;   
            }
            omContent += "</wsdlURL>";
        }

        if(attributes.interface_transportProtocols) {
            omContent += "<transportProtocols>";
            omContent += attributes.interface_transportProtocols;
            omContent += "</transportProtocols>";
        }

        if(attributes.interface_messageFormats) {
            omContent += "<messageFormats>";
            omContent += attributes.interface_messageFormats;
            omContent += "</messageFormats>";
        }

        if(attributes.interface_messageExchangePatterns) {
            omContent += "<messageExchangePatterns>";
            omContent += attributes.interface_messageExchangePatterns;
            omContent += "</messageExchangePatterns>";
        }

        omContent += "</interface>";

        if(attributes.docLinks_documentType) {
            omContent += "<docLinks>";

            omContent += "<documentType>";
            omContent += attributes.docLinks_documentType;
            omContent += "</documentType>";

            if(attributes.docLinks_url) {
                omContent += "<url>";
                omContent += attributes.docLinks_url;
                omContent += "</url>";
            }

            if(attributes.docLinks_documentComment) {
                omContent += "<documentComment>";
                omContent += attributes.docLinks_documentComment;
                omContent += "</documentComment>";
            }

            if(attributes.docLinks_documentType1) {
                omContent += "<documentType1>";
                omContent += attributes.docLinks_documentType1;
                omContent += "</documentType1>";

                if(attributes.docLinks_url1) {
                    omContent += "<url1>";
                    omContent += attributes.docLinks_url1;
                    omContent += "</url1>";
                }

                if(attributes.docLinks_documentComment1) {
                    omContent += "<documentComment1>";
                    omContent += attributes.docLinks_documentComment1;
                    omContent += "</documentComment1>";
                }
            }

            if(attributes.docLinks_documentType2) {
                omContent += "<documentType2>";
                omContent += attributes.docLinks_documentType2;
                omContent += "</documentType2>";

                if(attributes.docLinks_url2) {
                    omContent += "<url2>";
                    omContent += attributes.docLinks_url2;
                    omContent += "</url2>";
                }

                if(attributes.docLinks_documentComment2) {
                    omContent += "<documentComment2>";
                    omContent += attributes.docLinks_documentComment2;
                    omContent += "</documentComment2>";
                }
            }

            omContent += "</docLinks>";
        }

        omContent += "</metadata>";

        return omContent;
    }

    var createArtifact = function (manager, options) {
        var attributes = options.attributes;
        var omContent = createOMContent(attributes);

        var artifact = manager.newGovernanceArtifact(omContent);

        log.info('Finished creating Governance Artifact');
        
        setAttributes(artifact, attributes);
        setId(artifact, options.id);
        setContent(artifact, options.content);

        var lc = options.lifecycles;

        if (lc) {
            length = lc.length;
            for (var i = 0; i < length; i++) {
                artifact.attachLifeCycle(lc[i]);
            }
        }

        log.info('lifecycle is attached');

        return artifact;
    };


	return{
        get:function(id){
            var asset = this._super.get.call(this,id);
            if(asset.attributes.interface_wsdlURL) {
                var wsdlUUID = getRegistry(ctx.session).registry.get(asset.attributes.interface_wsdlURL).getUUID();
                asset.wsdl_uuid = wsdlUUID;
                asset.wsdl_url = asset.attributes.interface_wsdlURL;
            }

            return asset;
        },
        combineWithRxt:function(asset) {
            var modAsset = this._super.combineWithRxt.call(this,asset);

            if(asset.wsdl_uuid) {
                var wsdlUUID = asset.wsdl_uuid;
                modAsset.wsdl_uuid = wsdlUUID;
            }

            if(asset.wsdl_url) {
                var wsdlURL = asset.wsdl_url;
                modAsset.wsdl_url = wsdlURL;

                modAsset.tables[2].fields.wsdlUrl.value = modAsset.wsdl_url ;
            }

            return modAsset;
        },
		create:function(options){
			var log = new Log();
			var manager = this.am.manager;

            var artifact = createArtifact(manager, options);
			manager.addGenericArtifact(artifact);

			log.info('Service successfully created');
            options.id = artifact.getId();
		},
        update:function(options){
            var log = new Log();
            var manager = this.am.manager;

            var asset = this.get(options.id);

            var artifact = createArtifact(manager, options);
            manager.updateGenericArtifact(artifact);    

            log.info('Service successfully updated');
            options.id = artifact.getId();

            wsdlUUID = getRegistry(ctx.session).registry.get(options.attributes.interface_wsdlURL).getUUID();

            log.info(wsdlUUID);
        }
	}
};

asset.renderer = function(ctx) {

    var hideTables = function(page){
        var tables = [];

        for(var index in page.assets.tables){
            if(page.assets.tables[index].name == 'overview'){
                delete page.assets.tables[index].fields.scopes;
                delete page.assets.tables[index].fields.types;
                tables.push(page.assets.tables[index]);
            }
            else if(page.assets.tables[index].name == 'contacts'){
                tables.push(page.assets.tables[index]);
            }
            else if(page.assets.tables[index].name == 'interface'){
                tables.push(page.assets.tables[index]);
            }
            else if(page.assets.tables[index].name == 'docLinks'){
                tables.push(page.assets.tables[index]);
            }
        }

        page.assets.tables = tables;

        return page;
    };

    return {
        create: function(page) {
            return hideTables(page);
        },

        update: function(page) {
            return hideTables(page);
        },

        list: function(page) {
            return hideTables(page);
        },

        details: function(page) {
            return hideTables(page);
        }
    };
};
