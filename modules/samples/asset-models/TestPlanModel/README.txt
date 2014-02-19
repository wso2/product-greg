Test Plan Governance Model
==========================

Test Plan governance model can be use to govern test plans. It clearly define the test plan hierachy from test harness level to individual test case level. 
There are several representational levels in this model.  

Test harness 
        |
        |-- Test suite
                 |
                 |-- Test case
			     |
                             |-- Test method

Test harness can have several Test suites and a Test suite can have several Test methods and so on. Also a test suite can be in several test harnesses. Test plan model is design to represent all these associations within a Test harnesses. Also this sample includes a sample report generator to generate reports about the currently existing test suites.          


This model includes the following modules,
1. Registry extension file.
   *TestHarness.rxt
   *TestSuite.rxt
   *TestMethod.rxt
   *TestCase.rxt
2. Reporting template.
   *testsuite_template.jrxml
3. Report Generator code.


Populate the model
==================
You can populate the model by executing ant run-testplan command inside $CARBON_HOME/samples/asset_models/Populator directory.
