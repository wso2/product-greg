/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.samples.reporting;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.util.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.reporting.AbstractReportGenerator;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.util.JasperPrintProvider;
import org.wso2.carbon.reporting.util.ReportParamMap;
import org.wso2.carbon.reporting.util.ReportStream;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class ServiceReportGenerator extends AbstractReportGenerator {

    public static final String HTML = "html";
    public static final String CLASS = "class";
    public static final String IMAGE = "image";
    public static final String IMAGE_EXPRESSION = "imageExpression";
    private static Log log = LogFactory.getLog(ServiceReportGenerator.class);

    public ByteArrayOutputStream execute(String template, String type) throws IOException {
        try {
            Registry registry = getRegistry();
            if (registry == null) {
                throw new RuntimeException("Registry is null");
            }
            if (!registry.resourceExists(template)) {
                throw new FileNotFoundException("Template is not found");
            }
            // Read Template
            String templateContent = RegistryUtils.decodeBytes((byte[]) registry.get(template).getContent());
            //Read html file
            if (HTML.equalsIgnoreCase(type)) {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(templateContent)));
                //Find images and embed them
                NodeList nodes = doc.getElementsByTagName(IMAGE);
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node image = nodes.item(i);
                    NodeList list = image.getChildNodes();
                    for (int j = 0; j != list.getLength(); ++j) {
                        Node child = list.item(j);
                        if (IMAGE_EXPRESSION.equals(child.getNodeName()) && child.getTextContent() != null) {
                            String imgUrlStr = child.getTextContent().trim().replaceAll("^\"|\"$", "");
                            //Get image extension
                            String imageExtension = imgUrlStr.substring(imgUrlStr.lastIndexOf(".") + 1);

                            byte[] imageBytes = convertInputStream(imgUrlStr);
                            Element imgExp = doc.createElement(IMAGE_EXPRESSION);
                            String strBuilder = "\"data:image/" + imageExtension + ";base64," +
                                    new String(Base64.encodeBase64(imageBytes)) + "\"";
                            imgExp.appendChild(doc.createCDATASection(strBuilder));
                            imgExp.setAttribute(CLASS, String.class.getName());
                            image.replaceChild(imgExp, child);
                        }
                    }
                }
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                DOMSource source = new DOMSource(doc);

                StringWriter outWriter = new StringWriter();
                StreamResult result = new StreamResult(outWriter);

                transformer.transform(source, result);
                StringBuffer stringBuffer = outWriter.getBuffer();
                templateContent = stringBuffer.toString();
                stringBuffer.delete(0, stringBuffer.length());
            }
            // Create Report Bean Collection
            GenericArtifactManager genericArtifactManager = new GenericArtifactManager(registry, "restservice");
            List<ReportBean> beanList = new LinkedList<ReportBean>();
            for(GenericArtifact genericArtifact:genericArtifactManager.getAllGenericArtifacts()){
                beanList.add(new ReportBean(genericArtifact.getAttribute("overview_name"),
                        genericArtifact.getAttribute("overview_version"),
                        genericArtifact.getAttribute("overview_description")));
            }
            // Return Report Stream as a ByteArrayOutputStream
            return new ReportStream().getReportStream(new JasperPrintProvider()
                    .createJasperPrint(new JRBeanCollectionDataSource(beanList), templateContent,
                            new ReportParamMap[0]), type);

        } catch (RuntimeException e) {
            throw new IOException("Failed to get input stream", e);
        } catch (RegistryException e) {
            throw new IOException("Failed to find report template", e);
        } catch (TransformerException e) {
            throw new IOException("Failed to transform file", e);
        } catch (JRException e) {
            throw new IOException("Failed to create jasperprint", e);
        } catch (ReportingException e) {
            throw new IOException("Failed to create jasperprint", e);
        } catch (ParserConfigurationException e) {
            throw new IOException("Failed to create DocumentBuilder", e);
        } catch (SAXException e) {
            throw new IOException("Failed to parse inputSource", e);
        }
    }

    private byte[] convertInputStream(String imageUrlString) throws IOException {
        URL url = new URL(imageUrlString);
        InputStream inputStream = null;
        byte[] imageBytes;
        try {
            inputStream = url.openStream();
            imageBytes = IOUtils.toByteArray(inputStream);
            return imageBytes;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public static class ReportBean {
        private String overview_name;
        private String overview_version;
        private String overview_description;

        public ReportBean(String overview_name, String overview_version,
                          String overview_description) {
            this.overview_name = overview_name;
            this.overview_version = overview_version;
            this.overview_description = overview_description;
        }

        public String getOverview_name() {
            return overview_name;
        }

        public String getOverview_version() { return overview_version; }

        public String getOverview_description() {
            return overview_description;
        }
    }
}

