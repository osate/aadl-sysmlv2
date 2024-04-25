# Setting up the AADL to SysML Translator for Development
1. Follow the instructions for [Setting up an OSATE development environment](https://osate.org/setup-development.html).
2. Import the projects `org.osate.aadl2sysml`, `org.osate.aadl2sysml.tests`, and `org.osate.aadl2sysml.ui` into the
   workspace.

# Running unit tests
1. Tests can be run by right-clicking on the project `org.osate.aadl2sysml.tests` and selecting `Run As` and then
   `JUnit Plug-in Test`.

# Running the translator
1. To execute the translator, first launch OSATE from the development environment by running the `OSATE` launch
   configuration.
2. Within OSATE, either create or import an AADL project with AADL files in it.
3. For each AADL file that you want to translate, right-click on the file and select `Translate to SysML`.

# Importing the AADL library
1. Install the SysML Pilot implementation plug-ins into OSATE using the
   [SysML v2 Release Eclipse Installation](https://github.com/Systems-Modeling/SysML-v2-Release/blob/master/install/eclipse/README.adoc)
   instructions.
2. Import the project `aadl.library` into OSATE.