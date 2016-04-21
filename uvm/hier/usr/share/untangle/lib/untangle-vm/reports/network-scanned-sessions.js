{
    "category": "Network",
    "type": "EVENT_LIST",
    "conditions": [
        {
            "column": "bypassed",
            "javaClass": "com.untangle.node.reports.SqlCondition",
            "operator": "is",
            "value": "false"
        }
    ],
    "defaultColumns": ["time_stamp","username","hostname","protocol","c_client_port","s_server_addr","s_server_port"],
    "description": "All sessions that were not bypassed.",
    "displayOrder": 1020,
    "javaClass": "com.untangle.node.reports.ReportEntry",
    "table": "sessions",
    "title": "Scanned Sessions",
    "uniqueId": "network-cCHSVFktsM"
}