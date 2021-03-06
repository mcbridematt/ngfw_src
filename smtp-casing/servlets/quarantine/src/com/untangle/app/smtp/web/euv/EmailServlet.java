/**
 * $Id: RequestServlet.java 36445 2013-11-20 00:04:22Z dmorris $
 */
package com.untangle.app.smtp.web.euv;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.untangle.uvm.UvmContext;
import com.untangle.uvm.UvmContextFactory;

/**
 * Servlet used when requesting a digest email/login.
 */
@SuppressWarnings("serial")
public class EmailServlet extends HttpServlet
{
    private static final String REQ_DIGEST_VIEW = "/WEB-INF/jsp/email.jsp";

    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {

        UvmContext uvm = UvmContextFactory.context();
        req.setAttribute( "companyName", uvm.brandingManager().getCompanyName());
        req.setAttribute( "extjsTheme", uvm.skinManager().getSkinInfo().getExtjsTheme());
        req.getRequestDispatcher(REQ_DIGEST_VIEW).forward(req, resp);
    }
}
