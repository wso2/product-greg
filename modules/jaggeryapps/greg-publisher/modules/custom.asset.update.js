var log=new Log();
var config = require('/config/publisher.json');
var caramel = require('caramel');

var router = require('/modules/router-g.js').router();
var routeManager = new router.Router();


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

function listWSDL(context,fileName,registry){
    var list = (new org.wso2.carbon.governance.list.util.filter.FilterWSDL(null, registry,null)).getArtifacts();
    for (i = 0; i < list.length; i++) {
        var wsdlPath = list[i];
        if(wsdlPath.path.indexOf(fileName) != -1){
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

function update(context,artifactManager, modelManager,registry){
    var shortName = context.params.type;
    var id = context.params.id;
    var msg={};

    if(shortName == 'service' || shortName == 'servicex'){

        if (context.post['basic_url']|| context.post['basic_url']  != '') {

	try {

            var modelA = modelManager.getModel(shortName);
            var modelB = modelManager.getModel(shortName);
            var originalArtifact = artifactManager.get(id);
	     
            var dataContent = fetch(context.post['basic_url']);            
	    var contentType;
            var type;
            if(dataContent && dataContent.indexOf('wsdl') > -1){
                contentType = 'application/wsdl+xml';
                type = 'wsdl';
            }   else if(dataContent && dataContent.indexOf('wadl') > -1){
                contentType = 'application/wadl+xml';
                type = 'wadl';
            }
            var fileName = resolveName(context.post['basic_url'],type);

	    var array2D=[["serviceName",context.post['basic_name']]];
            var status = org.wso2.carbon.registry.resource.services.utils.ImportResourceUtil.importResource('/_system/governance/',fileName, contentType, '', context.post['basic_url'] , '',registry, array2D);
            

	    var service = listWSDL(context,fileName,registry);
	    
	    var originalArtifactNew = artifactManager.get(id);
	    log.info('AAAAAAAAAAAAa');
            modelA.import('form.importer', context.post);

            
            //log.info(modelA.getField('basic.description').value);

            //Validation requires the lifecycle state
            modelA.set('*.lifecycleState',originalArtifact.lifecycleState);
            modelA.set('*.lifecycle',originalArtifact.lifecycle);

            modelA.set('*.id', id);

            modelB.import('asset', originalArtifact);

            modelA.set("overview.createdtime",modelB.get("overview.createdtime").value);
            modelA.set("overview.provider",'admin');


            modelA.set('overview.description',context.post['basic_description']);
            modelA.set('overview.version',context.post['advance_version']);
            modelA.set('overview.namespace',context.post['advance_namespace']);
            modelA.set('overview.name',context.post['basic_name']);
            //modelA.getField('overview.namespace').value = modelA.getField('advance.namespace').value;
            //modelA.getField('overview.version').value = modelA.getField('advance.version').value;


            var artifact = modelA.export('asset.exporter');

            //artifact.getField('overview.description').value = context.post['basic_description'];

            //dataInjector.inject(artifact,DataInjectorModes.UPDATE);

            artifactManager.update(artifact);

            artifactManager.attachLifecycle(originalArtifact.lifecycle,artifact);

            msg['ok']=true;
            msg['message']='asset updated';
            msg['asset']=artifact;
            print(msg);

        } catch (e) {
            log.debug('The asset ' + id + ' could not be updated.The following exception was thrown: ' + e);
            response.sendError(404, 'The asset ' + id + ' could not be updated.Please check the server logs.');
        }


        } else{

            try {
		var modelA = modelManager.getModel(shortName);
    var modelB = modelManager.getModel(shortName);
            //Get the original artifact
            var originalArtifact = artifactManager.get(id);

            modelA.import('form.importer', context.post);

            //Validation requires the lifecycle state
            modelA.set('*.lifecycleState',originalArtifact.lifecycleState);

            var report=null; //modelA.validate();
log.info('sdfsdfsdfasdfsdfdsafas');
            //Check if the validation check has failed
            if((report)&&(report.failed)){

                msg['ok']=false;
                msg['report']=report;
                print(msg);
                log.debug(msg);
                return;
            }


            modelA.set('*.id', id);

            modelB.import('asset', originalArtifact);

            
            modelA.set("overview.createdtime",modelB.get("overview.createdtime").value);
	    modelA.set('overview.description',context.post['basic_description']);
            modelA.set('overview.version',context.post['advance_version']);
            modelA.set('overview.namespace',context.post['advance_namespace']);
            modelA.set('overview.name',context.post['basic_name']);
            modelA.set("overview.provider",modelB.get("overview.provider").value);


            var artifact = modelA.export('asset.exporter');

            //dataInjector.inject(artifact,DataInjectorModes.UPDATE);

            artifactManager.update(artifact);

            //dataInjector.inject(artifact,DataInjectorModes.DISPLAY);

            msg['ok']=true;
            msg['message']='asset updated';
            msg['asset']=artifact;
            print(msg);

        } catch (e) {
            log.debug('The asset ' + id + ' could not be updated.The following exception was thrown: ' + e);
            response.sendError(404, 'The asset ' + id + ' could not be updated.Please check the server logs.');
        }

        }






    }

}
