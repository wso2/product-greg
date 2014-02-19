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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class OrganizationReportGenerator extends AbstractReportGenerator{
    private Log log = LogFactory.getLog(OrganizationReportGenerator.class);
    private String foo;

    @Property(mandatory=false)
    public void setFoo(String foo) {
        this.foo = foo;
    }

    public ByteArrayOutputStream execute(String template, String type)
            throws IOException {
        Registry governanceRegistry;

        try {
            Registry registry = getRegistry();
            governanceRegistry = GovernanceUtils.getGovernanceUserRegistry(registry,CurrentSession.getUser());
            GenericArtifactManager manager = new GenericArtifactManager(governanceRegistry, "organizations");
            GenericArtifact[] genericArtifacts = manager.getAllGenericArtifacts();

            List<ProcessReportBean> beanList = new LinkedList<ProcessReportBean>();
            for(GenericArtifact artifact : genericArtifacts){
                ProcessReportBean bean = new ProcessReportBean();
                String[] attributeKeys = artifact.getAttributeKeys();
                List<String> departments = new ArrayList<String>();
                for(String key : attributeKeys){
                    String value = artifact.getAttribute(key);
                    if (key.equals("overview_name")) {
                        bean.setOverview_name(value);
                    } else if (key.equals("overview_president")) {
                        bean.setOverview_president(value);
                    } else if (key.equals("overview_visePresident")) {
                        bean.setOverview_visePresident(value);
                    } else if (key.equals("departments_department1")) {
                        departments.add(value);
                    } else if (key.equals("departments_department2")) {
                        departments.add(value);
                    } else if (key.equals("departments_department3")) {
                        departments.add(value);
                    }
                }
                bean.setDepartments(departments.toString().replaceAll("[ \\[\\] ]", ""));
                beanList.add(bean);
            }

            String templateContent = new String((byte []) registry.get(template).getContent());

            JRDataSource dataSource = new JRBeanCollectionDataSource(beanList);
            JasperPrint print = new JasperPrintProvider().createJasperPrint(dataSource, templateContent,
                    new ReportParamMap[0]);
            return new ReportStream().getReportStream(print,type.toLowerCase());

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

        private String overview_name;
        private String overview_president;
        private String overview_visePresident;
        private String departments;

        public String getOverview_name() {
            return overview_name;
        }

        public void setOverview_name(String overview_name) {
            this.overview_name = overview_name;
        }

        public String getOverview_president() {
            return overview_president;
        }

        public void setOverview_president(String overview_president) {
            this.overview_president = overview_president;
        }

        public String getOverview_visePresident() {
            return overview_visePresident;
        }

        public void setOverview_visePresident(String overview_visePresident) {
            this.overview_visePresident = overview_visePresident;
        }

        public String getDepartments() {
            return departments;
        }

        public void setDepartments(String departments) {
            this.departments = departments;
        }
    }
}
