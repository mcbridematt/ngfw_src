/*
 * Copyright (c) 2003,2004 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id$
 */

package com.metavize.tran.nat.gui;

import com.metavize.gui.util.Util;
import com.metavize.gui.transform.*;
import com.metavize.gui.widgets.dialogs.*;
import com.metavize.tran.nat.*;
import com.metavize.mvvm.networking.*;

import java.awt.Window;
import javax.swing.*;

public class AdvancedJPanel extends javax.swing.JPanel implements Refreshable<Object> {

    private MTransformControlsJPanel mTransformControlsJPanel;
    
    public AdvancedJPanel(MTransformControlsJPanel mTransformControlsJPanel) {
	this.mTransformControlsJPanel = mTransformControlsJPanel;
        initComponents();
    }

    public void doRefresh(Object settings){
	SetupState setupState = ((NatCommonSettings)settings).getSetupState();
	if( SetupState.ADVANCED == setupState ){
	    statusJLabel.setText("Advanced (Net Spaces & Routing)");
	    advancedJButton.setEnabled(false);
	    standardJButton.setEnabled(true);
	}
	else if( SetupState.BASIC == setupState ){
	    statusJLabel.setText("Standard (NAT & DMZ)");
	    advancedJButton.setEnabled(true);
	    standardJButton.setEnabled(false);
	}
	else{
	    // bad shite happened
	    advancedJButton.setEnabled(false);
	    standardJButton.setEnabled(false);
	}
    }
    

        private void initComponents() {//GEN-BEGIN:initComponents
                java.awt.GridBagConstraints gridBagConstraints;

                statusJPanel = new javax.swing.JPanel();
                statusJLabel = new javax.swing.JLabel();
                someJLabel = new javax.swing.JLabel();
                clientJPanel = new javax.swing.JPanel();
                advancedJButton = new javax.swing.JButton();
                jLabel1 = new javax.swing.JLabel();
                serverRoutingJPanel = new javax.swing.JPanel();
                standardJButton = new javax.swing.JButton();
                jLabel2 = new javax.swing.JLabel();

                setLayout(new java.awt.GridBagLayout());

                statusJPanel.setLayout(new java.awt.GridBagLayout());

                statusJPanel.setBorder(new javax.swing.border.EtchedBorder());
                statusJPanel.setMaximumSize(new java.awt.Dimension(1061, 29));
                statusJPanel.setMinimumSize(new java.awt.Dimension(1061, 29));
                statusJPanel.setPreferredSize(new java.awt.Dimension(1061, 29));
                statusJLabel.setFont(new java.awt.Font("Dialog", 0, 12));
                statusJLabel.setText("Unconfigured");
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
                statusJPanel.add(statusJLabel, gridBagConstraints);

                someJLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                someJLabel.setText("<html><b>Current Mode:</b></html>");
                someJLabel.setMaximumSize(new java.awt.Dimension(175, 15));
                someJLabel.setMinimumSize(new java.awt.Dimension(175, 15));
                someJLabel.setPreferredSize(new java.awt.Dimension(175, 15));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
                statusJPanel.add(someJLabel, gridBagConstraints);

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.ipadx = 175;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.weighty = 1.0;
                gridBagConstraints.insets = new java.awt.Insets(0, 50, 200, 50);
                add(statusJPanel, gridBagConstraints);

                clientJPanel.setLayout(new java.awt.GridBagLayout());

                clientJPanel.setBorder(new javax.swing.border.EtchedBorder());
                clientJPanel.setMaximumSize(new java.awt.Dimension(1061, 64));
                clientJPanel.setMinimumSize(new java.awt.Dimension(1061, 64));
                clientJPanel.setPreferredSize(new java.awt.Dimension(1061, 64));
                advancedJButton.setFont(new java.awt.Font("Dialog", 0, 12));
                advancedJButton.setText("<html><center>Configure in<br><b>Advanced Mode</b></center></html>");
                advancedJButton.setFocusPainted(false);
                advancedJButton.setFocusable(false);
                advancedJButton.setMaximumSize(new java.awt.Dimension(175, 50));
                advancedJButton.setMinimumSize(new java.awt.Dimension(175, 50));
                advancedJButton.setPreferredSize(new java.awt.Dimension(175, 50));
                advancedJButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                advancedJButtonActionPerformed(evt);
                        }
                });

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
                clientJPanel.add(advancedJButton, gridBagConstraints);

                jLabel1.setFont(new java.awt.Font("Dialog", 0, 12));
                jLabel1.setText("<html>This allows you to protect your internal network using multiple NAT spaces and a Routing table.  You can also setup DHCP, DNS, and Redirect rules.</html>");
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
                clientJPanel.add(jLabel1, gridBagConstraints);

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.ipadx = 218;
                gridBagConstraints.ipady = 16;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.weighty = 1.0;
                gridBagConstraints.insets = new java.awt.Insets(150, 50, 0, 50);
                add(clientJPanel, gridBagConstraints);

                serverRoutingJPanel.setLayout(new java.awt.GridBagLayout());

                serverRoutingJPanel.setBorder(new javax.swing.border.EtchedBorder());
                serverRoutingJPanel.setMaximumSize(new java.awt.Dimension(1061, 64));
                serverRoutingJPanel.setMinimumSize(new java.awt.Dimension(1061, 64));
                standardJButton.setFont(new java.awt.Font("Dialog", 0, 12));
                standardJButton.setText("<html><center>Configure in<br><b>Standard Mode</b></center></html>");
                standardJButton.setFocusPainted(false);
                standardJButton.setFocusable(false);
                standardJButton.setMaximumSize(new java.awt.Dimension(175, 50));
                standardJButton.setMinimumSize(new java.awt.Dimension(175, 50));
                standardJButton.setPreferredSize(new java.awt.Dimension(175, 50));
                standardJButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                standardJButtonActionPerformed(evt);
                        }
                });

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
                serverRoutingJPanel.add(standardJButton, gridBagConstraints);

                jLabel2.setFont(new java.awt.Font("Dialog", 0, 12));
                jLabel2.setText("<html>This allows you to protect your internal network using a single NAT and a single designated DMZ Host.  You can also setup DHCP, DNS, and Redirect rules.</html>");
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
                serverRoutingJPanel.add(jLabel2, gridBagConstraints);

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.ipadx = 218;
                gridBagConstraints.ipady = 16;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.weighty = 1.0;
                gridBagConstraints.insets = new java.awt.Insets(0, 50, 20, 50);
                add(serverRoutingJPanel, gridBagConstraints);

        }//GEN-END:initComponents

    private void standardJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_standardJButtonActionPerformed
        standardJButton.setEnabled(false);
	MTwoButtonJDialog proceedJDialog = MTwoButtonJDialog.factory((Window)getTopLevelAncestor(), "Network Sharing", "Proceeding will cause your currently saved Network Sharing settings to be rest and lost.", "Network Sharing Warning", "Network Sharing Warning");
	if( proceedJDialog.isProceeding() )
	    new NatModeResetThread(false);
	standardJButton.setEnabled(true);
    }//GEN-LAST:event_standardJButtonActionPerformed
    
    private void advancedJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advancedJButtonActionPerformed
	advancedJButton.setEnabled(false);
	MTwoButtonJDialog proceedJDialog = MTwoButtonJDialog.factory((Window)getTopLevelAncestor(), "Network Sharing", "You should only use this mode if Standard Mode is not capable of handling your network configuration, and you are an experienced administrator.", "Network Sharing Warning", "Network Sharing Warning");
	if( proceedJDialog.isProceeding() )
	    new NatModeResetThread(true);
	advancedJButton.setEnabled(true);
    }//GEN-LAST:event_advancedJButtonActionPerformed
    

    private class NatModeResetThread extends Thread{
	private boolean isAdvanced;
	private MProgressJDialog progressJDialog;
	public NatModeResetThread(boolean isAdvanced){
	    setDaemon(true);
	    this.isAdvanced = isAdvanced;
	    start();
	}
	public void run(){
	    SwingUtilities.invokeLater( new Runnable(){ public void run(){
		progressJDialog = MProgressJDialog.factory("Network Sharing Reconfiguring...",
									    "Please wait a moment...",
									    (Window)AdvancedJPanel.this.getTopLevelAncestor());
		progressJDialog.getJProgressBar().setString("Reconfiguring...");
		progressJDialog.getJProgressBar().setIndeterminate(true);
		progressJDialog.setVisible(true);
	    }});

	    try{
		Nat natTransform = com.metavize.tran.nat.gui.MTransformControlsJPanel.getNatTransform();
		if( isAdvanced )
		    natTransform.switchToAdvanced();
		else
		    natTransform.resetBasic();
	    }
	    catch(Exception e){
		try{ Util.handleExceptionWithRestart("Error reconfiguring", e); }
		catch(Exception f){
		    Util.handleExceptionNoRestart("Error reconfiguring", f);
		    MOneButtonJDialog.factory((Window)getTopLevelAncestor(),
					      "Network Sharing", "An error has occurred, please retry.",
					      "Network Sharing Warning", "Warning");
		}
	    }
	    mTransformControlsJPanel.refreshGui();

	    SwingUtilities.invokeLater( new Runnable(){ public void run(){
		progressJDialog.setVisible(false);
	    }});
	}
    }
    
        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton advancedJButton;
        private javax.swing.JPanel clientJPanel;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JPanel serverRoutingJPanel;
        private javax.swing.JLabel someJLabel;
        private javax.swing.JButton standardJButton;
        private javax.swing.JLabel statusJLabel;
        private javax.swing.JPanel statusJPanel;
        // End of variables declaration//GEN-END:variables
    
}
