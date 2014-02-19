/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.carbon.registry.samples.populator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.registry.resource.services.utils.InputStreamBasedDataSource;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.registry.samples.populator.utils.*;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.net.URI;
import javax.xml.namespace.QName;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class Main {
    private static String cookie;

    private static void setSystemProperties() {
        String trustStore = System.getProperty("carbon.home") + File.separator + "repository" + File.separator +
                "resources" + File.separator + "security" + File.separator + "wso2carbon.jks";
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode", "true");
    }

    public static void main(String[] args) {
        try {
            CommandHandler.setInputs(args);
            setSystemProperties();

            String axis2Configuration = System.getProperty("carbon.home") + File.separator + "repository" +
                    File.separator + "conf" + File.separator + "axis2" + File.separator + "axis2_client.xml";
            ConfigurationContext configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(axis2Configuration);

            Registry registry = new WSRegistryServiceClient(
                    CommandHandler.getServiceURL(), CommandHandler.getUsername(),
                    CommandHandler.getPassword(), configContext) {
                public void setCookie(String cookie) {
                    Main.cookie = cookie;
                    super.setCookie(cookie);
                }
            };

            ResourceServiceClient resourceServiceClient = new ResourceServiceClient(cookie,
                    CommandHandler.getServiceURL(), configContext);

            int currentTask = 0;
            int tasks = 10;
            addRXT(resourceServiceClient);
            currentTask = printStatusMessage("Completed uploading RXT definitions", tasks, currentTask);
            addReportTemplates(resourceServiceClient);
            currentTask = printStatusMessage("Completed uploading Jasper Reporting templates", tasks, currentTask);
            addReports(configContext);
            currentTask = printStatusMessage("Completed configuring reports", tasks, currentTask);
            addExtensions(configContext);
            currentTask = printStatusMessage("Completed uploading extensions", tasks, currentTask);
            addHandlers(configContext);
            currentTask = printStatusMessage("Completed configuring handlers", tasks, currentTask);
            addLifecycles(configContext);
            currentTask = printStatusMessage("Completed configuring lifecycles", tasks, currentTask);
            addData(configContext, registry);
            currentTask = printStatusMessage("Completed populating resource data", tasks, currentTask);
            addSubscriptions(configContext);
            currentTask = printStatusMessage("Completed setting up subscriptions", tasks, currentTask);
            performLifecycleOperations(configContext, registry);
            currentTask = printStatusMessage("Completed lifecycle operations", tasks, currentTask);
            addUsersRolesAndTenants(configContext);
            currentTask = printStatusMessage("Completed populating users and roles", tasks, currentTask);
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.exit(0);
    }

    private static int printStatusMessage(String message, int tasks, int currentTask) {
        StringBuffer sb = new StringBuffer(message);
        for (int i = message.length(); i < 48; i++) {
            sb.append(" ");
        }
        int value = (++currentTask * 100) / tasks;
        if ((currentTask * 100) % tasks > (float) tasks / 2f) {
            value++;
        }
        if (value < 10) {
            System.out.println(sb.append(" ").append(value).append("%").toString());
        } else if (value < 100) {
            System.out.println(sb.append(value).append("%").toString());
        } else {
            System.out.println(sb.deleteCharAt(sb.length() - 1).append(value).append("%").toString());
        }
        return currentTask;
    }

    private static void addRXT(ResourceServiceClient resourceServiceClient) throws Exception {
        File extensionFolder = new File(CommandHandler.getRxtFileLocation());
        File[] extensions = extensionFolder.listFiles();
        if (extensions != null) {
            for (File extension : extensions) {
                String extensionName = getResourceName(extension.getAbsolutePath().replace("\\", "/"));
                if (extensionName.endsWith(".rxt")) {
                    DataSource dataSource = new InputStreamBasedDataSource(
                            new FileInputStream(new File(extension.getAbsolutePath())));
                    DataHandler dataHandler = new DataHandler(dataSource);
                    resourceServiceClient.addResource(
                            "/_system/governance/repository/components/org.wso2.carbon.governance/types/" + extensionName,
                            "application/vnd.wso2.registry-ext-type+xml", null, dataHandler, null, null);
                }
            }
        }
    }

    private static void addReportTemplates(ResourceServiceClient resourceServiceClient) throws Exception {
        File templateFolder = new File(CommandHandler.getJRTemplateLocation());
        File[] templates = templateFolder.listFiles();
        if (templates != null) {
            for (File template : templates) {
                String templateName = getResourceName(template.getAbsolutePath().replace("\\", "/"));
                if (templateName.endsWith(".jrxml")) {
                    DataSource dataSource = new InputStreamBasedDataSource(
                            new FileInputStream(new File(template.getAbsolutePath())));
                    DataHandler dataHandler = new DataHandler(dataSource);
                    resourceServiceClient.addResource(
                            "/_system/governance/repository/components/org.wso2.carbon.governance/templates/" +
                                    templateName, "application/xml", null, dataHandler, null, null);
                }
            }

        }
    }

    private static void addReports(ConfigurationContext configContext) throws Exception {
        File reportsDirectory = new File(CommandHandler.getReportsLocation());
        if (reportsDirectory.exists()) {
            ReportGeneratorServiceClient client =
                    new ReportGeneratorServiceClient(cookie, CommandHandler.getServiceURL(), configContext);
            Workbook workbook = PopulatorUtil.getWorkbook(reportsDirectory, "list");
            if (workbook != null) {
                Sheet sheet = workbook.getSheet(workbook.getSheetName(0));
                if (sheet == null || sheet.getLastRowNum() == -1) {
                    throw new RuntimeException("The first sheet is empty");
                }
                int limit = sheet.getLastRowNum();
                for (int i = 0; i <= limit; i++) {
                    Row row = sheet.getRow(i);
                    if (row == null || row.getCell(0) == null) {
                        break;
                    }
                    client.saveReport(PopulatorUtil.getReportConfigurationBean(
                            row.getCell(0).getStringCellValue(), row.getCell(1).getStringCellValue(),
                            row.getCell(2).getStringCellValue(), row.getCell(3).getStringCellValue()));
                }
            }
        }
    }

    private static void addExtensions(ConfigurationContext configContext) throws Exception {
        String extensionJarLocation = CommandHandler.getHandlerJarLocation();
        if (extensionJarLocation != null) {
            org.wso2.carbon.registry.extensions.ui.clients.ResourceServiceClient extensionResourceServiceClient =
                    new org.wso2.carbon.registry.extensions.ui.clients.ResourceServiceClient(cookie,
                            CommandHandler.getServiceURL(), configContext);
            File folder = new File(extensionJarLocation);
            File[] filesList = folder.listFiles();
            if (filesList != null) {
                for (File file : filesList) {
                    String name = file.getName();
                    if (file.isFile() && name.endsWith(".jar")) {
                        String fileName = getResourceName(file.getAbsolutePath().replace("\\", "/"));
                        DataSource dataSource = new InputStreamBasedDataSource(
                                new FileInputStream(new File(file.getAbsolutePath())));
                        DataHandler dataHandler = new DataHandler(dataSource);
                        extensionResourceServiceClient.addExtension(fileName, dataHandler);
                    }
                }
            }
        }
    }

    private static void addHandlers(ConfigurationContext configContext) throws Exception {
        if (new File(CommandHandler.getHandlerDef()).exists()) {
            StAXOMBuilder builder = new StAXOMBuilder(CommandHandler.getHandlerDef());
            OMElement handlersOMElement = builder.getDocumentElement();
            Iterator<OMElement> handlers = handlersOMElement.getChildElements();
            PopulatorHandlerManagerServiceClient handlerManagementServiceClient
                    = new PopulatorHandlerManagerServiceClient(cookie, CommandHandler.getServiceURL(), configContext);
            while (handlers.hasNext()) {
                handlerManagementServiceClient.newHandler(handlers.next().toString());
            }
        }
    }

    private static void addLifecycles(ConfigurationContext configContext) throws Exception {
        if (CommandHandler.getLifecycleConfigLocation() != null) {
            File lifecycleConfigLocation = new File(CommandHandler.getLifecycleConfigLocation());
            if (lifecycleConfigLocation.exists()) {
                String[] filesList = lifecycleConfigLocation.list();
                if (filesList != null) {
                    LifeCycleManagementClient lifeCycleManagementClient = new LifeCycleManagementClient(
                            cookie, CommandHandler.getServiceURL(), configContext);
                    for (String file : filesList) {
                        lifeCycleManagementClient.createLifecycle(
                                new StAXOMBuilder(CommandHandler.getLifecycleConfigLocation() +
                                        File.separator + file).getDocumentElement().toString());
                    }
                }

            }
        }
    }

    private static void addData(ConfigurationContext configContext, Registry registry) throws Exception {
        File dataDirectory = new File(CommandHandler.getDataLocation());
        if (dataDirectory.exists()) {
            addAssets(registry, dataDirectory);
            addResourcesAndCollections(registry, dataDirectory);
            importAndUploadResources(registry, dataDirectory);
            addCommentsRatingsAndTags(registry, dataDirectory);
            addAssociations(registry, dataDirectory);
            addPermissions(configContext, dataDirectory);
        }
    }

    private static void addPermissions(ConfigurationContext configContext, File dataDirectory) throws Exception {
        UserManagementClient userManager =
                new UserManagementClient(cookie, CommandHandler.getServiceURL(), configContext);
        Workbook[] workbooks = PopulatorUtil.getWorkbooks(dataDirectory, "permission");
        for (Workbook workbook : workbooks) {
            Sheet sheet = workbook.getSheet(workbook.getSheetName(0));
            if (sheet == null || sheet.getLastRowNum() == -1) {
                throw new RuntimeException("The first sheet is empty");
            }
            int limit = sheet.getLastRowNum();
            for (int i = 0; i <= limit; i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) {
                    break;
                }
                userManager.setRoleResourcePermission(
                        getCellValue(row.getCell(0), null),
                        getCellValue(row.getCell(1), null),
                        splitAndTrim(getCellValue(row.getCell(2), null), ","));
            }
        }
    }

    private static void addAssociations(Registry registry, File dataDirectory) throws Exception {
        Workbook[] workbooks = PopulatorUtil.getWorkbooks(dataDirectory, "association");
        for (Workbook workbook : workbooks) {
            Sheet sheet = workbook.getSheet(workbook.getSheetName(0));
            if (sheet == null || sheet.getLastRowNum() == -1) {
                throw new RuntimeException("The first sheet is empty");
            }
            int limit = sheet.getLastRowNum();
            for (int i = 0; i <= limit; i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) {
                    break;
                }
                registry.addAssociation(getCellValue(row.getCell(0), null), getCellValue(row.getCell(1), null),
                        getCellValue(row.getCell(2), null));
            }
        }
    }

    private static void addCommentsRatingsAndTags(Registry registry, File dataDirectory) throws Exception {
        Workbook[] workbooks = PopulatorUtil.getWorkbooks(dataDirectory, "community");
        for (Workbook workbook : workbooks) {
            Sheet sheet = workbook.getSheet(workbook.getSheetName(0));
            if (sheet == null || sheet.getLastRowNum() == -1) {
                throw new RuntimeException("The first sheet is empty");
            }
            int limit = sheet.getLastRowNum();
            for (int i = 0; i <= limit; i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) {
                    break;
                }
                String type = getCellValue(row.getCell(0), null).toLowerCase();
                if (type.contains("tag")) {
                    String tag = getCellValue(row.getCell(2), null);
                    if (tag == null) {
                        String[] parts = getCellValue(row.getCell(1), null).split(";");
                        registry.removeTag(parts[0], parts[1].substring(5));
                    } else {
                        registry.applyTag(getCellValue(row.getCell(1), null), tag);
                    }
                } else if (type.contains("comment")) {
                    String path = getCellValue(row.getCell(1), null);
                    if (path.contains(";")) {
                        String comment = getCellValue(row.getCell(2), null);
                        if (comment == null) {
                            registry.removeComment(path);
                        } else {
                            registry.editComment(path, comment);
                        }
                    } else {
                        registry.addComment(path, new Comment(getCellValue(row.getCell(2), null)));
                    }
                } else if (type.contains("rate") || type.contains("rating")) {
                    registry.rateResource(getCellValue(row.getCell(1), null),
                            Integer.parseInt(getCellValue(row.getCell(2), "0")));
                }
            }
        }
    }

    private static void importAndUploadResources(Registry registry, File dataDirectory) throws Exception {
        Workbook[] workbooks = PopulatorUtil.getWorkbooks(dataDirectory, "import");
        for (Workbook workbook : workbooks) {
            Sheet sheet = workbook.getSheet(workbook.getSheetName(0));
            if (sheet == null || sheet.getLastRowNum() == -1) {
                throw new RuntimeException("The first sheet is empty");
            }
            int limit = sheet.getLastRowNum();
            for (int i = 0; i <= limit; i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) {
                    break;
                }
                String path = row.getCell(0).getStringCellValue();
                String url = row.getCell(1).getStringCellValue();
                Resource resource = registry.newResource();
                resource.setMediaType(getCellValue(row.getCell(2), null));
                resource.setDescription(getCellValue(row.getCell(3), "This resource was added using the " +
                        "WSO2 Governance Registry Sample Data Populator"));
                if (url.startsWith("file:")) {
                    resource.setContentStream(new BufferedInputStream(new FileInputStream(new File(new URI(url)))));
                    registry.put(path, resource);
                } else {
                    registry.importResource(path, url, resource);
                }
            }
        }
    }

    private static void addResourcesAndCollections(Registry registry, File dataDirectory) throws Exception {
        Workbook[] workbooks = PopulatorUtil.getWorkbooks(dataDirectory, "resource");
        for (Workbook workbook : workbooks) {
            Sheet sheet = workbook.getSheet(workbook.getSheetName(0));
            if (sheet == null || sheet.getLastRowNum() == -1) {
                throw new RuntimeException("The first sheet is empty");
            }
            int limit = sheet.getLastRowNum();
            for (int i = 0; i <= limit; i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) {
                    break;
                }
                String path = row.getCell(0).getStringCellValue();
                Resource resource;
                if (registry.resourceExists(path)) {
                    resource = registry.get(path);
                    String key = getCellValue(row.getCell(1), null);
                    String value = getCellValue(row.getCell(2), null);
                    if (value == null) {
                        resource.removeProperty(key);
                    } else {
                        resource.setProperty(key, value);
                    }
                } else {
                    String value = getCellValue(row.getCell(1), null);
                    if (value == null) {
                        resource = registry.newCollection();
                    } else {
                        resource = registry.newResource();
                        resource.setMediaType("text/plain");
                        resource.setContent(value);
                    }
                    resource.setDescription(getCellValue(row.getCell(2), "This resource was added using the " +
                            "WSO2 Governance Registry Sample Data Populator"));
                }
                registry.put(path, resource);
            }
        }
    }

    private static void addAssets(Registry registry, File dataDirectory) throws Exception {
        Workbook[] workbooks = PopulatorUtil.getWorkbooks(dataDirectory, "asset");
        for (Workbook workbook : workbooks) {
            Registry governanceRegistry =
                    GovernanceUtils.getGovernanceUserRegistry(registry, CommandHandler.getUsername());
            Sheet sheet = workbook.getSheet(workbook.getSheetName(0));
            if (sheet == null || sheet.getLastRowNum() == -1) {
                throw new RuntimeException("The first sheet is empty");
            }
            int limit = sheet.getLastRowNum();
            if (limit < 1) {
                throw new RuntimeException("Column headers were not specified in Asset Data Spreadsheet");
            }
            Row row = sheet.getRow(0);
            int key = -1;
            List<String> temp = new LinkedList<String>();
            String value;
            int count = 0;
            while ((value = getCellValue(row.getCell(count++), null)) != null) {
                if (value.equals("key")) {
                    key = count - 1;
                } else {
                    temp.add(value);
                }
            }
            String[] headers = temp.toArray(new String[temp.size()]);
            if (key == -1) {
                throw new RuntimeException("Asset Key was not specified");
            }
            for (int i = 1; i <= limit; i++) {
                row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) {
                    break;
                }
                String type = row.getCell(key).getStringCellValue();
                String nameAttribute = GovernanceUtils.findGovernanceArtifactConfiguration(
                        type, governanceRegistry).getArtifactNameAttribute();
                String namespaceAttribute = GovernanceUtils.findGovernanceArtifactConfiguration(
                        type, governanceRegistry).getArtifactNamespaceAttribute();
                GenericArtifactManager manager = new GenericArtifactManager(governanceRegistry, type);
                Map<String, String> attributeMap = new HashMap<String, String>();
                for (int j = 0; j < headers.length; j++) {
                    attributeMap.put(headers[j], row.getCell(j > key ? j + 1 : j).getStringCellValue());
                }
                GenericArtifact artifact = manager.newGovernanceArtifact(
                        new QName(attributeMap.containsKey(namespaceAttribute) ? attributeMap.get(namespaceAttribute) :
                                null, attributeMap.containsKey(nameAttribute) ? attributeMap.get(nameAttribute) :
                                UUIDGenerator.generateUUID()));
                for (Map.Entry<String, String> e : attributeMap.entrySet()) {
                    artifact.setAttribute(e.getKey(), e.getValue());
                }
                manager.addGenericArtifact(artifact);
            }
        }
    }

    private static void addSubscriptions(ConfigurationContext configContext) throws Exception {
        File subscriptionsDirectory = new File(CommandHandler.getSubscriptionsLocation());
        if (subscriptionsDirectory.exists()) {
            SubscriberClient manager = new SubscriberClient(
                    cookie, CommandHandler.getServiceURL(), configContext);
            Workbook workbook = PopulatorUtil.getWorkbook(subscriptionsDirectory, "list");
            if (workbook != null) {
                Sheet sheet = workbook.getSheet(workbook.getSheetName(0));
                if (sheet == null || sheet.getLastRowNum() == -1) {
                    throw new RuntimeException("The first sheet is empty");
                }
                int limit = sheet.getLastRowNum();
                for (int i = 0; i <= limit; i++) {
                    Row row = sheet.getRow(i);
                    if (row == null || row.getCell(0) == null) {
                        break;
                    }
                    manager.subscribe(row.getCell(0).getStringCellValue(),
                            row.getCell(1).getStringCellValue(),
                            row.getCell(2).getStringCellValue());
                }
            }
        }
    }

    private static void performLifecycleOperations(ConfigurationContext configContext, Registry registry)
            throws Exception {
        if (CommandHandler.getLifecycleConfigLocation() != null) {
            File lifecycleOperationsLocation = new File(CommandHandler.getLifecycleOperationsLocation());
            LifeCycleManagementClient client = new LifeCycleManagementClient(
                    cookie, CommandHandler.getServiceURL(), configContext);
            if (lifecycleOperationsLocation.exists()) {
                Workbook[] workbooks = PopulatorUtil.getWorkbooks(lifecycleOperationsLocation, "actions");
                for (Workbook workbook : workbooks) {
                    Sheet sheet = workbook.getSheet(workbook.getSheetName(0));
                    if (sheet == null || sheet.getLastRowNum() == -1) {
                        throw new RuntimeException("The first sheet is empty");
                    }
                    int limit = sheet.getLastRowNum();
                    for (int i = 0; i <= limit; i++) {
                        Row row = sheet.getRow(i);
                        if (row == null || row.getCell(0) == null) {
                            break;
                        }
                        String path = getCellValue(row.getCell(0), null);
                        String aspect = getCellValue(row.getCell(1), null);
                        String action = getCellValue(row.getCell(2), null);
                        if (action == null) {
                            if (aspect.equals(registry.get(path).getProperty("registry.LC.name"))) {
                                client.removeAspect(path, aspect);
                            } else {
                                client.addAspect(path, aspect);
                            }
                        } else {
                            String[] items = splitAndTrim(getCellValue(row.getCell(3), null), ",");
                            String temp = getCellValue(row.getCell(4), null);
                            if (temp == null) {
                                client.invokeAspect(path, aspect, action, items,
                                        Collections.<String, String>emptyMap());
                            } else {
                                Map<String, String> params = new LinkedHashMap<String, String>();
                                String[] pairs = splitAndTrim(temp, ",");
                                for (String pair : pairs) {
                                    String[] keyAndValue = splitAndTrim(pair, "[|]");
                                    params.put(keyAndValue[0], keyAndValue[1]);
                                }
                                client.invokeAspect(path, aspect, action, items, params);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void addUsersRolesAndTenants(ConfigurationContext configContext) throws Exception {
        File usersAndRolesDirectory = new File(CommandHandler.getUsersAndRolesLocation());
        if (usersAndRolesDirectory.exists()) {
            UserManagementClient userManager =
                    new UserManagementClient(cookie, CommandHandler.getServiceURL(), configContext);
            addTenants(usersAndRolesDirectory, userManager);
            addRoles(usersAndRolesDirectory, userManager);
            addUsers(usersAndRolesDirectory, userManager);
        }
    }

    private static void addUsers(File usersAndRolesDirectory, UserManagementClient userManager) throws Exception {
        Workbook[] workbooks = PopulatorUtil.getWorkbooks(usersAndRolesDirectory, "users");
        for (Workbook workbook : workbooks) {
            Sheet sheet = workbook.getSheet(workbook.getSheetName(0));
            if (sheet == null || sheet.getLastRowNum() == -1) {
                throw new RuntimeException("The first sheet is empty");
            }
            int limit = sheet.getLastRowNum();
            for (int i = 0; i <= limit; i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) {
                    break;
                }
                String name = row.getCell(0).getStringCellValue();
                String password = getCellValue(row.getCell(1), name + "123");
                String roles = getCellValue(row.getCell(2), null);
                userManager.addUser(name, password, splitAndTrim(roles == null ? null : roles, ","),
                        new ClaimValue[0], null);
                if (row.getCell(3) != null && row.getCell(3).getCellType() != Cell.CELL_TYPE_BLANK) {
                    UserProfileDTO profile = userManager.getUserProfile(name, "default");
                    UserFieldDTO[] fieldValues = profile.getFieldValues();
                    String email = row.getCell(3).getStringCellValue();
                    String firstName = getCellValue(row.getCell(4), name);
                    String lastName = getCellValue(row.getCell(5), name);
                    for (UserFieldDTO fieldValue : fieldValues) {
                        if (fieldValue.getClaimUri().equals("http://wso2.org/claims/emailaddress")) {
                            fieldValue.setFieldValue(email);
                        } else if (fieldValue.getClaimUri().equals("http://wso2.org/claims/givenname")) {
                            fieldValue.setFieldValue(firstName);
                        } else if (fieldValue.getClaimUri().equals("http://wso2.org/claims/lastname")) {
                            fieldValue.setFieldValue(lastName);
                        } else {
                            fieldValue.setFieldValue("");
                        }
                    }
                    userManager.setUserProfile(name, profile);
                }
            }
        }
    }

    private static void addRoles(File usersAndRolesDirectory, UserManagementClient userManager) throws Exception {
        Workbook[] workbooks = PopulatorUtil.getWorkbooks(usersAndRolesDirectory, "roles");
        for (Workbook workbook : workbooks) {
            Sheet sheet = workbook.getSheet(workbook.getSheetName(0));
            if (sheet == null || sheet.getLastRowNum() == -1) {
                throw new RuntimeException("The first sheet is empty");
            }
            int limit = sheet.getLastRowNum();
            for (int i = 0; i <= limit; i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) {
                    break;
                }
                String name = row.getCell(0).getStringCellValue();
                String[] permissions = splitAndTrim(row.getCell(1).getStringCellValue(), ",");
                if (name.equals("everyone") || name.equals("admin")) {
                    userManager.setRoleUIPermission(name, permissions);
                } else {
                    userManager.addRole(name, new String[0], permissions);
                }
            }
        }
    }

    private static void addTenants(File usersAndRolesDirectory, UserManagementClient userManager) throws Exception {
        Workbook[] workbooks = PopulatorUtil.getWorkbooks(usersAndRolesDirectory, "tenants");
        for (Workbook workbook : workbooks) {
            Sheet sheet = workbook.getSheet(workbook.getSheetName(0));
            if (sheet == null || sheet.getLastRowNum() == -1) {
                throw new RuntimeException("The first sheet is empty");
            }
            int limit = sheet.getLastRowNum();
            for (int i = 0; i <= limit; i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) {
                    break;
                }
                userManager.addTenant(getCellValue(row.getCell(0), null), getCellValue(row.getCell(1), null),
                        getCellValue(row.getCell(2), null), getCellValue(row.getCell(3), null),
                        getCellValue(row.getCell(4), null), getCellValue(row.getCell(5), null));
            }
        }
    }

    private static String getCellValue(Cell cell, String def) {
        return cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK ? cell.getStringCellValue() : def;
    }

    private static String[] splitAndTrim(String input, String splitBy) {
        if (input == null) {
            return null;
        } else {
            List<String> output = new LinkedList<String>();
            for (String string : input.split(splitBy)) {
                output.add(string.trim());
            }
            return output.toArray(new String[output.size()]);
        }
    }

    private static String getResourceName(String fileLocation) {
        String[] s = fileLocation.split("/");
        return s[s.length - 1];
    }
}

