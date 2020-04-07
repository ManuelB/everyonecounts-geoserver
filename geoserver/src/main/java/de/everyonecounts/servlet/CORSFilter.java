package de.everyonecounts.servlet;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * http://stackoverflow.com/questions/9389211/using-filters-to-redirect-from-https-to-http
 *
 * @author manuel
 *
 */
@WebFilter(urlPatterns = "/*")
public class CORSFilter implements Filter {

	private static final Logger log = Logger.getLogger(CORSFilter.class.getName());

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {

		// if we don't have http requests continue
		if (!(servletRequest instanceof HttpServletRequest) || !(servletResponse instanceof HttpServletResponse)) {
			chain.doFilter(servletRequest, servletResponse);
			return;
		}
		HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
		HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
		String origin = httpServletRequest.getHeader("Origin");


		// if we don't have an origin header continue
		if (origin == null) {
			chain.doFilter(servletRequest, servletResponse);
			return;
		}

		// This filter was already applied continue
		// Spring seams to apply this filter again
		// after it was applied by the servlet engine
		if(httpServletResponse.containsHeader("Access-Control-Expose-Headers")) {
			chain.doFilter(servletRequest, servletResponse);
			return;
		}

		try {

			// Make content length available for CORS
			// prevents: Refused to get unsafe header "Content-Length"
			httpServletResponse.addHeader("Access-Control-Expose-Headers", "Content-Length, Content-Disposition");

			URL originUrl = new URL(origin);
			URL url = new URL(httpServletRequest.getRequestURL().toString());

			// Allow requests from everywhere
			if (originUrl.getHost() != null) {

				// we allow any given origin that fullfils the condition above
				httpServletResponse.addHeader("Access-Control-Allow-Origin", originUrl.toString());
				httpServletResponse.addHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE");
				httpServletResponse.addHeader("Access-Control-Allow-Headers",
						"Origin, X-Requested-With, Content-Type, Content-Disposition, Accept, Authorization,"
								+ " MaxDataServiceVersion, SAP-Contextid-Accept,"
								+ " SAP-Cancel-On-Close, X-CSRF-Token, DataServiceVersion");
				httpServletResponse.addHeader("Access-Control-Allow-Credentials", "true");

				// Just ACCEPT and REPLY OK if OPTIONS
				if ("OPTIONS".equals(httpServletRequest.getMethod())) {
					httpServletResponse.setStatus(HttpServletResponse.SC_OK);
					return;
				}
			}
		} catch (Exception ex) {
			log.log(Level.WARNING, "Problem in CORS Filter", ex);
		}
		chain.doFilter(servletRequest, servletResponse);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// We don't need to init anything
	}

	@Override
	public void destroy() {
		// We don't neetd to destroy anything
	}
}
