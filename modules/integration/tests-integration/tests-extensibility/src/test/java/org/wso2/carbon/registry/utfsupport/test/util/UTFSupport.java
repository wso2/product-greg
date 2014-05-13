package org.wso2.carbon.registry.utfsupport.test.util;

import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Tag;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.TagBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.*;

import java.io.*;
import java.rmi.RemoteException;

public class UTFSupport {

    public static String readFile() throws IOException {
        String filePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                          File.separator + "GREG" + File.separator + "utf8" + File.separator +
                          "utf8-characters.txt";

        return readFile(filePath);
    }

    public static String readFile(String filePath) throws IOException {
        BufferedReader reader;
        StringBuilder stringBuilder;
        String line;
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
        stringBuilder = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line.replaceAll(",", ""));
            }
        } finally {
            reader.close();
        }
        return stringBuilder.toString();
    }


    public static boolean addDependency(RelationAdminServiceClient relationAdminServiceClient,
                                        String path1, String path2) throws Exception {

        relationAdminServiceClient.addAssociation(path1, "depends", path2, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(path1, "depends");
        String resourcePath = associationTreeBean.getAssociationTree();
        return resourcePath.contains(path2);
    }

    public static boolean addAssociation(RelationAdminServiceClient relationAdminServiceClient,
                                         String path1, String path2) throws Exception {
        relationAdminServiceClient.addAssociation(path1, "usedBy", path2, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(path1, "usedBy");
        String resourcePath = associationTreeBean.getAssociationTree();

        return (resourcePath.contains(path2));
    }

    public static boolean createLifecycle(LifeCycleManagementClient lifeCycleManagementClient,
                                          String LC_NAME) throws Exception {
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                              File.separator + "GREG" + File.separator + "lifecycle" + File.separator + "PromoteLC.xml";
        String lifeCycleContent = readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);

        String[] lifeClycles = lifeCycleManagementClient.getLifecycleList();
        boolean lcAdded = false;
        for (String lc : lifeClycles) {
            if (lc.equals(LC_NAME)) {
                lcAdded = true;
            }
        }

        return lcAdded;


    }

    public static boolean addRole(UserManagementClient userManagementClient,
                                  String roleName, AutomationContext automationContext) throws Exception {

        String userName = automationContext.getContextTenant().getContextUser().getUserName();
        String userNameWithoutDomain = "";

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        userManagementClient.addRole(roleName, new String[]{userNameWithoutDomain}, new String[]{""});
        return (userManagementClient.roleNameExists(roleName));

    }

    public static boolean addSubscription(InfoServiceAdminClient infoServiceAdminClient,
                                          String path1, String roleName, AutomationContext automationContext) throws Exception {

        String sessionCookie = getCookie(automationContext);

        infoServiceAdminClient.subscribe(path1, "work://" + roleName, "ResourceUpdated",
                sessionCookie);
        SubscriptionBean bean = infoServiceAdminClient.getSubscriptions(path1,
                sessionCookie);
        return (bean.getSubscriptionInstances() != null);
    }


    public static boolean addLc(WSRegistryServiceClient wsRegistryServiceClient, String path1,
                                String lcName,
                                LifeCycleAdminServiceClient lifeCycleAdminServiceClient)
            throws RegistryException, RemoteException,
                   CustomLifecyclesChecklistAdminServiceExceptionException,
                   ListMetadataServiceRegistryExceptionException,
                   ResourceAdminServiceExceptionException {

        wsRegistryServiceClient.associateAspect(path1, lcName);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(path1);

        Property[] properties = lifeCycle.getLifecycleProperties();

        boolean lcAdded = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(lcName)) {
                lcAdded = true;
            }
        }
        return lcAdded;
    }

    public static boolean addComment(InfoServiceAdminClient infoServiceAdminClient,
                                     String comment, String path1, AutomationContext  automationContext)
            throws Exception {

        String sessionCookie = getCookie(automationContext);

        infoServiceAdminClient.addComment(comment,
                                          path1, sessionCookie);
        CommentBean commentBean = infoServiceAdminClient.getComments(path1,
                sessionCookie);

        return (comment.equals(commentBean.getComments()[0].getContent()));

    }

    public static boolean addTag(InfoServiceAdminClient infoServiceAdminClient, String tagName,
                                 String path,
                                 AutomationContext automationContext)
            throws Exception {
        boolean status = false;

        String sessionCookie = getCookie(automationContext);

        infoServiceAdminClient.addTag(tagName, path, sessionCookie);
        TagBean tagBean = infoServiceAdminClient.getTags(path, sessionCookie);
        Tag[] tags = tagBean.getTags();

        for (Tag tag : tags) {

            if (tag.getTagName().equals(tagName)) {
                status = true;
            }
        }

        return status;
    }

    public static String getCookie (AutomationContext automationContext) throws Exception {

        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(automationContext);
        return loginLogoutClient.login();
    }
}
