package org.alfresco.web.site.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.RequestContextUtil;
import org.springframework.extensions.surf.UserFactory;
import org.springframework.extensions.surf.site.AuthenticationUtil;
import org.springframework.web.context.support.WebApplicationContextUtils;

// Unordered filter
@WebFilter(urlPatterns={"/*"})
public class AutologinFilter implements Filter {
	
	private static String USER_ID = "admin";
	private static String PASSWORD = "admin";
	
	private ApplicationContext context;
	private SlingshotLoginController loginController;
	private UserFactory userFactory;

	@Override
	public void init(FilterConfig config) throws ServletException {
        this.context = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
        this.loginController = (SlingshotLoginController) context.getBean("loginController");
        this.userFactory = (UserFactory) context.getBean("user.factory");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		HttpSession session = request.getSession();
		
        // Already authenticated
        if (AuthenticationUtil.isAuthenticated(request)) {
            chain.doFilter(request, response);
            return;
        }
		
        // Authenticate
        boolean authenticated = userFactory.authenticate(request, USER_ID, PASSWORD);
        if (authenticated) {
        	
            AuthenticationUtil.login(request, response, PASSWORD);
            
            // Set Aikau specific attributes
            session.setAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_ID, USER_ID);
            try {
				RequestContextUtil.initRequestContext(context, request, true);
                loginController.beforeSuccess(request, response);
            } catch (Exception e) {
            	throw new RuntimeException(e);
            }
            
        }
			
    	chain.doFilter(req, resp);

	}
	
	@Override
	public void destroy() {}
}
