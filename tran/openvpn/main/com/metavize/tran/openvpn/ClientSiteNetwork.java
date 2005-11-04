/*
 * Copyright (c) 2005 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id$
 */

package com.metavize.tran.openvpn;

/**
 *  A site network for a client.  Done this way so the client site networks and the server 
 *  site networks are in their own tables.
 *
 * @author <a href="mailto:rbscott@metavize.com">Robert Scott</a>
 * @version 1.0
 * @hibernate.class
 * table="tr_openvpn_c_site_network"
 */
public class  ClientSiteNetwork extends SiteNetwork
{
    // XXX Fixme
    // private static final long serialVersionUID = -3950973798403822835L;

    public ClientSiteNetwork()
    {
    }
}

    
