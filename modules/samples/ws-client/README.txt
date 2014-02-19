Running the WS Registry Client sample
=====================================

The WS Registry API exposes a complete set of registry operations that can be accessed through Web Service calls. This readme explains how to run the sample that uses the WS Registry client sample.

Pre - Requirements
==================

1. Before getting the sample to work you have to run ant from GREG_HOME/bin. This copies all the necessary libraries to their respective locations.

2. Start the WSO2 Governance Registry by running the respective script (wso2server.sh or wso2server.bat) from GREG_HOME/bin.


Steps for running this sample
--------------------------------

1. Execute the ant command 'ant run' from the ws-client sample directory.
 
2. Each operation that occurred will be printed on the console.

3. A resource will be created in root by the name of abc (i.e. path = \abc). This can be checked through the Resource Browser which can be accessed through the Management Console.
