/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.registry.ws.client.test.general.old;

import org.apache.axis2.AxisFault;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import static org.testng.Assert.fail;


/**
 * A test case which tests registry remote performance
 */
public class RemotePerfWSTestCase {

    private static int concurrentUsers = 10;
    private static int iterationsNumber = 10;
    private static WSRegistryServiceClient registry;


    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        int userId = 0;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

    }

    @Test(groups = {"wso2.greg"})
    public void runSuccessCase() {

        try {
            worker1();
            worker2();
            worker3();
            worker4();
            worker5();
        } catch (Exception e) {
            e.printStackTrace();
            fail("The Remote Perf Test for WS-API failed");
        }
    }

    private static void worker1() throws Exception {

        int numUsers = concurrentUsers;

        Worker[] workers = new Worker[numUsers];
        for (int i = 0; i < numUsers; i++) {

            Worker worker = new Worker1("T" + i, iterationsNumber, registry);
            workers[i] = worker;
        }

        long time1 = System.nanoTime();

        for (int i = 0; i < numUsers; i++) {
            workers[i].start();
        }

        for (int i = 0; i < numUsers; i++) {
            workers[i].join();
        }

        long time2 = System.nanoTime();
        long elapsedTime = time2 - time1;
        System.out.println("Time taken for the whole test: " + elapsedTime / 1000000 + " ms");
    }

    private static void worker2() throws Exception {

        int numUsers = concurrentUsers;

        Worker[] workers = new Worker[numUsers];
        for (int i = 0; i < numUsers; i++) {
            Worker worker = new Worker2("T" + i, iterationsNumber, registry);
            workers[i] = worker;
        }

        long time1 = System.nanoTime();

        for (int i = 0; i < numUsers; i++) {
            workers[i].start();
        }

        for (int i = 0; i < numUsers; i++) {
            workers[i].join();
        }

        long time2 = System.nanoTime();
        long elapsedTime = time2 - time1;
        System.out.println("Time taken for the whole test: " + elapsedTime / 1000000 + " ms");
    }

    private static void worker3() throws Exception {

        int numUsers = concurrentUsers;

        Worker[] workers = new Worker[numUsers];
        for (int i = 0; i < numUsers; i++) {
            Worker worker = new Worker3("T" + i, iterationsNumber, registry);
            workers[i] = worker;
        }

        long time1 = System.nanoTime();

        for (int i = 0; i < numUsers; i++) {
            workers[i].start();
        }

        for (int i = 0; i < numUsers; i++) {
            workers[i].join();
        }

        long time2 = System.nanoTime();
        long elapsedTime = time2 - time1;
        System.out.println("Time taken for the whole test: " + elapsedTime / 1000000 + " ms");
    }

    private static void worker4() throws Exception {

        int numUsers = concurrentUsers;

        Worker[] workers = new Worker[numUsers];
        for (int i = 0; i < numUsers; i++) {
            Worker worker = new Worker4("T" + i, iterationsNumber, registry);
            workers[i] = worker;
        }

        long time1 = System.nanoTime();

        for (int i = 0; i < numUsers; i++) {
            workers[i].start();
        }

        for (int i = 0; i < numUsers; i++) {
            workers[i].join();
        }

        long time2 = System.nanoTime();
        long elapsedTime = time2 - time1;
        System.out.println("Time taken for the whole test: " + elapsedTime / 1000000 + " ms");
    }

    private static void worker5() throws Exception {

        int numUsers = concurrentUsers;

        Worker[] workers = new Worker[numUsers];
        for (int i = 0; i < numUsers; i++) {
            Worker worker = new Worker5("T" + i, iterationsNumber, registry);
            workers[i] = worker;
        }

        long time1 = System.nanoTime();

        for (int i = 0; i < numUsers; i++) {
            workers[i].start();
        }

        for (int i = 0; i < numUsers; i++) {
            workers[i].join();
        }

        long time2 = System.nanoTime();
        long elapsedTime = time2 - time1;
        System.out.println("Time taken for the whole test: " + elapsedTime / 1000000 + " ms");
    }

}
