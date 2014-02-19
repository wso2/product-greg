package org.wso2.carbon.registry.ws.client.test.general.old;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.registry.ws.client.resource.OnDemandContentResourceImpl;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

/**
 * A test case which tests registry on demand content
 */
public class OnDemandContentWSTestCase {

    private WSRegistryServiceClient registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        int userId = 0;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

    }

    @Test(groups = {"wso2.greg"})
    public void runSuccessCase() {

        try {
            onDemandContent();
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
            Object content;
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


    @AfterClass
    public void cleanup() throws RegistryException {
        registry.delete("/ondemand");
    }
}
