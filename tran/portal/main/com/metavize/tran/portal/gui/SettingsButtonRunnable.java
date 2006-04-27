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


package com.metavize.tran.portal.gui;

import com.metavize.mvvm.portal.*;
import com.metavize.gui.widgets.dialogs.*;
import com.metavize.gui.util.*;
import java.awt.Window;
import java.awt.event.*;

public class SettingsButtonRunnable implements ButtonRunnable {
    private boolean isUserType;
    private boolean isEnabled;
    private PortalUser portalUser;
    private PortalGroup portalGroup;
    private Window topLevelWindow;
    public SettingsButtonRunnable(String isEnabled){
	if( "true".equals(isEnabled) ) {
	    this.isEnabled = true;
	}
	else if( "false".equals(isEnabled) ){
	    this.isEnabled = false;
	}
    }
    public String getButtonText(){ return "Edit Bookmarks"; }
    public boolean isEnabled(){ return isEnabled; }
    public void setEnabled(boolean isEnabled){ this.isEnabled = isEnabled; }
    public void setUserType(boolean isUserType){ this.isUserType = isUserType; }
    public void setPortalUser(PortalUser portalUser){ this.portalUser = portalUser; }
    public void setPortalGroup(PortalGroup portalGroup){ this.portalGroup = portalGroup; }
    public void setTopLevelWindow(Window topLevelWindow){ this.topLevelWindow = topLevelWindow; }
    public void actionPerformed(ActionEvent evt){ run(); }
    public void run(){
	if( isUserType ){
	    UserSettingsJDialog userSettingsJDialog = UserSettingsJDialog.factory(topLevelWindow, portalUser); 
	    userSettingsJDialog.setVisible(true);
	}
	else{
	    GroupSettingsJDialog groupSettingsJDialog = GroupSettingsJDialog.factory(topLevelWindow, portalGroup); 
	    groupSettingsJDialog.setVisible(true);
	}
    }
}
