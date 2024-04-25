/**
 * SysML API access examples and SysML to AADL translator
 *
 * Copyright 2024 Carnegie Mellon University.
 *
 * NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE ENGINEERING INSTITUTE MATERIAL IS
 * FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON UNIVERSITY MAKES NO WARRANTIES OF ANY KIND,
 * EITHER EXPRESSED OR IMPLIED, AS TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS
 * FOR PURPOSE OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE MATERIAL.
 * CARNEGIE MELLON UNIVERSITY DOES NOT MAKE ANY WARRANTY OF ANY KIND WITH RESPECT TO FREEDOM FROM
 * PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 *
 * Licensed under an Eclipse Public License - v 2.0-style license, please see license.txt or
 * contact permission@sei.cmu.edu for full terms.
 *
 * [DISTRIBUTION STATEMENT A] This material has been approved for public release and unlimited
 * distribution.  Please see Copyright notice for non-US Government use and distribution.
 *
 * This Software includes and/or makes use of Third-Party Software each subject to its own license.
 *
 * DM24-0393
 */
package org.osate.sysml2aadl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.omg.kerml.xtext.KerMLStandaloneSetup;
import org.omg.sysml.lang.sysml.Element;
import org.omg.sysml.lang.sysml.SysMLPackage;
import org.omg.sysml.util.SysMLUtil;
import org.omg.sysml.xtext.SysMLStandaloneSetup;
import org.osate.pluginsupport.PluginSupportUtil;
import org.osate.sysml.api.SysMLApiAccess;
import org.osate.sysml.util.PrintingProgressMonitor;
import org.osate.xtext.aadl2.Aadl2StandaloneSetup;

/**
 * Command line utility to convert a SysML model to AADL.
 * The SysML model can be read from a file or from a repository via the REST API.
 */
public class SysML2AADLUtil extends SysMLUtil {

	private boolean verbose = false;

	private String baseURL = null;

	private String aadlLibraryPath = null;

	private String sysmlLibraryPath = null;

	private String outputPath = null;

	private Set<Resource> libraryResources = new HashSet<>();

	SysML2AADLUtil() {
		super();
		KerMLStandaloneSetup.doSetup();
		this.addExtension(".kerml");
		SysMLStandaloneSetup.doSetup();
		this.addExtension(".sysml");
		Aadl2StandaloneSetup.doSetup();
	}

	protected String[] processArgs(String[] args) {
		int n = args.length;
		if (n > 0) {
			int i = 0;
			while (i < n && ("-a".equals(args[i]) || "-b".equals(args[i]) || "-s".equals(args[i])
					|| "-o".equals(args[i]) || "-v".equals(args[i]))) {
				if ("-a".equals(args[i])) {
					aadlLibraryPath = args[++i];
				} else if ("-b".equals(args[i])) {
					baseURL = args[++i];
				} else if ("-s".equals(args[i])) {
					sysmlLibraryPath = args[++i];
				} else if ("-o".equals(args[i])) {
					outputPath = args[++i];
					if (!outputPath.endsWith("/")) {
						outputPath += "/";
					}
				} else if ("-v".equals(args[i])) {
					verbose = true;
				}
				i++;
			}
			if (i < n) {
				args = Arrays.copyOfRange(args, i, n);
				return args;
			}
		}
		return null;
	}

	/**
	 * Convert previously read input resources to AADL
	 */
	protected void process() {
		var propertyLookupContext = loadContributedAADL();
		var monitor = verbose ? new PrintingProgressMonitor(System.out) : new NullProgressMonitor();
		var converter = new SysML2AADLConverter(getResourceSet(), libraryResources, propertyLookupContext, monitor);
		var outputResources = converter.convert(getInputResources());

		if (verbose)
			printGeneratedAADL(outputResources);

		if (outputPath != null)
			saveAADL(outputResources);
	}

	/**
	 * Read project via sysml rest api and convert to AADL
	 * 
	 * @param projectName
	 */
	protected void process(String projectName) {
		var propertyLookupContext = loadContributedAADL();

		var monitor = verbose ? new PrintingProgressMonitor(System.out) : new NullProgressMonitor();
		var apiAccess = new SysMLApiAccess(baseURL, getResourceSet(), libraryResources, monitor);
		var sysmlResources = apiAccess.importProject(projectName);

		if (verbose)
			printImportedSysML(sysmlResources);

		var converter = new SysML2AADLConverter(getResourceSet(), libraryResources, propertyLookupContext, monitor);
		var outputResources = converter.convert(sysmlResources);

		if (verbose)
			printGeneratedAADL(outputResources);

		if (outputPath != null)
			saveAADL(outputResources);
	}

	private void printImportedSysML(List<Resource> outputResources) {
		System.out.println("ImportedSysML");
		for (var or : outputResources) {
			System.out.println(or.getURI().toString());

			var r = getResourceSet().createResource(URI.createFileURI("imported.sysml"));

			for (var eo : or.getContents()) {
				if (eo.eClass() == SysMLPackage.eINSTANCE.getNamespace()) {
					r.getContents().add(EcoreUtil.copy(eo));
					break;
				}
			}

			var iter = r.getAllContents();
			while (iter.hasNext()) {
				var e = (Element) iter.next();
				e.setElementId(null);
			}

			try {
				r.save(System.out, null);
				System.out.println();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void printGeneratedAADL(List<Resource> outputResources) {
		if (outputResources.isEmpty()) {
			System.out.println("No AADL was generated");
		} else {
			System.out.println("Generated AADL");
			for (var or : outputResources) {
				System.out.println();
				System.out.println(or.getURI().toString() + ":\n");
				try {
					or.save(System.out, null);
					System.out.println();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void saveAADL(List<Resource> outputResources) {
		if (outputResources.isEmpty()) {
			System.out.println("\nNo AADL files to save");
		} else {
			System.out.println("\nSaving files:");
			for (var or : outputResources) {
				var fileName = or.getURI().toString();
				or.setURI(URI.createFileURI(outputPath + fileName));
				if (verbose) {
					System.out.println(or.getURI().toString());
				}
				try {
					or.save(null);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void printUsage() {
		var message = """
				Usage:

				java -jar sysml2aadl.jar -a path -s path -o path -v file ...

				This form reads files and translates them to AADL.

				java -jar sysml2aadl.jar -a path -s path -o path iv -b URL project ...

				This form reads projects from a SysML repository via its REST API.

				The following options are available:

				-a path

				Read the SysML library for AADL from this directory. The translator reads all
				*.sysml files in this directory and its subdirectories. If the path or any of
				its subdirectories contains a space character, the path must be absolute.

				-s path
				
				Read the SysML standard library from this directory. The translator reads all
				*.sysml files in this directory and its subdirectories. If the path or any of
				its subdirectories contains a space character, the path must be absolute.

				The current version of the translator only needs the file SI.sysml from the
				SysML library to translate time units s, min, and hr to AADL. It is sufficient
				to pass the path to this file to the translator.

				-o path

				Write generated AADL files to this directory.

				-v

				Produce more verbose output to the console during translation.

				-b URL

				Read the SysML models from the SysML repository accessible via this URL.

				""";
		System.out.println(message);
	}
	
	public void run(String[] args) {
		if (args.length == 0) {
			printUsage();
			return;
		}
		
		args = processArgs(args);

		if (args != null) {
			if (sysmlLibraryPath != null) {
				readAll(sysmlLibraryPath, false);
				libraryResources.addAll(getInputResources());
				getInputResources().clear();
			}
			if (aadlLibraryPath != null) {
				readAll(aadlLibraryPath, true);
				libraryResources.addAll(getInputResources());
				getInputResources().clear();
			}
			if (baseURL == null) {
				// interpret remaining args as files as sysml files to read and convert to AADL
				for (var arg : args) {
					readAll(arg, true);
				}
				process();
			} else {
				// interpret remaining args as project names to read from API
				for (var arg : args) {
					process(arg);
				}
			}
		}
	}

	private EObject loadContributedAADL() {
		EcorePlugin.ExtensionProcessor.process(Thread.currentThread().getContextClassLoader());
		var contributed = PluginSupportUtil.getContributedAadl();
		var uriConverter = getResourceSet().getURIConverter();
		EObject propertyLookupContext = null;

		for (final URI uri : contributed) {
			var r = getResourceSet().getResource(uriConverter.normalize(uri), true);
			if (r == null) {
				throw new RuntimeException("Error opening resource: " + uri.toString());
			}
			addResourceToIndex(r);
			if (propertyLookupContext == null) {
				propertyLookupContext = r.getContents().get(0);
			}
		}
		return propertyLookupContext;
	}

	public static void main(String[] args) {
		try {
			new SysML2AADLUtil().run(args);
		} catch (Exception e) {
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
	}

}
