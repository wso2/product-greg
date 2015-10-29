@product.name@, v@product.version@
==================================

The @product.name@ is a Registry and a Repository available under the
Apache Software License v2.0 which provides the right level of structure
straight out of the box to support SOA Governance, configuration governance,
development process governance, design and run-time governance, lifecycle
management, and team collaboration in a web-friendly and community-enabled
way. It was built with enterprise metadata for SOA in mind, but really
it's up to you - the @product.name@ can hold any kind of "stuff" including
images, service descriptions, text files, office documents... and every 
resource you put in the @product.name@ becomes a center for social activity.

Eschewing the complexity (and WS-* focus) of specs like UDDI, our 
@product.name@ uses the Atom Publishing Protocol (via Apache Abdera) to offer 
a standard and RESTful simple remote interface, which can be accessed
easily by custom code, feed readers, and even browsers.

The @product.name@ has been designed both to encourage "grass-roots" community 
around your data and also to allow for IT-friendly governance.  A full 
feature list follows. We hope you'll find many uses for it, and that 
you'll share them with us as we work to make it even better!

The project home page is http://wso2.com/products/governance-registry

Please see the "release_notes.html" file in this directory for up-to-date
information on the current release, including known issues, new features,
etc.


New Features In This Release
============================
 * Create and manage registry resources, APIs and Swagger instances with the newly introduced Governance Centre UI
 * Installation provision for API-M, enabling API publisher and store functionalities, including API creation, publishing, subscription, token generation and regeneration within WSO2 Governance Registry
 * Centrally integrated with the latest version of WSO2 Enterprise Store 2.0
 * Out of the Box APIM Manager integration support
 * Graphical differentiation view to compare two inter-related assets
 * New asset association UIs and extension point to perform add/remove associations based activities
 * Improved extension points to write tasks based on registry API and governance API
 * New Governance REST API
 * Monitor time duration of artifacts in specific lifecycle states
 * Ability to set  specific lifecycle state checkpoints
 * Service and application discovery for third-party servers
 * Graphical visualization (dependency graphs) of artifacts with associated resources and dependent artifacts enabling users to perform impact analysis when changes are made to an artifact
 * Out of the box versioning support for WSDL, WADL and XSD
 * Out of the box support for Swagger imports, with facility to upload via a file or import using a link
 * Ability to specify custom storage paths for any RXT
 * Improved notifications to support clustered deployments and tenants
 * Multiple life-cycle associations for artifacts
 * Improvement in performance by solr based advance search
 * New SOAP and REST service artifacts to improve service categorization
 

Issues Fixed in This Release
============================

 * @product.name@ Issue Tracker -
       https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=@fixed.issue.filter@


Features
========

 * Storing and managing arbitrary resources and collections
 * Tagging, commenting and rating
 * Managing users and roles
 * Authentication and authorization on all resources and actions
 * Resource / collection versioning and rollback
 * Advanced search capabilities - tags, users, etc.
 * Built in media type support for common types (WSDL, XSD)
 * Built in support for known repository types
 * Dependency management - maintain relationships between dependent resources for impact analysis
 * Pluggable media type handlers for handling custom media types
 * Pluggable custom UIs for resource types
 * Activity log and monitoring with filtering support for the activity logs
 * Atom Publishing Protocol (APP) support for reading/writing the data store remotely
 * Subscribe to resources, collections, comments, tags, etc. with any standard feed reader
   (Bloglines, Google Reader, etc)
 * Intuitive User Interface with an Ajax-based Web 2.0 interface
 * Internationalization
 * Support for deploying back end and front end (UI) in separate servers
 * WSDL Tool to import service information from a given WSDL, and WSDL custom-view
 * @product.name@ federation - mounting a remote @product.name@ to a specified location
 * Based on the OSGi based WSO2 Carbon architecture: this is a unification of
   all Java based products from WSO2
 * Advanced service governance through, discovery, impact analysis, versioning and automatically
   extraction of service meta data
 * Dashboard support with gadgets with design-time and run-time governance information
 * Advanced Life cycle management with checklists, and scripting support
 * Validation policies. e.g. WSDL Validation, WS-I Validation and Schema Validation
 * Eventing and notifications
 * Supports remote links, symbolic links for resources
 * Attach remote @product.name@ instances, providing one interface for many @product.name@ instances
 * Support for processing custom URL patterns via pluggable URL handlers
 * Support for custom query languages via pluggable query processors
 * Import/export resources and collections
 * Support for WebSphere, WebLogic, and JBoss
 * Provisioning and feature management support for WSO2 Carbon family of products
 * Support for clustering
 * Check-in/Checkout tool
 * Gadgets for impact analysis on services and registry resources
 * Dynamic Handler configuration, and simulation
 * Tree-based Resource view
 * API to govern SOA artifacts
 * Complete Web Services API for Registry
 * WS-Discovery support
 * Platform, and Group/Cluster based deployment model, with support for adding remote mounts
 * Support for multiple server instances
 * E-mail verification for subscriptions
 * Hierarchical permission model with granular and extensible permissions
 * Governance Archive for uploading WSDLs and Schemas with imports
 * Rich text editor for editing text resources
 * XML editor for editing handler, lifecycle and service UI configurations
 * Support for deploying Carbon Applications (C-App)
 * Content Search
 * WebDAV support
 * Open-Social API
 * JCR API
 * UDDIv3 support
 * Cache-backed Registry
 * Resource Locking/Retention
 * Configurable Governance Artifacts
 * Conflict Resolution for Concurrent Updates
 * Adding Operators to Advanced Search API (Negation and Range Queries for Property Values)
 * UUID Support at Resource Level
 * Hierarchical Subscriptions for Collections
 * Onetime Email Verification for Subscriptions 
 * URL Referenced Assets
 * API Assets
 * Worklist Notification Bar
 * Task Scheduling for Registry
 * Support for Source Configuration Management(SCM)
 * Support for Java Management Extensions(JMX)
 * XPath Query Support for Search
 * Lifecycle Audit
 * Invocation Statistics API
 * WS API for Governance Artifacts(CRUD Operation Support)
 * Adding Executor for Services to Publish as APIs


System Requirements
===================

1. Minimum memory - 2GB

2. Processor      - Pentium 800MHz or equivalent at minimum

3. Java SE Development Kit 1.7.x or higher

4. The Management Console requires you to enable JavaScript of the Web browser,
   with MS IE 10 or above. In addition to JavaScript, ActiveX should also be enabled
   with IE. This can be achieved by setting your security level to medium or lower.

5. To build @product.name@ from the Source distribution, it is also necessary that you
   have Maven 3.0.5 or later.

For more details see
   http://docs.wso2.org/display/Governance510/Installation+Prerequisites


Project Resources
=================

* Home page          : http://wso2.com/products/governance-registry
* Library            : http://wso2.org/library/registry
* JIRA-Issue Tracker : https://wso2.org/jira/browse/REGISTRY
                     : https://wso2.org/jira/browse/CARBON
     All Open Issues : https://wso2.org/jira/secure/IssueNavigator.jspa?requestId=12424&mode=hide
* Forums             : http://stackoverflow.com/questions/tagged/wso2
* Mailing Lists
     Developer List  : dev@wso2.org 
     User List       : user@wso2.org 
     Subscribe       : http://wso2.org/mail#registry

    
Installation and Running
========================

1. Extract the downloaded zip file
2. Run the wso2server.sh or wso2server.bat file in the /bin directory
3. Once the server starts, point your Web browser to https://localhost:9443/carbon/
4. For more information, see the Installation Guide:
     Locally    : INSTALL.txt
     On the web : http://docs.wso2.org/wiki/display/Governance510/Getting+Started


Known issues of @product.name@ @product.version@
==============================================

 * @product.name@ Issue Tracker -
       https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=@known.issue.filter@


Support
=======

We are committed to ensuring that your enterprise middleware deployment is completely supported
from evaluation to production. Our unique approach ensures that all support leverages our open
development methodology and is provided by the very same engineers who build the technology.

For more details and to take advantage of this unique opportunity please visit
http://wso2.com/support/

For more information about @product.name@ please see
http://wso2.com/products/governance-registry visit the WSO2 Oxygen Tank developer portal for
additional resources.


@product.name@ Binary Distribution Directory Structure
================================================================

    CARBON_HOME
        |-- bin <folder>
        |-- dbscripts <folder>
        |-- juddi_install_data <folder>
        |-- lib <folder>
        |-- repository <folder>
        |   |-- carbonapps <folder>
        |   |-- components <folder>
        |   |-- conf <folder>
        |   |-- data <folder>
        |   |-- database <folder>
        |   |-- deployment <folder>
        |   |-- lib <folder>
        |   |-- logs <folder>
        |   |-- resources <folder>
        |   |   |-- dashboard <folder>
        |   |   |-- lifecycles <folder>
        |   |   |-- security <folder>
        |   |   |-- reports <folder>
        |   |   |-- rxts <folder>
        |   |   |-- lifecycle-config.xsd <file>
        |   |   |-- lifecycle-local.xsd <file>
        |   |   |-- rxt.xsd <file>
        |   |   |-- service-ui-config.xsd <file>
        |   |   |-- services-config.xml <file>
        |   |   `-- services-config.xsd <file>
        |   `-- tenants <folder>
        |-- samples <folder>
        |-- tmp <folder>
        |-- webapp-mode <folder>
        |-- INSTALL.txt <file>
        |-- juddiv3.properties <file>
        |-- LICENSE.txt <file>
        |-- README.txt <file>
        |-- release-notes.html <file>
	 `-- resources <folder>

    - bin
      Contains various scripts .sh & .bat scripts

    - dbscripts
      Contains the SQL scripts for setting up the database on a variety of
      Database Management Systems, including H2, Derby, MSSQL, MySQL abd
      Oracle.

    - juddi_install_data
      Installation data for the UDDI Registry

    - lib
      Contains the basic set of libraries required to start-up @product.name@
      in standalone mode

    - repository
      The repository where services and modules deployed in @product.name@
      are stored.

        - carbonapps
          Location for copying .car files (Carbon Applications)

        - components
          Contains OSGi bundles and configurations
      
        - conf
          Contains configuration files

        - data
          Contains server runtime data

        - database
          Contains the database

        - deployment
          Contains Axis2 deployment details

        - lib
          Contains all client side libraries

        - logs
          Contains all log files created during execution

        - resources
          Contains additional resources that may be required

            - dashboard
              Contains dashboard configuration files

            - lifecycles
              Contains default lifecycle configuration files

            - security
              Contains security resources

            - reports
              Location for copying .jrxml files (Jasper Report Templates)

            - rxts
              Location for copying .rxt files (Configurable Governance Artifacts)

        - tenants
          Contains tenant details

    - samples
      Contains some sample applications that demonstrate the functionality
      and capabilities of @product.name@

    - tmp
      Used for storing temporary files, and is pointed to by the
      java.io.tmpdir System property

    - webapp-mode
      Bundles required for WAR Deployment

    - INSTALL.txt
      This document will contain information on installing @product.name@

    - juddiv3.properties
      Configuration for UDDI Registry

    - LICENSE.txt
      Apache License 2.0 under which @product.name@ is distributed.

    - README.txt
      This document.

    - release-notes.html
      Release information for @product.name@ @product.version@

    - resources
      Resources that are required for @product.name@ @product.version@ themes.

Crypto Notice
=============

   This distribution includes cryptographic software.  The country in
   which you currently reside may have restrictions on the import,
   possession, use, and/or re-export to another country, of
   encryption software.  BEFORE using any encryption software, please
   check your country's laws, regulations and policies concerning the
   import, possession, or use, and re-export of encryption software, to
   see if this is permitted.  See <http://www.wassenaar.org/> for more
   information.

   The U.S. Government Department of Commerce, Bureau of Industry and
   Security (BIS), has classified this software as Export Commodity
   Control Number (ECCN) 5D002.C.1, which includes information security
   software using or performing cryptographic functions with asymmetric
   algorithms.  The form and manner of this Apache Software Foundation
   distribution makes it eligible for export under the License Exception
   ENC Technology Software Unrestricted (TSU) exception (see the BIS
   Export Administration Regulations, Section 740.13) for both object
   code and source code.

   The following provides more details on the included cryptographic
   software:

   Apache Rampart   : http://ws.apache.org/rampart/
   Apache WSS4J     : http://ws.apache.org/wss4j/
   Apache Santuario : http://santuario.apache.org/
   Bouncycastle     : http://www.bouncycastle.org/

For further details, see the WSO2 Carbon documentation at
http://docs.wso2.org/wiki/display/@carbon.version@/WSO2+Carbon+Documentation

---------------------------------------------------------------------------
(c) @copyright.year@, WSO2 Inc.


