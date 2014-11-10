asset.manager=function(ctx){

   var createArtifact = function (manager, options) {

        var QName = Packages.javax.xml.namespace.QName;
        var IOUtils = Packages.org.apache.commons.io.IOUtils;

        var qName=new QName(options.attributes.overview_namespace, options.name);
        log.info('After creating qName');
        log.info('options: ' + stringify(options));
        log.info('namespace: ' + stringify(options.attributes.overview_namespace));
        var name, attribute, i, length, lc;

        for(var key in manager){
            log.info('Keys: '+key)
        }

        var artifact = manager.newGovernanceArtifact(qName);
        var attributes = options.attributes;

        log.info('Finished creating Governance Artifact');
        
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
        if (options.id) {
            artifact.id = options.id;
        }
        if (options.content) {
            if (options.content instanceof Stream) {
                artifact.setContent(IOUtils.toByteArray(options.content.getStream()));
            } else {
                artifact.setContent(new java.lang.String(options.content).getBytes());
            }
        }
        lc = options.lifecycles;
        if (lc) {
            length = lc.length;
            for (i = 0; i < length; i++) {
                artifact.attachLifeCycle(lc[i]);
            }
        }
        return artifact;
    };


	return{
		create:function(options){
			var log=new Log();
			log.info('Service create method called!');
			var manager=this.am.manager;
            log.info('Calling addGenericArtifact');
            log.info('Options: '+ stringify(options));
            var artifact=createArtifact(manager, options);
			manager.addGenericArtifact(artifact);		
            // createArtifact(manager, options);       
			log.info('Service create method ended!');
            options.id=artifact.getId();
		},
        update:function(options){
            var log=new Log();
            log.info('Service update method called!');
            var manager=this.am.manager;
            log.info('Calling updateGenericArtifact');
            log.info('Options: '+ stringify(options));
            var artifact=createArtifact(manager, options);
            manager.updateGenericArtifact(artifact);            
            log.info('Service update method ended!');
            options.id=artifact.getId();
        }
	}
};

asset.renderer = function(ctx) {
    var buildListLeftNav = function(page, util) {
        var log = new Log();
        return [{
            name: 'Add ',
            iconClass: 'icon-plus-sign-alt',
            url: util.buildUrl('create')
        }, {
            name: 'Statistics',
            iconClass: 'icon-dashboard',
            url: util.buildUrl('stats')
        }];
    };
    var buildDefaultLeftNav = function(page, util) {
        var id=page.assets.id;
        return [{
            name: 'Overview',
            iconClass: 'icon-list-alt',
            url: util.buildUrl('details')+'/'+id
        }, {
            name: 'Edit',
            iconClass: 'icon-edit',
            url: util.buildUrl('update')+'/'+id
        }, {
            name: 'Life Cycle',
            iconClass: 'icon-retweet',
            url: util.buildUrl('lifecycle')+'/'+id
        }];
    };
    var isActivatedAsset = function(assetType) {
        var activatedAssets = ctx.tenantConfigs.assets;
        return true;
        if (!activatedAssets) {
            throw 'Unable to load all activated assets for current tenant: ' + ctx.tenatId + '.Make sure that the assets property is present in the tenant config';
        }
        for (var index in activatedAssets) {
            if (activatedAssets[index] == assetType) {
                return true;
            }
        }
        return false;
    };

    var hideTables=function(page){
        log.info('The service create page was called');
        var tables=[];
        for(var index in page.assets.tables){
            if(page.assets.tables[index].name=='overview'){
                delete page.assets.tables[index].fields.scopes;
                delete page.assets.tables[index].fields.types;
                tables.push(page.assets.tables[index]);
            }
            else if(page.assets.tables[index].name=='contacts'){
                tables.push(page.assets.tables[index]);
            }
            else if(page.assets.tables[index].name=='interface'){
                tables.push(page.assets.tables[index]);
            }
            /*
            else if(page.assets.tables[index].name=='security'){
                tables.push(page.assets.tables[index]);
            }
            else if(page.assets.tables[index].name=='endpoints'){
                tables.push(page.assets.tables[index]);
            }*/
            else if(page.assets.tables[index].name=='docLinks'){
                tables.push(page.assets.tables[index]);
            }
        }

        page.assets.tables=tables;

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
        },

        lifecycle: function(page) {},
        leftNav: function(page) {
            switch (page.meta.pageName) {
                case 'list':
                    page.leftNav = buildListLeftNav(page, this);
                    break;
                default:
                    page.leftNav = buildDefaultLeftNav(page, this);
                    break;
            }
            return page;
        },
        ribbon: function(page) {
            var ribbon = page.ribbon = {};
            var DEFAULT_ICON = 'icon-cog';
            var assetTypes = [];
            var assetType;
            var assetList = ctx.rxtManager.listRxtTypeDetails();
            for (var index in assetList) {
                assetType = assetList[index];
                if (isActivatedAsset(assetType.shortName)) {
                    assetTypes.push({
                        url: this.buildBaseUrl(assetType.shortName) + '/list',
                        assetIcon: assetType.ui.icon || DEFAULT_ICON,
                        assetTitle: assetType.singularLabel
                    });
                }
            }
            ribbon.currentType = page.rxt.singularLabel;
            ribbon.currentTitle = page.rxt.singularLabel;
            ribbon.currentUrl = page.meta.currentPage;
            ribbon.shortName = page.rxt.singularLabel;
            ribbon.query = 'Query';
            ribbon.breadcrumb = assetTypes;
            return page;
        }
    };
};
