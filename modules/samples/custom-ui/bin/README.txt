
This file explains the usage of the custom UI sample (Adding endpoint references as custom resources).
======================================================================================================

This sample demonstrates how a simple custom UI can be created to add, view, and edit, endpoints stored on the WSO2 Governance Registry. Please note that the Resource Name, Endpoint Name and the Endpoint URI are mandatory fields, and The URI must be valid.

Adding an endpoint reference as a resource
------------------------------------------

1. Drop the sample's jars into GREG_HOME/repository/components/dropins.

      -  Copy "org.wso2.carbon.registry.samples.custom.topics-@product.version@.jar" into the "dropins" folder.
      -  Copy "org.wso2.carbon.registry.samples.custom.topics.ui-@product.version@.jar" into the "dropins" folder.

2. Restart the server and Log in to the admin console and go to the Resources menu.

3. Click "Add Resource" link to add a new resource and choose "Create custom content" from Method combo box. 

4. A text box named "Media type" will be displayed, from which select "other" option from drop down button for media type.

5. Enter "epr" as the media type.

6. Click "Create Content". A form to fill endpoint details will be displayed.

7. Fill the form and click "Save'. The resource  will be added. 

Browsing the resource
---------------------

1. Browse the added resource from the Entries table and click on it.

2. The content of the resource will be shown in a custom view. 

3. The content can be edited by clicking "Edit endpoint".
