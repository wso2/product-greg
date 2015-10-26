Registry Asset Populator
============================

 This will automatically 

  1. Add WSDL, WADL and schemas to the WSO2 Governance Registry.
  2. Create respective soap and rest services
 

Command to Populate Governance Models
--------------------------------------
ant client - ant run
Linux shell script - sh populator.sh

Categorize Artifacts
--------------------------------------
ant rxt-modify command will add the category field to REST and SOAP service RXTs
ant categorize command will assign random categories to all REST and SOAP services

NOTE: Make sure you do not have actual service metadata and have only sample data when you run categorization ant tasks. It will modify all REST and SOAP service artifacts in the registry.
