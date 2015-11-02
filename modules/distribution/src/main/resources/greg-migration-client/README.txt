# WSO2 Governance Registry Migration Client
This is used to migrate RXTs created using previous versions of WSO2 API Manager.
This Client is only supported for WSO2 Governance Registry 4.6.0 to 5.0.0 Migrations

Follow the steps below
    - Visit https://docs.wso2.com/display/Governance510/Upgrading+from+the+Previous+Release
    - Follow the given instructions

This client can be used to,
    - Registry Resource Migrations (WSDL, WADL and other RXT in the registry)

How to use,
    - Start the server with -Dmigrate=<MIGRATE_VERSION> for all the migrations
            For example -Dmigrate=5.0.0 for migrate to WSO2 Governance Registry 5.0.0
    - Start the server with -DmigrateReg=true for migrate only the registry resources
