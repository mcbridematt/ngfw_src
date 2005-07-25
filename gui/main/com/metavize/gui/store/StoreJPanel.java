/*
 * Copyright (c) 2004, 2005 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id: StoreJPanel.java 194 2005-04-06 19:13:55Z inieves $
 */

package com.metavize.gui.store;

import com.metavize.gui.util.Util;

import javax.jnlp.ServiceManager;
import javax.jnlp.BasicService;
import java.net.URL;

/**
 *
 * @author  inieves
 */
public class StoreJPanel extends javax.swing.JPanel {
    
    private String url;
    
    /** Creates new form StoreJPanel */
    public StoreJPanel(String url) {
        this.url = url;
        
        initComponents();
        
        statusProgressBar.setVisible(false);
        
        if( Util.isLocal() ){
            moreJButton.setEnabled(false);
        }
        else{
            localJLabel.setVisible(false);
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        purchaseJPanel = new javax.swing.JPanel();
        mTransformJPanel = new javax.swing.JPanel();
        storeJScrollPane = new javax.swing.JScrollPane();
        descriptionJTextArea = new javax.swing.JTextArea();
        mTransformJPanel1 = new javax.swing.JPanel();
        accountJLabel1 = new javax.swing.JLabel();
        priceJLabel = new javax.swing.JLabel();
        moreJButton = new javax.swing.JButton();
        localJLabel = new javax.swing.JLabel();
        statusProgressBar = new javax.swing.JProgressBar();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        purchaseJPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        mTransformJPanel.setLayout(new java.awt.GridBagLayout());

        mTransformJPanel.setBorder(new javax.swing.border.TitledBorder(null, "Software Appliance to Procure", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11)));
        mTransformJPanel.setOpaque(false);
        purchaseJPanel.add(mTransformJPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, 210, 150));

        storeJScrollPane.setBorder(new javax.swing.border.TitledBorder(null, "Full Description", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11)));
        storeJScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        storeJScrollPane.setDoubleBuffered(true);
        storeJScrollPane.setOpaque(false);
        storeJScrollPane.getViewport().setOpaque(false);
        descriptionJTextArea.setEditable(false);
        descriptionJTextArea.setFont(new java.awt.Font("Arial", 0, 12));
        descriptionJTextArea.setLineWrap(true);
        descriptionJTextArea.setWrapStyleWord(true);
        descriptionJTextArea.setDoubleBuffered(true);
        descriptionJTextArea.setMargin(new java.awt.Insets(5, 5, 5, 5));
        descriptionJTextArea.setOpaque(false);
        storeJScrollPane.setViewportView(descriptionJTextArea);

        purchaseJPanel.add(storeJScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 30, 300, 150));

        mTransformJPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        mTransformJPanel1.setBorder(new javax.swing.border.TitledBorder(null, "Procurement Information", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11)));
        mTransformJPanel1.setOpaque(false);
        accountJLabel1.setFont(new java.awt.Font("Arial", 0, 12));
        accountJLabel1.setText("Account #:");
        accountJLabel1.setDoubleBuffered(true);
        mTransformJPanel1.add(accountJLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, -1, -1));

        priceJLabel.setFont(new java.awt.Font("Arial", 0, 12));
        priceJLabel.setText("Price:");
        priceJLabel.setDoubleBuffered(true);
        mTransformJPanel1.add(priceJLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, -1, -1));

        purchaseJPanel.add(mTransformJPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 200, 210, 90));

        moreJButton.setFont(new java.awt.Font("Dialog", 0, 12));
        moreJButton.setText("Read more online...");
        moreJButton.setFocusPainted(false);
        moreJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moreJButtonActionPerformed(evt);
            }
        });

        purchaseJPanel.add(moreJButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 210, -1, -1));

        localJLabel.setFont(new java.awt.Font("Dialog", 0, 12));
        localJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        localJLabel.setText("<html><center>(Disabled because there is no browser present.)<br>\nPlease go to http://www.metavize.com to<br>\nlearn about this Software Appliance.\n</center></html>");
        purchaseJPanel.add(localJLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 240, -1, -1));

        add(purchaseJPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        statusProgressBar.setMaximumSize(new java.awt.Dimension(32767, 16));
        statusProgressBar.setMinimumSize(new java.awt.Dimension(10, 16));
        statusProgressBar.setPreferredSize(new java.awt.Dimension(148, 16));
        statusProgressBar.setString("");
        statusProgressBar.setStringPainted(true);
        add(statusProgressBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 330, 575, -1));

    }//GEN-END:initComponents

    private void moreJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moreJButtonActionPerformed
        try{
	    URL newURL = new URL( url );
	    ((BasicService) ServiceManager.lookup("javax.jnlp.BasicService")).showDocument(newURL);
	}
	catch(Exception f){
            Util.handleExceptionNoRestart("error launching browser for EdgeReport", f);
	}
    }//GEN-LAST:event_moreJButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel accountJLabel1;
    protected javax.swing.JTextArea descriptionJTextArea;
    private javax.swing.JLabel localJLabel;
    protected javax.swing.JPanel mTransformJPanel;
    private javax.swing.JPanel mTransformJPanel1;
    private javax.swing.JButton moreJButton;
    private javax.swing.JLabel priceJLabel;
    private javax.swing.JPanel purchaseJPanel;
    private javax.swing.JProgressBar statusProgressBar;
    private javax.swing.JScrollPane storeJScrollPane;
    // End of variables declaration//GEN-END:variables
    
}
