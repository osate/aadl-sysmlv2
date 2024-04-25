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
public class SubcomponentTest {
	@Inject
	private TestHelper<AadlPackage> testHelper;

	@Test
	public void testSingleSubcomponent() throws Exception {
		var aadl = """
				package single_subcomponent
				public
					system s
					end s;

					system implementation s.i
						subcomponents
							sub: device;
					end s.i;
				end single_subcomponent;
				""";
		var sysml = """
				package single_subcomponent {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part sub : AADL::Device;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testEverySubcomponentCategory() throws Exception {
		var aadl = """
				package every_subcomponent_category
				public
					abstract a
					end a;

					abstract implementation a.i
						subcomponents
							sub1: abstract;
							sub2: bus;
							sub3: data;
							sub4: device;
							sub5: memory;
							sub6: process;
							sub7: processor;
							sub8: system;
							sub9: subprogram;
							sub10: subprogram group;
							sub11: thread;
							sub12: thread group;
							sub13: virtual bus;
							sub14: virtual processor;
					end a.i;
				end every_subcomponent_category;
				""";
		var sysml = """
				package every_subcomponent_category {
					part def a :> AADL::Abstract;

					part def 'a.i' :> a {
						part sub1 : AADL::Abstract;
						part sub2 : AADL::Bus;
						part sub3 : AADL::Data;
						part sub4 : AADL::Device;
						part sub5 : AADL::Memory;
						part sub6 : AADL::Process;
						part sub7 : AADL::Processor;
						part sub8 : AADL::System;
						part sub9 : AADL::Subprogram;
						part sub10 : AADL::SubprogramGroup;
						part sub11 : AADL::Thread;
						part sub12 : AADL::ThreadGroup;
						part sub13 : AADL::VirtualBus;
						part sub14 : AADL::VirtualProcessor;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testSubcomponentPointsToType() throws Exception {
		var aadl = """
				package subcomponent_points_to_type
				public
					system s
					end s;

					system implementation s.i
						subcomponents
							sub: device dev;
							s: system s;
					end s.i;

					device dev
					end dev;
				end subcomponent_points_to_type;
				""";
		var sysml = """
				package subcomponent_points_to_type {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part sub : subcomponent_points_to_type::dev;
						part s : subcomponent_points_to_type::s;
					}

					part def dev :> AADL::Device;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testSubcomponentPointsToTypeInNestedPackage() throws Exception {
		var aadl = """
				package subcomponent::nested::pkg
				public
					system s
					end s;

					system implementation s.i
						subcomponents
							sub: device dev;
					end s.i;

					device dev
					end dev;
				end subcomponent::nested::pkg;
				""";
		var sysml = """
				package 'subcomponent::nested::pkg' {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part sub : 'subcomponent::nested::pkg'::dev;
					}

					part def dev :> AADL::Device;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testSubcomponentPointsToTypeInOtherFile() throws Exception {
		var subcomponentPointsToTypeInOtherFile = """
				package subcomponent_points_to_type_in_other_file
				public
					with package_with_base_type;

					system s
					end s;

					system implementation s.i
						subcomponents
							sub: system package_with_base_type::base_type;
					end s.i;
				end subcomponent_points_to_type_in_other_file;
				""";
		var packageWithBaseType = """
				package package_with_base_type
				public
					system base_type
					end base_type;
				end package_with_base_type;
				""";
		var sysml = """
				package subcomponent_points_to_type_in_other_file {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part sub : package_with_base_type::base_type;
					}
				}""";
		var parsed = testHelper.parseString(subcomponentPointsToTypeInOtherFile, packageWithBaseType);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testSubcomponentPointsToTypeInOtherNestedPackage() throws Exception {
		var subcomponentPointsToTypeInOtherNestedPackage = """
				package subcomponent_points_to_type_in_other_nested_package
				public
					with a::b::c::d;

					system s
					end s;

					system implementation s.i
						subcomponents
							sub: system a::b::c::d::base_type;
					end s.i;
				end subcomponent_points_to_type_in_other_nested_package;
				""";
		var nestedPackage = """
				package a::b::c::d
				public
					system base_type
					end base_type;
				end a::b::c::d;
				""";
		var sysml = """
				package subcomponent_points_to_type_in_other_nested_package {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part sub : 'a::b::c::d'::base_type;
					}
				}""";
		var parsed = testHelper.parseString(subcomponentPointsToTypeInOtherNestedPackage, nestedPackage);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testSubcomponentPointsToImpl() throws Exception {
		var aadl = """
				package subcomponent_points_to_impl
				public
					system s
					end s;

					system implementation s.i
						subcomponents
							sub: device dev.i;
					end s.i;

					device dev
					end dev;

					device implementation dev.i
					end dev.i;
				end subcomponent_points_to_impl;
				""";
		var sysml = """
				package subcomponent_points_to_impl {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part sub : 'dev.i';
					}

					part def dev :> AADL::Device;
					part def 'dev.i' :> dev;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testSubcomponentPointsToImplInOtherFile() throws Exception {
		var subcomponentPointsToImplInOtherFile = """
				package subcomponent_points_to_impl_in_other_file
				public
					with package_with_system_type_and_impl;

					system s
					end s;

					system implementation s.i
						subcomponents
							sub: system package_with_system_type_and_impl::s.i;
					end s.i;
				end subcomponent_points_to_impl_in_other_file;
				""";
		var packageWithSystemTypeAndImpl = """
				package package_with_system_type_and_impl
				public
					system s
					end s;

					system implementation s.i
					end s.i;
				end package_with_system_type_and_impl;
				""";
		var sysml = """
				package subcomponent_points_to_impl_in_other_file {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part sub : package_with_system_type_and_impl::'s.i';
					}
				}""";
		var parsed = testHelper.parseString(subcomponentPointsToImplInOtherFile, packageWithSystemTypeAndImpl);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testSubcomponentPointsToImplInOtherNestedPackage() throws Exception {
		var subcomponentPointsToImplInOtherNestedPackage = """
				package subcomponent_points_to_impl_in_other_nested_package
				public
					with a::b::c;

					system s
					end s;

					system implementation s.i
						subcomponents
							sub: system a::b::c::s.i;
					end s.i;
				end subcomponent_points_to_impl_in_other_nested_package;
				""";
		var nestedPackage = """
				package a::b::c
				public
					system s
					end s;

					system implementation s.i
					end s.i;
				end a::b::c;
				""";
		var sysml = """
				package subcomponent_points_to_impl_in_other_nested_package {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part sub : 'a::b::c'::'s.i';
					}
				}""";
		var parsed = testHelper.parseString(subcomponentPointsToImplInOtherNestedPackage, nestedPackage);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testSubcomponentPointsToPrototype() throws Exception {
		var aadl = """
				package subcomponent_points_to_prototype
				public
					system s
						prototypes
							proto: device;
					end s;

					system implementation s.i
						subcomponents
							sub: device proto;
					end s.i;
				end subcomponent_points_to_prototype;
				""";
		var sysml = """
				package subcomponent_points_to_prototype {
					part def s :> AADL::System;

					part def 's.i' :> s {
						// WARNING: Reference to prototype 'proto' not translated.
						part sub : AADL::Device;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testSubcomponentRefinements() throws Exception {
		var aadl = """
				package subcomponent_refinements
				public
					system s
						prototypes
							proto: memory;
					end s;

					system implementation s.i1
						subcomponents
							sub1: abstract a1.i;
							sub2: abstract;
							sub3: abstract;
					end s.i1;

					system implementation s.i2 extends s.i1
						subcomponents
							sub1: refined to abstract a2.i;
							sub2: refined to device;
							sub3: refined to memory proto;
					end s.i2;

					abstract a1
					end a1;

					abstract implementation a1.i
					end a1.i;

					abstract a2 extends a1
					end a2;

					abstract implementation a2.i
					end a2.i;
				end subcomponent_refinements;
				""";
		var sysml = """
				package subcomponent_refinements {
					part def s :> AADL::System;

					part def 's.i1' :> s {
						part sub1 : 'a1.i';
						part sub2 : AADL::Abstract;
						part sub3 : AADL::Abstract;
					}

					part def 's.i2' :> 's.i1' {
						part : 'a2.i' :>> sub1;
						part : AADL::Device :>> sub2;

						// WARNING: Reference to prototype 'proto' not translated.
						part : AADL::Memory :>> sub3;
					}

					part def a1 :> AADL::Abstract;
					part def 'a1.i' :> a1;
					part def a2 :> a1;
					part def 'a2.i' :> a2;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}
}