Ext.define('Ung.view.reports.EventReport', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.eventreport',

    viewModel: {
        data: { eventsData: [], propsData: [] },
        stores: {
            events: {
                data: '{eventsData}',
                listeners: {
                    datachanged: 'onDataChanged'
                }
            },
            props: {
                data: '{propsData}'
            }
        }
    },

    config: {
        widget: null
    },

    layout: 'border',

    border: false,
    bodyBorder: false,

    defaults: {
        border: false
    },

    items: [{
        xtype: 'ungrid',
        stateful: true,
        itemId: 'eventsGrid',
        reference: 'eventsGrid',
        region: 'center',
        bind: '{events}',
        plugins: ['gridfilters'],
        selModel: {
            type: 'rowmodel'
        },
        // emptyText: '<p style="text-align: center; margin: 0; line-height: 2;"><i class="fa fa-info-circle fa-2x"></i> <br/>No Records!</p>',
        enableColumnHide: true,
        listeners: {
            select: 'onEventSelect'
        }
    }, {
        xtype: 'unpropertygrid',
        itemId: 'eventsProperties',
        reference: 'eventsProperties',
        region: 'east',
        title: 'Details'.t(),
        collapsible: true,
        collapsed: true,
        animCollapse: false,
        titleCollapse: true,
        bind: {
            source: '{eventProperty}',
        }
    }],

    controller: {
        control: {
            '#': {
                afterrender: 'onAfterRender',
                deactivate: 'onDeactivate'
            }
        },

        listen: {
            global: {
                defaultcolumnschange: 'onDefaultColumn'
            }
        },

        onAfterRender: function (view) {
            var me = this, vm = this.getViewModel();

            // find and set the widget component if report is rendered inside a widget
            view.setWidget(view.up('reportwidget'));

            // hide property grid if rendered inside widget
            if (view.getWidget()) {
                view.down('unpropertygrid').hide();
            }

            vm.bind('{entry}', function (entry) {
                if (!entry || entry.get('type') !== 'EVENT_LIST') { return; }
                me.entry = entry;
                me.tableConfig = Ext.clone(TableConfig.getConfig(me.entry.get('table')));
                if (view.getWidget()) {
                    me.setupGrid();
                    DashboardQueue.addFirst(view.getWidget());
                } else {
                    vm.set('eventsData', []);
                    me.fetchData(true);
                }
            });

            // vm.bind('{entry}', function (entry) {
            //     if (!entry || entry.get('type') !== 'EVENT_LIST') { return; }
            //     console.log('step1');
            //     me.entry = entry;
            //     me.tableConfig = Ext.clone(TableConfig.getConfig(me.entry.get('table')));
            // });

            // vm.bind('{eEntry}', function (entry) {
            //     if (!entry || entry.get('type') !== 'EVENT_LIST') { return; }
            //     console.log('step2');
            //     me.entry = entry;
            //     me.tableConfig = Ext.clone(TableConfig.getConfig(me.entry.get('table')));
            // });


            // // update tableColumns when table is changed
            // vm.bind('{eEntry.table}', function (table) {
            //     console.log(table);
            //     if (!table) {
            //         vm.set('tableColumns', []);
            //         return;
            //     }
            //     var tableConfig = TableConfig.generate(table);

            //     if (vm.get('eEntry.type') !== 'EVENT_LIST') {
            //         vm.set('tableColumns', tableConfig.comboItems);
            //         return;
            //     }

            //     // for EVENT_LIST setup the columns
            //     var defaultColumns = Ext.clone(vm.get('eEntry.defaultColumns'));

            //     // initially set none as default
            //     Ext.Array.each(tableConfig.comboItems, function (item) {
            //         item.isDefault = false;
            //     });

            //     Ext.Array.each(vm.get('eEntry.defaultColumns'), function (defaultColumn) {
            //         var col = Ext.Array.findBy(tableConfig.comboItems, function (item) {
            //             return item.value === defaultColumn;
            //         });
            //         // remove default columns if not in TableConfig
            //         if (!col) {
            //             vm.set('eEntry.defaultColumns', Ext.Array.remove(defaultColumns, defaultColumn));
            //         } else {
            //             // otherwise set it as default
            //             col.isDefault = true;
            //         }
            //     });
            //     console.log(tableConfig.comboItems);
            //     vm.set('tableColumns', tableConfig.comboItems);
            //     me.fetchData();
            // });
        },

        setupGrid: function () {
            var me = this, vm = me.getViewModel(), grid = me.getView().down('grid');

            if (!me.entry) { return; }

            if (me.getView().up('reportwidget')) {
                me.isWidget = true;
            }

            me.tableConfig = Ext.clone(TableConfig.getConfig(me.entry.get('table')));
            me.defaultColumns = me.isWidget ? vm.get('widget.displayColumns') : me.entry.get('defaultColumns'); // widget or report columns

            Ext.Array.each(me.tableConfig.fields, function (field) {
                if (!field.sortType) {
                    field.sortType = 'asUnString';
                }
            });

            Ext.Array.each(me.tableConfig.columns, function (column) {
                if (me.defaultColumns && !Ext.Array.contains(me.defaultColumns, column.dataIndex)) {
                    column.hidden = true;
                }
                // TO REVISIT THIS BECASE OF STATEFUL
                // grid.initComponentColumn(column);
                if (column.rtype) {
                    column.renderer = 'columnRenderer';
                }
            });

            grid.reconfigure(me.tableConfig.columns);

            var propertygrid = me.getView().down('#eventsProperties');
            vm.set('eventProperty', null);
            propertygrid.fireEvent('beforerender');
            propertygrid.fireEvent('beforeexpand');

            // me.fetchData();
            // if (!me.getView().up('reportwidget')) {
            //     me.fetchData();
            // }
        },

        onDefaultColumn: function (defaultColumn) {
            var me = this, vm = me.getViewModel(),
                grid = me.getView().down('ungrid'),
                entry = vm.get('eEntry');
            if (!entry) { return; }
            Ext.Array.each(grid.getColumns(), function (column) {
                if (column.dataIndex === defaultColumn.get('value')) {
                    column.setHidden(!defaultColumn.get('isDefault'));
                    if (defaultColumn.get('isDefault')) {
                        entry.get('defaultColumns').push(column.dataIndex);
                    } else {
                        Ext.Array.remove(entry.get('defaultColumns'), column.dataIndex);
                    }

                }
            });
        },


        onDeactivate: function () {
            this.modFields = { uniqueId: null };
            this.getViewModel().set('eventsData', []);
            this.getView().down('grid').getSelectionModel().deselectAll();
        },

        fetchData: function (reset, cb) {
            var me = this,
                vm = me.getViewModel(),
                // entry = vm.get('eEntry') || vm.get('entry'),
                reps = me.getView().up('#reports'),
                startDate, endDate;

            if (!me.entry) { return; }

            if (reset) {
                vm.set('eventsData', []);
                me.setupGrid();
            }


            var limit = 1000;
            if (me.getView().up('entry')) {
                limit = me.getView().up('entry').down('#eventsLimitSelector').getValue();
            }

            // date range setup
            if (!me.getView().renderInReports) {
                // if not rendered in reports than treat as widget so from server startDate is extracted the timeframe
                startDate = new Date(Util.getMilliseconds() - (Ung.dashboardSettings.timeframe * 3600 || 3600) * 1000);
                endDate = null;
            } else {
                // if it's a report, convert UI client start date to server date
                startDate = Util.clientToServerDate(vm.get('f_startdate'));
                endDate = Util.clientToServerDate(vm.get('f_enddate'));
            }

            me.getView().setLoading(true);
            if (reps) { reps.getViewModel().set('fetching', true); }

            Rpc.asyncData('rpc.reportsManager.getEventsForDateRangeResultSet',
                            me.entry.getData(), // entry
                            vm.get('globalConditions'), // etra conditions
                            limit,
                            startDate, // start date
                            endDate) // end date
                .then(function(result) {
                    if (!me.getView()) { return; }
                    me.getView().setLoading(false);
                    if (reps) { reps.getViewModel().set('fetching', false); }
                    me.loadResultSet(result);

                    if (cb) { cb(); }
                });
        },

        loadResultSet: function (reader) {
            var me = this, vm = me.getViewModel(), grid = me.getView().down('grid');

            // this.getView().setLoading(true);
            grid.getStore().setFields( me.tableConfig.fields );
            var eventData = [];
            var result = [];
            while( true ){
                result = reader.getNextChunk(1000);
                if(result && result.list && result.list.length){
                    result.list.forEach(function(value){
                        eventData.push(value);
                    });
                    continue;
                }
                break;
            }
            reader.closeConnection();
            vm.set('eventsData', eventData);
        },

        onEventSelect: function (el, record) {
            var me = this, vm = this.getViewModel(), propsData = [];

            if (me.isWidget) { return; }

            Ext.Array.each(me.tableConfig.columns, function (column) {
                propsData.push({
                    name: column.header,
                    value: record.get(column.dataIndex)
                });
            });

            vm.set('propsData', propsData);
            // when selecting an event hide Settings if open
            if (me.getView().up('entry')) {
                me.getView().up('entry').lookupReference('settingsBtn').setPressed(false);
            }

        },

        onDataChanged: function(){
            var me = this,
                v = me.getView(),
                vm = me.getViewModel();

            if( vm.get('eventProperty') == null ){
                v.down('grid').getSelectionModel().select(0);
            }

            if( v.up().down('ungridstatus') ){
                v.up().down('ungridstatus').fireEvent('update');
            }
        }
    }
});
