package org.wso2.carbon.registry.samples.handler.process.utils;

import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

public class ArtifactBean {
    private GenericArtifactManager genericArtifactManager;
    private GenericArtifact processArtifact;
    private Map<String, String> attributeMap = new HashMap<String, String>();

    public ArtifactBean(GenericArtifactManager genericArtifactManager){
        this.genericArtifactManager = genericArtifactManager;
    }

    public void setArtifactId(String id) throws GovernanceException {
        processArtifact = genericArtifactManager.newGovernanceArtifact(new QName(null, id));
    }

    public void setAttribute(String key, String value){
        attributeMap.put(key, value);
    }

    public GenericArtifact getArtifact() throws GovernanceException {
        for(String key : attributeMap.keySet()){
            processArtifact.setAttribute(key, attributeMap.get(key));
        }
        return processArtifact;
    }
}
