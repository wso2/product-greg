Shutterbug Sample
-----------------

Introduction
============
The idea of this sample is to demonstrate how to use the WSO2 Governance Registry to build real world applications. This sample is designed to be used to host a competition on the WSO2 Governance Registry that will select 12 images out of several hundreds of images to be published on a calendar. Each user will be allowed to upload up to 2 images, and can vote for 12 images. Finally the administrators can browse the images with the highest number of votes and decide on the winners.

Steps
-----
1. Deploy G-Reg on tomcat under a context named 'greg'. More information can be found at http://opensource-soa.blogspot.com/2009/03/deploying-wso2-registry-on-tomcat.html.

2. Copy org.wso2.governance.samples.shutterbug.ui/src/main/resources/web/shutterbug/resources/crossdomain.xml in to the tomcat's 'ROOT' context.

3. Copy the jars found in GREG_HOME/samples/shutterbug/bin to GREG_HOME/repository/components/dropins.

4. Set the Index Page URL in GREG_HOME/repository/conf/carbon.xml as follows.

<IndexPageURL>/carbon/shutterbug/shutterbug-ajaxprocessor.jsp</IndexPageURL>

5. Start the server.

6. Go to the admin console.


Cooliris 3D Embed Wall
======================
This sample uses the Cooliris 3D Embed Wall (online), to display uploaded images. More information on how to use the Cooliris 3D Embed Wall, and Terms and Conditions of usage can be found at http://www.cooliris.com/.
