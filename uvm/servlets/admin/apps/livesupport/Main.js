Ext.define('Ung.apps.livesupport.Main', {
    extend: 'Ung.cmp.AppPanel',
    alias: 'widget.app-livesupport',

    items: [{
        title: 'Status'.t(),
        border: false,
        bodyPadding: 10,
        scrollable: 'y',
        items: [{
            xtype: 'component',
            cls: 'app-desc',
            html: '<img src="/skins/modern-rack/images/admin/apps/untangle-node-live-support_80x80.png" width="80" height="80"/>' +
                '<h3>Live Support</h3>' +
                '<p>' + 'Live Support provides on-demand help for any technical issues.'.t() + '</p>'
        }, {
            xtype: 'fieldset',
            title: '<i class="fa fa-life-ring"></i> ' + 'Live Support'.t(),
            padding: 10,
            margin: '20 0',
            cls: 'app-section',
            items: [{
                xtype: 'component',
                html: Ext.String.format('This {0} Server is entitled to Live Support.'.t(), rpc.companyName),
                margin: '0 0 10 0'
            }, {
                xtype: 'button',
                text: 'Get Support!'.t()
            }]
        }, {
            xtype: 'fieldset',
            title: '<i class="fa fa-info-circle"></i> ' + 'Support Information'.t(),
            padding: 10,
            margin: '20 0',
            cls: 'app-section',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 50,
                labelAlign: 'right'
            },
            items: [{
                fieldLabel: '<strong>' + 'UID'.t() + '</strong>',
                value: rpc.serverUID
            }, {
                fieldLabel: '<strong>' + 'Build'.t() + '</strong>',
                value: rpc.fullVersionAndRevision
            }]
        }, {
            xtype: 'appremove'
        }]
    }]

});