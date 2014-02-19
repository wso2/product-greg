/*
 * Copyright 2001-2009 The Apache Software Foundation.
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
package org.apache.juddi.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.Buffer;
import org.apache.juddi.subscription.NotificationList;


/**
 * This servlet is used print out notifications received
 * from subscriptions in the subscription portal. This servlet is
 * used for this demo only and has no other purpose.
 * 
 * @author <a href="mailto:tcunning@apache.org">Tom Cunningham</a>
 */
public class NotifyServlet extends HttpServlet
{
	private static final long serialVersionUID = 4862936257096400737L;

	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request,
			HttpServletResponse response) throws
		ServletException, IOException {
		StringBuffer sb = new StringBuffer();

		Buffer nl = NotificationList.getInstance().getNotifications();
		Iterator<String> it = nl.iterator();
		while (it.hasNext()) {
			String notification = (String) it.next();		
			sb.append(notification);
		}
		nl.clear();
		PrintWriter out = response.getWriter();
		out.println(sb.toString());
	}
}
