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
package org.osate.sysml.importer.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.omg.kerml.xtext.KerMLStandaloneSetup;
import org.omg.sysml.util.SysMLUtil;
import org.omg.sysml.xtext.SysMLStandaloneSetup;
import org.osate.pluginsupport.PluginSupportUtil;
import org.osate.sysml2aadl.SysML2AADLConverter;
import org.osate.xtext.aadl2.Aadl2StandaloneSetup;

public class SysMLTestHelper extends SysMLUtil {

	private String aadlLibraryPath = "../../aadl.library";
	
	private String sysmlLibraryPath = "../../../../Systems-Modeling/SySML-v2-Pilot-Implementation/sysml.library/Domain Libraries/Quantities and Units/SI.sysml";

	private Set<Resource> aadlLibraryResources = new HashSet<>();

	private EObject propertyLookupContext;
	
	private int preloadedCount;
	
	SysMLTestHelper() {
		super();
		KerMLStandaloneSetup.doSetup();
		this.addExtension(".kerml");
		SysMLStandaloneSetup.doSetup();
		this.addExtension(".sysml");
		Aadl2StandaloneSetup.doSetup();
	}

	void initialize() {
		var dir = System.getProperty("user.dir") + "/";
		readAll(dir + sysmlLibraryPath, false);
		propertyLookupContext = loadContributedAADL();
		readAll(dir + aadlLibraryPath, true);
		aadlLibraryResources.addAll(getInputResources());
		getInputResources().clear();
		preloadedCount = getResourceSet().getResources().size();
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

	List<Resource> testFiles(String... fname) {
		for (var fn : fname) {
			readAll(fn, true);
		}
		var converter = new SysML2AADLConverter(getResourceSet(), aadlLibraryResources, propertyLookupContext);
		var results = converter.convert(getInputResources());
		getInputResources().clear();
		return results;
	}

	void cleanResourceSet() {
		EList<Resource> resources = getResourceSet().getResources();
		getResourceSet().getResources().stream().skip(preloadedCount).forEach(Resource::unload);
		for (int i = resources.size(); i > preloadedCount; i--) {
			resources.remove(i - 1);
		}
	}

}
