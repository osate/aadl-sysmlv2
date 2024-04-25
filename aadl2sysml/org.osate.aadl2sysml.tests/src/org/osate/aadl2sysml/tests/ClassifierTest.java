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
public class ClassifierTest {
	@Inject
	private TestHelper<AadlPackage> testHelper;

	@Test
	public void testPackageWithSystemType() throws Exception {
		var aadl = """
				package package_with_system_type
				public
					system s
					end s;
				end package_with_system_type;
				""";
		var sysml = """
				package package_with_system_type {
					part def s :> AADL::System;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testPackageWithSystemTypeAndImpl() throws Exception {
		var aadl = """
				package package_with_system_type_and_impl
				public
					system s
					end s;

					system implementation s.i
					end s.i;
				end package_with_system_type_and_impl;
				""";
		var sysml = """
				package package_with_system_type_and_impl {
					part def s :> AADL::System;
					part def 's.i' :> s;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testPackageWithAllCategories() throws Exception {
		var aadl = """
				package package_with_all_categories
				public
					abstract a
					end a;

					abstract implementation a.i
					end a.i;

					bus b
					end b;

					bus implementation b.i
					end b.i;

					data d
					end d;

					data implementation d.i
					end d.i;

					device dev
					end dev;

					device implementation dev.i
					end dev.i;

					memory m
					end m;

					memory implementation m.i
					end m.i;

					process ps
					end ps;

					process implementation ps.i
					end ps.i;

					processor proc
					end proc;

					processor implementation proc.i
					end proc.i;

					subprogram subp
					end subp;

					subprogram implementation subp.i
					end subp.i;

					subprogram group subpg
					end subpg;

					subprogram group implementation subpg.i
					end subpg.i;

					system s
					end s;

					system implementation s.i
					end s.i;

					thread t
					end t;

					thread implementation t.i
					end t.i;

					thread group tg
					end tg;

					thread group implementation tg.i
					end tg.i;

					virtual bus vb
					end vb;

					virtual bus implementation vb.i
					end vb.i;

					virtual processor vproc
					end vproc;

					virtual processor implementation vproc.i
					end vproc.i;
				end package_with_all_categories;
				""";
		var sysml = """
				package package_with_all_categories {
					part def a :> AADL::Abstract;
					part def 'a.i' :> a;
					part def b :> AADL::Bus;
					part def 'b.i' :> b;
					part def d :> AADL::Data;
					part def 'd.i' :> d;
					part def dev :> AADL::Device;
					part def 'dev.i' :> dev;
					part def m :> AADL::Memory;
					part def 'm.i' :> m;
					part def ps :> AADL::Process;
					part def 'ps.i' :> ps;
					part def proc :> AADL::Processor;
					part def 'proc.i' :> proc;
					part def subp :> AADL::Subprogram;
					part def 'subp.i' :> subp;
					part def subpg :> AADL::SubprogramGroup;
					part def 'subpg.i' :> subpg;
					part def s :> AADL::System;
					part def 's.i' :> s;
					part def t :> AADL::Thread;
					part def 't.i' :> t;
					part def tg :> AADL::ThreadGroup;
					part def 'tg.i' :> tg;
					part def vb :> AADL::VirtualBus;
					part def 'vb.i' :> vb;
					part def vproc :> AADL::VirtualProcessor;
					part def 'vproc.i' :> vproc;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testComponentTypeExtension() throws Exception {
		var aadl = """
				package component_type_extension
				public
					system s1
					end s1;

					system s2 extends s1
					end s2;

					system s3 extends s2
					end s3;

					system implementation s3.i
					end s3.i;
				end component_type_extension;
				""";
		var sysml = """
				package component_type_extension {
					part def s1 :> AADL::System;
					part def s2 :> s1;
					part def s3 :> s2;
					part def 's3.i' :> s3;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testComponentTypeExtensionAcrossFiles() throws Exception {
		var packageWithTypeExtension = """
				package package_with_type_extension
				public
					with package_with_base_type;

					system type_extension extends package_with_base_type::base_type
					end type_extension;
				end package_with_type_extension;
				""";
		var packageWithBaseType = """
				package package_with_base_type
				public
					system base_type
					end base_type;
				end package_with_base_type;
				""";
		var sysml = """
				package package_with_type_extension {
					part def type_extension :> package_with_base_type::base_type;
				}""";
		var parsed = testHelper.parseString(packageWithTypeExtension, packageWithBaseType);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testComponentTypeExtensionFromNestedPackage() throws Exception {
		var typeExtensionFromNestedPackage = """
				package type_extension_from_nested_package
				public
					with a::b::c::d;

					system type_extension extends a::b::c::d::base_type
					end type_extension;
				end type_extension_from_nested_package;
				""";
		var nestedPackage = """
				package a::b::c::d
				public
					system base_type
					end base_type;
				end a::b::c::d;
				""";
		var sysml = """
				package type_extension_from_nested_package {
					part def type_extension :> 'a::b::c::d'::base_type;
				}""";
		var parsed = testHelper.parseString(typeExtensionFromNestedPackage, nestedPackage);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testRealizationAcrossFiles() throws Exception {
		var packageWithImpl = """
				package package_with_impl
				public
					with package_with_base_type;
					renames system package_with_base_type::base_type;

					system implementation base_type.i
					end base_type.i;
				end package_with_impl;
				""";
		var packageWithBaseType = """
				package package_with_base_type
				public
					system base_type
					end base_type;
				end package_with_base_type;
				""";
		var sysml = """
				package package_with_impl {
					part def 'base_type.i' :> package_with_base_type::base_type;
				}""";
		var parsed = testHelper.parseString(packageWithImpl, packageWithBaseType);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testRealizationFromNestedPackage() throws Exception {
		var realizationFromNestedPackage = """
				package realization_from_nested_package
				public
					with a::b::c::d;
					renames system a::b::c::d::base_type;

					system implementation base_type.i
					end base_type.i;
				end realization_from_nested_package;
				""";
		var nestedPackage = """
				package a::b::c::d
				public
					system base_type
					end base_type;
				end a::b::c::d;
				""";
		var sysml = """
				package realization_from_nested_package {
					part def 'base_type.i' :> 'a::b::c::d'::base_type;
				}""";
		var parsed = testHelper.parseString(realizationFromNestedPackage, nestedPackage);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testComponentImplExtension() throws Exception {
		var aadl = """
				package component_impl_extension
				public
					system s
					end s;

					system implementation s.i1
					end s.i1;

					system implementation s.i2 extends s.i1
					end s.i2;
				end component_impl_extension;
				""";
		var sysml = """
				package component_impl_extension {
					part def s :> AADL::System;
					part def 's.i1' :> s;
					part def 's.i2' :> 's.i1';
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testComponentImplExtensionAcrossFiles() throws Exception {
		var packageWithImplExtension = """
				package package_with_impl_extension
				public
					with package_with_system_type_and_impl;
					renames system package_with_system_type_and_impl::s;

					system implementation s.i2 extends package_with_system_type_and_impl::s.i
					end s.i2;
				end package_with_impl_extension;
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
				package package_with_impl_extension {
					part def 's.i2' :> package_with_system_type_and_impl::'s.i';
				}""";
		var parsed = testHelper.parseString(packageWithImplExtension, packageWithSystemTypeAndImpl);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testComponentImplExtensionFromNestedPackage() throws Exception {
		var componentImplExtensionFromNestedPackage = """
				package component_impl_extension_from_nested_package
				public
					with a::b::c;
					renames system a::b::c::s;

					system implementation s.i2 extends a::b::c::s.i
					end s.i2;
				end component_impl_extension_from_nested_package;
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
				package component_impl_extension_from_nested_package {
					part def 's.i2' :> 'a::b::c'::'s.i';
				}""";
		var parsed = testHelper.parseString(componentImplExtensionFromNestedPackage, nestedPackage);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testDiamondInheritance() throws Exception {
		var aadl = """
				package diamond_inheritance
				public
					system s1
					end s1;

					system implementation s1.i
					end s1.i;

					system s2 extends s1
					end s2;

					system implementation s2.i extends s1.i
					end s2.i;
				end diamond_inheritance;
				""";
		var sysml = """
				package diamond_inheritance {
					part def s1 :> AADL::System;
					part def 's1.i' :> s1;
					part def s2 :> s1;
					part def 's2.i' :> s2, 's1.i';
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testDiamondInheritanceAcrossFiles() throws Exception {
		var diamond_inheritance_across_files = """
				package diamond_inheritance_across_files
				public
					with diamond_inheritance_base;
					renames system diamond_inheritance_base::s2;

					system implementation s2.i extends diamond_inheritance_base::s1.i
					end s2.i;
				end diamond_inheritance_across_files;
				""";
		var diamond_inheritance_base = """
				package diamond_inheritance_base
				public
					system s1
					end s1;

					system implementation s1.i
					end s1.i;

					system s2 extends s1
					end s2;
				end diamond_inheritance_base;
				""";
		var sysml = """
				package diamond_inheritance_across_files {
					part def 's2.i' :> diamond_inheritance_base::s2, diamond_inheritance_base::'s1.i';
				}""";
		var parsed = testHelper.parseString(diamond_inheritance_across_files, diamond_inheritance_base);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testDiamondInheritanceFromNestedPackage() throws Exception {
		var diamond_inheritance_from_nested_package = """
				package diamond_inheritance_from_nested_package
				public
					with a::b::c::d::e;
					renames system a::b::c::d::e::s2;

					system implementation s2.i extends a::b::c::d::e::s1.i
					end s2.i;
				end diamond_inheritance_from_nested_package;
				""";
		var nestedPackage = """
				package a::b::c::d::e
				public
					system s1
					end s1;

					system implementation s1.i
					end s1.i;

					system s2 extends s1
					end s2;
				end a::b::c::d::e;
				""";
		var sysml = """
				package diamond_inheritance_from_nested_package {
					part def 's2.i' :> 'a::b::c::d::e'::s2, 'a::b::c::d::e'::'s1.i';
				}""";
		var parsed = testHelper.parseString(diamond_inheritance_from_nested_package, nestedPackage);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testPacakgeWithFeatureGroupType() throws Exception {
		var aadl = """
				package package_with_feature_group_type
				public
					feature group fgt
					end fgt;
				end package_with_feature_group_type;
				""";
		var sysml = """
				package package_with_feature_group_type {
					// WARNING: 'fgt' not translated.
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}
}