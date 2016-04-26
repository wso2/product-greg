Sample Assets Populator
============================

 This will automatically add the following assets.

  1. Sample WSDL, WADL, schemas, soap services, rest services, swaggers and policies.
  2. Add categories and tags to soap and rest services for demonstration purposes.
 

Command to add sample assets and add categories to Governance Models
---------------------------------------------------------------------
First start the server.
Then enter following command inside $GREG_HOME/samples/getting_started directory.
ant run (This command will add sample categories and tags to added rest and soap services. )
(If server is running on a different port(for ex: 9445) then enter the following command.
ant run -Dport=9445
or to specify the IP address(for ex: 192.168.1.2) run the following command
ant run -Dhost=192.168.1.2
or to specify both port and IP address run the following command
ant run -Dport=9445 -Dhost=192.168.1.2)
Go to "Governance Center - Store" (https://localhost:9443/) to view the added assets and categories.

Command to remove all added sample assets and categories.
---------------------------------------------------------
Enter following command inside $GREG_HOME/samples/getting_started directory.
ant remove (make sure the server is running)
(This will re deploy the original rest and soap service RXTs.)
(If server is running on a different port(for ex: 9445) then enter the following command.
ant remove -Dport=9445
or to specify the IP address(for ex: 192.168.1.2) run the following command
ant remove -Dhost=192.168.1.2
or to specify both port and IP address run the following command
ant remove -Dport=9445 -Dhost=192.168.1.2)

Note -:
       Apart from above ant commands shell scripts are provided only to add sample assets and to remove them. Please note that these will
       not add categories. Following are the commands to execute shell scripts.

       Add sample assets
       ------------------
       First start the server.
       Then enter following command inside $GREG_HOME/samples/getting_started directory.
       sh populator.sh

       Remove sample assets
       ---------------------
       First start the server.
       Then enter following command inside $GREG_HOME/samples/getting_started directory.
       sh cleanup.sh


