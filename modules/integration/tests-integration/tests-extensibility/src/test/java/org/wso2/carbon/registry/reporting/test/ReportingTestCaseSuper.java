/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.carbon.registry.reporting.test;

import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.LogViewerClient;
import org.wso2.greg.integration.common.clients.ReportAdminServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class ReportingTestCaseSuper extends GREGIntegrationBaseTest {

    protected String artifactName = "testCycle";
    protected String applicationName = "testApplication";
    protected Registry governance;
    protected WSRegistryServiceClient registry;
    protected LogViewerClient logViewerClient;
    protected ReportAdminServiceClient reportAdminServiceClient;
    protected ResourceAdminServiceClient resourceAdminServiceClient;
    private String[] artifactIDs;
    protected static final String Dest_file = "destination";
    protected final String[] testLCattributes = {"responsibleQA|paramQA",
                                                 "responsibleQAA|paramQAA"};
    protected final String testTemplateCollection = "/_system/governance/repository/components/org.wso2.carbon.governance/templates";
    protected final String testGovernanceLCtemplate = "/_system/governance/repository/components/org.wso2.carbon.governance/templates/TestGovernanceLC.jrxml";
    protected final String testGovernanceLCEditedTemplate = "/_system/governance/repository/components/org.wso2.carbon.governance/templates/TestGovernanceLCEdited.jrxml";
    protected final String testGovernanceLCMismatchtemplate = "/_system/governance/repository/components/org.wso2.carbon.governance/templates/TestGovernanceLCMismatch.jrxml";
    protected final String testGovernanceLCInvalidtemplate = "/_system/governance/repository/components/org.wso2.carbon.governance/templates/TestGovernanceLC-invalid.jrxml";
    protected final String testGovernanceLCtemplateAnyLocation = "/_system/governance/repository/components/TestGovernanceLC.jrxml";
    protected final String applicationTemplate = "/_system/governance/repository/components/org.wso2.carbon.governance/templates/application_template.jrxml";

    protected final String testGovernanceLCRXT = "/_system/governance/repository/components/org.wso2.carbon.governance/types/TestGovernanceCycle.rxt";
    protected final String applicationRXT = "/_system/governance/repository/components/org.wso2.carbon.governance/types/application.rxt";

    private final String testGovernanceLCJAR = "TestingLCReportGenerator.jar";
    protected final String applicationJAR = "org.wso2.carbon.registry.samples.extensions.application-4.5.0.jar";

    protected final String testGovernanceLCClass = "org.wso2.carbon.registry.samples.reporting.TestingLCReportGenerator";
    protected final String applicationClass = "org.wso2.carbon.registry.samples.reporting.ApplicationReportGenerator";

    protected final String scheduleReportLocation = "/_system/governance/report";
    protected final String scheduleBenchmarkLocation = "/_system/governance/benchMark";

    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    protected void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);

        logViewerClient =
                new LogViewerClient(backEndUrl,
                                    sessionCookie);
        reportAdminServiceClient =
                new ReportAdminServiceClient(backEndUrl,
                                             sessionCookie);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);
        artifactIDs = new String[5];
    }

    /**
     * method to convert a org.wso2.carbon.registry.reporting.stub.beans.xsd.
     * ReportConfigurationBean to a
     * org.wso2.carbon.registry.common.beans.ReportConfigurationBean
     *
     * @param toChange
     * @return
     */
    protected org.wso2.carbon.registry.common.beans.ReportConfigurationBean createCommonBean(
            ReportConfigurationBean toChange) {
        org.wso2.carbon.registry.common.beans.ReportConfigurationBean retCommonBean =
                new org.wso2.carbon.registry.common.beans.ReportConfigurationBean();
        retCommonBean.setName(toChange.getName());
        retCommonBean.setTemplate(toChange.getTemplate());
        retCommonBean.setType(toChange.getType());
        retCommonBean.setReportClass(toChange.getReportClass());

        return retCommonBean;
    }

    /**
     * Add resources needed to generate a proper Life Cycle report
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    protected void testAddResourcesLCReport() throws MalformedURLException,
                                                     RemoteException,
                                                     ResourceAdminServiceExceptionException {
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                              + "artifacts" + File.separator + "GREG" + File.separator
                              + "reports" + File.separator + "TestGovernanceLC.jrxml";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(testGovernanceLCtemplate,
                                               "application/xml", "TstDec", dh);

        assertTrue(resourceAdminServiceClient
                           .getResource(testGovernanceLCtemplate)[0].getAuthorUserName()
                           .contains(userNameWithoutDomain));

        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                       + "artifacts" + File.separator + "GREG" + File.separator
                       + "rxt" + File.separator + "TestGovernanceCycle.rxt";

        dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(testGovernanceLCRXT,
                                               "application/vnd.wso2.registry-ext-type+xml", "TstDec", dh);

        assertTrue(resourceAdminServiceClient.getResource(testGovernanceLCRXT)[0]
                           .getAuthorUserName().contains(userNameWithoutDomain));

        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                       + "artifacts" + File.separator + "GREG" + File.separator
                       + "reports" + File.separator + testGovernanceLCJAR;

        dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addExtension(testGovernanceLCJAR, dh);

        assertTrue(isExtensionExist(testGovernanceLCJAR));
    }

    /**
     * Add resources needed to generate a proper Application report
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    protected void testAddResourcesApplicationReport()
            throws MalformedURLException, RemoteException,
                   ResourceAdminServiceExceptionException {
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                              + "artifacts" + File.separator + "GREG" + File.separator
                              + "reports" + File.separator + "application_template.jrxml";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(applicationTemplate,
                                               "application/xml", "TstDec", dh);

        assertTrue(resourceAdminServiceClient.getResource(applicationTemplate)[0]
                           .getAuthorUserName().contains(userNameWithoutDomain));

        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                       + "artifacts" + File.separator + "GREG" + File.separator
                       + "rxt" + File.separator + "application.rxt";

        dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(applicationRXT,
                                               "application/vnd.wso2.registry-ext-type+xml", "TstDec", dh);

        assertTrue(resourceAdminServiceClient.getResource(applicationRXT)[0]
                           .getAuthorUserName().contains(userNameWithoutDomain));

        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                       + "artifacts" + File.separator + "GREG" + File.separator
                       + "reports" + File.separator + applicationJAR;

        dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addExtension(applicationJAR, dh);

        assertTrue(isExtensionExist(applicationJAR));
    }

    /**
     * method to check whether a specified extension exist in the registry
     *
     * @param name
     * @return
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    private boolean isExtensionExist(String name) throws RemoteException,
                                                         ResourceAdminServiceExceptionException {

        String[] extensions = resourceAdminServiceClient.listExtensions();
        boolean assertVal = false;

        for (int i = 0; i < extensions.length; i++) {
            if (extensions[i].equals(name)) {
                assertVal = true;
                break;
            }
        }

        return assertVal;
    }

    /**
     * add Life Cycle template to a location other than
     * "/_system/governance/repository/components/
     * org.wso2.carbon.governance/templates"
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    protected void testAddLCTemplateAnyLocation() throws MalformedURLException,
                                                         RemoteException,
                                                         ResourceAdminServiceExceptionException {
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                              + "artifacts" + File.separator + "GREG" + File.separator
                              + "reports" + File.separator + "TestGovernanceLC.jrxml";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(testGovernanceLCtemplate,
                                               "application/xml", "TstDec", dh);

        resourceAdminServiceClient.addResource(
                testGovernanceLCtemplateAnyLocation, "application/xml",
                "TstDec", dh);

        assertTrue(resourceAdminServiceClient
                           .getResource(testGovernanceLCtemplateAnyLocation)[0]
                           .getAuthorUserName().contains(userNameWithoutDomain));
    }

    /**
     * remove life cycle template added to any location other than
     * "/_system/governance/repository/components/
     * org.wso2.carbon.governance/templates"
     *
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    protected void removeLCTemplateAnyLocation() throws RemoteException,
                                                        ResourceAdminServiceExceptionException {
        resourceAdminServiceClient
                .deleteResource(testGovernanceLCtemplateAnyLocation);
    }

    /**
     * add invalid Life Cycle template to registry
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    protected void testAddInvalidLCTemplate() throws MalformedURLException,
                                                     RemoteException,
                                                     ResourceAdminServiceExceptionException {
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                              + "artifacts" + File.separator + "GREG" + File.separator
                              + "reports" + File.separator + "TestGovernanceLC-invalid.jrxml";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(testGovernanceLCInvalidtemplate,
                                               "application/xml", "TstDec", dh);

        assertTrue(resourceAdminServiceClient
                           .getResource(testGovernanceLCInvalidtemplate)[0]
                           .getAuthorUserName().contains(userNameWithoutDomain));
    }

    /**
     * remove added invalid template
     *
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    protected void removeInvalidLCTemplate() throws RemoteException,
                                                    ResourceAdminServiceExceptionException {
        resourceAdminServiceClient
                .deleteResource(testGovernanceLCInvalidtemplate);
    }

    /**
     * add Life Cycle artifact for testing purposes
     *
     * @throws RegistryException
     */
    protected void testAddLCArtifact() throws RegistryException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(
                governance, "testGovernance");
        GenericArtifact artifact = artifactManager
                .newGovernanceArtifact(new QName(artifactName + "1"));

        artifact.setAttribute("details_govCycleName", "G-regTesting");
        artifact.setAttribute("details_product", "governance registry");
        artifact.setAttribute("details_version", "4.5.0");
        artifact.setAttribute("details_addedby", "Evanthika Amarasiri");
        artifact.setAttribute("details_qa", "Amal Perera");
        artifact.setAttribute("details_qaa", "Krishantha Samaraweera");
        artifact.setAttribute("details_comments", "Smoke test");

        artifact.setAttribute("functionalTestCases_feature", "REST API");
        artifact.setAttribute("functionalTestCases_url",
                              "https://10.200.3.57:9443/carbon/REST");
        artifact.setAttribute("functionalTestCases_comment",
                              "Created by Sameera");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact recievedArtifact = artifactManager
                .getGenericArtifact(artifact.getId());
        artifactIDs[0] = artifact.getId();
        assertTrue(recievedArtifact.getQName().toString()
                           .contains(artifactName + "1"), "artifact name not found");

    }

    /**
     * add 1 application artifact for testing purposes
     *
     * @throws RegistryException
     */
    protected void testAddApplicationArtifact1() throws RegistryException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(
                governance, "applications");
        GenericArtifact artifact = artifactManager
                .newGovernanceArtifact(new QName(applicationName + "1"));

        artifact.setAttribute("overview_name", "test application");
        artifact.setAttribute("overview_version", "4.5.0");
        artifact.setAttribute("overview_description", "Description");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact recievedArtifact = artifactManager
                .getGenericArtifact(artifact.getId());
        artifactIDs[1] = artifact.getId();
        assertTrue(
                recievedArtifact.getQName().toString()
                        .contains(applicationName + "1"),
                "artifact name not found");

    }

    /**
     * add 2nd application artifact for testing purposes
     *
     * @throws RegistryException
     */
    protected void testAddApplicationArtifact2() throws RegistryException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(
                governance, "applications");
        GenericArtifact artifact = artifactManager
                .newGovernanceArtifact(new QName(applicationName + "2"));

        artifact.setAttribute("overview_name", "test application2");
        artifact.setAttribute("overview_version", "4.6.0");
        artifact.setAttribute("overview_description", "Description2");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact recievedArtifact = artifactManager
                .getGenericArtifact(artifact.getId());
        artifactIDs[2] = artifact.getId();
        assertTrue(
                recievedArtifact.getQName().toString()
                        .contains(applicationName + "2"),
                "artifact name not found");

    }

    /**
     * add 3rd and 4th application artifact for testing purposes
     *
     * @throws RegistryException
     */
    protected void testAddApplicationArtifacts() throws RegistryException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(
                governance, "applications");
        GenericArtifact artifact = artifactManager
                .newGovernanceArtifact(new QName(applicationName + "3"));

        artifact.setAttribute("overview_name", "test application3");
        artifact.setAttribute("overview_version", "4.7.0");
        artifact.setAttribute("overview_description", "Description3");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact recievedArtifact = artifactManager
                .getGenericArtifact(artifact.getId());
        artifactIDs[3] = artifact.getId();
        assertTrue(
                recievedArtifact.getQName().toString()
                        .contains(applicationName + "3"),
                "artifact name not found");

        artifact = artifactManager.newGovernanceArtifact(new QName(
                applicationName + "4"));

        artifact.setAttribute("overview_name", "test application4");
        artifact.setAttribute("overview_version", "4.8.0");
        artifact.setAttribute("overview_description", "Description4");

        artifactManager.addGenericArtifact(artifact);

        recievedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        artifactIDs[4] = artifact.getId();
        assertTrue(
                recievedArtifact.getQName().toString()
                        .contains(applicationName + "4"),
                "artifact name not found");
    }

    /**
     * create an edited Life Cycle template for testing purposes
     *
     * @throws RegistryException
     */
    protected void createEditedLCtemplate() throws RegistryException {
        Resource template = registry.get(testGovernanceLCtemplate);

        String templateString = convertResourceToString(template);

        templateString = templateString.replace("Version", "Version_Edited");
        templateString = templateString.replace(
                "This contains a list of lifecycles added by WSO2 QAs.",
                "Edited_heading");
        template.setContent(templateString);

        registry.put(testGovernanceLCEditedTemplate, template);
    }

    /**
     * delete added edited life cycle artifact
     *
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    protected void removeEditedLCtemplate() throws RemoteException,
                                                   ResourceAdminServiceExceptionException {
        resourceAdminServiceClient
                .deleteResource(testGovernanceLCEditedTemplate);
    }

    /**
     * create a Life Cycle template to mismatch with RXT for testing purposes
     *
     * @throws RegistryException
     */
    protected void createMismatchLCtemplate() throws RegistryException {
        Resource template = registry.get(testGovernanceLCtemplate);

        String templateString = convertResourceToString(template);

        templateString = templateString.replace("details_version",
                                                "details_version1");
        template.setContent(templateString);

        registry.put(testGovernanceLCMismatchtemplate, template);
    }

    /**
     * delete mismatch template
     *
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    protected void removeMismatchLCtemplate() throws RemoteException,
                                                     ResourceAdminServiceExceptionException {
        resourceAdminServiceClient
                .deleteResource(testGovernanceLCMismatchtemplate);
    }

    /**
     * returns the string format of a given resource
     *
     * @param rsc
     * @return String
     * @throws RegistryException
     */
    protected String convertResourceToString(Resource rsc)
            throws RegistryException {
        Object o = rsc.getContent();
        if (o instanceof byte[]) {
            return new String((byte[]) o);
        } else {
            return (String) o;
        }
    }

    /**
     * method to convert given input stream to a string
     *
     * @param in
     * @return String
     * @throws IOException
     */
    protected String readInputStreamAsString(InputStream in) throws IOException {

        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        return buf.toString();
    }

    /**
     * write data hander to a specified file in the disk
     *
     * @param dataHandler
     * @throws IOException
     */
    protected void saveDataHandlerToFile(DataHandler dataHandler) throws IOException {
        OutputStream out;
        try {
            out = new FileOutputStream(Dest_file);
            dataHandler.writeTo(out);
            out.close();
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File not found " + e);
        } catch (IOException e) {
            throw new IOException("File IO error " + e);
        }
    }

    /**
     * remove resources added for Life cycle report generation
     *
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    protected void removeResourcesLCReport() throws RemoteException,
            ResourceAdminServiceExceptionException, RegistryException {
        if (registry.resourceExists(testGovernanceLCtemplate)) {
            resourceAdminServiceClient.deleteResource(testGovernanceLCtemplate);
        }
        if (registry.resourceExists(testGovernanceLCRXT)) {
            resourceAdminServiceClient.deleteResource(testGovernanceLCRXT);
        }
        resourceAdminServiceClient.removeExtension(testGovernanceLCJAR);
    }

    /**
     * remove added life cycle artifact
     *
     * @throws RegistryException
     */
    protected void removeLCArtifact() throws RegistryException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);

        GenericArtifactManager artifactManager = new GenericArtifactManager(
                governance, "testGovernance");
        if (artifactIDs[0] != null) {
            artifactManager.removeGenericArtifact(artifactIDs[0]);
        }
    }

    /**
     * remove resources added for application report generation
     *
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    protected void removeResourcesAppicationReport() throws RemoteException,
                                                            ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.deleteResource(applicationTemplate);
        resourceAdminServiceClient.deleteResource(applicationRXT);
        resourceAdminServiceClient.removeExtension(applicationJAR);
    }

    /**
     * delete a specified application artifact
     *
     * @param i
     * @throws RegistryException
     */
    protected void removeAppicationArtifact(int i) throws RegistryException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);

        GenericArtifactManager artifactManager = new GenericArtifactManager(
                governance, "applications");
        if(artifactIDs[i] != null ){
        	artifactManager.removeGenericArtifact(artifactIDs[i]);
        }        
    }

    /**
     * method to remove all the saved report configuration
     *
     * @throws Exception
     */
    protected void removeAllReports() throws Exception {
        ReportConfigurationBean retrievedBeans[] = reportAdminServiceClient
                .getSavedReports();

        if (retrievedBeans != null) {
            for (ReportConfigurationBean retrievedBean : retrievedBeans) {
                reportAdminServiceClient.deleteSavedReport(retrievedBean
                                                                   .getName());
            }
        }
    }

    protected boolean deleteDestiationFile() {
        File file = new File(Dest_file);
        return file.delete();
    }

    /**
     * remove resources added by report scheduling
     *
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    protected void removeScheduleGeneratedResources() throws RemoteException,
                                                             ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.deleteResource(scheduleBenchmarkLocation);
        resourceAdminServiceClient.deleteResource(scheduleReportLocation);
    }

    /**
     * remove collection added at the location specified by variable "testTemplateCollection"
     *
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    protected void removeTemplateCollection() throws RemoteException,
            ResourceAdminServiceExceptionException, RegistryException {
        if (registry.resourceExists(testTemplateCollection)) {
            resourceAdminServiceClient.deleteResource(testTemplateCollection);
        }
    }

    /**
     * method to clear class level variables
     */
    protected void clear() {
        governance = null;
        registry = null;
        logViewerClient = null;
        reportAdminServiceClient = null;
        resourceAdminServiceClient = null;
    }
}
