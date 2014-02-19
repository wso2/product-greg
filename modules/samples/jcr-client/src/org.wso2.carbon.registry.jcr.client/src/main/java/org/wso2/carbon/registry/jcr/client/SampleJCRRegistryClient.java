package org.wso2.carbon.registry.jcr.client;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.Calendar;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.wso2.carbon.registry.jcr.RegistryRepositoryFactory;
import org.wso2.carbon.registry.jcr.RegistrySession;
import org.wso2.carbon.registry.jcr.RegistryPropertyType;
import org.wso2.carbon.registry.jcr.RegistrySimpleCredentials;

import javax.jcr.*;


public class SampleJCRRegistryClient {

    private static String backendURL = "http://localhost:9763/services/";
    private String cookie = null;
    private static ConfigurationContext configContext = null;

    private static final String CARBON_HOME ="../../../";

    private static String username = "admin";
    private static String password = "admin";
    private static String serverURL = "https://localhost:9443/registry";
    private static RegistrySimpleCredentials credentials = null;
    private static Repository repository = null;
    String policyPath = "META-INF/policy.xml";
    private static Log log = LogFactory.getLog(SampleJCRRegistryClient.class);

    private static void initialize() throws RepositoryException {
        String certpath= CARBON_HOME + "repository/resources/security/client-truststore.jks";
        System.setProperty("javax.net.ssl.trustStore", certpath);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType","JKS");
	System.setProperty("carbon.home",CARBON_HOME);
        Map map = new HashMap<String,String>();
        map.put("registryURL",serverURL);
        map.put("userName",username);
        map.put("password",password);
        map.put("org.wso2.registry.jcr", "greg");
        try{
            RegistryRepositoryFactory rf = new RegistryRepositoryFactory();
            repository = rf.getRepository(map);
            credentials = new RegistrySimpleCredentials("admin", new String("admin").toCharArray());

        }catch (RepositoryException e){
            log.error("Error connecting Remote Registry instance",e);
        }
    }
    private static Node getOrAddNode(Node node, String name) throws RepositoryException {
            Node node1 = null;
            try {
                if(!node.hasNode(name)){
                    node1 = node.addNode(name);
                } else {
                    node1 = node.getNode(name);
                }

            } catch (RepositoryException e) {

                String msg = "failed to resolve the path of the given node ";
                log.debug(msg);
                throw new RepositoryException(msg, e);
            }
            return node1;
        }



        private static void addPropertyTestData(Node node) throws RepositoryException {
            System.out.println("addPropertyTestData:"+node);
            node.setProperty("boolean", true);
            node.setProperty("double", Math.PI);
            node.setProperty("long", 90834953485278298l);
            Calendar c = Calendar.getInstance();
            c.set(2005, 6, 18, 17, 30);
            node.setProperty("calendar", c);
            ValueFactory factory = node.getSession().getValueFactory();
            node.setProperty("path", factory.createValue("/", RegistryPropertyType.PATH));
            node.setProperty("multi", new String[]{"one", "two", "three"});
        }

        private static void addQueryTestData(Node node) throws RepositoryException {
            while (node.hasNode("node1")) {
                node.getNode("node1").remove();
            }
            getOrAddNode(node, "node1").setProperty(
                    "prop1", "You can have it good, cheap, or fast. Any two.");
            getOrAddNode(node, "node1").setProperty("prop1", "foo bar");
            getOrAddNode(node, "node1").setProperty("prop1", "Hello world!");
            getOrAddNode(node, "node2").setProperty("prop1", "Apache Jackrabbit");
        }

        private static void addNodeTestData(Node node) throws RepositoryException, IOException {
            if (node.hasNode("multiReference")) {
                node.getNode("multiReference").remove();
            }
            if (node.hasNode("resReference")) {
                node.getNode("resReference").remove();
            }
            if (node.hasNode("myResource")) {
                node.getNode("myResource").remove();
            }

            Node resource = node.addNode("myResource", "nt:resource");
            // nt:resource not longer referenceable since JCR 2.0
            resource.addMixin("mix:referenceable");
            resource.setProperty("jcr:encoding", "UTF-8");
            resource.setProperty("jcr:mimeType", "text/plain");
            resource.setProperty(
                    "jcr:data",
                    new ByteArrayInputStream("Hello w\u00F6rld.".getBytes("UTF-8")));
            resource.setProperty("jcr:lastModified", Calendar.getInstance());

            Node resReference = getOrAddNode(node, "reference");
            resReference.setProperty("ref", resource);
            // make this node itself referenceable
            resReference.addMixin("mix:referenceable");

            Node multiReference = node.addNode("multiReference");
            ValueFactory factory = node.getSession().getValueFactory();
            multiReference.setProperty("ref", new Value[]{
                    factory.createValue(resource),
                    factory.createValue(resReference)
            });
        }

//        private  void addLifecycleTestData(Node node) throws RepositoryException {
//        Node policy = getOrAddNode(node, "policy");
//        policy.addMixin(NodeType.MIX_REFERENCEABLE);
//        Node transitions = getOrAddNode(policy, "transitions");
//        Node transition = getOrAddNode(transitions, "identity");
//        transition.setProperty("from", "identity");
//        transition.setProperty("to", "identity");
//
//        Node lifecycle = getOrAddNode(node, "node");
//        ((NodeImpl) lifecycle).assignLifecyclePolicy(policy, "identity");
//    }

        private static void addExportTestData(Node node) throws RepositoryException, IOException {
            getOrAddNode(node, "invalidXmlName").setProperty("propName", "some text");

            // three nodes which should be serialized as xml text in docView export
            // separated with spaces
            getOrAddNode(node, "jcr_xmltext").setProperty(
                    "jcr:xmlcharacters", "A text without any special character.");
            getOrAddNode(node, "some-element");
            getOrAddNode(node, "jcr_xmltext").setProperty(
                    "jcr:xmlcharacters",
                    " The entity reference characters: <, ', ,&, >,  \" should"
                            + " be escaped in xml export. ");
            getOrAddNode(node, "some-element");
            getOrAddNode(node, "jcr_xmltext").setProperty(
                    "jcr:xmlcharacters", "A text without any special character.");

            Node big = getOrAddNode(node, "bigNode");
            big.setProperty(
                    "propName0",
                    "SGVsbG8gd8O2cmxkLg==;SGVsbG8gd8O2cmxkLg==".split(";"),
                    RegistryPropertyType.BINARY);
            big.setProperty("propName1", "text 1");
            big.setProperty(
                    "propName2",
                    "multival text 1;multival text 2;multival text 3".split(";"));
            big.setProperty("propName3", "text 1");

//        addExportValues(node, "propName");
//        addExportValues(node, "Prop<>prop");
        }

        private static void addExportValues(Node node, String name)
                throws RepositoryException, IOException {
            String prefix = "valid";
            if (name.indexOf('<') != -1) {
                prefix = "invalid";
            }
            node = getOrAddNode(node, prefix + "Names");

            String[] texts = new String[]{
                    "multival text 1", "multival text 2", "multival text 3"};
            getOrAddNode(node, prefix + "MultiNoBin").setProperty(name, texts);

            Node resource = getOrAddNode(node, prefix + "MultiBin");
            resource.setProperty("jcr:encoding", "UTF-8");
            resource.setProperty("jcr:mimeType", "text/plain");
            String[] values =
                    new String[]{"SGVsbG8gd8O2cmxkLg==", "SGVsbG8gd8O2cmxkLg=="};
            resource.setProperty(name, values, RegistryPropertyType.BINARY);
            resource.setProperty("jcr:lastModified", Calendar.getInstance());

            getOrAddNode(node, prefix + "NoBin").setProperty(name, "text 1");

            resource = getOrAddNode(node, "invalidBin");
            resource.setProperty("jcr:encoding", "UTF-8");
            resource.setProperty("jcr:mimeType", "text/plain");
            byte[] bytes = "Hello w\u00F6rld.".getBytes("UTF-8");
            resource.setProperty(name, new ByteArrayInputStream(bytes));
            resource.setProperty("jcr:lastModified", Calendar.getInstance());
        }

    private static void loadTestdata(Session session) throws RepositoryException {

        try {

            loadSystemSpecificDate(session);
            Node data = getOrAddNode(session.getRootNode(), "testdata");
            addPropertyTestData(getOrAddNode(data, "property"));
            addQueryTestData(getOrAddNode(data, "query"));
            addNodeTestData(getOrAddNode(data, "node"));
            //      addLifecycleTestData(getOrAddNode(data, "lifecycle"));
            addExportTestData(getOrAddNode(data, "docViewTest"));

        } catch (Exception e) {
            String msg = "cannot load test data ";
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }

    }
    private static void loadSystemSpecificDate(Session session) throws RepositoryException {
        if(!session.getRootNode().hasNode("jcr_system")) {
            session.getRootNode().addNode("jcr_system");
        }
        if(!session.getRootNode().getNode("jcr_system").hasNode("jcr_activities")) {
            session.getRootNode().getNode("jcr_system").addNode("jcr_activities");
        }
          if(!session.getRootNode().getNode("jcr_system").hasNode("jcr_gregVersionLabels")) {
              session.getRootNode().getNode("jcr_system").addNode("jcr_gregVersionLabels");
          }
    }
    
    public static void main(String[] args) throws Exception {

        initialize();
        Session session = repository.login(credentials);
        Workspace workspace = session.getWorkspace();
        if(!session.getRootNode().hasNode("testroot")) {
            Node root_node = session.getRootNode().addNode("testroot");
        }
        loadTestdata(session);
    }
}

