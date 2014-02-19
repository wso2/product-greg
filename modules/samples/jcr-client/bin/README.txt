Running the JCR Client sample
=====================================

JCR Client is using the JCR implementation of WSO2 G-Reg and doing sample operations through JCR API to the running G-Reg instance.

Pre - Requirements
==================

1. Before getting the sample to work you have to run ant from GREG_HOME/bin. This copies all the necessary libraries to their respective locations.

2. Start the WSO2 Governance Registry by running the respective script (wso2server.sh or wso2server.bat) from GREG_HOME/bin.


Steps for running this sample
--------------------------------

1. Change the directory to the src directory (GREG_HOME/samples/jcr-client/src) of the JCR sample.

2. Run the ant command "ant", which will add a set of Collections and Resources to the Registry.

3. A collection will be created in root by the name of jcr_system (i.e., path = /jcr_system). This can be checked through the Resource Browser which can be accessed through the Management Console.