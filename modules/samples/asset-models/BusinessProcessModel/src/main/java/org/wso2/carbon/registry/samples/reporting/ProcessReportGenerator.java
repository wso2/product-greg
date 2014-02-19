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
package org.wso2.carbon.registry.samples.reporting;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.reporting.AbstractReportGenerator;
import org.wso2.carbon.registry.reporting.annotation.Property;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.util.JasperPrintProvider;
import org.wso2.carbon.reporting.util.ReportParamMap;
import org.wso2.carbon.reporting.util.ReportStream;
import org.wso2.carbon.registry.core.session.CurrentSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ProcessReportGenerator extends AbstractReportGenerator {
    private Log log = LogFactory.getLog(ProcessReportGenerator.class);
    private String foo;

    @Property(mandatory=true)
    public void setFoo(String foo) {
        this.foo = foo;
    }

    public ByteArrayOutputStream execute(String template, String type)
            throws IOException {
        Registry governanceRegistry;

        try {
            Registry registry = getRegistry();
            governanceRegistry = GovernanceUtils.getGovernanceUserRegistry(registry,CurrentSession.getUser());
            GenericArtifactManager manager = new GenericArtifactManager(governanceRegistry, "processes");
            GenericArtifact[] genericArtifacts = manager.getAllGenericArtifacts();

            List<ProcessReportBean> beanList = new LinkedList<ProcessReportBean>();
            for(GenericArtifact artifact : genericArtifacts){
                ProcessReportBean bean = new ProcessReportBean();
                String[] attributeKeys = artifact.getAttributeKeys();
                for(String key : attributeKeys){
                    String value = artifact.getAttribute(key);
                    if (key.equals("details_name")) {
                        bean.setDetails_name(value);
                    } else if (key.equals("details_id")) {
                        bean.setDetails_id(value);
                    } else if (key.equals("details_executability")) {
                        bean.setDetails_executability(Boolean.parseBoolean(value));
                    }
                }
                beanList.add(bean);
            }

            String templateContent = new String((byte []) registry.get(template).getContent());

            JRDataSource dataSource = new JRBeanCollectionDataSource(beanList);
            JasperPrint print = new JasperPrintProvider().createJasperPrint(dataSource, templateContent,
                    new ReportParamMap[0]);
            return new ReportStream().getReportStream(print,type);

        } catch (RegistryException e) {
            log.error("Error while getting the Governance Registry", e);
        } catch (JRException e) {
            log.error("Error occured while creating the jasper print ", e);
        } catch (ReportingException e) {
            log.error("Error while generating the report", e);
        }

        return new ByteArrayOutputStream(0);
    }

    @SuppressWarnings("unused")
    public static class ProcessReportBean {

        private String details_name;
        private String details_id;
        private Boolean details_executability;

        public String getDetails_name() {
            return details_name;
        }

        public void setDetails_name(String details_name) {
            this.details_name = details_name;
        }

        public String getDetails_id() {
            return details_id;
        }

        public void setDetails_id(String details_id) {
            this.details_id = details_id;
        }

        public Boolean getDetails_executability() {
            return details_executability;
        }

        public void setDetails_executability(Boolean details_executability) {
            this.details_executability = details_executability;
        }
    }
}
