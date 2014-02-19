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
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
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

public class TestSuiteReportGenerator extends AbstractReportGenerator {
    private Log log = LogFactory.getLog(TestSuiteReportGenerator.class);
    private String foo;
    private String bar;

    @Property(mandatory=true)
    public void setFoo(String foo) {
        this.foo = foo;
    }

    @Property(mandatory=false)
    public void setBar(String bar) {
        this.bar = bar;
    }

    public ByteArrayOutputStream execute(String template, String type)
            throws IOException {
        Registry governanceRegistry;

        try {
            Registry registry = getRegistry();
            governanceRegistry = GovernanceUtils.getGovernanceUserRegistry(registry,CurrentSession.getUser());
            GenericArtifactManager suiteArtifactManager = new GenericArtifactManager(governanceRegistry, "suites");
            GenericArtifact[] genericArtifacts = suiteArtifactManager.getAllGenericArtifacts();
            GenericArtifactManager caseArtifactManager = new GenericArtifactManager(governanceRegistry, "case");
            List<TestSuiteReportBean> beanList = new LinkedList<TestSuiteReportBean>();
            for(GenericArtifact artifact : genericArtifacts){
                TestSuiteReportBean bean = new TestSuiteReportBean();
                String[] attributeKeys = artifact.getAttributeKeys();
                List<String> testCases = new ArrayList<String>();
                for(String key : attributeKeys){
                    if (key.equals("overview_name")) {
                        String value = artifact.getAttribute(key);
                        bean.setOverview_name(value);
                    } else if (key.equals("overview_version")) {
                        String value = artifact.getAttribute(key);
                        bean.setOverview_version(value);
                    } else if (key.equals("overview_type")) {
                        String value = artifact.getAttribute(key);
                        bean.setOverview_type(value);
                    } if (key.equals("testCases_entry")) {
                        String[] values = artifact.getAttributes(key);
                        for(String value : values){
                            Resource r = registry.get(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + value.split(":")[1]);
                            GenericArtifact a = caseArtifactManager.getGenericArtifact(r.getUUID());
                            testCases.add(a.getAttribute("overview_name"));
                        }
                    }
                }
                bean.setTestCases_entry(testCases.toString().replaceAll("[ \\[\\] ]", ""));
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
    public static class TestSuiteReportBean {
        String overview_name;
        String overview_version;
        String overview_type;
        String testCases_entry;

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

        public String getOverview_type() {
            return overview_type;
        }

        public void setOverview_type(String overview_type) {
            this.overview_type = overview_type;
        }

        public String getTestCases_entry() {
            return testCases_entry;
        }

        public void setTestCases_entry(String testCases_entry) {
            this.testCases_entry = testCases_entry;
        }
    }
}
