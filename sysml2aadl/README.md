= THESE INSTRUCTIONS ARE STILL WORK IN PROGRESS

This repository contains the source code for an experimental translator from
SysMLv2 to AADL. It reads SysML files or retrieves a SysML project from a SysML
repository via the REST API.

# Building the importer

## Set up the development environment

### Install Eclipse 2023-12 Modeling Tools

### Add OSATE plugins to Eclipse

Install feature OSATE2 for AADL and plugins Errormodel Edit Plugin and Testsupport

During installation: 

*  Licenses: accept licenses
*  Trust Authorities: select all authorities and trust selected
*  Trust Unsigned: select all authorities and trust selected

One of the trust dialogs may get stuck: change URL of py4j update site to

`https://osate-build.sei.cmu.edu/p2/py4j`

### Import SysMLv2 pilot implementation plugin sources

File > Import > Oomph

Add URL of pilot implementation setup (Github project)

`https://raw.githubusercontent.com/Systems-Modeling/SysML-v2-Pilot-Implementation/master/org.omg.sysml.installer/SysML2.setup`

Import master

Close jupyter related projects 

*  jupyter-sysml-kernel
*  org.omg.sysml.interactive
*  org.omg.sysml.interactive.tests
*  org.omg.jupyter.installer
*  org.omg.jupyter.jupyterlab

### Get translator sources

Import the projects from this repository

## Create the runnable jar

Select project *org.osate.sysml.importer* in the project explorer view.

*File* > *Export*... > *Java* > *Runnable JAR file* > *Next*

In the Runnable JAR File Export dialog:

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


