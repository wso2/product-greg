Exporting/importing a file to Remote registry
============================================

Introduction
============
This sample demonstrates how to export a local file system into a remote registry, and how to import a remote registry into a local file system.  We can build our content in the local file system with the folder hierarchy we want, including any type of content.  Then we can use the registry API to export our local file system into the registry.

Once we upload a local file system into the registry we have "socially enabled" it.  We can comment on resources, we can tag, we can rate, etc... 

We can also build our hierarchy in the Registry with the structure we want, and then we can download or import the remote registry into our local file system.  Then the registry will create the exact same folder structure and download all the files in the registry.

Running the sample
==================

Running the sample is just a matter of executing an ant file.  If you don't have ant, you can download it at http://ant.apache.org/. 

Setting the required environment
--------------------------------
Run "ant" at the "GREG_HOME/bin" folder of the distribution.

Jars you may need to run the sample
-----------------------------------
1.jaxen-1.1.1.jar (http://www.java2s.com/Code/JarDownload/jaxen-1.1.1.jar.zip)

Place all those jars in "GREG_HOME/lib" folder
   
Exporting local file system into a remote registry 
--------------------------------------------------

Run "ant upload" inside the "GREG_HOME/samples/filesampleutils/src" folder.  You will be asked for:

 - Key Store file path : Provide GREG_HOME/repository/resources/security/client-truststore.jks
 - Registry URL : Provide "https://localhost:9443/registry"
                     (If your registry is in somewhere else then give that path)
 - User name : If you have not changed the admin user then pass "admin"
 - Password : Use "admin"
 - FromPath : Location in the file system to export from.  Enter the
                 full path to the "resources" folder(eg:- C:\test\test.txt)
 - ToPath : Where to put the resource.  In this sample let's use
               "/sample/file"

 - If you login to the management console you can see that all the files have been
  moved and the registry has the same structure as the filesystem under 'Resources' listing.


Importing a file into the local file system
-----------------------------------------------

Now we go the other way.  Again, run "ant download" first, and provide values:

  - Key Store file path : Provide GREG_HOME/repository/resources/security/client-truststore.jks
  - Registry URL : Provide "https://localhost:9443/registry"
                    (If your registry is in somewhere else then give that path)
  - User name : If you have not changed the admin user then pass "admin"
  - Password : Use "admin"
  - FromPath :  Resource path, such as "/sample/file"
  - ToPath : Where to put the resource in the local filesystem. For this
               sample let's use "C:\Documents and Settings\test"

- Once you run ant you will see a newly created folder hierarchy in the path you specified.
