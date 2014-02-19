/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.registry.jaxr.sample;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * This contain the basic operations (like add org, service , and delete org, service )
 * for UDDI Registry
 */
public class JaxrServiceTest {
    private BusinessQueryManager businessQueryManager = null;
    protected BusinessLifeCycleManager businessLifeCycleManager;

    String serviceName = "WSO2 Registry JAXR Service";
    String OrgName = "WSO2 Registry JAXR Service Organization";    

    public void publishFindAndDeleteService(Connection connection) {
        try {
            RegistryService rs = connection.getRegistryService();
            businessQueryManager = rs.getBusinessQueryManager();
            businessLifeCycleManager = rs.getBusinessLifeCycleManager();

            System.out.println("\nCreating temporary organization...\n");
            Organization org = createOrganization();

            createService(org);

            findAndDeleteService(org.getKey());
            deleteOrganization(org.getKey());

        } catch (JAXRException e) {
            e.printStackTrace();
        }
    }


    private void createService(Organization org) throws JAXRException {
        System.out.println("\nCreating service... "+"("+ serviceName +")"+ "\n");

        Service service = businessLifeCycleManager.createService(getIString(serviceName));
        service.setDescription(getIString("Services in UDDI Registry"));
        service.setProvidingOrganization(org);
        
        Collection<ServiceBinding> serviceBindings = new ArrayList<ServiceBinding>();
        ServiceBinding binding = businessLifeCycleManager.createServiceBinding();

        binding.setDescription(getIString("Services in UDDI Registry"));
        binding.setValidateURI(false);
        serviceBindings.add(binding);
        
        service.addServiceBindings(serviceBindings);
        
        ArrayList<Service> services = new ArrayList<Service>();
        services.add(service);

        BulkResponse br = businessLifeCycleManager.saveServices(services);
        if (br.getStatus() == JAXRResponse.STATUS_SUCCESS) {
            System.out.println("Service Saved");
            Collection coll = br.getCollection();
            for (Object aColl : coll) {
                Key key = (Key) aColl;
                System.out.println("Saved Key=" + key.getId());
            }
        } else {
            System.err.println("JAXRExceptions " +
                    "occurred during save:");
            Collection exceptions = br.getExceptions();
            for (Object exception : exceptions) {
                Exception e = (Exception) exception;
                System.err.println(e.toString());
            }
        }
    }

    private void findAndDeleteService(Key orgKey) throws JAXRException {
        Collection<String> findQualifiers = new ArrayList<String>();
        findQualifiers.add(FindQualifier.SORT_BY_NAME_ASC);
        Collection<String> namePatterns = new ArrayList<String>();
        namePatterns.add("%" + serviceName + "%");

        BulkResponse br = businessQueryManager.findServices(orgKey,
                findQualifiers, namePatterns, null, null);
        Collection services = br.getCollection();

        if (services == null) {
            System.out.println("\n-- Matched 0 orgs");

        } else {
            System.out.println("\n-- Matched " + services.size() + " services --\n");

            // then step through them
            for (Object service : services) {
                Service s = (Service) service;

                System.out.println("Id: " + s.getKey().getId());
                System.out.println("Name: " + s.getName().getValue());

                // Print spacer between messages
                System.out.println(" --- ");

                deleteService(s.getKey());

                System.out.println("\n ============================== \n");
            }
        }
    }

    private void deleteService(Key key) throws JAXRException {

        String id = key.getId();

        System.out.println("\nDeleting service with id " + id + "\n");

        Collection<Key> keys = new ArrayList<Key>();
        keys.add(key);
        BulkResponse response = businessLifeCycleManager.deleteServices(keys);

        Collection exceptions = response.getExceptions();
        if (exceptions == null) {
            Collection retKeys = response.getCollection();
            Iterator keyIter = retKeys.iterator();
            javax.xml.registry.infomodel.Key orgKey;
            if (keyIter.hasNext()) {
                orgKey =
                        (javax.xml.registry.infomodel.Key) keyIter.next();
                id = orgKey.getId();
                System.out.println("Service with ID=" + id + " was deleted");
            }
        }
    }

    private Organization createOrganization() throws JAXRException {

        Key orgKey;
        Organization org = businessLifeCycleManager.createOrganization(getIString(OrgName));
        org.setDescription(getIString("Temporary organization to test saveService()"));

        Collection<Organization> orgs = new ArrayList<Organization>();
        orgs.add(org);
        BulkResponse br = businessLifeCycleManager.saveOrganizations(orgs);

        if (br.getStatus() == JAXRResponse.STATUS_SUCCESS) {
            orgKey = (Key) br.getCollection().iterator().next();
            System.out.println("Temporary Organization Created with id=" + orgKey.getId());
            org.setKey(orgKey);
        } else {
            System.err.println("JAXRExceptions " +
                    "occurred during creation of temporary organization:");

            for (Object o : br.getCollection()) {
                Exception e = (Exception) o;
                System.err.println(e.toString());
            }

        }

        return org;
    }

    private void deleteOrganization(Key orgKey) throws JAXRException {

        String id = orgKey.getId();

        System.out.println("\nDeleting temporary organization with id " + id + "\n");

        Collection<Key> keys = new ArrayList<Key>();
        keys.add(orgKey);
        BulkResponse response = businessLifeCycleManager.deleteOrganizations(keys);

        Collection exceptions = response.getExceptions();
        if (exceptions == null) {
            Collection retKeys = response.getCollection();
            Iterator keyIter = retKeys.iterator();
            if (keyIter.hasNext()) {
                orgKey =
                        (javax.xml.registry.infomodel.Key) keyIter.next();
                id = orgKey.getId();
                System.out.println("Organization with ID=" + id + " was deleted");
            }
        }
    }

    private InternationalString getIString(String str)
            throws JAXRException {
        return businessLifeCycleManager.createInternationalString(str);
    }
}
