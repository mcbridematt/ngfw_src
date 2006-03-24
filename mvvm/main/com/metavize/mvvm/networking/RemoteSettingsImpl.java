/*
 * Copyright (c) 2003, 2004, 2005 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 *  $Id$
 */

package com.metavize.mvvm.networking;

import java.io.Serializable;

import java.net.Inet4Address;
import java.net.InetAddress;

import com.metavize.mvvm.tran.IPaddr;
import com.metavize.mvvm.tran.Equivalence;
import com.metavize.mvvm.tran.HostName;
import com.metavize.mvvm.tran.ParseException;
import com.metavize.mvvm.tran.Validatable;
import com.metavize.mvvm.tran.ValidateException;

public class RemoteSettingsImpl implements Serializable, RemoteSettings, Equivalence
{
    // private static final long serialVersionUID = 172494253701617361L;    
    public static final boolean DEF_IS_INSIDE_INSECURE_EN = true;
    public static final boolean DEF_IS_OUTSIDE_EN         = false;
    public static final boolean DEF_IS_OUTSIDE_RESTRICTED = false;
    public static final boolean DEF_IS_SSH_EN             = false;
    public static final boolean DEF_IS_EXCEPTION_REPORTING_EN = false;
    public static final boolean DEF_IS_TCP_WIN_EN         = false;

    /* Post configuration script is empty */
    public static final String DEF_POST_CONFIGURATION = "";
    
    public static final int DEF_HTTPS_PORT = NetworkUtil.DEF_HTTPS_PORT;

    /**
     * True if SSH remote debugging is enabled.
     */
    private boolean isSshEnabled = DEF_IS_SSH_EN;

    /**
     * True if exception emails are to be emailed
     */
    private boolean isExceptionReportingEnabled = DEF_IS_EXCEPTION_REPORTING_EN;
    
    /**
     * True if TCP Window Scaling is enabled.
     * disabled by default.
     * See: http://oss.sgi.com/archives/netdev/2004-07/msg00121.html or bug 163
     */
    private boolean isTcpWindowScalingEnabled = DEF_IS_TCP_WIN_EN;

    private boolean isInsideInsecureEnabled   = DEF_IS_INSIDE_INSECURE_EN;
    private boolean isOutsideAccessEnabled    = DEF_IS_OUTSIDE_EN;
    private boolean isOutsideAccessRestricted = DEF_IS_OUTSIDE_RESTRICTED;

    private IPaddr outsideNetwork = NetworkUtil.DEF_OUTSIDE_NETWORK;
    private IPaddr outsideNetmask = NetworkUtil.DEF_OUTSIDE_NETMASK;
    
    private HostName hostname = NetworkUtil.DEFAULT_HOSTNAME;
    private boolean isHostnamePublic = false;
    
    private boolean isPublicAddressEnabled = false;
    private IPaddr publicIPaddr;
    private int publicPort = -1;
    
    private int httpsPort = DEF_HTTPS_PORT;

    /* This is a script that gets executed after the bridge configuration runs */
    private String postConfigurationScript = DEF_POST_CONFIGURATION;

    public RemoteSettingsImpl()
    {
    }

    /* Set the post configuration script */
    public String getPostConfigurationScript()
    {
        if ( this.postConfigurationScript == null ) this.postConfigurationScript = DEF_POST_CONFIGURATION;
        return this.postConfigurationScript;
    }
    
    /* XXXX This should be validated */
    public void setPostConfigurationScript( String newValue )
    {
        if ( newValue == null ) newValue = DEF_POST_CONFIGURATION;
        this.postConfigurationScript = newValue;
    }

    public boolean isSshEnabled()
    {
        return this.isSshEnabled;
    }

    public void isSshEnabled( boolean isEnabled ) 
    {
        this.isSshEnabled = isEnabled;
    }

    public boolean isExceptionReportingEnabled()
    {
        return this.isExceptionReportingEnabled;
    }
    
    public void isExceptionReportingEnabled( boolean isEnabled )
    {
        this.isExceptionReportingEnabled = isEnabled;
    }
    
    public void isTcpWindowScalingEnabled( boolean isEnabled )
    {
        this.isTcpWindowScalingEnabled = isEnabled;
    }

    public boolean isTcpWindowScalingEnabled()
    {
        return isTcpWindowScalingEnabled;
    }

    /**
     * True if insecure access from the inside is enabled.
     */
    public void isInsideInsecureEnabled( boolean isEnabled )
    {
        this.isInsideInsecureEnabled = isEnabled;
    }

    public boolean isInsideInsecureEnabled()
    {
        return isInsideInsecureEnabled;
    }

    /**
     * True if outside (secure) access is enabled.
     */
    public void isOutsideAccessEnabled( boolean isEnabled )
    {
        this.isOutsideAccessEnabled = isEnabled;
    }

    public boolean isOutsideAccessEnabled()
    {
        return isOutsideAccessEnabled;
    }

    /**
     * True if outside (secure) access is restricted.
     */
    public void isOutsideAccessRestricted( boolean isRestricted )
    {
        this.isOutsideAccessRestricted = isRestricted;
    }

    public boolean isOutsideAccessRestricted()
    {
        return isOutsideAccessRestricted;
    }

    /**
     * The netmask of the network/host that is allowed to administer the box from outside
     * This is ignored if outside access is not enabled, null for just
     * one host.
     */

    /**
     * Set the network with an IP Maddr
     */
    public void outsideNetwork( IPaddr newValue )
    {
        if ( newValue == null ) newValue = NetworkUtil.DEF_OUTSIDE_NETWORK;

        this.outsideNetwork = newValue;
    }

    public IPaddr outsideNetwork()
    {
        if ( this.outsideNetwork == null ) this.outsideNetwork = NetworkUtil.DEF_OUTSIDE_NETWORK;

        return this.outsideNetwork;
    }

    /**
     * Set the network with an IP Maddr
     */
    public void outsideNetmask( IPaddr newValue )
    {
        if ( newValue == null ) newValue = NetworkUtil.DEF_OUTSIDE_NETMASK;
            
        this.outsideNetmask = newValue;
    }

    public IPaddr outsideNetmask()
    {
        if ( this.outsideNetmask == null ) this.outsideNetmask = NetworkUtil.DEF_OUTSIDE_NETMASK;

        return this.outsideNetmask;
    }

    public int httpsPort()
    {
        /* Make sure it is a valid port */
        if (( this.httpsPort == 0 ) || ( this.httpsPort > 0xFFFF ) || ( httpsPort == 80 )) {
            this.httpsPort = DEF_HTTPS_PORT;
        }
        
        return this.httpsPort;
    }

    public void httpsPort( int httpsPort )
    {
        /* Make sure that it is a valid port */
        if (( httpsPort == 0 ) || ( httpsPort > 0xFFFF ) || ( httpsPort == 80 )) httpsPort = DEF_HTTPS_PORT;
        
        this.httpsPort = httpsPort;
    }

    /** The hostname for the box(this is the hostname that goes into certificates). */
    public HostName getHostname()
    {
        if ( this.hostname == null ) this.hostname = NetworkUtil.DEFAULT_HOSTNAME;
            
        return this.hostname;
    }
    
    public void setHostname( HostName newValue )
    {
        if ( newValue == null ) newValue = NetworkUtil.DEFAULT_HOSTNAME;

        this.hostname = newValue;
    }
    
    /* True if the hostname for the box is publicly resolvable */
    public boolean getIsHostnamePublic()
    {
        return this.isHostnamePublic;
    }

    public void setIsHostnamePublic( boolean newValue )
    {
        this.isHostnamePublic = newValue;
    }

    public boolean getIsPublicAddressEnabled()
    {
        return this.isPublicAddressEnabled;
    }

    public void setIsPublicAddressEnabled( boolean newValue )
    {
        this.isPublicAddressEnabled = newValue;
    }

    /** @return the public url for the box, this is the address (may be hostname or ip address) */
    public String getPublicAddress()
    {
        return NetworkUtil.getInstance().generatePublicAddress( getPublicIPaddr(), getPublicPort());
    }

    public void setPublicAddress( String newValue ) throws ParseException
    {
        NetworkUtil.getInstance().parsePublicAddress( this, newValue );
    }

    /** @return the public url for the box, this is the address (may be hostname or ip address) */
    public IPaddr getPublicIPaddr()
    {
        return this.publicIPaddr;
    }

    public void setPublicIPaddr( IPaddr newValue )
    {
        this.publicIPaddr = newValue;
    }

    /** @return the public url for the box, this is the address (may be hostname or ip address) */
    public int getPublicPort()
    {
        if ( this.publicPort < 0 || this.publicPort > 0xFFFF ) this.publicPort = DEF_HTTPS_PORT;

        return this.publicPort;
    }

    public void setPublicPort( int newValue )
    {
        if ( newValue < 0 || newValue > 0xFFFF ) newValue = DEF_HTTPS_PORT;
        this.publicPort = newValue;
    }

    /* Return true if the current settings have a public address */
    public boolean hasPublicAddress()
    {
        return (( this.publicIPaddr != null ) &&  !this.publicIPaddr.isEmpty());
    }

    @Override
    public boolean equals(Object newObject)
    {
        if (null == newObject ||
            false == (newObject instanceof RemoteSettings)) {
            return false;
        }

        RemoteSettings newNC = (RemoteSettings) newObject;
        RemoteSettings curNC = this;

        if ( false == curNC.getPostConfigurationScript().equals(newNC.getPostConfigurationScript())) {
            return false;
        }

        if ( curNC.httpsPort() != newNC.httpsPort()) {
            return false;
        }

        if (curNC.isSshEnabled() != newNC.isSshEnabled()) {
            return false;
        }

        if (curNC.isExceptionReportingEnabled() != newNC.isExceptionReportingEnabled()) {
            return false;
        }

        if (curNC.isTcpWindowScalingEnabled() != newNC.isTcpWindowScalingEnabled()) {
            return false;
        }

        if (curNC.isInsideInsecureEnabled() != newNC.isInsideInsecureEnabled()) {
            return false;
        }

        if (curNC.isOutsideAccessEnabled() != newNC.isOutsideAccessEnabled()) {
            return false;
        }

        if (curNC.isOutsideAccessRestricted() != newNC.isOutsideAccessRestricted()) {
            return false;
        }

        if (false == curNC.outsideNetwork().equals(newNC.outsideNetwork())) {
            return false;
        }

        if (false == curNC.outsideNetmask().equals(newNC.outsideNetmask())) {
            return false;
        }

        return true;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "script:      " + getPostConfigurationScript());               
        sb.append( "\nssh:         " + isSshEnabled());
        sb.append( "\nexceptions:  " + isExceptionReportingEnabled());
        sb.append( "\ntcp window:  " + isTcpWindowScalingEnabled());
        sb.append( "\ninside-in:   " + isInsideInsecureEnabled());
        sb.append( "\noutside:     " + isOutsideAccessEnabled()); 
        sb.append( "\nrestriced:   " + isOutsideAccessRestricted());
        sb.append( "\nrestriction: " + outsideNetwork() + "/" + outsideNetmask());
        sb.append( "\nHTTPS:       " + httpsPort());
        sb.append( "\npublic:      " + getPublicAddress());
        sb.append( "\nhostname:    " + getHostname() + " [" + getIsHostnamePublic() + "]" );
        
        return sb.toString();
    }
}
