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
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

/**
 * A test case which tests registry comment operation
 */
public class CommentWSTestCase extends TestSetup {

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() {
        super.init();
    }

    @Test(groups = {"wso2.greg"})
    public void addComment() throws Exception {
        Resource r1 = registry.newResource();
        String path = "/d112/r3";
        byte[] r1content = "R1 content".getBytes();
        r1.setContent(r1content);
        registry.put(path, r1);

        String comment1 = "this is qa comment 4";
        String comment2 = "this is qa comment 5";
        Comment c1 = new Comment();
        c1.setResourcePath(path);
        c1.setText("This is default comment");
        c1.setUser("admin1");

        registry.addComment(path, c1);
        registry.addComment(path, new Comment(comment1));
        registry.addComment(path, new Comment(comment2));

        Comment[] comments = registry.getComments(path);

        boolean commentFound = false;

        for (Comment comment : comments) {
            if (comment.getText().equals(comment1)) {
                commentFound = true;
                //System.out.println(comment.getPath());
                assertEquals(comment.getText(), comment1);
                assertEquals(comment.getUser(), "admin");
                assertEquals(comment.getResourcePath(), path);
                //System.out.println(comment.getPath());
                //break;
            }

            if (comment.getText().equals(comment2)) {
                commentFound = true;
                assertEquals(comment.getText(), comment2);
                assertEquals(comment.getUser(), "admin");
                assertEquals(comment.getResourcePath(), path);
                //break;
            }

            if (comment.getText().equals("This is default comment")) {
                commentFound = true;
                assertEquals(comment.getText(), "This is default comment");
                assertEquals(comment.getUser(), "admin");
                //break;
            }
        }


        assertTrue(commentFound, "comment '" + comment1 +
                " is not associated with the artifact /d1/r3");

        Resource commentsResource = registry.get("/d112/r3;comments");
        assertTrue(commentsResource instanceof Collection, "Comment collection resource should be a directory.");
        comments = (Comment[]) commentsResource.getContent();

        List commentTexts = new ArrayList();
        for (Comment comment : comments) {
            Resource commentResource = registry.get(comment.getPath());
            commentTexts.add(commentResource.getContent());
        }

        assertTrue(commentTexts.contains(comment1), comment1 + " is not associated for resource /d112/r3.");
        assertTrue(commentTexts.contains(comment2), comment2 + " is not associated for resource /d112/r3.");
    }

    @Test(groups = {"wso2.greg"})
    public void addCommentToResource() throws Exception {
        Resource r1 = registry.newResource();
        byte[] r1content = "R1 content".getBytes();
        r1.setContent(r1content);
        registry.put("/d1/r3", r1);

        String comment1 = "this is qa comment 4";
        String comment2 = "this is qa comment 5";
        Comment c1 = new Comment();
        c1.setResourcePath("/d1/r3");
        c1.setText("This is default comment");
        c1.setUser("admin");

        registry.addComment("/d1/r3", c1);
        registry.addComment("/d1/r3", new Comment(comment1));
        registry.addComment("/d1/r3", new Comment(comment2));

        Comment[] comments = registry.getComments("/d1/r3");

        boolean commentFound = false;

        for (Comment comment : comments) {
            if (comment.getText().equals(comment1)) {
                commentFound = true;

//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
                //break;
            }

            if (comment.getText().equals(comment2)) {
                commentFound = true;
//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
                //break;
            }

            if (comment.getText().equals("This is default comment")) {
                commentFound = true;
//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
                //break;
            }
        }


        assertTrue(commentFound, "comment '" + comment1 +
                " is not associated with the artifact /d1/r3");

        Resource commentsResource = registry.get("/d1/r3;comments");
        assertTrue(commentsResource instanceof Collection, "Comment collection resource should be a directory.");
        comments = (Comment[]) commentsResource.getContent();

        List commentTexts = new ArrayList();
        for (Comment comment : comments) {
            Resource commentResource = registry.get(comment.getPath());
            commentTexts.add(commentResource.getContent());
        }

        assertTrue(commentTexts.contains(comment1), comment1 + " is not associated for resource /d1/r3.");
        assertTrue(commentTexts.contains(comment2), comment2 + " is not associated for resource /d1/r3.");

        /*try {
            //registry.delete("/d12");
        } catch (RegistryException e) {
            fail("Failed to delete test resources.");
        } */
    }

    @Test(groups = {"wso2.greg"})
    public void addCommentToCollection() throws Exception {

        Resource r1 = registry.newCollection();
        r1.setDescription("this is a collection to add comment");

        registry.put("/d11/d12", r1);

        String comment1 = "this is qa comment 1 for collection d12";
        String comment2 = "this is qa comment 2 for collection d12";

        Comment c1 = new Comment();
        c1.setResourcePath("/d11/d12");
        c1.setText("This is default comment for d12");
        c1.setUser("admin");

        try {
            registry.addComment("/d11/d12", c1);
            registry.addComment("/d11/d12", new Comment(comment1));
            registry.addComment("/d11/d12", new Comment(comment2));
        } catch (RegistryException e) {
            fail("Valid commenting for resources scenario failed");
        }

        Comment[] comments = null;

        try {

            comments = registry.getComments("/d11/d12");
        } catch (RegistryException e) {
            fail("Failed to get comments for the resource /d11/d12");
        }

        boolean commentFound = false;

        for (Comment comment : comments) {
            if (comment.getText().equals(comment1)) {
                commentFound = true;

//       //System.out.println(comment.getText());
//       //System.out.println(comment.getResourcePath());
//       //System.out.println(comment.getUser());
//       //System.out.println(comment.getTime());
                //break;
            }

            if (comment.getText().equals(comment2)) {
                commentFound = true;
//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
                //break;
            }

            if (comment.getText().equals(c1.getText())) {
                commentFound = true;
//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
                //break;
            }
        }

        assertTrue(commentFound, "comment '" + comment1 +
                " is not associated with the artifact /d11/d12");

        try {

            Resource commentsResource = registry.get("/d11/d12;comments");
            assertTrue(commentsResource instanceof Collection,
                    "Comment collection resource should be a directory.");
            comments = (Comment[]) commentsResource.getContent();

            List commentTexts = new ArrayList();
            for (Comment comment : comments) {
                Resource commentResource = registry.get(comment.getPath());
                commentTexts.add(commentResource.getContent());
            }

            assertTrue(commentTexts.contains(comment1),
                    comment1 + " is not associated for resource /d11/d12.");
            assertTrue(commentTexts.contains(comment2),
                    comment2 + " is not associated for resource /d11/d12.");

        } catch (RegistryException e) {
            e.printStackTrace();
            fail("Failed to get comments form URL: /d11/d12;comments");
        }

/*try {
   //registry.delete("/d12");
} catch (RegistryException e) {
   fail("Failed to delete test resources.");
} */


    }

    @Test(groups = {"wso2.greg"})
    public void addCommenttoRoot() throws Exception {

        String comment1 = "this is qa comment 1 for root";
        String comment2 = "this is qa comment 2 for root";


        Comment c1 = new Comment();
        c1.setResourcePath("/");
        c1.setText("This is default comment for root");
        c1.setUser("admin");

        try {
            registry.addComment("/", c1);
            registry.addComment("/", new Comment(comment1));
            registry.addComment("/", new Comment(comment2));
        } catch (RegistryException e) {
            fail("Valid commenting for resources scenario failed");
        }

        Comment[] comments = null;

        try {

            comments = registry.getComments("/");
        } catch (RegistryException e) {
            fail("Failed to get comments for the resource /");
        }

        boolean commentFound = false;

        for (Comment comment : comments) {
            if (comment.getText().equals(comment1)) {
                commentFound = true;

//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
//                //System.out.println("\n");
                //break;
            }

            if (comment.getText().equals(comment2)) {
                commentFound = true;
//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
//                //System.out.println("\n");
                //break;
            }

            if (comment.getText().equals(c1.getText())) {
                commentFound = true;
//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
//                //System.out.println("\n");
                //break;
            }
        }

        assertTrue(commentFound, "comment '" + comment1 +
                " is not associated with the artifact /");

        try {

            Resource commentsResource = registry.get("/;comments");
            assertTrue(commentsResource instanceof Collection,
                    "Comment collection resource should be a directory.");
            comments = (Comment[]) commentsResource.getContent();

            List commentTexts = new ArrayList();
            for (Comment comment : comments) {
                Resource commentResource = registry.get(comment.getPath());
                commentTexts.add(commentResource.getContent());
            }

            assertTrue(commentTexts.contains(comment1),
                    comment1 + " is not associated for resource /.");
            assertTrue(commentTexts.contains(comment2),
                    comment2 + " is not associated for resource /.");

        } catch (RegistryException e) {
            fail("Failed to get comments form URL: /;comments");
        }

/*try {
   //registry.delete("/d12");
} catch (RegistryException e) {
   fail("Failed to delete test resources.");
} */

    }

    @Test(groups = {"wso2.greg"})
    public void editComment() throws Exception {
        Resource r1 = registry.newResource();
        byte[] r1content = "R1 content".getBytes();
        r1.setContent(r1content);
        r1.setDescription("this is a resource to edit comment");
        registry.put("/c101/c11/r1", r1);

        Comment c1 = new Comment();
        c1.setResourcePath("/c10/c11/r1");
        c1.setText("This is default comment ");
        c1.setUser("admin");

        String commentPath = registry.addComment("/c101/c11/r1", c1);

        Comment[] comments = registry.getComments("/c101/c11/r1");

        boolean commentFound = false;

        for (Comment comment : comments) {
            if (comment.getText().equals(c1.getText())) {
                commentFound = true;
//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
//                //System.out.println("\n");
                //break;
            }
        }

        assertTrue(commentFound, "comment:" + c1.getText() +
                " is not associated with the artifact /c101/c11/r1");

        try {

            Resource commentsResource = registry.get("/c101/c11/r1;comments");

            assertTrue(commentsResource instanceof Collection, "Comment resource should be a directory.");
            comments = (Comment[]) commentsResource.getContent();

            List commentTexts = new ArrayList();
            for (Comment comment : comments) {
                Resource commentResource = registry.get(comment.getPath());
                commentTexts.add(commentResource.getContent());
            }

            assertTrue(commentTexts.contains(c1.getText()),
                    c1.getText() + " is not associated for resource /c101/c11/r1.");
            registry.editComment(comments[0].getPath(), "This is the edited comment");
            comments = registry.getComments("/c101/c11/r1");
//            System.out.println(comments);
            Resource resource = registry.get(comments[0].getPath());
            assertEquals(resource.getContent(), "This is the edited comment");
        } catch (RegistryException e) {
            e.printStackTrace();
            fail("Failed to get comments form URL:/c101/c11/r1;comments");
        }

        /*Edit comment goes here*/
        String editedCommentString = "This is the edited comment";
        registry.editComment(commentPath, editedCommentString);
        Comment[] comments1 = registry.getComments("/c101/c11/r1");

        boolean editedCommentFound = false;
        boolean defaultCommentFound = true;

        for (Comment comment : comments1) {
            if (comment.getText().equals(editedCommentString)) {
                editedCommentFound = true;
            } else if (comment.getText().equals(c1.getText())) {
                defaultCommentFound = false;
//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
//                //System.out.println("\n");
                //break;
            }
        }
        assertTrue(editedCommentFound, "comment:" + editedCommentString +
                " is not associated with the artifact /c101/c11/r1");

    }

    @Test(groups = {"wso2.greg"})
    public void commentDelete() throws Exception {
        String r1Path = "/c1d1/c1";
        clearResource(r1Path);
        Collection r1 = registry.newCollection();
        registry.put(r1Path, r1);

        String c1Path = registry.addComment(r1Path, new Comment("test comment1"));
        registry.addComment(r1Path, new Comment("test comment2"));

        Comment[] comments1 = registry.getComments(r1Path);

        assertEquals(comments1.length, 2, "There should be two comments.");

        String[] cTexts1 = {comments1[0].getText(), comments1[1].getText()};

        assertTrue(containsString(cTexts1, "test comment1"), "comment is missing");
        assertTrue(containsString(cTexts1, "test comment2"), "comment is missing");

        registry.delete(c1Path);

        Comment[] comments2 = registry.getComments(r1Path);

        assertEquals(comments2.length, 1, "There should be one comment.");

        String[] cTexts2 = {comments2[0].getText()};

        assertTrue(containsString(cTexts2, "test comment2"), "comment is missing");
        assertTrue(!containsString(cTexts2, "test comment1"), "deleted comment still exists");
    }

    private boolean containsString(String[] array, String value) {

        boolean found = false;
        for (String anArray : array) {
            if (anArray.startsWith(value)) {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * this method will clean the given resource if it already exists
     */
    private void clearResource(String path) throws RegistryException {
        if (registry.resourceExists(path)) {
            registry.delete(path);
        }

    }
}
