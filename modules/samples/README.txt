WSO2 Governance Registry Samples
--------------------------------

1.) asset-model

This sample contains a set of Pre-built metadata models to demonstrate how registry extensions are being created and  how to create a corresponding report to registry extension artifacts. This sample contains following sub samples which are made to create different sets of RXTs in accordance with corresponding scenarios. 

	i)   Application Model
	ii)  Business Process Model
        iii) People Model
        iv)  Project Model
         v)  Test Plan Model 

2.) custom-ui 

This sample demonstrates how a simple custom UI can be created to add, view, and edit, endpoints stored on the WSO2 Governance Registry. Please note that the Resource Name, Endpoint Name and the Endpoint URI are mandatory fields, and The URI must be valid.

3.) filesampleutils

This sample demonstrates how to export a local file system into a remote registry, and how to import a remote registry into a local file system.  User can build content in the local file system with the folder hierarchy prefered, including any type of content.  Then the user can use the registry API to export the local file system into the registry.Once upload a local file system into the registry it is "socially enabled".

User can also build the hierarchy in Registry with a prefered structure, and it is possible to download or import the remote registry into local file system.  Then the registry will create the exact same folder structure and download all the files in the registry.

4.) handler

The idea of this sample is to demonstrate how to use media type handlers. The sample application is to process project proposals, so this sample uses a component that intercepts Registry put() requests with a particular "proposal" media-type.  The Handler will read the proposal and mark it as valid or not.  In order to be a valid proposal there should be a number of fields - if all of them are there then the handler will mark the proposal as valid.  If one or more fields are missing then the proposal would be invalid.

5.) jaxr

JAXR enables Java  programmers to use an  abstraction API to access a variety of XML registries. This sample creates an organization and a service in UDDI registry and browses through the UDDI registry to find that organization and service.

6.) jcr-client

JCR Client is using the JCR implementation of WSO2 Governance Registry and does sample operations through JCR API to the running G-Reg instance.
 
7.) ws-client

The WS Registry API exposes a complete set of registry operations that can be accessed through Web Service calls. This sample explains how to uses the WS Registry API as an client.

8.) ws-executor

This sample explains how the Web Service Executor can be used in lifecycle configurations to invoke a Web Service in a synchronous manner.
