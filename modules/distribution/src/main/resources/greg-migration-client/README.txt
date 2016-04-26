# WSO2 Governance Registry Migration Client
This contains several migration clients.

MigrateFrom460To500:
This is used to migrate RXTs created using previous versions of WSO2 API Manager.
This Client is only supported for WSO2 Governance Registry 4.6.0 to 5.0.0 Migrations

EmailUserNameMigrationClient:
This client migrate assets which have e-mail user names in resource path.

ProviderMigrationClient:
This client remove "provider" child from the resource content.

MigrateFrom510To520:
This client remove the old store tenant configuration (store.json) coming from G-Reg 5.1.0.

Follow the steps below
    - Visit https://docs.wso2.com/display/Governance520/Upgrading+from+the+Previous+Release
    - Follow the given instructions

How to use,
    - Start the server with -Dmigrate=<MIGRATE_VERSION> for all the migrations
            For example -Dmigrate=5.0.0 for migrate to WSO2 Governance Registry 5.0.0
                        -Dmigrate=5.2.0 to clean old store.json file
    - Start the server with -DmigrateReg=true for migrate only the registry resources
