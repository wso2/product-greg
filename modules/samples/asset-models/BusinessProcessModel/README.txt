Process Governance Model
========================

Process Governance Model is use to govern different business processes defined using Business Process Execution Language(BPEL), Business Process Model and Notation(BPMN) or XML Process Definition Language(XPDL). This will process the definition files and identify the different sub tasks (service invocations, user interactions) in it and list down as Service Tasks and User Tasks. Also this sample includes a sample report generator to generate reports about the currently existing processes.

This model includes the following modules,
1. Registry extension file.
   *Process.rxt
   *ServiceTask.rxt
   *UserTask.rxt
2. Handler definition
3. Handler code
4. Reporting template.
   *process_template.jrxml
5. Report Generator code.

Populate the model
==================
You can populate the model by executing ant run-process command inside $CARBON_HOME/samples/asset_models/Populator directory.

Use Business Process Model to govern Business Processes
=======================================================

To govern the business process file, 
1. Go to resource browser.
2. Add the BPEL/BPMN/BPEL file as a resource with the mediatype as "application/vnd.wso2.registry-ext-process+xml".

You can use the sample processes (EngineeringII Example.xpdl, LoanProcess.bpel, Ticket.bpmn) to see how this work.




