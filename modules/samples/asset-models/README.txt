WSO2 Governance Registry @product.version@ Asset-Models Sample
==============================================================

The purpose of this sample is to demonstrate how a registry extension is created. Additionally it shows how to create Reports to registry extension artifacts.

Registry extension includes the following components,
1. Registry extension file (.rxt) which define new metadata type. Some metadata model can have sevaral registry extension files. For more information on how to create a registry extension file please refer http://docs.wso2.org/wiki/display/@product.version.docs@/Configurable+Governance+Artifacts.
2. Handler definition and Handler code (.jar) for the new metatype (Not mandatory). For more information on how to create a registry handler 
please refer http://docs.wso2.org/wiki/display/@product.version.docs@/Extensions.

In each this samples you can find the registry extension files as well as the hanlder code.

Report generation includes,
1. Report template which is a jasper report template (.jrxml). You can use jasper reporting guide to create the jasper report template.
2. Report generator code which use to populate the data to the report from the registry.

In each this samples you can find sample report templates as well as report generator code. 

Asset models
============

In this sample we introduce five new asset models.

1. People Governance Model.
2. Process Governance Model.
3. Test Plan Governance Model.
4. Application Governance Model.
5. Project Governance Model.

For further details on these asset models please refer the README.txt files inside each asset model.

Populating the extensions
=========================

Populating instructions
-----------------------
1. Start WSO2 Governance Registry if its not already started.
2. Open a command line.
3. Change directory to $CARBON_HOME/samples/asset_models/Populator directory.
4. Choose the appropriate ant command for the asset model you want to populate from following list and execute in the command line. If you don't have ant, you can download it at http://ant.apache.org/.
    * People Governance Model - 'ant run-people'
    * Process Governance Model - 'ant run-process'
    * Test Plan Governance Model - 'ant run-testplan'
    * Application Governance Model - 'ant run-application'
    * Project Governance Model - 'ant run-project'
5. Sign-out if you are already signed-in then sign-in. 

This will automatically 

  1. Add Registry Extension files to the WSO2 Governance Registry.
  2. Add Extension jar files needed.
  3. Add Handlers.

You can check whether they are added using the management console.
1. Registry extension files in "/_system/governance/repository/components/org.wso2.carbon.governance/types".
2. New artifact types in Metadata -> Add/List.
3. Handler definition in Extensions -> Configure -> Handlers if available.
4. Handler jar in Extensions -> Extensions -> List  if available.
5. Reporting template in "/_system/governance/repository/components/org.wso2.carbon.governance/templates".
6. Report list in Resources -> Reports. 

Using extended asset model
==========================

After populating the asset sample model, 
1. You can add/list/delete/update artifacts related to new assert model.  
2. You can use the added reporting model in Resources -> Reports to generate/schedule reports for the new asset model.
