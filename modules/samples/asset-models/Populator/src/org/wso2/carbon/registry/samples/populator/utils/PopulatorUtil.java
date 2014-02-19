/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.samples.populator.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean;

import java.io.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Util methods used
 */
public class PopulatorUtil {

    public static void setCookie(Stub stub, String cookie) {
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        if (cookie != null) {
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        }
    }

    /**
     * Authenticate user
     *
     * @param ctx
     * @param serverURL
     * @param username
     * @param password
     * @return
     * @throws AxisFault
     * @throws AuthenticationException
     */
    public static String authenticate(ConfigurationContext ctx,
                                      String serverURL, String username, String password)
            throws AxisFault, AuthenticationException {
        String serviceEPR = serverURL + "AuthenticationAdmin";

        AuthenticationAdminStub stub = new AuthenticationAdminStub(ctx, serviceEPR);
        PopulatorUtil.setCookie(stub, null);
        try {
            boolean result = stub.login(username, password, new URL(serviceEPR).getHost());
            if (result) {
                return (String) stub._getServiceClient().getServiceContext().
                        getProperty(HTTPConstants.COOKIE_STRING);
            }
            return null;
        } catch (Exception e) {
            String msg = "Error occurred while logging in";
            throw new AuthenticationException(msg, e);
        }
    }

    /**
     * Return the reporting configuration bean
     *
     * @param name
     * @param template
     * @param type
     * @param reportClass
     * @return
     */
    public static ReportConfigurationBean getReportConfigurationBean(
            String name, String template, String type, String reportClass) {
        ReportConfigurationBean bean = new ReportConfigurationBean();
        bean.setName(name);
        bean.setTemplate(template);
        bean.setType(type.toLowerCase());
        bean.setReportClass(reportClass);
        return bean;
    }

    /**
     * Create workbook array from files in a given directory
     *
     * @param usersDir
     * @param prefix
     * @return
     */
    public static Workbook[] getWorkbooks(File usersDir, String prefix) {
        List<Workbook> workbooks = new LinkedList<Workbook>();
        FileFilter filter = new PrefixFileFilter(prefix);
        File[] files = usersDir.listFiles(filter);
        for (File file : files) {
            try {
                InputStream ins = new BufferedInputStream(new FileInputStream(files[0]));
                String extension = FilenameUtils.getExtension(files[0].getName());
                if (extension.equals("xlsx")) {
                    workbooks.add(new XSSFWorkbook(ins));
                } else {
                    POIFSFileSystem fs = new POIFSFileSystem(ins);
                    workbooks.add(new HSSFWorkbook(fs));
                }
            } catch (Exception e) {
                throw new RuntimeException("Workbook creation failed", e);
            }
        }
        return workbooks.toArray(new Workbook[workbooks.size()]);
    }

    /**
     * Create a workbook from a file in a given location
     *
     * @param usersDir
     * @param prefix
     * @return
     */
    public static Workbook getWorkbook(File usersDir, String prefix) {
        Workbook[] workbooks = getWorkbooks(usersDir, prefix);
        if (workbooks.length == 0) {
            return null;
        }
        return workbooks[0];
    }

}
