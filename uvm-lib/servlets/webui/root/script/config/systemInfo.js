if(!Ung.hasResource["Ung.SystemInfo"]) {
Ung.hasResource["Ung.SystemInfo"]=true;

Ung.SystemInfo = Ext.extend(Ung.ConfigWin, {
    panelVersion: null,
    panelRegistration: null,
    panelLicenseAgreement: null,
    panelBranding: null,
	initComponent: function() {
        this.breadcrumbs=[{title:i18n._("Configuration"), action: function(){
                this.cancelAction();
            }.createDelegate(this) },
            {title:i18n._('System Info')}];
		Ung.SystemInfo.superclass.initComponent.call(this);
	},
    onRender: function(container, position) {
    	//call superclass renderer first
    	Ung.SystemInfo.superclass.onRender.call(this,container, position);
    	this.initSubCmps.defer(1, this);
		//builds the 2 tabs
    },
    initSubCmps: function() {
		this.buildVersion();
		this.buildRegistration();
		this.buildLicenseAgreement();
        this.buildBranding();
		//builds the tab panel with the tabs
		this.buildTabPanel([this.panelVersion,this.panelRegistration,this.panelLicenseAgreement,this.panelBranding]);
        this.tabs.activate(this.panelBranding);
        this.panelVersion.disable();
        this.panelRegistration.disable();
        this.panelLicenseAgreement.disable();
    	
    },
    getTODOPanel: function(title) {
    	return new Ext.Panel({
		    title: this.i18n._(title),
		    layout: "form",
		    bodyStyle:'padding:5px 5px 0px 5px;',
			items: [{
	            xtype:'fieldset',
	            title: this.i18n._(title),
	            autoHeight:true,
	            items :[
	            	{
						xtype:'textfield',
						fieldLabel: this.i18n._('TODO'),
	                    name: 'todo',
	                    allowBlank:false,
	                    value: 'todo',
	                    disabled: true
	                }]
			}]
    	});
    },
    buildVersion: function() {
    	this.panelVersion=this.getTODOPanel("Version");
    },
    buildRegistration: function() {
    	this.panelRegistration=this.getTODOPanel("Registration");
    },
    buildLicenseAgreement: function() {
    	this.panelLicenseAgreement=this.getTODOPanel("License Agreement");
    },
    getBrandingManager: function() {
        if(rpc.brandingManager===undefined) {
            rpc.brandingManager=rpc.jsonrpc.RemoteUvmContext.brandingManager();
        }
        return rpc.brandingManager
    },
    getBrandingSettings: function(forceReload) {
        if(forceReload || this.rpc.brandingSettings===undefined) {
            this.rpc.brandingSettings=this.getBrandingManager().getBrandingSettings();
        }
        return this.rpc.brandingSettings;
    },
    buildBranding: function() {
    	var brandingSettings=this.getBrandingSettings();
    	this.panelBranding = new Ext.Panel({
            //private fields
            parentId: this.getId(),
            title: this.i18n._('Branding'),
            layout: "form",
            logo: brandingSettings.logo,
            bodyStyle:'padding:5px 5px 0px 5px;',
            autoScroll: true,
            defaults: {
                xtype:'fieldset',
                autoHeight:true,
                buttonAlign: 'left'
            },
            enableFileUpload: function(enabled) {
            	try {
                	var formItem = this.items.get(1).items.get(2);
                	if(enabled) {
                    	formItem.items.get(0).enable();
                    	//formItem.buttons[0].enable();
                	} else {
                        formItem.items.get(0).disable();
                        //formItem.buttons[0].disable();
                		
                	}
            	} catch(e) {
            	}
            },
            items: [
                {
                    bodyStyle:'padding:0px 0px 5px 0px;',
                    border: false,
                    html: this.i18n._("The Branding Settings are used to set the logo and contact information that will be seen by users (e.g. reports).")
                },
            	{
                title: this.i18n._('Logo'),
                items :[
                    {
                        xtype: 'radio',
                        name: 'logoType',
                        hideLabel: true,
                        boxLabel: 'Use Default Logo',
                        value: 'default',
                        checked: brandingSettings.logo==null,
                        listeners: {
                            "check": {
                                fn: function(elem, checked) {
                                    if(checked) {
                                        this.getBrandingSettings().logo=null;
                                        this.panelBranding.enableFileUpload(false);
                                    }
                                }.createDelegate(this)
                            }
                        }
                    },
                    {
                        xtype: 'radio',
                        name: 'logoType',
                        hideLabel: true,
                        boxLabel: 'Use Custom Logo',
                        value: 'custom',
                        checked: brandingSettings.logo!=null,
                        listeners: {
                            "check": {
                                fn: function(elem, checked) {
                                	if(checked) {
                                        this.getBrandingSettings().logo=this.panelBranding.logo;
                                        this.panelBranding.enableFileUpload(true);
                                	}
                                }.createDelegate(this)
                            }
                        }
                    },
                    {
                        fileUpload:true,
                        xtype:'form',
                        bodyStyle:'padding:0px 0px 0px 25px',
                        buttonAlign: 'left',
                        id:'upload_logo_form',
                        url: 'upload',
                        border: false,
                        items:[{
                            fieldLabel: 'File', 
                            name: 'file', 
                            inputType: 'file', 
                            xtype:'textfield',
                            disabled: (brandingSettings.logo==null),
                            allowBlank:false 
                        },{
                         xtype:'hidden',
                         name: 'type',
                         value: 'logo'
                         
                        }]
                        /*,
                        buttons :[{
                            text: this.i18n._("Upload"),
                            handler: function() {this.panelBranding.onUpload();}.createDelegate(this),
                            disabled: (brandingSettings.logo==null)
                        }]*/
                    }
                ]              
                
            }, {
                title: this.i18n._('Contact Information'),
                defaults: {
                	width: 300
                },
                items :[
                    {
                        xtype:'textfield',
                        fieldLabel: this.i18n._('Company Name'),
                        name: 'companyName',
                        allowBlank:true,
                        value: brandingSettings.companyName,
                        listeners: {
                            "change": {
                                fn: function(elem, newValue) {
                                    this.getBrandingSettings().companyName=newValue;
                                }.createDelegate(this)
                            }
                        }
                    },{
                        xtype:'textfield',
                        fieldLabel: this.i18n._('Company URL'),
                        name: 'companyUrl',
                        allowBlank:true,
                        value: brandingSettings.companyUrl,
                        listeners: {
                            "change": {
                                fn: function(elem, newValue) {
                                    this.getBrandingSettings().companyUrl=newValue;
                                }.createDelegate(this)
                            }
                        }
                    },{
                        xtype:'textfield',
                        fieldLabel: this.i18n._('Contact Name'),
                        name: 'contactName',
                        allowBlank:true,
                        value: brandingSettings.contactName,
                        listeners: {
                            "change": {
                                fn: function(elem, newValue) {
                                    this.getBrandingSettings().contactName=newValue;
                                }.createDelegate(this)
                            }
                        }
                    },{
                        xtype:'textfield',
                        fieldLabel: this.i18n._('Contact Email'),
                        name: 'contactEmail',
                        allowBlank:true,
                        value: brandingSettings.contactEmail,
                        listeners: {
                            "change": {
                                fn: function(elem, newValue) {
                                    this.getBrandingSettings().contactEmail=newValue;
                                }.createDelegate(this)
                            }
                        }
                    }
                ]
                
            }],
            onUpload : function() {
                var prova = Ext.getCmp('upload_logo_form');
                var cmp = Ext.getCmp(this.parentId); 

                var form = prova.getForm();
                form.submit( {
                    parentId: cmp.getId(),
                    waitMsg: cmp.i18n._('Please wait while your logo image is uploaded...'),
                    success: function(form,action) { 
                        var cmp = Ext.getCmp(action.options.parentId);
                        cmp.panelBranding.logo=action.result.msg;
                        cmp.getBrandingSettings().logo=cmp.panelBranding.logo;
                        //Ext.MessageBox.alert(cmp.i18n._("Successed"), cmp.i18n._("Upload Logo Successed"));
                    },
                    failure: function(form,action) {
                        var cmp = Ext.getCmp(action.options.parentId); 
                        if (action.result.msg) {
                            Ext.MessageBox.alert(cmp.i18n._("Failed"), cmp.i18n._(action.result.msg));
                        } else {
                            Ext.MessageBox.alert(cmp.i18n._("Failed"), cmp.i18n._("Upload Logo Failed")); 
                        }
                    }
                });
            }
        });
    },
    // save function
    saveAction: function() {
        if (this.validate()) {
            //disable tabs during save
            this.tabs.disable();
            rpc.brandingManager.setBrandingSettings(function (result, exception) {
                //re-enable tabs
                this.tabs.enable();
                if(exception) {Ext.MessageBox.alert(i18n._("Failed"),exception.message); return;}
                var formFile = this.items.get(1).items.get(2).items.get(0);
                if(formFile.getValue().length>0) {
                    var prova = Ext.getCmp('upload_logo_form');
                    var form = prova.getForm();
                    form.submit( {
                        parentId: this.getId(),
                        waitMsg: this.i18n._('Please wait while your logo image is uploaded...'),
                        success: function(form,action) { 
                            var cmp = Ext.getCmp(action.options.parentId);
                            cmp.panelBranding.logo=action.result.msg;
                            cmp.getBrandingSettings().logo=cmp.panelBranding.logo;
                            //Ext.MessageBox.alert(cmp.i18n._("Successed"), cmp.i18n._("Upload Logo Successed"));
                            //exit settings screen
                            cmp.cancelAction();
                        },
                        failure: function(form,action) {
                            var cmp = Ext.getCmp(action.options.parentId); 
                            if (action.result.msg) {
                                Ext.MessageBox.alert(cmp.i18n._("Failed"), cmp.i18n._(action.result.msg));
                            } else {
                                Ext.MessageBox.alert(cmp.i18n._("Failed"), cmp.i18n._("Upload Logo Failed")); 
                            }
                        }
                    });
                } else {
                	this.cancelAction();
                }
                
            }.createDelegate(this),
                    this.getBrandingSettings());
        }
    }
    
});

}