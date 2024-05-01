This repository contains the source code for an experimental translator from
SysMLv2 to AADL. It reads SysML files or retrieves a SysML project from a SysML
repository via the REST API.

# Building the importer

## Set up the development environment

These instructions assume some familiarity with Eclipse.

### Install Eclipse 2023-12 Modeling Tools

Download the Eclipse installer from https://eclipse.org. 

Start the installer, switch to advanced mode, select _Eclipse Modeling Tools_ as the product, product version _2023-12_, and a Java 17 VM.

During installation: accept license, trust unsigned content

### Install OSATE plugins

In Eclipse: Help > Install New Software...

Type _https://osate-build.sei.cmu.edu/download/osate/stable/2.14.0/updates_ into the "Work with" text field > Enter

Check _OSATE2 for AADL2_ and _Uncategorized_ > Next > (wait a bit) > Next

Accept licenses > Finish

In the "Trust authorities" dialog check all three update sites > Trust Selected

In the "Trust Unsigned" dialog > Select All > Trust Selected

Restart to finish the installation

### Import SysMLv2 pilot implementation plugin source code

In Eclipse: File > Import... > Oomph > Projects from Catalog > Next

Click the green '+' icon

Select catalog _Github Projects_ and enter Resource URI

_https://raw.githubusercontent.com/Systems-Modeling/SysML-v2-Pilot-Implementation/master/org.omg.sysml.installer/SysML2.setup_

Click OK

Select project _Github Projects_ > _<User>_ > SysML2 and click Next

In the "Variables" dialog

* Select a location for the clone of the SysML2 git repository
* Select _HTTPS (read-only, annonymous)_ for the "SysML2 Github repository"
* Select a Java 17(!) version for the "JRE 11 Location"

Click Next, then Finish

Trust authorities and unsigned content

Finally, click Finish to restart

In Eclipse open the Java perspective, click on the blinking icon at the bottom, click Finish to restart again

Click finish once the setup actions are done

In the Package Explorer view show working sets

Close jupyter related projects 

*  jupyter-sysml-kernel
*  org.omg.sysml.interactive
*  org.omg.sysml.interactive.tests
*  org.omg.sysml.jupyter.installer
*  org.omg.sysml.jupyter.jupyterlab

Remove the entry _com.ibm.icu.impl.text;version="58.2.0",_ from file MANIFEST.MF in project _org.omg.sysml_

### Get translator sources

In Eclipse: File > Import... > Git > Projects from Git > Next > Clone URI

Enter URI _https://github.com/osate/aadl-sysmlv2.git_ > Next > Next

Enter the location for the repository clone > Next > Next
NOTE: The tests assume that the sysml pilot implementation and the repository are cloned into the same git root directory. 
The tests read the SI.sysml library file from the pilot implementation, see SysMLTestHelper line 45.

Select projects for import:
* aadl.library
* org.osate.sysml.importer
* org.osate.sysml.importer.test

Run the tests:

Right click on project _org.osate.sysml.importer.test_ int the project explorer view > Run As > JUnit Test

## Create the runnable jar

Select project *org.osate.sysml.importer* in the project explorer view.

*File* > *Export*... > *Java* > *Runnable JAR file* > *Next*

In the "Runnable JAR File Export" dialog:

1. Select launch configuration *SysML2AADLUtil*
2. Select a directory and file name for the jar file, e.g., 
   *org.osate.sysml.importer/export/sysml2aadl.jar*
3. Select library handling *Package required libraries into generated JAR*

*Finish*

# Using the importer

The importer requires Java 17 or greater.

`java -jar sysml2aadl.jar` will print a usage message.

Synopsis:

`java -jar sysml2aadl.jar -a path -s path -o path -v file ...`

The form reads files and translates them to AADL.

`java -jar sysml2aadl.jar -a path -s path -o path iv -b URL project ...`

This form reads projects from a SysML repository via its REST API.

The following options are available:

-a <u>path</u>

Read the SysML library for AADL from this directory. The translator reads all
*.sysml files in this directory and its subdirectories. If the path or any of
its subdirectories contains a space character, the path must be absolute.

-s <u>path</u>

Read the SysML standard library from this directory. The translator reads all
*.sysml files in this directory and its subdirectories. If the path or any of
its subdirectories contains a space character, the path must be absolute.

The current version of the translator only needs the file SI.sysml from the
SysML library to translate time units s, min, and hr to AADL. It is sufficient
to pass the path to this file to the translator.

-o <u>path</u>

Write generated AADL files to this directory.

-v

Produce more verbose output to the console during translation.

-b <u>URL</u>

Read the SysML models from the SysML repository accessible via this URL.


