Ext.define('Ung.widget.WidgetController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.widget',

    control: {
        '#': {
            afterrender: 'onAfterRender',
            afterdata: 'onAfterData'
            //show: 'onShow'
        }
    },

    // listen: {
    //     store: {
    //         '#stats': {
    //             datachanged: 'onStatsUpdate'
    //         }
    //     }
    // },

    init: function (view) {
        var vm = view.getViewModel(), entryType;
        if (vm.get('entry')) {
            entryType = vm.get('entry.type');
            if (entryType === 'TIME_GRAPH' || entryType === 'TIME_GRAPH_DYNAMIC') {
                view.add({ xtype: 'timechart', reference: 'chart', height: 250 });
            }

            if (entryType === 'PIE_GRAPH') {
                view.add({ xtype: 'piechart', reference: 'chart',  height: 250 });
            }

            if (entryType === 'EVENT_LIST') {
                view.add({ xtype: 'component', html: 'Not Implemented',  height: 250 });
            }
        }
    },

    onAfterRender: function (widget) {
        widget.getViewModel().bind('{widget.enabled}', function (enabled) {
            if (enabled && Ext.isFunction(widget.fetchData)) {
                Ung.view.dashboard.Queue.add(widget);
            }
        });
    },

    onAfterData: function () {
        var widget = this.getView();
        Ung.view.dashboard.Queue.next();
        if (widget.refreshIntervalSec && widget.refreshIntervalSec > 0) {
            widget.refreshTimeoutId = setTimeout(function () {
                Ung.view.dashboard.Queue.add(widget);
            }, widget.refreshIntervalSec * 1000);
        }
    },

    onShow: function (widget) {
        console.log('on show', widget.getViewModel().get('widget.type'));
        if (Ext.isFunction(widget.fetchData)) {
            Ung.view.dashboard.Queue.add(widget);
        }
    },

    fetchData: function () {
        var widget = this.getView();
        if (widget.refreshTimeoutId) {
            clearTimeout(widget.refreshTimeoutId);
        }
        Ung.view.dashboard.Queue.addFirst(widget);
    },


    // not used
    resizeWidget: function () {
        var view = this.getView();
        if (view.hasCls('small')) {
            view.removeCls('small').addCls('medium');
        } else {
            if (view.hasCls('medium')) {
                view.removeCls('medium').addCls('large');
            } else {
                if (view.hasCls('large')) {
                    view.removeCls('large').addCls('x-large');
                } else {
                    if (view.hasCls('x-large')) {
                        view.removeCls('x-large').addCls('small');
                    }
                }
            }
        }
        view.updateLayout();
    }

});
