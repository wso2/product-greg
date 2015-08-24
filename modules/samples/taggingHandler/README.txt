CustomTagServiceHandler Sample
-----------------------------
Introduction
============
The idea of this sample is to apply namespace as a Tag while creating the service. A tag is a keyword that
allows to find a resource or collection while using the Search function. With this you can retrieve the resource 
by the namespace tag.

Steps
-----
1. Execute the maven command 'mvn clean install' from the taggingHandler/src sample directory.

2. Copy the 'org.wso2.carbon.registry.samples.taggingHandler-5.0.0.jar' into GREG_HOME/repository/components/dropins.

   You also can upload handlers through the administration console by visiting the Extensions --> Add menu.

3. Edit the registry.xml file which is in 'GREG_HOME/repository/conf' folder with the following xml snippet.

    	<handler class="org.wso2.carbon.registry.samples.taggingHandler.CustomTagServiceHandler">
        	<filter class="org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher">
           		 <property name="mediaType">application/vnd.wso2-service+xml</property>
       		</filter>
    	</handler>

   You also can add the handler configuration through the administration console by opening the
   'Handlers' configuration (Extensions --> Configure --> Handlers) menu and clicking on 'Add New Handler'.

4. Start the server.

5. Go to admin console.

6. Browse the Metadata Add list and select services.

7. Add a new service.

8. Save the service.

9. You could see the Tag with the namespace have been added to the resource.
