/*
 * Copyright (c) 2003,2004 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id: RedirectJPanel.java 194 2005-04-06 19:13:55Z inieves $
 */
package com.metavize.tran.nat.gui;


import com.metavize.gui.transform.*;
import com.metavize.gui.pipeline.MPipelineJPanel;
import com.metavize.gui.widgets.editTable.*;
import com.metavize.gui.util.*;

import com.metavize.mvvm.tran.*;
import com.metavize.mvvm.tran.firewall.*;
import com.metavize.tran.nat.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

public class RedirectJPanel extends MEditTableJPanel {

            
    public RedirectJPanel() {
        super(true, true);
        super.setFillJButtonEnabled( true );
        super.setInsets(new Insets(4, 4, 2, 2));
        super.setTableTitle("");
        super.setDetailsTitle("");
        super.setAddRemoveEnabled(true);
        
        // create actual table model
        RedirectTableModel redirectTableModel = new RedirectTableModel();
        this.setTableModel( redirectTableModel );

    }
    


class RedirectTableModel extends MSortedTableModel{ 
    
    private static final int  T_TW = Util.TABLE_TOTAL_WIDTH_LARGE;
    private static final int  C0_MW = Util.STATUS_MIN_WIDTH; /* status */
    private static final int  C1_MW = Util.LINENO_MIN_WIDTH; /* # */
    private static final int  C2_MW = 75;  /* redirect */
    private static final int  C3_MW = 55;  /* log */
    private static final int  C4_MW = 100; /* traffic type */
    private static final int  C5_MW = 160; /* direction */
    private static final int  C6_MW = 130;  /* source address */
    private static final int  C7_MW = 130;  /* destination address */
    private static final int  C8_MW = 110;  /* source port */
    private static final int  C9_MW = 110;  /* destination port */
    private static final int C10_MW = 130;  /* redirect address */
    private static final int C11_MW = 110;  /* redirect port */
    private static final int C12_MW = 120;  /* category */
    
    private final int C13_MW = Util.chooseMax(T_TW - (C0_MW + C1_MW + C2_MW + C3_MW + C4_MW + C5_MW + C6_MW + C7_MW + C8_MW + C9_MW + C10_MW + C11_MW + C12_MW), 120); /* description */


    
    private ComboBoxModel protocolModel = super.generateComboBoxModel( ProtocolMatcher.getProtocolEnumeration(), ProtocolMatcher.getProtocolDefault() );
    private ComboBoxModel directionModel = super.generateComboBoxModel( TrafficRule.getDirectionEnumeration(), TrafficRule.getDirectionDefault() );
    

    protected boolean getSortable(){ return false; }
    
    public TableColumnModel getTableColumnModel(){
        
        DefaultTableColumnModel tableColumnModel = new DefaultTableColumnModel();
        //                                 #   min    rsz    edit   remv   desc   typ            def
        addTableColumn( tableColumnModel,  0,  C0_MW, false, false, false, false, String.class,  null, sc.TITLE_STATUS );
        addTableColumn( tableColumnModel,  1,  C1_MW, false, false, false, false, Integer.class, null, sc.TITLE_INDEX );
        addTableColumn( tableColumnModel,  2,  C2_MW, false, true,  false, false, Boolean.class, "false", sc.bold("redirect") );
        addTableColumn( tableColumnModel,  3,  C3_MW, false, true,  false, false, Boolean.class, "false",  sc.bold("log"));
        addTableColumn( tableColumnModel,  4,  C4_MW, false, true,  false, false, ComboBoxModel.class, protocolModel, sc.html("traffic<br>type") );
        addTableColumn( tableColumnModel,  5,  C5_MW, false, true,  false, false, ComboBoxModel.class, directionModel, "direction" );
        addTableColumn( tableColumnModel,  6,  C6_MW, true,  true,  false, false, String.class, "1.2.3.4", sc.html("source<br>address") );
        addTableColumn( tableColumnModel,  7,  C7_MW, true,  true,  false, false, String.class, "1.2.3.4", sc.html("destination<br>address") );
        addTableColumn( tableColumnModel,  8,  C8_MW, true,  true,  false, false, String.class, "2-5", sc.html("source<br>port") );
        addTableColumn( tableColumnModel,  9,  C9_MW, true,  true,  false, false, String.class, "2-5", sc.html("destination<br>port") );
        addTableColumn( tableColumnModel, 10, C10_MW, true,  true,  false, false, String.class, "1.2.3.4", sc.html("redirect<br>address") );
        addTableColumn( tableColumnModel, 11, C11_MW, true,  true,  false, false, String.class, "5", sc.html("redirect<br>port") );
        addTableColumn( tableColumnModel, 12, C12_MW, true,  true,  false, false, String.class, sc.EMPTY_CATEGORY, sc.TITLE_CATEGORY);
        addTableColumn( tableColumnModel, 13, C13_MW, true,  true,  false, true,  String.class, sc.EMPTY_DESCRIPTION, sc.TITLE_DESCRIPTION);
        addTableColumn( tableColumnModel, 14, 10,     false, false, true,  false, RedirectRule.class, null, "");
        return tableColumnModel;
    }
    
    
    public void generateSettings(Object settings, Vector<Vector> tableVector, boolean validateOnly) throws Exception {
        List elemList = new ArrayList(tableVector.size());
	RedirectRule newElem = null;
        int rowIndex = 0;

        for( Vector rowVector : tableVector ){
            rowIndex++;
            newElem = (RedirectRule) rowVector.elementAt(14);
            newElem.setLive( (Boolean) rowVector.elementAt(2) );
            newElem.setLog( (Boolean) rowVector.elementAt(3) );
            newElem.setProtocol( ProtocolMatcher.parse(((ComboBoxModel) rowVector.elementAt(4)).getSelectedItem().toString()) );
            newElem.setDirection( ((ComboBoxModel) rowVector.elementAt(5)).getSelectedItem().toString() );
            try{ newElem.setSrcAddress( IPMatcher.parse((String) rowVector.elementAt(6)) ); }
            catch(Exception e){ throw new Exception("Invalid \"source address\" in row: " + rowIndex); }
            try{ newElem.setDstAddress( IPMatcher.parse((String) rowVector.elementAt(7)) ); }
            catch(Exception e){ throw new Exception("Invalid \"destination address\" in row: " + rowIndex); }
            try{ newElem.setSrcPort( PortMatcher.parse((String) rowVector.elementAt(8)) ); }
            catch(Exception e){ throw new Exception("Invalid \"source port\" in row: " + rowIndex); }
            try{ newElem.setDstPort( PortMatcher.parse((String) rowVector.elementAt(9)) ); }
            catch(Exception e){ throw new Exception("Invalid \"destination port\" in row: " + rowIndex); }
            try{ newElem.setRedirectAddress((String) rowVector.elementAt(10)); }
            catch(Exception e){ throw new Exception("Invalid \"redirect address\" in row: " + rowIndex); }
            try{ newElem.setRedirectPort((String) rowVector.elementAt(11)); }
            catch(Exception e){ throw new Exception("Invalid \"redirect port\" in row: " + rowIndex); }
            newElem.setCategory( (String) rowVector.elementAt(12) );                
            newElem.setDescription( (String) rowVector.elementAt(13) );
	    newElem.setDstRedirect( true );  // For now, all redirects are destination redirects

            elemList.add(newElem);
        }
        
	// SAVE SETTINGS ////////////
	if( !validateOnly ){
	    NatSettings natSettings = (NatSettings) settings;
	    natSettings.setRedirectList( elemList );
	}
    }
    
    
    public Vector<Vector> generateRows(Object settings) {
        NatSettings natSettings = (NatSettings) settings;
	List<RedirectRule> redirectList = (List<RedirectRule>) natSettings.getRedirectList();
        Vector<Vector> allRows = new Vector<Vector>(redirectList.size());
	Vector tempRow = null;
        int rowIndex = 0;

        for( RedirectRule redirectRule : redirectList ){
	    rowIndex++;
	    tempRow = new Vector(15);
	    tempRow.add( super.ROW_SAVED );
	    tempRow.add( rowIndex );
	    tempRow.add( redirectRule.isLive() );
	    tempRow.add( redirectRule.getLog() );
	    tempRow.add( super.generateComboBoxModel( ProtocolMatcher.getProtocolEnumeration(), redirectRule.getProtocol().toString() ) );
	    tempRow.add( super.generateComboBoxModel( TrafficRule.getDirectionEnumeration(), redirectRule.getDirection().toString() ) );
	    tempRow.add( redirectRule.getSrcAddress().toString() );
	    tempRow.add( redirectRule.getDstAddress().toString() );
	    tempRow.add( redirectRule.getSrcPort().toString() );
	    tempRow.add( redirectRule.getDstPort().toString() );
	    tempRow.add( redirectRule.getRedirectAddressString().toString() );
	    tempRow.add( redirectRule.getRedirectPortString() );
	    tempRow.add( redirectRule.getCategory() );
	    tempRow.add( redirectRule.getDescription() );
	    tempRow.add( redirectRule );
	    allRows.add( tempRow );
        }

        return allRows;
    }
    
}

}
