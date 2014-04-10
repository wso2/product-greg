var meta={
	use:'action',
    purpose:'save',
	type:'form',
    source:'default',
	applyTo:'*',
	required:['model','template'],
	name:'asset.action.save'
};

/*
Description:Saves the contents of the model to an artifact instance and then retrieves the
            id
Filename: asset.action.save.js
Created Date: 8/8/2013
 */


var module=function(){

    var configs=require('/config/publisher.json');
    var log=new Log();

    /*
    adding asset details to Social Cache DB.
     */
    function addToSocialCache(id, type) {
        if (id) {
            var logged = require('store').server.current(session);
            var domain = (logged && logged.tenantDomain) ? logged.tenantDomain : "carbon.super";

            var CREATE_QUERY = "CREATE TABLE IF NOT EXISTS SOCIAL_CACHE (id VARCHAR(255) NOT NULL,tenant VARCHAR(255),type VARCHAR(255), " +
                "body VARCHAR(5000), rating DOUBLE,  PRIMARY KEY ( id ))";
            var server = require('store').server;
            server.privileged(function () {
                var db = new Database("SOCIAL_CACHE");
                db.query(CREATE_QUERY);
                var combinedId = type + ':' + id;
                db.query("MERGE INTO SOCIAL_CACHE (id,tenant,type,body,rating) VALUES('" + combinedId + "','" + domain + "','" + type + "','',0)");
                db.close();
            });
        }
    }

    function fetch(urlStr) {
        if (typeof java == "undefined") {
            throw "This script requires java to run.";
        }
        else {
            importPackage(java.io, java.net);

            var url = new URL(urlStr);
            var urlStream = url.openStream();
            var reader = new BufferedReader(new InputStreamReader(urlStream, "latin1"));

            var html = "";
            var line;
            while (line = reader.readLine()) {
                if (line == null) break;
                html += line;
            }
            return html;
        }
    }

    function resolveName(filepath, type) {
        var filename = "";
        if (filepath.indexOf("\\") != -1) {
            filename = filepath.substring(filepath.lastIndexOf('\\') + 1, filepath.length);
        } else {
            filename = filepath.substring(filepath.lastIndexOf('/') + 1, filepath.length);
        }
        if (filename.search(/\.[^?]*$/i) < 0) {
            filename = filename.replace("?", ".");
            var suffix = '.'+type;
            if (filename.indexOf(".") > 0) {
                filename = filename.substring(0, filename.lastIndexOf(".")) + suffix;
            } else {
                filename = filename + suffix;
            }
        }
        var notAllowedChars = "!@#;%^*+={}|<>";
        for (i = 0; i < notAllowedChars.length; i ++) {
            var c = notAllowedChars.charAt(i);
            filename = filename.replace(c, "_");
        }
        return filename;
    }

    function listWSDL(context,fileName){
        var registry = context.rxtManager.registry.registry;
        var list = (new org.wso2.carbon.governance.list.util.filter.FilterWSDL(null, registry,null)).getArtifacts();
        for (i = 0; i < list.length; i++) {
            var wsdlPath = list[i];
            if(wsdlPath.path.indexOf(fileName) != -1){
                log.info(wsdlPath.path);
                var associations =   registry.getAssociations(wsdlPath.path, org.wso2.carbon.governance.api.util.GovernanceConstants.USED_BY);
                for (a = 0; a < associations.length; a++) {
                    var destinationPath = associations[a].getDestinationPath();
                    var artifact = org.wso2.carbon.governance.api.util.GovernanceUtils.retrieveGovernanceArtifactByPath(registry, destinationPath);
                    var path = artifact.path;
                    if(path.indexOf('/services/') != -1){
                        artifact.setAttribute('overview_provider','admin');
                        return artifact;

                    }
                }
            }
        }
    }

    return{
		execute:function(context){

            var utility=require('/modules/utility.js').rxt_utility();

            log.debug('Entered : '+meta.name);

            log.debug(stringify(context.actionMap));

            var model=context.model;
            var template=context.template;

            var now =new String(new Date().valueOf());
            var length = now.length;
            var prefix = configs.constants.assetCreatedDateLength;
            var onsetVal = '';
            if(length != prefix){
                    var onset = prefix - length;
                    for(var i = 0; i < onset; i++){
                       onsetVal+='0';
                    }
            }
            model.setField('overview.createdtime',onsetVal+now);

	    
            if(model.getField('basic.name') && model.getField('basic.name').value.trim() != ''){
                model.getField('overview.name').value = model.getField('basic.name').value;
                model.getField('overview.description').value = model.getField('basic.description').value;
                model.getField('overview.provider').value = model.getField('basic.provider').value;
                if(model.getField('basic.namespace'))
                    model.getField('overview.namespace').value = model.getField('advance.namespace').value;
                if(model.getField('basic.version')){
                    model.getField('overview.version').value = model.getField('advance.version').value;
                } else {
                    model.getField('overview.version').value = '1.0.0'; // here adding default version, if version is not presented
                }

            }

            if (model.getField('basic.url')  && model.getField('basic.url').value.trim() != '') {

                var registry =  context.rxtManager.registry.registry;

                var dataContent = fetch(model.getField('basic.url').value);
                var contentType;
                var type;
                if(dataContent && dataContent.indexOf('wsdl') > -1){
                    contentType = 'application/wsdl+xml';
                    type = 'wsdl';
                }   else if(dataContent && dataContent.indexOf('wadl') > -1){
                    contentType = 'application/wadl+xml';
                    type = 'wadl';
                }
                var fileName = resolveName(model.getField('basic.url').value,type);

                var resourceS = new org.wso2.carbon.registry.resource.services.ResourceService();
                var registry2 =  context.registry;

                var status = org.wso2.carbon.registry.resource.services.utils.ImportResourceUtil.importResource('/_system/governance/',fileName, contentType, '', model.getField('basic.url').value , '',registry, null);

		log.info(model);
                var service = listWSDL(context,fileName);
                var shortName=template.shortName;

                var artifactManager  = org.wso2.carbon.governance.api.generic.GenericArtifactManager(registry, shortName);

                service.setAttribute('overview_provider',model.getField('basic.provider').value);
                service.setAttribute('overview_version', model.getField('advance.version').value);
                service.setAttribute('overview_description', model.getField('basic.description').value);
                service.setAttribute('overview_createdtime', model.getField('overview.createdtime').value);
                //var stringArray = java.lang.reflect.Array.newInstance(java.lang.String, 2);
                //stringArray[0] = "None:Test";
                //stringArray[1] = "None:Test2";
		var contact = model.getField('contacts.contact').value;
		var contactList = contact.split(',');
                service.setAttributes('contacts_contact',contactList);
		
		var documentType = model.getField('docLinks.documentType').value;
		var documentTypeList = documentType.split(',');
		service.setAttributes('docLinks_documentType',documentTypeList);
	
		var documentLink = model.getField('docLinks.url').value;
		var documentLinkList = documentLink.split(',');
		service.setAttributes('docLinks_url',documentLinkList);

		var documentComment = model.getField('docLinks.documentComment').value;
		var documentCommentList = documentComment.split(',');
		service.setAttributes('docLinks_documentComment',documentCommentList);		

                artifactManager.updateGenericArtifact(service);


                var id=service.id||' ';


                log.info('Setting id of model to '+id);

                //adding asset to social
                addToSocialCache(id,template.shortName);

                //Save the id data to the model
                model.setField('*.id',id);

                log.debug('Finished saving asset with id: '+id);

                log.info(artifact);
            } else if(model.getField('basic.filepath')   && model.getField('basic.filepath').value.trim() != ''){
                var registry =  context.rxtManager.registry.registry;

                var shortName=template.shortName;
                var artifactManager  = org.wso2.carbon.governance.api.generic.GenericArtifactManager(registry, shortName);
                var  addedServcies = org.wso2.carbon.registry.extensions.utils.ZipUtil.getServices('wsdl_arch_with_imports_folder.gar');
                for (i = 0; i < addedServcies.size(); i++) {
                    var path =  addedServcies.get(i);
                    var service = org.wso2.carbon.governance.api.util.GovernanceUtils.retrieveGovernanceArtifactByPath(registry, path);
                    service.setAttribute('overview_provider',model.getField('basic.provider').value);
                    service.setAttribute('overview_version', model.getField('advance.version').value);
                    service.setAttribute('overview_description', model.getField('basic.description').value);
                    service.setAttribute('overview_createdtime', model.getField('overview.createdtime').value);
                    service.setAttributes('contacts_entry',['None:test','None:Test3']);

                    artifactManager.updateGenericArtifact(service);

                }



                log.info(org.wso2.carbon.registry.extensions.utils.ZipUtil.getServices('wsdl_arch_with_imports_folder.gar').size());

            }  else {
                // Here is default logic



		    var name=model.getField('overview.name').value;
		    var version=model.getField('overview.version').value;
		    var shortName=template.shortName;

		    log.debug('Artifact name: '+name);

		    log.debug('Converting model to an artifact for use with an artifact manager');

		    //Export the model to an asset
		    var asset=context.parent.export('asset.exporter');

		    log.debug('Finished exporting model to an artifact');

		    //Save the artifact
		    log.debug('Saving artifact with name :'+name);


		    //Get the artifact using the name
		    var rxtManager=context.rxtManager;

		    var artifactManager=rxtManager.getArtifactManager(shortName);

		    artifactManager.add(asset);

		    //name='test-gadget-7';

		    log.debug('Finished saving asset : '+name);

		    //The predicate object used to compare the assets
		    var predicate={
		      attributes:{
		          overview_name:name,
		          overview_version:version
		      }
		    };
		    var artifact=artifactManager.find(function(adapter){
		        //Check if the name and version are the same
		       //return ((adapter.attributes.overview_name==name)&&(adapter.attributes.overview_version==version))?true:false;
		       return utility.assertEqual(adapter,predicate);
		    },null);

		    log.debug('Locating saved asset: '+stringify(artifact)+' to get the asset id.');

		    var id=artifact[0].id||' ';

		    log.debug('Setting id of model to '+id);

		    //adding asset to social
		    addToSocialCache(id,template.shortName);

		    //Save the id data to the model
		    model.setField('*.id',id);

		    log.debug('Finished saving asset with id: '+id);
		}
	  }
	}
};
