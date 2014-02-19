People Governance Model
=======================

People governance model can be use to govern organizational human resources. It clearly define the organizational hierachy from Organizational level to individual person level. When you add different people to different post in the organization you can see it as a association of that person. 
There are several representational levels in this model.  

Organization 
        |
        |-- Department
                 |
                 |-- Project group
			      |
                              |--Person  

Organization can have several Departments and a Department can have several Project group and so on. Also people in the organization are holding different status like Organization president, Department head, Project manager. People model is design to represent all these associations within a organization. Also this sample includes a sample report generator to generate reports about the currently existing reports organizations.          


This model includes the following modules,
1. Registry extension file.
   *organization.rxt
   *department.rxt
   *project-group.rxt
   *person.rxt
2. Reporting template.
   *organization_template.jrxml
3. Report Generator code.


Populate the model
==================
You can populate the model by executing ant run-people command inside $CARBON_HOME/samples/asset_models/Populator directory.
