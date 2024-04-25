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
package org.osate.sysml.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.omg.kerml.xtext.KerMLStandaloneSetup;
import org.omg.sysml.lang.sysml.Element;
import org.omg.sysml.lang.sysml.SysMLPackage;
import org.omg.sysml.util.SysMLUtil;
import org.omg.sysml.xtext.SysMLStandaloneSetup;
import org.osate.sysml.util.PrintingProgressMonitor;
import org.osate.xtext.aadl2.Aadl2StandaloneSetup;

/**
 * Command line utility to read projects from a SysML repository and print the resulting SysML.
 * Note that this uses Xtext serialization to print the SysML sources, which does not work for
 * all SysML models, i.e., it may crash with an exception.
 */
public class SysMLApiReader extends SysMLUtil {

	private boolean verbose = false;

	private String baseURL = "http://localhost:9001";

	private String aadlLibraryPath = null;

	private String sysmlLibraryPath = null;

	private Set<Resource> libraryResources = new HashSet<>();

	private List<Resource> outputResources = null;

	SysMLApiReader() {
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
			while (i < n
					&& ("-a".equals(args[i]) || "-b".equals(args[i]) || "-s".equals(args[i]) || "-v".equals(args[i]))) {
				if ("-a".equals(args[i])) {
					aadlLibraryPath = args[++i];
				} else if ("-b".equals(args[i])) {
					baseURL = args[++i];
				} else if ("-s".equals(args[i])) {
					sysmlLibraryPath = args[++i];
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

	protected void process(String projectName) {
		var monitor = verbose ? new PrintingProgressMonitor(System.out) : new NullProgressMonitor();
		var apiAccess = new SysMLApiAccess(baseURL, getResourceSet(), libraryResources, monitor);
		outputResources = apiAccess.importProject(projectName);

		printImportedSysML(outputResources);
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

	public void run(String[] args) {
		args = processArgs(args);

		if (args != null) {
			if (sysmlLibraryPath != null) {
				readAll(sysmlLibraryPath, true);
				libraryResources.addAll(getInputResources());
				getInputResources().clear();
			}
			if (aadlLibraryPath != null) {
				readAll(aadlLibraryPath, true);
				libraryResources.addAll(getInputResources());
				getInputResources().clear();
			}
			for (var arg : args) {
				process(arg);
			}
		}
	}

	public static void main(String[] args) {
		try {
			new SysMLApiReader().run(args);
		} catch (Exception e) {
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
	}

}
