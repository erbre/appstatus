/*
 * Copyright 2010 Capgemini
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 */
package net.sf.appstatus.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.appstatus.core.AppStatus;
import net.sf.appstatus.core.AppStatusStatic;
import net.sf.appstatus.core.IServletContextProvider;
import net.sf.appstatus.core.batch.IBatch;
import net.sf.appstatus.core.check.ICheckResult;
import net.sf.appstatus.core.services.IService;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class StatusServlet extends HttpServlet {

	private static final String ENCODING = "UTF-8";
	private static Logger logger = LoggerFactory.getLogger(AppStatus.class);
	private static final long serialVersionUID = 3912325072098291029L;
	private static AppStatus status = null;
	private static final String STATUS_ERROR = "error";
	private static final String STATUS_JOB = "job";
	private static final String STATUS_OK = "ok";
	private static final String STATUS_PROP = "prop";
	private static final String STATUS_WARN = "warn";
	private String allow = null;
	private final String styleSheet = "<style type=\"text/css\" media=\"screen\">"
			+ "table { font-size: 80%; }"
			+ "table ,th, td {  border: 1px solid black; border-collapse:collapse;}"
			+ "th { background-color: #DDDDDD; }" + "</style>";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		if (allow != null) {
			if (!req.getRemoteAddr().equals(allow)) {
				resp.sendError(401, "IP not authorized");
				return;
			}
		}

		if (req.getParameter("icon") != null) {
			doGetResource(req.getParameter("icon"), req, resp);
			return;
		}

		List<ICheckResult> results = status.checkAll();
		boolean statusOk = true;
		int statusCode = 200;
		for (ICheckResult r : results) {
			if (r.isFatal()) {
				resp.setStatus(500);
				statusCode = 500;
				statusOk = false;
				break;
			}
		}

		resp.setContentType("text/html");
		resp.setCharacterEncoding(ENCODING);

		ServletOutputStream os = resp.getOutputStream();

		os.write("<html><head>".getBytes(ENCODING));
		os.write(styleSheet.getBytes(ENCODING));
		os.write("<body>".getBytes(ENCODING));
		os.write("<h1>Status Page</h1>".getBytes(ENCODING));
		os.write(("<p>Online:" + statusOk + "</p>").getBytes(ENCODING));
		os.write(("<p>Code:" + statusCode + "</p>").getBytes(ENCODING));

		os.write("<h2>Status</h2>".getBytes(ENCODING));
		os.write("<table>".getBytes(ENCODING));
		os.write("<tr><th></th><th>Name</th><th>Description</th><th>Code</th><th>Resolution</th></tr>"
				.getBytes(ENCODING));

		for (ICheckResult r : results) {
			generateRow(os, getStatus(r), r.getProbeName(), r.getDescription(),
					String.valueOf(r.getCode()), r.getResolutionSteps());
		}
		os.write("</table>".getBytes(ENCODING));

		os.write("<h2>Properties</h2>".getBytes(ENCODING));
		Map<String, Map<String, String>> properties = status.getProperties();
		os.write("<table>".getBytes(ENCODING));
		os.write("<tr><th></th><th>Category</th><th>Name</th><th>Value</th></tr>"
				.getBytes(ENCODING));

		for (Entry<String, Map<String, String>> cat : properties.entrySet()) {
			String category = cat.getKey();

			for (Entry<String, String> r : cat.getValue().entrySet()) {
				generateRow(os, STATUS_PROP, category, r.getKey(), r.getValue());
			}

		}

		os.write("</table>".getBytes(ENCODING));

		os.write("<h2>Batchs</h2>".getBytes(ENCODING));
		os.write("<table>".getBytes(ENCODING));
		os.write(("<tr><th></th><th>Category</th><th>Name</th><th>Start</th><th>Progress</th><th>End (est.)</th>"
				+ "<th>Status</th><th>Task</th><th>Last Msg</th><th>Rejected</th>"
				+ "<th>Last Update</th>" + "</tr>").getBytes(ENCODING));

		for (IBatch batch : status.getRunningBatches()) {
			generateRow(os, STATUS_JOB, batch.getGroup(), batch.getName(),
					batch.getStartDate(), batch.getProgressStatus() + "%",
					batch.getEndDate(), batch.getStatus(),
					batch.getCurrentTask(), batch.getLastMessage(),
					StringUtils.collectionToCommaDelimitedString(batch
							.getRejectedItemsId()), batch.getLastUpdate());
		}

		os.write("</table>".getBytes(ENCODING));

		os.write("<h2>Services</h2>".getBytes(ENCODING));
		os.write("<table>".getBytes(ENCODING));
		os.write("<tr><th></th><th>Category</th><th>Name</th><th>Hits</th><th>Cache</th><th>Running</th><th>min</th><th>max</th><th>avg</th><th>min (Cache)</th><th>max (Cache)</th><th>avg (Cache)</th></tr>"
				.getBytes(ENCODING));

		for (IService service : status.getServices()) {
			generateRow(os, STATUS_JOB, service.getGroup(), service.getName(),
					service.getHits(), service.getCacheHits(),
					service.getRunning(), service.getMinResponseTime(),
					service.getMaxResponseTime(), service.getAvgResponseTime(),
					service.getMinResponseTimeWithCache(),
					service.getMaxResponseTimeWithCache(),
					service.getAvgResponseTimeWithCache());
		}

		os.write("</table>".getBytes(ENCODING));

		os.write("</body></html>".getBytes(ENCODING));
	}

	/**
	 * Serve icons
	 * 
	 * @param id
	 * @param req
	 * @param resp
	 * @throws IOException
	 */
	protected void doGetResource(String id, HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		String location = null;
		if (STATUS_OK.equals(id)) {
			location = "/org/freedesktop/tango/22x22/status/weather-clear.png";
		} else if (STATUS_WARN.equals(id)) {
			location = "/org/freedesktop/tango/22x22/status/weather-overcast.png";
		} else if (STATUS_ERROR.equals(id)) {
			location = "/org/freedesktop/tango/22x22/status/weather-severe-alert.png";
		} else if (STATUS_PROP.equals(id)) {
			location = "/org/freedesktop/tango/22x22/actions/format-justify-fill.png";
		} else if (STATUS_JOB.equals(id)) {
			location = "/org/freedesktop/tango/22x22/categories/preferences-system.png";
		}

		InputStream is = this.getClass().getResourceAsStream(location);
		OutputStream os = resp.getOutputStream();
		IOUtils.copy(is, os);
	}

	/**
	 * Outputs one table row
	 * 
	 * @param os
	 * @param status
	 * @param cols
	 * @throws IOException
	 */
	private void generateRow(ServletOutputStream os, String status,
			Object... cols) throws IOException {
		os.write("<tr>".getBytes());

		os.write(("<td><img src='?icon=" + status + "'></td>")
				.getBytes(ENCODING));

		for (Object obj : cols) {
			os.write("<td>".getBytes());
			if (obj != null) {
				os.write(obj.toString().getBytes(ENCODING));
			}
			os.write("</td>".getBytes());

		}
		os.write("</tr>".getBytes());
	}

	/**
	 * Returns status icon id.
	 * 
	 * @param result
	 * @return
	 */
	private String getStatus(ICheckResult result) {

		if (result.isFatal()) {
			return STATUS_ERROR;
		}

		if (result.getCode() == ICheckResult.OK) {
			return STATUS_OK;
		}

		return STATUS_WARN;
	}

	@Override
	public void init() throws ServletException {
		super.init();
		String beanName = null;
		try {
			beanName = getInitParameter("bean");

			InputStream is = StatusServlet.class
					.getResourceAsStream("/status-web-conf.properties");

			if (is == null) {
				logger.warn("/status-web-conf.properties not found in classpath. Using default configuration");
			} else {
				Properties p = new Properties();
				p.load(is);
				is.close();
				allow = (String) p.get("ip.allow");
			}
		} catch (Exception e) {
			logger.error(
					"Error loading configuration from /status-web-conf.properties.",
					e);
		}

		if (beanName != null) {
			status = (AppStatus) (new SpringObjectInstantiationListener(
					this.getServletContext()).getInstance(beanName));
		} else {
			status = AppStatusStatic.getInstance();
		}

		status.setServletContextProvider(new IServletContextProvider() {
			public ServletContext getServletContext() {
				return StatusServlet.this.getServletContext();
			}
		});
		status.init();
	}
}
