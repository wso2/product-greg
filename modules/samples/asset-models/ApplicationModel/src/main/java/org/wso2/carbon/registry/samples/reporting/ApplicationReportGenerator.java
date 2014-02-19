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
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.reporting.AbstractReportGenerator;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.util.JasperPrintProvider;
import org.wso2.carbon.reporting.util.ReportParamMap;
import org.wso2.carbon.reporting.util.ReportStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ApplicationReportGenerator extends AbstractReportGenerator{
    private Log log = LogFactory.getLog(ApplicationReportGenerator.class);

    public ByteArrayOutputStream execute(String template, String type)
            throws IOException {
        Registry governanceRegistry;

        try {
            Registry registry = getRegistry();
            governanceRegistry = GovernanceUtils.getGovernanceUserRegistry(registry,CurrentSession.getUser());
            GenericArtifactManager manager = new GenericArtifactManager(governanceRegistry, "applications");
            GenericArtifact[] genericArtifacts = manager.getAllGenericArtifacts();

            List<ApplicationReportBean> beanList = new LinkedList<ApplicationReportBean>();
            for(GenericArtifact artifact : genericArtifacts){
                ApplicationReportBean bean = new ApplicationReportBean();
                String[] attributeKeys = artifact.getAttributeKeys();
                for(String key : attributeKeys){
                    String value = artifact.getAttribute(key);
                    if (key.equals("overview_name")) {
                        bean.setOverview_name(value);
                    } else if (key.equals("overview_version")) {
                        bean.setOverview_version(value);
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
    public static class ApplicationReportBean {
        private String overview_name;
        private String overview_version;
        private String overview_description;

        public String getOverview_name() {
            return overview_name;
        }

        public void setOverview_name(String overview_name) {
            this.overview_name = overview_name;
        }

        public String getOverview_version() {
            return overview_version;
        }

        public void setOverview_version(String overview_version) {
            this.overview_version = overview_version;
        }

        public String getOverview_description() {
            return overview_description;
        }

        public void setOverview_description(String overview_description) {
            this.overview_description = overview_description;
        }
    }
}