package org.wso2.carbon.registry;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.integration.framework.TestServerManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.resource.test.TestUtils;
import org.wso2.carbon.utils.FileManipulator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.fail;
import static org.wso2.carbon.registry.resource.test.TestUtils.*;


/**
 * Prepares the WSO2 GReg for test runs, starts the server, and stops the server after
 * test runs
 */
public class RegistryTestServerManager extends TestServerManager {

    private static final Log log = LogFactory.getLog(RegistryTestServerManager.class);

    @Override
    @BeforeSuite(timeOut = 4000000)
    public String startServer() throws IOException {
        String carbonHome = super.startServer();
        log.info(" ------- Starting Server -------- ");
        System.setProperty("carbon.home", carbonHome);
//        TestUtils.addSearchSupplResource();
        return carbonHome;
    }

    @Override
    @AfterSuite(timeOut = 60000)
    public void stopServer() throws Exception {
        log.info(" ------- Stopping Server -------- ");
        super.stopServer();
    }

    @Override
    protected void copyArtifacts(String carbonHome) throws IOException {
        // copy the scripts required to start the server in debug mode.
        copyDebugScripts(carbonHome);

        copyUDDIScripts(carbonHome);

        // backward associationHandler sample
        copyBackwardAssociationHandler(carbonHome);

        // edit registry xml and add new handle
        editRegistryXML(carbonHome);
        increaseSearchIndexStartTimeDelay(carbonHome);
    }

    private void copyUDDIScripts(String carbonHome) throws IOException {
        if (!"true".equals(System.getProperty("uddi.mode"))) {
            return;
        }
        String frameworkPath = FrameworkSettings.getFrameworkPath();
        assert carbonHome != null : "carbonHome cannot be null";

        for (String fileName : new String[]{"wso2server.sh", "wso2server.bat"}) {
            File srcFile = new File(frameworkPath + File.separator + ".." + File.separator+ "src"
                    + File.separator + "test" + File.separator + "java" + File.separator
                    + "resources" + File.separator + "uddiMode" + File.separator + fileName);
            assert srcFile.exists() : srcFile.getAbsolutePath() + " does not exist";

            File depFile = new File(carbonHome + File.separator + "bin");
            File dstFile = new File(depFile.getAbsolutePath() + File.separator + fileName);
            log.info("Copying " + srcFile.getAbsolutePath() + " => " + dstFile.getAbsolutePath());
            FileManipulator.copyFile(srcFile, dstFile);
        }

    }

    private void increaseSearchIndexStartTimeDelay(String carbonHome) {
        try {

            String registryXmlPath = carbonHome + File.separator + "repository" + File.separator
                    + "conf" + File.separator + "registry.xml";

            File registryFile = new File(registryXmlPath);
            assert registryFile.exists() : registryFile.getAbsolutePath() + " does not exist";
            FileInputStream inputStream = new FileInputStream(registryFile);
            OMElement documentElement = new StAXOMBuilder(inputStream).getDocumentElement();
            AXIOMXPath xpathExpression = new AXIOMXPath("/wso2registry/indexingConfiguration/startingDelayInSeconds");
            OMElement indexConfigNode = (OMElement) xpathExpression.selectSingleNode(documentElement);
            indexConfigNode.setText("5");

            AXIOMXPath xpathExpression1 = new AXIOMXPath("/wso2registry/indexingConfiguration/indexingFrequencyInSeconds");
            OMElement indexConfigNode1 = (OMElement) xpathExpression1.selectSingleNode(documentElement);
            indexConfigNode1.setText("5");

            FileOutputStream fileOutputStream = new FileOutputStream(registryFile);
            XMLStreamWriter writer =
                    XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            documentElement.serialize(writer);
            Thread.sleep(2000);

        } catch (FileNotFoundException e) {
            log.error("Registry.xml file not found" + e.getMessage());
            fail("Registry.xml file not found" + e.getMessage());
        } catch (XMLStreamException e) {
            log.error("XML stream exception" + e.getMessage());
            fail("XML stream exception" + e.getMessage());
        } catch (InterruptedException ignored) {

        } catch (JaxenException e) {
            log.error("Jaxen exception " + e.getMessage());
            fail("Jaxen exception" + e.getMessage());
        }

    }


    private void copyBackwardAssociationHandler(String carbonHome) throws IOException {
        String frameworkPath = FrameworkSettings.getFrameworkPath();
        String handlerSampleBundle = getTestSamplesDir(frameworkPath) + File.separator +
                "backwardAssociationHandler" + File.separator + "target" +
                File.separator + "backwardAssociationHandler.jar";

        assert carbonHome != null : "carbonHome cannot be null";
        File srcFile = new File(handlerSampleBundle);
        assert srcFile.exists() : srcFile.getAbsolutePath() + " does not exist";

        String deploymentPath = carbonHome + File.separator + "repository" + File.separator
                + "components" + File.separator + "dropins";
        File depFile = new File(deploymentPath);
        if (!depFile.exists() && !depFile.mkdir()) {
            throw new IOException("Error while creating the deployment folder : " + deploymentPath);
        }
        File dstFile = new File(depFile.getAbsolutePath() + File.separator +
                "backwardAssociationHandler.jar");
        log.info("Copying " + srcFile.getAbsolutePath() + " => " + dstFile.getAbsolutePath());
        FileManipulator.copyFile(srcFile, dstFile);
    }

    private void copyDebugScripts(String carbonHome) throws IOException {
        if (!"true".equals(System.getProperty("debug.mode"))) {
            return;
        }
        String frameworkPath = FrameworkSettings.getFrameworkPath();
        assert carbonHome != null : "carbonHome cannot be null";

        for (String fileName : new String[]{"wso2server.sh", "wso2server.bat"}) {
            File srcFile = new File(frameworkPath + File.separator + ".." + File.separator + "src"
                    + File.separator + "test" + File.separator + "java" + File.separator
                    + "resources" + File.separator + "debugMode" + File.separator + fileName);
            assert srcFile.exists() : srcFile.getAbsolutePath() + " does not exist";

            File depFile = new File(carbonHome + File.separator + "bin");
            File dstFile = new File(depFile.getAbsolutePath() + File.separator + fileName);
            log.info("Copying " + srcFile.getAbsolutePath() + " => " + dstFile.getAbsolutePath());
            FileManipulator.copyFile(srcFile, dstFile);
        }
    }

    public static String getTestSamplesDir(String frameworkPath) {
        return frameworkPath + File.separator + ".." + File.separator + ".." +
                File.separator + "test.samples";
    }

    public static void editRegistryXML(String carbonHome) {
        String registryXmlPath = carbonHome + File.separator + "repository" + File.separator
                + "conf" + File.separator + "registry.xml";
        assert carbonHome != null : "carbonHome cannot be null";
        File srcFile = new File(registryXmlPath);
        assert srcFile.exists() : srcFile.getAbsolutePath() + " does not exist";

        try {
            OMElement handlerConfig = getHandlerOmElement(FrameworkSettings.getFrameworkPath());
            OMElement registryXML = getRegistryXmlOmElement(carbonHome);
            registryXML.addChild(handlerConfig);
            log.debug("Registry xml content after modifications " + registryXML);
            FileOutputStream fileOutputStream = new FileOutputStream(srcFile);
            XMLStreamWriter writer =
                    XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            registryXML.serialize(writer);
            Thread.sleep(2000); //wait for 1 sec after registry.xml serializing

            //ToDo introduce abstract method to edit config files in TestServerManager
        } catch (FileNotFoundException e) {
            log.error("Registry.xml file not found" + e.getMessage());
            fail("Registry.xml file not found" + e.getMessage());
        } catch (XMLStreamException e) {
            log.error("XML stream exception" + e.getMessage());
            fail("XML stream exception" + e.getMessage());
        } catch (InterruptedException ignored) {
        }
    }

    public static OMElement getHandlerOmElement(String frameworkPath)
            throws FileNotFoundException, XMLStreamException {
        String sampleHandlerName = "backwardAssociationHandler.xml";
        String handlerFilePath = getTestResourcesDir(frameworkPath) + File.separator + "handler" +
                File.separator + sampleHandlerName;


        File handlerFile = new File(handlerFilePath);
        assert handlerFile.exists() : handlerFile.getAbsolutePath() + " does not exist";
        FileInputStream inputStream = new FileInputStream(handlerFile);
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        StAXOMBuilder builder = new StAXOMBuilder(parser);

        return builder.getDocumentElement();

    }

    public static OMElement getRegistryXmlOmElement(String carbonHome)
            throws FileNotFoundException, XMLStreamException {
        String registryXmlPath = carbonHome + File.separator + "repository" + File.separator
                + "conf" + File.separator + "registry.xml";


        File registryFile = new File(registryXmlPath);
        assert registryFile.exists() : registryFile.getAbsolutePath() + " does not exist";
        FileInputStream inputStream = new FileInputStream(registryFile);
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        StAXOMBuilder builder = new StAXOMBuilder(parser);

        return builder.getDocumentElement();

    }

    public static String getTestResourcesDir(String frameworkPath) {
        return frameworkPath + File.separator + ".." + File.separator + ".." + File.separator +
                File.separator + "tests" + File.separator + "src" + File.separator + "test" +
                File.separator + "java" + File.separator + "resources";
    }
}

