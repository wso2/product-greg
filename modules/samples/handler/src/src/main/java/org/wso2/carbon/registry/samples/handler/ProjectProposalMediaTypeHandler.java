/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.samples.handler;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import org.wso2.carbon.registry.core.jdbc.handlers.Handler;


import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.util.Iterator;

public class ProjectProposalMediaTypeHandler extends Handler {

	public static final String ENVIRONMENT = "environmental damage accessment";
	public static final String FEASIBILITY = "feasibility study";
	public static final String RESETTLEMENT = "resettlement plan";

	public static final String PROVIDED = "Provided";
	public static final String NOT_PROVIDED = "Not provided";
	public static final String COMPLETE_TAG = "complete";
	public static final String INCOMPLETE_TAG = "incomplete";

	public static final String invalidProposal = "Invalid project proposal. Please make sure that "
			+ "all project proposals confirm to the formal specified in "
			+ "\"http://example.org/deptOfHighways/projectProposal.pdf\".";

	public Resource get(RequestContext requestContext) throws RegistryException {
		return null;
	}

	public void put(RequestContext requestContext) throws RegistryException {
		Resource resource = requestContext.getResource();
		Object o = resource.getContent();
		String resourcePath = requestContext.getResourcePath().getPath();
		Registry registry = requestContext.getRegistry();

		if (o == null || !(o instanceof byte[])) {
			String msg = "Project proposals should have a valid content of type byte[].";
			throw new RegistryException(msg);
		}

		byte[] content = (byte[]) resource.getContent();
		ByteArrayInputStream in = new ByteArrayInputStream(content);
		OMElement docElement;
		try {
			StAXOMBuilder builder = new StAXOMBuilder(in);
			docElement = builder.getDocumentElement();
		} catch (Exception ae) {
			throw new RegistryException(
					"Failed to parse the propject proposal. "
							+ "All project proposals should be in XML format.");
		}

		OMElement nameElement = docElement.getFirstChildWithName(new QName(
				"name"));
		if (nameElement == null) {
			throw new RegistryException(invalidProposal);
		}
		resource.setProperty("Project name", nameElement.getText());

		OMElement descElement = docElement.getFirstChildWithName(new QName(
				"abstract"));
		if (descElement == null) {
			throw new RegistryException(invalidProposal);
		}
		resource.setDescription(descElement.getText());

		OMElement conElement = docElement.getFirstChildWithName(new QName(
				"contractor"));
		if (conElement == null) {
			throw new RegistryException(invalidProposal);
		}
		resource.setProperty("Contractor", conElement.getText());

		OMElement consElement = docElement.getFirstChildWithName(new QName(
				"consultant"));
		if (consElement == null) {
			throw new RegistryException(invalidProposal);
		}
		resource.setProperty("Consultant", consElement.getText());

		OMElement costElement = docElement.getFirstChildWithName(new QName(
				"cost"));
		if (costElement == null) {
			throw new RegistryException(invalidProposal);
		}
		resource.setProperty("Estimated cost", costElement.getText());

		OMElement durElement = docElement.getFirstChildWithName(new QName(
				"duration"));
		if (durElement == null) {
			throw new RegistryException(invalidProposal);
		}
		resource.setProperty("Duration", durElement.getText());

		boolean env = false;
		boolean feasibility = false;
		boolean resettlement = false;
		Iterator reports = docElement.getChildrenWithName(new QName("report"));
		while (reports.hasNext()) {
			OMElement reportElement = (OMElement) reports.next();

			String reportName = reportElement.getAttributeValue(new QName(
					"name"));
			if (reportName != null
					&& ENVIRONMENT.equalsIgnoreCase(reportName.trim())) {
				env = true;
			}

			reportName = reportElement.getAttributeValue(new QName("name"));
			if (reportName != null
					&& FEASIBILITY.equalsIgnoreCase(reportName.trim())) {
				feasibility = true;
			}

			reportName = reportElement.getAttributeValue(new QName("name"));
			if (reportName != null
					&& RESETTLEMENT.equalsIgnoreCase(reportName.trim())) {
				resettlement = true;
			}
		}

		if (env) {
			resource.setProperty(ENVIRONMENT, PROVIDED);
		} else {
			resource.setProperty(ENVIRONMENT, NOT_PROVIDED);
		}

		if (feasibility) {
			resource.setProperty(FEASIBILITY, PROVIDED);
		} else {
			resource.setProperty(FEASIBILITY, NOT_PROVIDED);
		}

		if (resettlement) {
			resource.setProperty(RESETTLEMENT, PROVIDED);
		} else {
			resource.setProperty(RESETTLEMENT, NOT_PROVIDED);
		}

		requestContext.getRepository().put(resourcePath, resource);
		if (env && feasibility && resettlement) {
			registry.applyTag(resourcePath, COMPLETE_TAG);
		} else {
			registry.applyTag(resourcePath, INCOMPLETE_TAG);
		}

		requestContext.setProcessingComplete(true);
	}

	public void importResource(RequestContext requestContext)
			throws RegistryException {

	}

	public void delete(RequestContext requestContext) throws RegistryException {

	}

	public void putChild(RequestContext requestContext)
			throws RegistryException {

	}

	public void importChild(RequestContext requestContext)
			throws RegistryException {

	}

}
