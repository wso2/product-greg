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

public class ProjectReportGenerator extends AbstractReportGenerator {
    private Log log = LogFactory.getLog(ProjectReportGenerator.class);
    private String foo;
    private String bar;

    @Property(mandatory=true)
    public void setFoo(String foo) {
        this.foo = foo;
    }

    @Property(mandatory=true)
    public void setBar(String bar) {
        this.bar = bar;
    }

    public ByteArrayOutputStream execute(String template, String type)
            throws IOException {
        Registry governanceRegistry;
        try {
            Registry registry = getRegistry();
            governanceRegistry = GovernanceUtils.getGovernanceUserRegistry(registry,CurrentSession.getUser());
            GenericArtifactManager manager = new GenericArtifactManager(governanceRegistry, "projects");
            GenericArtifact[] genericArtifacts = manager.getAllGenericArtifacts();

            List<ProjectReportBean> beanList = new LinkedList<ProjectReportBean>();
            for(GenericArtifact artifact : genericArtifacts){
                ProjectReportBean bean = new ProjectReportBean();
                String[] attributeKeys = artifact.getAttributeKeys();
                for(String key : attributeKeys){
                    String value = artifact.getAttribute(key);
                    if (key.equals("overview_name")) {
                        bean.setOverview_name(value);
                    } else if (key.equals("overview_projectManager")) {
                        bean.setOverview_projectManager(value);
                    } else if (key.equals("overview_description")) {
                        bean.setOverview_description(value);
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
    public static class ProjectReportBean {
        private String overview_name;
        private String overview_projectManager;
        private String overview_description;

        public String getOverview_name() {
            return overview_name;
        }

        public void setOverview_name(String overview_name) {
            this.overview_name = overview_name;
        }

        public String getOverview_projectManager() {
            return overview_projectManager;
        }

        public void setOverview_projectManager(String overview_projectManager) {
            this.overview_projectManager = overview_projectManager;
        }

        public String getOverview_description() {
            return overview_description;
        }

        public void setOverview_description(String overview_description) {
            this.overview_description = overview_description;
        }
    }
}
