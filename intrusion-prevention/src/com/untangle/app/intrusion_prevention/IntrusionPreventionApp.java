/**
 * $Id: IntrusionPreventionApp.java 38584 2014-09-03 23:23:07Z dmorris $
 */
package com.untangle.app.intrusion_prevention;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.untangle.uvm.UvmContext;
import com.untangle.uvm.UvmContextFactory;
import com.untangle.uvm.SettingsManager;
import com.untangle.uvm.HookCallback;
import com.untangle.uvm.ExecManagerResult;
import com.untangle.uvm.util.I18nUtil;
import com.untangle.uvm.network.NetworkSettings;
import com.untangle.uvm.network.InterfaceSettings;
import com.untangle.uvm.network.InterfaceStatus;
import com.untangle.uvm.app.IPMaskedAddress;
import com.untangle.uvm.app.AppMetric;
import com.untangle.uvm.app.AppManager;
import com.untangle.uvm.app.AppBase;
import com.untangle.uvm.vnet.PipelineConnector;
import com.untangle.uvm.servlet.DownloadHandler;

/**
 * Manage Intrusion Prevention configuration.
 *
 * The major difference between this and other applications is that configuration is handled through
 * special upload/downloaders.  This is due to the size of IPS settings which:
 *
 * * Are not used by uvm at all so they take up 20+MB of memory.
 * * Deallocating that much memory causes uvm to cause garbage collection errors.
 *
 */
public class IntrusionPreventionApp extends AppBase
{
    private final Logger logger = Logger.getLogger(getClass());

    private static final String STAT_SCAN = "scan";
    private static final String STAT_DETECT = "detect";
    private static final String STAT_BLOCK = "block";
    
    private final EventHandler handler;
    private final PipelineConnector [] connectors = new PipelineConnector[0];
    private final IntrusionPreventionEventMonitor ipsEventMonitor;    

    private static final String IPTABLES_SCRIPT = System.getProperty("prefix") + "/etc/untangle/iptables-rules.d/740-suricata";
    private static final String GET_LAST_UPDATE = System.getProperty( "uvm.bin.dir" ) + "/intrusion-prevention-get-last-update-check";
    private static final String ENGINE_RULES_DIRECTORY = "/etc/suricata/rules";
    private static final String CURRENT_RULES_DIRECTORY = "/usr/share/untangle-suricata-config/current";
    // private static final String SNORT_DEBIAN_CONF = "/etc/snort/snort.debian.conf";
    // private static final String SURICATA_CONF = "/etc/snort/suricata.conf";
    private static final String DATE_FORMAT_NOW = "yyyy-MM-dd_HH-mm-ss";
    // private static final String GET_STATUS_COMMAND = "/usr/bin/tail -20 /var/log/snort.log | /usr/bin/tac";

    private boolean updatedSettingsFlag = false;

    private final HookCallback networkSettingsChangeHook;

    private List<IPMaskedAddress> homeNetworks = null;
    private List<String> interfaceIds = null;

    /**
     * Setup IPS application
     *
     * @param appSettings       Application settings.
     * @param appProperties     Application properties
     */
    public IntrusionPreventionApp( com.untangle.uvm.app.AppSettings appSettings, com.untangle.uvm.app.AppProperties appProperties )
    {
        super( appSettings, appProperties );

        this.handler = new EventHandler(this);
        this.homeNetworks = this.calculateHomeNetworks( UvmContextFactory.context().networkManager().getNetworkSettings());
        this.interfaceIds = this.calculateInterfaces( UvmContextFactory.context().networkManager().getNetworkSettings() );
        this.networkSettingsChangeHook = new IntrusionPreventionNetworkSettingsHook();

        this.addMetric(new AppMetric(STAT_SCAN, I18nUtil.marktr("Sessions scanned")));
        this.addMetric(new AppMetric(STAT_DETECT, I18nUtil.marktr("Sessions logged")));
        this.addMetric(new AppMetric(STAT_BLOCK, I18nUtil.marktr("Sessions blocked")));

        setScanCount(0);
        setDetectCount(0);
        setBlockCount(0);

        this.ipsEventMonitor = new IntrusionPreventionEventMonitor( this );

        initializeSettings();

        UvmContextFactory.context().servletFileManager().registerDownloadHandler( new IntrusionPreventionSettingsDownloadHandler() );
    }

    /**
     * Get the pineliene connector.
     *
     * @return PipelineConector
     */
    @Override
    protected PipelineConnector[] getConnectors()
    {
        return this.connectors;
    }

    /**
     * Post IPS initialization
     *
     * @return PipelineConector
     */
    @Override
    protected void postInit()
    {
        logger.info("Post init");

        readAppSettings();
    }

    /**
     * Pre IPS stop. Register callback?
     *
     * @param isPermanentTransition
     */
    @Override
    protected void preStop( boolean isPermanentTransition )
    {
        UvmContextFactory.context().hookManager().registerCallback( com.untangle.uvm.HookManager.NETWORK_SETTINGS_CHANGE, this.networkSettingsChangeHook );
        try{
            this.ipsEventMonitor.stop();
        }catch( Exception e ){
            logger.warn( "Error disabling Intrusion Prevention Event Monitor", e );
        }
    }

    /**
     * Post IPS stop.  Shut down suricata.  Run iptables rules to remove.
     *
     * @param isPermanentTransition
     */
    @Override
    protected void postStop( boolean isPermanentTransition )
    {
        UvmContextFactory.context().daemonManager().decrementUsageCount( "suricata" );
        iptablesRules();
    }

    /**
     * Pre IPS start. Start suricata, unregister calllback hook.
     *
     * @param isPermanentTransition
     */
    @Override
    protected void preStart( boolean isPermanentTransition )
    {
        File settingsFile = new File( getSettingsFileName() );
        // File suricataConf = new File(SURICATA_CONF);
        // File suricataDebianConf = new File(SNORT_DEBIAN_CONF);
        // if (settingsFile.lastModified() > suricataDebianConf.lastModified() ||
        // if (settingsFile.lastModified() > suricataDebianConf.lastModified() ||
        //     suricataConf.lastModified() > suricataDebianConf.lastModified() ) {
        //     logger.warn("Settings file newer than suricata debian configuration, Syncing...");
        //     reconfigure();
        // }

        Map<String,String> i18nMap = UvmContextFactory.context().languageManager().getTranslations("untangle");
        I18nUtil i18nUtil = new I18nUtil(i18nMap);
        UvmContextFactory.context().daemonManager().incrementUsageCount( "suricata" );
        UvmContextFactory.context().daemonManager().enableDaemonMonitoring( "suricata", 3600, "suricata");
        UvmContextFactory.context().hookManager().unregisterCallback( com.untangle.uvm.HookManager.NETWORK_SETTINGS_CHANGE, this.networkSettingsChangeHook );
        this.ipsEventMonitor.start();
    }

    /**
     * Post  IPS start. Start iptables rules.
     *
     * @param isPermanentTransition
     */
    @Override
    protected void postStart( boolean isPermanentTransition )
    {
        iptablesRules();

    }

    /**
     * Reconfigure IPS.
     */
    public void reconfigure()
    {

        this.homeNetworks = this.calculateHomeNetworks( UvmContextFactory.context().networkManager().getNetworkSettings());
        this.interfaceIds = this.calculateInterfaces( UvmContextFactory.context().networkManager().getNetworkSettings() );

        String homeNetValue = "";
        for( IPMaskedAddress ma : this.homeNetworks ){
            homeNetValue += 
                ( homeNetValue.length() > 0 ? "," : "" ) + 
                ma.getMaskedAddress().getHostAddress().toString() + "/" + ma.getPrefixLength();
        }

        String interfacesValue = "";
        for( String i : this.interfaceIds ){
            interfacesValue += 
                ( interfacesValue.length() > 0 ? "," : "" ) + i; 
        }

        // ALSO NEED ENGINE_RULES_DIRECTORY
        String configCmd = new String(System.getProperty("uvm.bin.dir") + 
            "/intrusion-prevention-create-config.py" + 
            " --app_id \"" + this.getAppSettings().getId().toString() + "\"" +
            " --home_net \"" + homeNetValue + "\"" +
            " --iptables_script \"" + IPTABLES_SCRIPT + "\""
        );

        String result = UvmContextFactory.context().execManager().execOutput(configCmd );
        try{
            String lines[] = result.split("\\r?\\n");
            for ( String line : lines ){
                if( line.trim().length() > 1 ){
                    logger.warn("reconfigure: intrusion-prevention-create-config: " + line);
                }
            }
        }catch( Exception e ){
            logger.warn( "Unable to generate suricata.configuration:", e );
        }
        reloadEventMonitorMap();
        stop();
        start();
    }

    /**
     * Get the last time signarures were updated.
     *
     * @return Last signature update.
     */
    public Date getLastUpdate()
    {
        try {
            String result = UvmContextFactory.context().execManager().execOutput( GET_LAST_UPDATE + " signatures");
            long timeSeconds = Long.parseLong( result.trim());

            return new Date( timeSeconds * 1000l );
        } catch ( Exception e ) {
            logger.warn( "Unable to get last update.", e );
            return null;
        } 
    }

    /**
     * Get the last time signarures were checked.  An update may not have occured if signures didn't change.
     *
     * @return Last signature update.
     */
    public Date getLastUpdateCheck()
    {
        try {
            String result = UvmContextFactory.context().execManager().execOutput( GET_LAST_UPDATE );
            long timeSeconds = Long.parseLong( result.trim());

            return new Date( timeSeconds * 1000l );
        } catch ( Exception e ) {
            logger.warn( "Unable to get last update.", e );
            return null;
        } 
    }

    // private methods ---------------------------------------------------------

    /**
     * Determine the settings filename to use.
     */
    private void readAppSettings()
    {
        String settingsFile = System.getProperty("uvm.settings.dir") + "/intrusion-prevention/settings_" + this.getAppSettings().getId().toString() + ".js";

        logger.info("Loading settings from " + settingsFile);

    }

    /**
     * Set the scan count to the specified value.
     *
     * @param value     New scan count value
     */
    public void setScanCount( long value )
    {
        this.setMetric(STAT_SCAN, value);
    }

    /**
     * Set the detect count to the specified value.
     *
     * @param value     New detect count value
     */
    public void setDetectCount( long value)
    {
        this.setMetric(STAT_DETECT, value);
    }

    /**
     * Set the block count to the specified value.
     *
     * @param value     New block count value
     */
    public void setBlockCount( long value )
    {
        this.setMetric(STAT_BLOCK, value);
    }

    /**
     * Return result of suricata status.
     * @return String of suricata log.
     */
    public String getStatus()
    {
        // ??? maybe use systemctl status instead
        // return UvmContextFactory.context().execManager().execOutput(GET_STATUS_COMMAND);
        return "";
    }

    /**
     * Insert or remove iptables rules if suricata daemon is running
     */
    private synchronized void iptablesRules()
    {
        File f = new File( IPTABLES_SCRIPT  );
        if( !f.exists() ){
            logger.warn("Cannot find init script:" + IPTABLES_SCRIPT);
        }

        ExecManagerResult result = UvmContextFactory.context().execManager().exec( IPTABLES_SCRIPT );
        try {
            String[] lines = result.getOutput().split("\\r?\\n");
            logger.info( IPTABLES_SCRIPT + ": ");
            for ( String line : lines )
                logger.info( IPTABLES_SCRIPT + ": " + line);
        } catch (Exception e) {
            logger.warn( "Unable to process " + IPTABLES_SCRIPT + ":", e );
        }

        if ( result.getResult() != 0 ) {
            logger.error("Failed to run " + IPTABLES_SCRIPT+ " (return code: " + result.getResult() + ")");
            throw new RuntimeException("Failed to manage iptables rules");
        }
    }

    /**
     * Get settings filename
     *
     * @return  Settings filename.
     */
    public String getSettingsFileName()
    {
        return System.getProperty("uvm.settings.dir") + "/intrusion-prevention/settings_" + this.getAppSettings().getId().toString() + ".js";
    }

    /**
     * Initialize settings.
     */
    public void initializeSettings()
    {
        SettingsManager settingsManager = UvmContextFactory.context().settingsManager();
        String appId = this.getAppSettings().getId().toString();
        String tempFileName = "/tmp/settings_" + getAppSettings().getAppName() + "_" + appId + ".js";

            // " --signatures /usr/share/untangle-suricata.config/current" +
        String configCmd = new String(System.getProperty("uvm.bin.dir") + 
            "/intrusion-prevention-sync-settings.py" + 
            " --app_id " + appId +
            " --settings " + tempFileName
        );
        String result = UvmContextFactory.context().execManager().execOutput(configCmd );
        try{
            String[] lines = result.split("\\r?\\n");
            for ( String line : lines ){
                if( line.trim().length() > 1 ){
                    logger.warn("initializeSettings: intrusion-prevention-sync-settings: " + line);
                }
            }
        }catch( Exception e ){
            logger.warn("Unable to initialize settings: ", e );
        }

        try {
            settingsManager.save( getSettingsFileName(), tempFileName, true );
        } catch (Exception exn) {
            logger.error("Could not save app settings", exn);
        }
    }

    /**
     * Save settings.
     *
     * @param tempFileName  Temporary filename.
     */
    public void saveSettings( String tempFileName )
    {
        SettingsManager settingsManager = UvmContextFactory.context().settingsManager();

        try {
            settingsManager.save( getSettingsFileName(), tempFileName, true );
        } catch (Exception exn) {
            logger.error("Could not save app settings", exn);
        }
    }

    /**
     * Set the update settings flag.
     *
     * @param updatedSettingsFlag Boolean value
     */
    public void setUpdatedSettingsFlag( boolean updatedSettingsFlag )
    {
        this.updatedSettingsFlag = updatedSettingsFlag;
    }

    /**
     * Get the update settings flag.
     *
     * @return 
     *  true updated set, false not updated..
     */
    public boolean getUpdatedSettingsFlag()
    {
        return this.updatedSettingsFlag;
    }

    /**
     * Tell the ips monitor to reload the event map.
     */
    public void reloadEventMonitorMap()
    {
        this.ipsEventMonitor.unified2Parser.reloadEventMap();
    }

    /**
     * Force stats to restart by stopping and restarting.
     */
    public void forceUpdateStats()
    {
        this.ipsEventMonitor.stop();
        this.ipsEventMonitor.start();
        try { Thread.sleep( 2000 ); } catch ( InterruptedException e ) {}
    }

    /**
     * IPS settings are managed through a download hander to get/set.
     *
     * This seems like overkill.  However, we originally supported editing all
     * signatures, resulting in a settings file around 300MB.  Managing this through
     * uvm's standard settings management causes Java garbage collection to go nuts
     * and almost always causes uvm to reload.
     * 
     * Besides not wanting to re-work uvm's GC settings, the bigger issue
     * is that uvm does not need to know anything about IPS settings;
     * everything is handled in backend scripts that manage and generate
     * configuration for Suricata.
     *
     * Therefore, the easiest way to get around the GC issue is to simply
     * make IPS settings use the download manager for downloads and uploads.
     *
     * Today we use rules as the primary means to control signatures.  In theory
     * most people will use that instead of editing signatures manually either via
     * via the UI or export/import.  However, the fact stlll remains that uvm 
     * doesn't need to know anything about IPS settings and that's why we retain
     * this method so no memory is used for maintaining IPS settings/
     *
     */
    private class IntrusionPreventionSettingsDownloadHandler implements DownloadHandler
    {
        private static final String CHARACTER_ENCODING = "utf-8";

        /**
         * IPS download/upload handler.
         *
         * @return Name of the download handler.
         */
        @Override
        public String getName()
        {
            return "IntrusionPreventionSettings";
        }
        
        /**
         * Handle upload/download.
         *
         * @param req   HTTP request
         * @param resp  HTTP response.
         */
        public void serveDownload( HttpServletRequest req, HttpServletResponse resp )
        {

            String action = req.getParameter("arg1");
            String appId = req.getParameter("arg2");

            UvmContext uvm = UvmContextFactory.context();
            AppManager nm = uvm.appManager();
            IntrusionPreventionApp app = (IntrusionPreventionApp) nm.app( Long.parseLong(appId) );

            if (app == null ) {
                logger.warn("Invalid parameters: " + appId );
                return;
            }

            if(action.equals("signatures")){
                resp.setCharacterEncoding(CHARACTER_ENCODING);
                resp.setHeader("Content-Type","text/plain");

                List<File> signatureFiles = new LinkedList<>();
                // !!! LOOP ENGINE_RULES_DIRECTORY
                getSignatureFiles( signatureFiles, new File(CURRENT_RULES_DIRECTORY));
                getSignatureFiles( signatureFiles, new File(ENGINE_RULES_DIRECTORY));

                byte[] buffer = new byte[1024];
                int read;
                FileInputStream fis = null;
                try{
                    OutputStream out = resp.getOutputStream();
                    for(File entry: signatureFiles){
                        String entryLine = "# filename: " + entry.getName() + "\n";
                        byte[] name = entryLine.getBytes(CHARACTER_ENCODING);
                        out.write(name);
                        fis = new FileInputStream(entry);
                        while ( ( read = fis.read( buffer ) ) > 0 ) {
                            out.write( buffer, 0, read);
                        }
                        fis.close();
                    }
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    logger.warn("Failed to load IPS settings",e);
                }finally{
                    try{
                        if(fis != null){
                            fis.close();
                        }
                    }catch( IOException e){
                        logger.warn("Failed to close file");
                    }
                }

            }else if( action.equals("load") ){
                String settingsName;
                    settingsName = app.getSettingsFileName();
                FileInputStream fis = null;
                try{
                    resp.setCharacterEncoding(CHARACTER_ENCODING);
                    resp.setHeader("Content-Type","application/json");

                    File f = new File( settingsName );
                    if( !f.exists() && 
                        action.equals("load") ){
                        app.initializeSettings();
                    }
                    byte[] buffer = new byte[1024];
                    int read;
                    fis = new FileInputStream(settingsName);
                    OutputStream out = resp.getOutputStream();
                
                    while ( ( read = fis.read( buffer ) ) > 0 ) {
                        out.write( buffer, 0, read);
                    }

                    fis.close();
                    out.flush();
                    out.close();

                } catch (Exception e) {
                    logger.warn("Failed to load IPS settings",e);
                }finally{
                    try{
                        if(fis != null){
                            fis.close();
                        }
                    }catch( IOException e){
                        logger.warn("Failed to close file");
                    }
                }
                app.setUpdatedSettingsFlag( false );
            }else if( action.equals("save")) {
                /*
                 * Save/uploads are a bit of a problem due to size.  For load/downloads,
                 * the settings file is automatically compressed by Apache/Tomcat from 
                 * around 30MB to 3MB which is hardly noticable.
                 *
                 * The reverse is almost never true and the client will attempt to upload 
                 * without compression.  To get around this, we receive a JSON "patch"
                 * which we pass to the configuration management scripts to integrate into settings.
                 */
                // String tempPatchName = "/tmp/changedDataSet_intrusion-prevention_settings_" + appId + ".js";
                String tempSettingsName = "/tmp/intrusion-prevention_settings_" + appId + ".js";
                int verifyResult = 1;
                FileOutputStream fos = null;
                try{
                    byte[] buffer = new byte[1024];
                    int read;
                    InputStream in = req.getInputStream();
                    fos = new FileOutputStream( tempSettingsName );

                    while ( ( read = in.read( buffer ) ) > 0 ) {
                        fos.write( buffer, 0, read);
                    }

                    in.close();
                    fos.flush();

                    String verifyCommand = new String( "python -m simplejson.tool " + tempSettingsName + "> /dev/null 2>&1" );
                    verifyResult = UvmContextFactory.context().execManager().execResult(verifyCommand);

                    String configCmd = new String(
                        System.getProperty("uvm.bin.dir") +
                        "/intrusion-prevention-sync-settings.py" +
                        " --app_id " + appId +
                        " --settings " + tempSettingsName
                    );
                    String result = UvmContextFactory.context().execManager().execOutput(configCmd );

                    try{
                        String[] lines = result.split("\\r?\\n");
                        for ( String line : lines ){
                            if( line.trim().length() > 1 ){
                                logger.warn("DownloadHandler: intrusion-prevention-sync-settings: " + line);
                            }
                        }
                    }catch( Exception e ){
                        logger.warn("Unable to initialize settings: ", e );
                    }

                    app.saveSettings( tempSettingsName );

                    File fp = new File( tempSettingsName );
                    fp.delete();
                }catch( IOException e ){
                    logger.warn("Failed to save IPS settings");
                }finally{
                    try{
                        if(fos != null){
                            fos.close();
                        }
                    }catch( IOException e){
                        logger.warn("Failed to close file");
                    }
                }

                String responseText = "{success:true}";
 
                try{
                    resp.setCharacterEncoding(CHARACTER_ENCODING);
                    resp.setHeader("Content-Type","application/json");

                    OutputStream out = resp.getOutputStream();
                    out.write( responseText.getBytes(), 0, responseText.getBytes().length );
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    logger.warn("Failed to send IPS save response");
                }
            }else if(action.equals("export")){
                String tempPatchName = "/tmp/changedDataSet_intrusion-prevention_settings_" + appId + ".js";
                String tempSettingsName = "/tmp/intrusion-prevention_settings_" + appId + ".js";

                String changedSet = req.getParameter("arg4");
                BufferedWriter writer = null;
                try{
                    /*
                     * Create a patch based on the currently changed dataset as export does elsewhere.
                     */
                    writer = new BufferedWriter( new FileWriter(tempPatchName));
                    writer.write(changedSet);
                    writer.close();

                    /*
                     * If client takes too long to upload, we'll get an incomplete settings file and all will be bad.
                     */
                    String verifyCommand = new String( "python -m simplejson.tool " + tempPatchName + "> /dev/null 2>&1" );
                    UvmContextFactory.context().execManager().execResult(verifyCommand);

                    // !!! also need ENGINE_RULES_DIRECTORY
                    String configCmd = new String(
                        System.getProperty("uvm.bin.dir") + 
                        "/intrusion-prevention-sync-settings.py" + 
                        " --app_id " + appId +
                        " --settings " + tempSettingsName + 
                        " --patch " + tempPatchName + 
                        " --export"
                    );
                    String result = UvmContextFactory.context().execManager().execOutput(configCmd );
                    try{
                        String lines[] = result.split("\\r?\\n");
                        for ( String line : lines ){
                            if( line.trim().length() > 1 ){
                                logger.warn("DownloadHandler: export, intrusion-prevention-sync-settings: " + line);
                            }
                        }
                    }catch( Exception e ){
                        logger.warn("Unable to sync export settings: ", e );
                    }

                    File fp = new File( tempPatchName );
                    fp.delete();

                }catch( IOException e ){
                    logger.warn("Failed to synchronize export IPS settings");
                }

                try{
                    String oemName = UvmContextFactory.context().oemManager().getOemName();
                    String version = UvmContextFactory.context().version().replace(".","_");
                    String hostName = UvmContextFactory.context().networkManager().getNetworkSettings().getHostName().replace(".","_");
                    String dateStr = (new SimpleDateFormat(DATE_FORMAT_NOW)).format((Calendar.getInstance()).getTime());
                    String gridName = req.getParameter("arg3");

                    String filename = oemName + "-" + version + "-" + gridName + "-" + hostName + "-" + dateStr + ".json";
                    resp.setCharacterEncoding(CHARACTER_ENCODING);
                    resp.setHeader("Content-Disposition","attachment; filename="+filename);

                    byte[] buffer = new byte[1024];
                    int read;
                    FileInputStream fis = new FileInputStream(tempSettingsName);
                    OutputStream out = resp.getOutputStream();
                
                    while ( ( read = fis.read( buffer ) ) > 0 ) {
                        out.write( buffer, 0, read);
                    }

                    fis.close();
                    out.flush();
                    out.close();

                } catch (Exception e) {
                    logger.warn("Failed to export IPS settings",e);
                }
            }
        }

        /**
         * [getSignatureFiles description]
         * @param files [description]
         * @param path  [description]
         */
        private void getSignatureFiles(List<File> files, File path){
            if(path.isDirectory()){
                File[] entries = path.listFiles();
                for(File entry : entries){
                    getSignatureFiles(files, entry);
                }
            }else{
                if(path.getName().endsWith(".rules")){
                    files.add(path);
                }
            }
            return;
        }
    }

    /*
     * The HOME_NET suricata value is highly dependent on non-WAN interface values.
     * If it changes, we must reconfigure suricata.  However, reconfiguring suricata
     * is an expensive operation due to timeto restart suricata.  To make this as painless
     * as possible, at startup we calculate initial HOME_NET value and recalc on
     * network changes.  Only if HOME_NET changes will a reconfigure occur.
     */

    /**
     * Build known networks
     *
     * @param networkSettings   Network settings.
     * @return List of IP addresses.
     */
    private List<IPMaskedAddress> calculateHomeNetworks( NetworkSettings networkSettings)
    {
        boolean match;
        IPMaskedAddress maskedAddress;
        List<IPMaskedAddress> addresses = new LinkedList<>();
        /*
         * Pull static addresses
         */
        for( InterfaceSettings interfaceSettings : networkSettings.getInterfaces() ){
            if ( interfaceSettings.getConfigType() != InterfaceSettings.ConfigType.ADDRESSED ){
                continue;
            }
            if ( interfaceSettings.getV4ConfigType() != InterfaceSettings.V4ConfigType.STATIC ){
                continue;
            }
            
            addresses.add(new IPMaskedAddress( interfaceSettings.getV4StaticAddress(), interfaceSettings.getV4StaticPrefix()));
            for ( InterfaceSettings.InterfaceAlias alias : interfaceSettings.getV4Aliases() ) {
                /*
                 * Don't add if already in list 
                 */
                match = false;
                maskedAddress = new IPMaskedAddress( alias.getStaticAddress(), alias.getStaticNetmask() );
                for( IPMaskedAddress ma : addresses ){
                    if( ma.getMaskedAddress().getHostAddress().equals( maskedAddress.getMaskedAddress().getHostAddress() ) &&
                        ( ma.getPrefixLength() == maskedAddress.getPrefixLength() ) ){
                        match = true;
                    }
                }
                if( match == false ){
                    addresses.add( maskedAddress );
                }
            }   
        }
        /*
         * Pull dynamic addresses for WAN interfaces
         */
        boolean isWanInterface;
        for( InterfaceStatus intfStatus : UvmContextFactory.context().networkManager().getInterfaceStatus() ) {
            isWanInterface = false;
            for( InterfaceSettings interfaceSettings : networkSettings.getInterfaces() ){
                if(interfaceSettings.getInterfaceId() != intfStatus.getInterfaceId()) {
                    continue;
                }
                if(interfaceSettings.getConfigType() != InterfaceSettings.ConfigType.ADDRESSED) {
                    continue;
                }
            }
            if ( intfStatus.getV4Address() == null || intfStatus.getV4Netmask() == null ){
                continue;
            }
            match = false;
            maskedAddress = new IPMaskedAddress( intfStatus.getV4Address(), intfStatus.getV4PrefixLength());
            for( IPMaskedAddress ma : addresses ){
                if( ma.getMaskedAddress().getHostAddress().equals( maskedAddress.getMaskedAddress().getHostAddress() ) &&
                    ( ma.getPrefixLength() == maskedAddress.getPrefixLength() ) ){
                    match = true;
                }
            }
            if( match == false ){
                addresses.add( maskedAddress );
            }
        }
        return addresses; 
    }

    /**
     * Build active interface identifiers.
     *
     * @param networkSettings   Network settings.
     * @return List of IP addresses.
     */
    private List<String> calculateInterfaces( NetworkSettings networkSettings )
    {
        List<String> interfaces = new LinkedList<>();
        for( InterfaceSettings interfaceSettings : networkSettings.getInterfaces() ){
            if (interfaceSettings.getConfigType() == InterfaceSettings.ConfigType.DISABLED) {
                continue;
            }
            interfaces.add( interfaceSettings.getSystemDev() );
        }
        return interfaces; 
    }

    /**
     * Compare currently known non-WAN addresses to new addresses.  
     * If they're different, trigger a reconfigure event.
     *
     * @param networkSettings   Network settings.
     * @throws Exception Exception if something happens.
     */
    private void networkSettingsEvent( NetworkSettings networkSettings ) throws Exception
    {
        List<IPMaskedAddress> newHomeNetworks = calculateHomeNetworks( networkSettings);

        boolean sameNetworks = true;
        if( newHomeNetworks.size() != this.homeNetworks.size() ){
            sameNetworks = false;
        }else{
            int minLength = Math.min( this.homeNetworks.size(), newHomeNetworks.size() );
            for( int i = 0; i < minLength; i++ ){
                if( ( this.homeNetworks.get(i).getMaskedAddress().getHostAddress().toString().equals(newHomeNetworks.get(i).getMaskedAddress().getHostAddress().toString()) == false ) ||
                    ( this.homeNetworks.get(i).getPrefixLength() != newHomeNetworks.get(i).getPrefixLength() ) ){
                    sameNetworks = false;
                }
            }
        }
        if( sameNetworks == false ){
            this.homeNetworks = newHomeNetworks;
            this.interfaceIds = calculateInterfaces(networkSettings);
            this.reconfigure();
        }
    }

    /**
     * Hook into network setting saves.
     */
    private class IntrusionPreventionNetworkSettingsHook implements HookCallback
    {
        /**
        * @return Name of callback hook
        */
        public String getName()
        {
            return "intrusion-prevention-network-settings-change-hook";
        }

        /**
         * Callback documentation
         *
         * @param args  Args to pass
         */
        public void callback( Object... args )
        {
            Object o = args[0];
            if ( ! (o instanceof NetworkSettings) ) {
                logger.warn( "Invalid network settings: " + o);
                return;
            }
                 
            NetworkSettings settings = (NetworkSettings)o;

            if ( logger.isDebugEnabled()){
                logger.debug( "network settings changed:" + settings );  
            } 
            try {
                networkSettingsEvent( settings );
            } catch( Exception e ) {
                logger.error( "Unable to reconfigure IPS" );
            }
        }
    }
}
