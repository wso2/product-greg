/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.backward.association.handler;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.backward.association.handler.utils.CommonUtil;

import javax.xml.namespace.QName;
import java.util.*;

public class BackwardAssociationHandler extends Handler {
    private OMElement assocationMappingElement;
    private Map<String, String> typeMap = new HashMap<String, String>();

    /*
    * The sample handler configuration will use the configuration in the following structure
    *
    * <handler>
    *     <property name="associationMappings" type="xml">
    *         <type original="called" dual="calledBy"/>
    *     </property>
    * </handler>
    *
    * All the types should be given in the configuration
    *
    * */

    public OMElement getAssociationMappings() {
        return assocationMappingElement;
    }

    public void setAssociationMappings(OMElement assocationMappingElement) {
        Iterator mappingElemets = assocationMappingElement.getChildElements();

        while (mappingElemets.hasNext()) {
            OMElement typeElement = (OMElement) mappingElemets.next();
            typeMap.put(typeElement.getAttributeValue(new QName("original")), typeElement.getAttributeValue(new QName("dual")));
        }

        this.assocationMappingElement = assocationMappingElement;
    }


    @Override
    public Association[] getAllAssociations(RequestContext requestContext) throws RegistryException {

        if (!CommonUtil.isGetAllAssociationLockAvailable()) {
            return null;
        }
        CommonUtil.acquireGetAllAssociationLock();
        try {
            List<Association> newAssociationList = new ArrayList<Association>();

            Registry registry = requestContext.getRegistry();


            Association[] allAssociations = registry.getAllAssociations(requestContext.getResourcePath().getPath());

//        The list of associations that were retrieved are added to an arrayList to return a combined list

            for (Association association : allAssociations) {
                if (!association.getSourcePath().contains(";version:")
                        && !association.getDestinationPath().contains(";version:")) {
                    newAssociationList.add(association);
                    if (typeMap.containsKey(association.getAssociationType())) {
                        Association newAssociation = new Association();
                        newAssociation.setSourcePath(association.getDestinationPath());
                        newAssociation.setDestinationPath(association.getSourcePath());
                        newAssociation.setAssociationType(typeMap.get(association.getAssociationType()));

                        newAssociationList.add(newAssociation);
                    }
                }
            }
            requestContext.setProcessingComplete(true);
            Association[] retArray = new Association[newAssociationList.size()];
            return newAssociationList.toArray(retArray);
        } finally {
            CommonUtil.releaseGetAllAssociationLock();
        }
    }

    @Override
    public Association[] getAssociations(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isGetAssociationLockAvailable()) {
            return null;
        }
        CommonUtil.acquireGetAssociationLock();

        try {
            List<Association> newAssociationList = new ArrayList<Association>();
            Registry registry = requestContext.getRegistry();

//            Getting all the associations with the given path
            String sourcePath = requestContext.getResourcePath().getPath();
            Association[] allAssociations = registry.getAllAssociations(sourcePath);

//            Iterate through all of the associations to get the matching ones
            for (Association association : allAssociations) {
                if(association.getSourcePath().equals(sourcePath)
                        && association.getAssociationType().equals(requestContext.getAssociationType())){
                    newAssociationList.add(association);
                }
            }
            requestContext.setProcessingComplete(true);
            Association[] retArray = new Association[newAssociationList.size()];
            return newAssociationList.toArray(retArray);
        } finally {
            CommonUtil.releaseGetAssociationLock();
        }
    }

    @Override
    public void removeAssociation(RequestContext requestContext) throws RegistryException {
         if (!CommonUtil.isRemoveAssociationLockAvailable()) {
            return;
        }
        CommonUtil.acquireRemoveAssociationLock();

        try {
            Registry registry = requestContext.getRegistry();
            registry.removeAssociation(requestContext.getSourcePath(),requestContext.getTargetPath(),requestContext.getAssociationType());

            if(typeMap.containsValue(requestContext.getAssociationType())){
                for (Map.Entry<String, String> entry : typeMap.entrySet()) {
                    if(entry.getValue().equals(requestContext.getAssociationType())){
                        registry.removeAssociation(requestContext.getTargetPath(),requestContext.getSourcePath(),entry.getKey());
                    }
                }

            }


        } finally {
            CommonUtil.releaseRemoveAssociationLock();
        }
    }
}
