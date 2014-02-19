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

package org.wso2.carbon.registry.ws.client.test.general;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.core.Resource;

import static org.testng.Assert.*;

/**
 * A test case which tests registry rating operation
 */
public class RatingWSTestCase extends TestSetup {


    @BeforeClass(groups = {"wso2.greg"})
    public void initTassertEest() {
        super.init();
    }

    @Test(groups = {"wso2.greg"})
    public void addResourceRating() throws Exception {
        Resource r1 = registry.newResource();
        byte[] r1content = "R1 content".getBytes();
        r1.setContent(r1content);

        registry.put("/d16/d17/r1", r1);

        registry.rateResource("/d16/d17/r1", 5);

        float rating = registry.getAverageRating("/d16/d17/r1");

        assertEquals(rating, (float) 5.0, (float) 0.01, "Rating of the resource /d16/d17/r1 should be 5.");
    }

    @Test(groups = {"wso2.greg"})
    public void addCollectionRating() throws Exception {
        Resource r1 = registry.newCollection();

        registry.put("/d16/d18", r1);
        registry.rateResource("/d16/d18", 4);

        float rating = registry.getAverageRating("/d16/d18");

        assertEquals(rating, (float) 4.0,
                (float) 0.01, "Rating of the resource /d16/d18 should be 5.");
    }

    @Test(groups = {"wso2.greg"})
    private void editResourceRating() throws Exception {
        Resource r1 = registry.newResource();
        byte[] r1content = "R1 content".getBytes();
        r1.setContent(r1content);

        registry.put("/d61/d17/d18/r1", r1);
        registry.rateResource("/d61/d17/d18/r1", 5);

        float rating = registry.getAverageRating("/d61/d17/d18/r1");


        assertEquals(rating, (float) 5.0, (float) 0.01, "Rating of the resource /d61/d17/d18/r1 should be 5.");

        /*rate the same resource again*/

        registry.rateResource("/d61/d17/d18/r1", 3);

        float rating_edit = registry.getAverageRating("/d61/d17/d18/r1");

        assertEquals(rating_edit, (float) 3.0, (float) 0.01,
                "Rating of the resource /d61/d17/d18/r1 should be 3.");
    }

    @Test(groups = {"wso2.greg"})
    private void ratingsPath() throws Exception {
        Resource r5 = registry.newResource();
        String r5Content = "this is r5 content";
        r5.setContent(r5Content.getBytes());
        r5.setDescription("production ready.");
        String r5Path = "/c1/r5";

        registry.put(r5Path, r5);

        registry.rateResource("/c1/r5", 3);

        String[] ratingPaths;
        Resource ratings = registry.get("/c1/r5;ratings");
        ratingPaths = (String[]) ratings.getContent();
        int rating;
        Resource c1 = registry.get(ratingPaths[0]);

        Object o = c1.getContent();
        if (o instanceof Integer) {
            rating = (Integer) o;
        } else {
            rating = Integer.parseInt(o.toString());
        }

        assertEquals(rating, 3, "Ratings are not retrieved properly as resources.");


    }


}
