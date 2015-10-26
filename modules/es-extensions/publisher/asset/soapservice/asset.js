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
asset.manager = function(ctx) {
    var configs = require("/extensions/assets/soapservice/config/properties.json");
    var getRegistry = function(cSession) {
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
    var wsdlAssetManager = function(session){
        var rxt = require('rxt');
        var am = rxt.asset.createUserAssetManager(session, 'wsdl');
        return am;
    };
    var setContent = function(artifact, content) {
        if (content) {
            if (content instanceof Stream) {
                artifact.setContent(IOUtils.toByteArray(content.getStream()));
            } else {
                artifact.setContent(new java.lang.String(content).getBytes());
            }
        }
    };
    /**
     * Checks if there are any other asset versions
     * @param  {[type]}  asset [description]
     * @return {Boolean}       [description]
     */
    var isOnlyAssetVersion = function(asset, am) {
        var versions = am.getAssetGroup(asset);
        return (versions.length < 1) ? true : false;
    };
    var createOMContent = function(attributes) {
        var omContent = "<metadata xmlns=\"http://www.wso2.org/governance/metadata\"><overview><name>";
        omContent += attributes.overview_name;
        omContent += "</name><namespace>";
        omContent += attributes.overview_namespace;
        omContent += "</namespace><version>";
        omContent += attributes.overview_version;
        omContent += "</version>";
        if (attributes.overview_description) {
            omContent += "<description>";
            omContent += attributes.overview_description;
            omContent += "</description>";
        }
        omContent += "</overview>";

        if(attributes.contacts_entry) {
            omContent += "<contacts>";
            for(var index = 0; index< attributes.contacts_entry.length; index++){
                omContent += "<entry>";
                omContent += attributes.contacts_entry[index];
                omContent += "</entry>";
            }

            omContent += "</contacts>";
        }

        omContent += "<interface>";
        if (attributes.interface_wsdlURL || attributes.interface_wsdlUrl) {
            omContent += "<wsdlURL>";
            if (attributes.interface_wsdlUrl) {
                omContent += attributes.interface_wsdlUrl;
            } else {
                omContent += attributes.interface_wsdlURL;
            }
            omContent += "</wsdlURL>";
        }
        if (attributes.interface_transportProtocols) {
            omContent += "<transportProtocols>";
            omContent += attributes.interface_transportProtocols;
            omContent += "</transportProtocols>";
        }
        if (attributes.interface_messageFormats) {
            omContent += "<messageFormats>";
            omContent += attributes.interface_messageFormats;
            omContent += "</messageFormats>";
        }
        if (attributes.interface_messageExchangePatterns) {
            omContent += "<messageExchangePatterns>";
            omContent += attributes.interface_messageExchangePatterns;
            omContent += "</messageExchangePatterns>";
        }
        omContent += "</interface>";
        if (attributes.docLinks_documentType) {
            omContent += "<docLinks>";
            omContent += "<documentType>";
            omContent += attributes.docLinks_documentType;
            omContent += "</documentType>";
            if (attributes.docLinks_url) {
                omContent += "<url>";
                omContent += attributes.docLinks_url;
                omContent += "</url>";
            }
            if (attributes.docLinks_documentComment) {
                omContent += "<documentComment>";
                omContent += attributes.docLinks_documentComment;
                omContent += "</documentComment>";
            }
            if (attributes.docLinks_documentType1) {
                omContent += "<documentType1>";
                omContent += attributes.docLinks_documentType1;
                omContent += "</documentType1>";
                if (attributes.docLinks_url1) {
                    omContent += "<url1>";
                    omContent += attributes.docLinks_url1;
                    omContent += "</url1>";
                }
                if (attributes.docLinks_documentComment1) {
                    omContent += "<documentComment1>";
                    omContent += attributes.docLinks_documentComment1;
                    omContent += "</documentComment1>";
                }
            }
            if (attributes.docLinks_documentType2) {
                omContent += "<documentType2>";
                omContent += attributes.docLinks_documentType2;
                omContent += "</documentType2>";
                if (attributes.docLinks_url2) {
                    omContent += "<url2>";
                    omContent += attributes.docLinks_url2;
                    omContent += "</url2>";
                }
                if (attributes.docLinks_documentComment2) {
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
    var createArtifact = function(manager, options) {
        var attributes = options.attributes;
        var omContent = createOMContent(attributes);
        var artifact = manager.newGovernanceArtifact(omContent);
        log.debug('Finished creating Governance Artifact');
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
        log.debug('lifecycle is attached');
        return artifact;
    };
    var setCustomAssetAttributes = function(asset, userRegistry) {
        var interfaceUrl=asset.attributes.interface_wsdlURL;
        if (interfaceUrl != null) {
            try {
                var resource = userRegistry.registry.get(interfaceUrl);
                var wsdlUUID = resource.getUUID();
                asset.wsdl_uuid = wsdlUUID;
                asset.wsdl_url = asset.attributes.interface_wsdlURL;
            } catch(e) {
                asset.wsdl_uuid = "";
                asset.wsdl_url = "";
            }
        }
    };
    var getAssociations = function(genericArtifacts, userRegistry){
        //Array to store the association names.
        var associations = [];
        if (genericArtifacts != null) {
            for (var index in genericArtifacts) {
                var deps = {};
                if (genericArtifacts[index] != null) {
                    //extract the association name via the path.
                    var path = genericArtifacts[index].getPath();
                    var subPaths = path.split('/');
                    var associationTypePlural = subPaths[2];
                    var associationName = subPaths[subPaths.length - 1];
                    var resource = userRegistry.registry.get(configs.depends_asset_path_prefix + path);
                    var associationUUID = resource.getUUID();
                    deps.associationName = associationName;
                    deps.associationType = associationTypePlural.substring(0, associationTypePlural.lastIndexOf('s'));
                    deps.associationUUID = associationUUID;

                    if(deps.associationType == "wsdl") {
                        associations.push(deps);
                    }
                }
            }
        }
        return associations;
    };

    var setDependencies = function(genericArtifact, asset ,userRegistry) {
        try {
            //get dependencies of the artifact.
            var dependencyArtifacts = genericArtifact.getDependencies();
            asset.dependencies = getAssociations(dependencyArtifacts, userRegistry);
        } catch(e) {
            asset.dependencies = [];
        }
    };

    var setDependents = function(genericArtifact, asset, userRegistry) {
        try {
            var dependentArtifacts = genericArtifact.getDependents();
            asset.dependents = getAssociations(dependentArtifacts, userRegistry);
        } catch(e) {
            asset.dependents = [];
        }
    };

    var addDefaultPropertyIfNotExist = function(registry, path, name){
        var associations = registry.getAllAssociations(path);

        for(var index = 0; index< associations.length; index++){
            var associatedResourcePath = associations[index].getDestinationPath();
            if(associatedResourcePath.indexOf("wsdl") > -1){     
                var associatedWSDL = registry.get(associatedResourcePath);

                var wsdlName = name + ".wsdl";
                var q = {};
                q.overview_name = wsdlName;
                var artifacts = wsdlAssetManager(ctx.session).search(q);
                log.debug(artifacts.length);
                if(artifacts.length < 2) {
                    associatedWSDL.addProperty("default", "true");
                    registry.put(associatedResourcePath, associatedWSDL);
                }
            }
        }
    };

    return {
        get: function(id) {
            var asset;
            try {
                asset = this._super.get.call(this, id); 
                var userRegistry = getRegistry(ctx.session);
                setCustomAssetAttributes(asset, userRegistry);
                var rawArtifact = this.am.manager.getGenericArtifact(id);
                setDependencies(rawArtifact, asset, userRegistry);
                setDependents(rawArtifact, asset, userRegistry);
            } catch(e) {
                log.error(e);
                return null;
            }

            return asset;
        },
        combineWithRxt: function(asset) {
            var modAsset = this._super.combineWithRxt.call(this, asset);
            if (asset.wsdl_uuid) {
                var wsdlUUID = asset.wsdl_uuid;
                modAsset.wsdl_uuid = wsdlUUID;
            }
            if (asset.wsdl_url) {
                var wsdlURL = asset.wsdl_url;
                modAsset.wsdl_url = wsdlURL;
                modAsset.tables[2].fields.wsdlURL.value = modAsset.wsdl_url;
            }
            if (asset.interfaceType) {
                var interfaceType = asset.interfaceType;
                modAsset.interfaceType = interfaceType;
            }
            if (asset.dependencies) {
                var dependencies = asset.dependencies;
                modAsset.dependencies = dependencies;
            }
            if (asset.dependents) {
                var dependents = asset.dependents;
                modAsset.dependents = dependents;
            }
            return modAsset;
        },
        create: function(options) {
            var log = new Log();
            var isDefault = false;
            var manager = this.am.manager;
            var artifact = createArtifact(manager, options);
            if((options.hasOwnProperty('_default')) &&(options._default===true)){
                delete options._default;
                isDefault = true;
            }
            manager.addGenericArtifact(artifact);
            log.debug('Service successfully created');
            options.id = artifact.getId();
            var asset = this.get(options.id);
            if(!this.rxtManager.isGroupingEnabled(this.type)){
                log.debug('Omitted grouping');
                return;
            }
            if ( (isDefault) || (isOnlyAssetVersion(asset,this)) ){
                this.setAsDefaultAsset(asset);
            }

            var wsdlRelativePath = artifact.getPath();
            var wsdlPath = "/_system/governance" + wsdlRelativePath;
            addDefaultPropertyIfNotExist(getRegistry(ctx.session).registry, wsdlPath, options.attributes.overview_name);
        },
        update: function(options) {
            var log = new Log();
            var manager = this.am.manager;
            var isDefault = false;
            if((options.hasOwnProperty('_default'))&&(options._default === true)){
                isDefault = true;
            }
            var asset = this.get(options.id);
            var artifact = createArtifact(manager, options);
            manager.updateGenericArtifact(artifact);
            log.debug('Service successfully updated');
            options.id = artifact.getId();
            if(!this.rxtManager.isGroupingEnabled(this.type)){
                log.debug('Omitting grouping step');
                return;
            }
            if(isDefault){
                this.setAsDefaultAsset(asset);
            }
        },
        postCreate:function(){
            
        }
    }
};

asset.renderer = function(ctx) {
    var hideTables = function(page) {
        var tables = [];
        for (var index in page.assets.tables) {
            if (page.assets.tables[index].name == 'overview') {
                delete page.assets.tables[index].fields.scopes;
                delete page.assets.tables[index].fields.types;
                tables.push(page.assets.tables[index]);
            } else if (page.assets.tables[index].name == 'contacts') {
                tables.push(page.assets.tables[index]);
            } else if (page.assets.tables[index].name == 'interface') {
                tables.push(page.assets.tables[index]);
            } else if (page.assets.tables[index].name == 'docLinks') {
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
            require('/modules/page-decorators.js').pageDecorators.assetCategoryDetails(ctx, page, this);
            return hideTables(page);
        },
        details: function(page) {
            return hideTables(page);
        }
    };
};
asset.configure = function() {
    return {
        meta: {
            ui: {
                icon: 'fw fw-soap'
            },
            lifecycle: {
                commentRequired: false,
                defaultAction: '',
                deletableStates: ['*'],
                defaultLifecycleEnabled:false,
                publishedStates: ['Published']
            },
            grouping: {
                groupingEnabled: false,
                groupingAttributes: ['overview_name']
            }
        },
        table: {
            overview: {
                fields: {
                    name: {
                        placeholder: "WeatherService"
                    },
                    namespace: {
                        placeholder: "http://example.namespace.com"
                    },
                    version: {
                        placeholder: "1.0.0"
                    },
                    description: {
                        placeholder: "This is a sample service"
                    }
                }
            },
            interface: {
                fields: {
                    wsdlUrl: {
                        placeholder: "https://www.example.com/sample.wsdl"
                    }
                }
            }
        }
    }
};
