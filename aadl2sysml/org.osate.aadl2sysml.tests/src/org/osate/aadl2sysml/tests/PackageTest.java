/*
 * AADL translator to SysMLV2
 *
 * Copyright 2024 Carnegie Mellon University.
 *
 * NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE ENGINEERING INSTITUTE MATERIAL IS FURNISHED ON AN "AS-IS"
 * BASIS. CARNEGIE MELLON UNIVERSITY MAKES NO WARRANTIES OF ANY KIND, EITHER EXPRESSED OR IMPLIED, AS TO ANY MATTER
 * INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED
 * FROM USE OF THE MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT MAKE ANY WARRANTY OF ANY KIND WITH RESPECT TO FREEDOM
 * FROM PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 *
 * Licensed under an Eclipse Public License - v 2.0-style license, please see license.txt or contact
 * permission@sei.cmu.edu for full terms.
 *
 * [DISTRIBUTION STATEMENT A] This material has been approved for public release and unlimited distribution.  Please see
 * Copyright notice for non-US Government use and distribution.
 *
 * DM24-0312
 */
package org.osate.aadl2sysml.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2sysml.Aadl2SysmlTranslator;
import org.osate.testsupport.Aadl2InjectorProvider;
import org.osate.testsupport.TestHelper;

import com.google.inject.Inject;

@RunWith(XtextRunner.class)
@InjectWith(Aadl2InjectorProvider.class)
public class PackageTest {
	@Inject
	private TestHelper<AadlPackage> testHelper;

	@Test
	public void testEmptyPackage() throws Exception {
		var aadl = """
				package empty_package
				public
				end empty_package;
				""";
		var sysml = "package empty_package;";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testNestedPackage() throws Exception {
		var aadl = """
				package a::b::c
				public
					system s
					end s;

					system implementation s.i
					end s.i;
				end a::b::c;
				""";
		var sysml = """
				package 'a::b::c' {
					part def s :> AADL::System;
					part def 's.i' :> s;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testPrivateOnly() throws Exception {
		var aadl = """
				package package_with_private_only
				private
					system system_in_private_section
					end system_in_private_section;
				end package_with_private_only;
				""";
		var sysml = "package package_with_private_only;";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}
}