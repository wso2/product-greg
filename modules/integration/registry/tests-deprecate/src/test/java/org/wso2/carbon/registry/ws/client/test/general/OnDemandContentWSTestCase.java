package org.wso2.carbon.registry.ws.client.test.general;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.ws.client.resource.OnDemandContentResourceImpl;

import static org.testng.Assert.*;

/**
 * A test case which tests registry on demand content
 */
public class OnDemandContentWSTestCase extends TestSetup {

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() {
        super.init();
    }

    @Test(groups = {"wso2.greg"})
    public void runSuccessCase() {
        onDemandContent();
        try {

        } catch (Exception e) {
            e.printStackTrace();
            fail("The OnDemand Content Test for WS-API failed");
        }
    }

    private void onDemandContent() {
        try {
            String testPath = "ondemand/test";
            Resource r1 = registry.newResource();
            r1.setContent("This is test content. It should not be loaded unless getContent() is called.".getBytes());
            registry.put(testPath, r1);

            OnDemandContentResourceImpl r1_get = (OnDemandContentResourceImpl) registry.get(testPath);
            r1_get.setClient(null);
            Object content = null;
            try {
                content = r1_get.getContent();
                assertNull(content, "Resource content should not exist");
                fail("Content has not been pre-fetched, not on demand");
            } catch (Exception ignored) {

            }

            Resource r1_get2 = registry.get(testPath);
            content = r1_get2.getContent();
            assertNotNull(content, "Resource content should be fetched on demand");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unable to fetch content on demand");
        }
    }
}
