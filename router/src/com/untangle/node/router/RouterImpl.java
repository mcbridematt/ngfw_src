/**
 * $Id$
 */
package com.untangle.node.router;

import java.net.InetAddress;

import org.apache.log4j.Logger;

import com.untangle.uvm.UvmContextFactory;
import com.untangle.uvm.vnet.Affinity;
import com.untangle.uvm.vnet.Fitting;
import com.untangle.uvm.vnet.NodeBase;
import com.untangle.uvm.vnet.PipelineConnector;

public class RouterImpl extends NodeBase implements Router
{
    private final RouterEventHandler handler;
    private final DhcpMonitor dhcpMonitor;

    private final PipelineConnector routerConnector;

    private final PipelineConnector[] connectors;

    private final Logger logger = Logger.getLogger( RouterImpl.class );

    public RouterImpl( com.untangle.uvm.node.NodeSettings nodeSettings, com.untangle.uvm.node.NodeProperties nodeProperties )
    {
        super( nodeSettings, nodeProperties );

        this.handler          = new RouterEventHandler(this);
        this.dhcpMonitor      = new DhcpMonitor( this );

        /**
         * Have to figure out pipeline ordering, this should always towards the server
         */
        routerConnector = UvmContextFactory.context().pipelineFoundry().create("router", this, null, this.handler, Fitting.OCTET_STREAM, Fitting.OCTET_STREAM, Affinity.SERVER, 32 - 1);

        connectors = new PipelineConnector[] { routerConnector };
    }

    @Override
    protected PipelineConnector[] getConnectors()
    {
        return this.connectors;
    }

    public String lookupHostname( InetAddress address )
    {
        if (dhcpMonitor != null)
            return dhcpMonitor.lookupHostname( address );
        return null;
    }
    
    protected void preStart() 
    {
        dhcpMonitor.start();
    }

    protected void postStop() 
    {
        killAllSessions();

        dhcpMonitor.stop();
    }
} 
