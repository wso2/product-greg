/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.registry.es.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;

import javax.mail.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmailUtil {

    private static final Log log = LogFactory.getLog(EmailUtil.class);
    private String emailAddress;
    private static char[] emailPassword;
    private HttpClient httpClient;
    private String pointBrowserURL;
    private String loginURL;
    private String userName;
    private String password;
    private List<NameValuePair> urlParameters = new ArrayList<>();
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";

    public EmailUtil(String URL, String user, String UserPassword) throws XPathExpressionException {
        emailAddress = "gregtestes@gmail.com";
        emailPassword = new char[] { 'g', 'r', 'e', 'g', '1', '2', '3', '4' };
        loginURL = URL;
        userName = user;
        password = UserPassword;
        DefaultHttpClient client = new DefaultHttpClient();

        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        Scheme sch = new Scheme("https", 443, socketFactory);
        ClientConnectionManager mgr = client.getConnectionManager();
        mgr.getSchemeRegistry().register(sch);
        httpClient = new DefaultHttpClient(mgr, client.getParams());

        // Set verifier
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }

    public String readGmailInboxForVerification() throws Exception {
        String pointBrowserURL = "";
        Properties props = new Properties();
        props.load(new FileInputStream(new File(
                TestConfigurationProvider.getResourceLocation("GREG") + File.separator + "axis2" + File.separator
                        + "smtp.properties")));
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect("smtp.gmail.com", emailAddress, java.nio.CharBuffer.wrap(emailPassword).toString());

        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_WRITE);
        Thread.sleep(5000);

        int messageCount = inbox.getMessageCount();
        log.info("Total Messages:- " + messageCount);
        Message[] messages = inbox.getMessages();

        for (Message message : messages) {
            log.info("Mail Subject:- " + message.getSubject());
            if (message.getSubject().contains("EmailVerification")) {
                pointBrowserURL = getBodyFromMessage(message);
            }

            // Optional : deleting the inbox resource updated mail
            message.setFlag(Flags.Flag.DELETED, true);
        }
        inbox.close(true);
        store.close();
        return pointBrowserURL;
    }

    public boolean readGmailInboxForNotification(String notificationType) throws Exception {
        boolean isNotificationMailAvailable = false;
        Properties props = new Properties();
        props.load(new FileInputStream(new File(
                TestConfigurationProvider.getResourceLocation("GREG") + File.separator + "axis2" + File.separator
                        + "smtp.properties")));
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect("smtp.gmail.com", emailAddress, java.nio.CharBuffer.wrap(emailPassword).toString());

        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_WRITE);
        Thread.sleep(10000);

        int messageCount = inbox.getMessageCount();
        log.info("Total Messages:- " + messageCount);
        Message[] messages = inbox.getMessages();

        for (Message message : messages) {
            log.info("Mail Subject:- " + message.getSubject());

            if (message.getSubject().contains(notificationType)) {
                isNotificationMailAvailable = true;

            }
            // Optional : deleting the inbox resource updated mail
            message.setFlag(Flags.Flag.DELETED, true);

        }
        inbox.close(true);
        store.close();
        return isNotificationMailAvailable;
    }

    public void browserRedirectionOnVerification(String pointBrowserURL) throws Exception {

        pointBrowserURL = replaceIP(pointBrowserURL);
        HttpResponse verificationUrlResponse = sendGetRequest(String.format(pointBrowserURL));

        EntityUtils.consume(verificationUrlResponse.getEntity());

        urlParameters.clear();
        urlParameters.add(new BasicNameValuePair("username", userName));
        urlParameters.add(new BasicNameValuePair("password", password));

        HttpResponse loginResponse = sendPOSTMessage(loginURL + "admin/login.jsp", urlParameters);
        EntityUtils.consume(loginResponse.getEntity());

        HttpResponse reDirectionResponse = sendPOSTMessage(loginURL + "admin/login_action.jsp", urlParameters);
        String redirectionUrl = locationHeader(reDirectionResponse);
        EntityUtils.consume(reDirectionResponse.getEntity());

        HttpResponse newReDirectionResponse = sendGetRequest(String.format(redirectionUrl));
        EntityUtils.consume(newReDirectionResponse.getEntity());

        HttpResponse verificationConfirmationResponse = sendGetRequest(
                String.format(loginURL + "email-verification/validator_ajaxprocessor.jsp?confirmation=" +
                        pointBrowserURL.split("confirmation=")[1].split("&")[0]));
        EntityUtils.consume(verificationConfirmationResponse.getEntity());

        String newRedirectionUrl = locationHeader(reDirectionResponse);

        HttpResponse confirmationSuccessResponse = sendGetRequest(String.format(newRedirectionUrl));
        EntityUtils.consume(confirmationSuccessResponse.getEntity());

        log.info("Your email has been confirmed successfully");

    }

    private String replaceIP(String pointBrowserURL) {
        String IPAddressPattern = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
        pointBrowserURL = pointBrowserURL.replaceAll(IPAddressPattern, "localhost");
        return pointBrowserURL;
    }

    private String locationHeader(HttpResponse response) {

        org.apache.http.Header[] headers = response.getAllHeaders();
        String url = null;
        for (org.apache.http.Header header : headers) {
            if ("Location".equals(header.getName())) {
                url = header.getValue();
                break;
            }
        }
        return url;
    }

    private HttpResponse sendGetRequest(String url) throws IOException {
        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        return httpClient.execute(request);
    }

    private HttpResponse sendPOSTMessage(String url, List<NameValuePair> urlParameters) throws Exception {
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", USER_AGENT);
        post.addHeader("Referer", url);
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    private String getBodyFromMessage(Message message) throws IOException, MessagingException {
        if (message.isMimeType("text/plain")) {
            String[] arr = message.getContent().toString().split("\\r?\\n");
            for (int x = 0; x <= arr.length; x++) {
                if (arr[x].contains("https://")) {
                    return arr[x];
                }
            }

        }
        return "";
    }
}
