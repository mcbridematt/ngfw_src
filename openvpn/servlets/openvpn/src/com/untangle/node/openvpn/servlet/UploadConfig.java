/**
 * $Id: UploadConfig.java,v 1.00 2013/04/27 13:56:05 dmorris Exp $
 */
package com.untangle.node.openvpn.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.apache.log4j.Logger;

import com.untangle.uvm.UvmContextFactory;
import com.untangle.node.openvpn.OpenVpnNode;

/**
 * Servlet used to upload a client configuration file.
 */
@SuppressWarnings({"deprecation","serial"}) 
public class UploadConfig extends HttpServlet
{
    private static ServletFileUpload SERVLET_FILE_UPLOAD;

    private final Logger logger = Logger.getLogger( this.getClass());

    protected void doGet(  HttpServletRequest request,  HttpServletResponse response )
    {
        logger.warn( "Retrieve a GET request for client setup." );
    }

    @SuppressWarnings("unchecked") // uplead.parseRequest
    protected void doPost( HttpServletRequest request,  HttpServletResponse response )
        throws ServletException, IOException
    {
        OpenVpnNode node = (OpenVpnNode) UvmContextFactory.context().nodeManager().node( "untangle-node-openvpn" );

        if ( node == null ) {
            logger.warn( "Unable to retrieve the OpenVPN node." );
            response.sendError( HttpServletResponse.SC_UNAUTHORIZED, "Please access this through the admin client." );
            return;
        }

        //FIXME check ADMIN

        /* See the extjs documentation for why this is text/html.
         * when the flag fileUpload is set to true on a form panel,
         * then it must return text/html as the content type,
         * otherwise the return script is modified before it is
         * decoded as JSON. */
        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        
        if ( !ServletFileUpload.isMultipartContent( request )) {
            logger.debug( "User has invalid post." );
            writer.write( "{ \"success\" : false, \"code\" : \"client error\", \"type\" : 1 }" );
            return;
        }
        
        ServletFileUpload upload = getServletFileUpload();
        
        List<FileItem> items = null;
        try {
            items = upload.parseRequest( request );
        } catch ( FileUploadException e ) {
            logger.warn( "Unable to parse the request", e );
            writer.write( "{ \"success\" : false, \"code\" : \"client error\", \"type\" : 2 }" );
            return;
        }
        
        String serverName = request.getParameter( "serverName" );

        if ( serverName == null ) {
            logger.info( "UploadConfig is missing the server name." );
            writer.write( "{ \"success\" : false, \"code\" : \"client error\", \"type\" : 3 }" );
            return;
        } else {
            serverName = serverName.replaceAll("/[^A-Za-z0-9 ]/", "");
        }
        
        InputStream inputStream = null;
        
        for ( FileItem item : items ) {
            if ( ! "uploadConfigFileName".equals( item.getFieldName() ) ) continue;
            inputStream = item.getInputStream();
            break;
        }

        if ( inputStream == null ) {
            logger.info( "UploadConfig is missing the client file." );
            writer.write( "{ \"success\" : false, \"code\" : \"client error\", \"type\" : 4 }" );
            return;
        }
            
        /* Write out the file. */
        File temp = null;
        OutputStream outputStream = null;
        try {
            temp = File.createTempFile( "openvpn-" + serverName, ".zip" );
            temp.deleteOnExit();
            outputStream = new FileOutputStream( temp );
            
            byte[] data = new byte[1024];
            int len = 0;
            while (( len = inputStream.read( data )) > 0 ) outputStream.write( data, 0, len );
        } catch ( IOException e ) {
            logger.warn( "Unable to validate client file.", e  );
            writer.write( "{ \"success\" : false, \"code\" : \"internal error\", \"type\" : 1 }" );
            return;
        } finally {
            try {
                if ( outputStream != null ) outputStream.close();
            } catch ( Exception e ) {
                logger.warn( "Error closing writer", e );
            }

            try {
                if ( inputStream != null ) inputStream.close();
            } catch ( Exception e ) {
                logger.warn( "Error closing input stream", e );
            }
        }
        
        try {
            boolean a = true;
            logger.error("IMPLEMENT ME FIXME node.addServer( " + temp.getPath() + ")");
            //node.addServerMethod( temp.getPath() );
        } catch ( Exception e ) {
            logger.warn( "Unable to install the client configuration", e );
            writer.write( "{ \"success\" : false, \"code\" : \"unknown\" }" );
            return;
        }

        writer.write( "{ \"success\" : true }" );
    }

    private static synchronized ServletFileUpload getServletFileUpload()
    {
        if ( SERVLET_FILE_UPLOAD == null ) {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            SERVLET_FILE_UPLOAD =  new ServletFileUpload(factory);
        }

        return SERVLET_FILE_UPLOAD;
    }


}
