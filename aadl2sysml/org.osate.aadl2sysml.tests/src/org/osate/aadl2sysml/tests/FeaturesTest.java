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
public class FeaturesTest {
	@Inject
	private TestHelper<AadlPackage> testHelper;

	@Test
	public void testSimpleAbstractFeature() throws Exception {
		var aadl = """
				package simple_abstract_feature
				public
					system s
						features
							f1: in feature;
							f2: out feature;
							f3: feature;
					end s;
				end simple_abstract_feature;
				""";
		var sysml = """
				package simple_abstract_feature {
					part def s :> AADL::System {
						in port f1 : AADL::AbstractFeature;
						out port f2 : AADL::AbstractFeature;
						inout port f3 : AADL::AbstractFeature;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testFeaturePointsToType() throws Exception {
		var aadl = """
				package feature_points_to_type
				public
					system s
						features
							f1: in feature b;
							f2: out feature b;
							f3: feature b;
					end s;

					bus b
					end b;
				end feature_points_to_type;
				""";
		var sysml = """
				package feature_points_to_type {
					part def s :> AADL::System {
						in port f1 : AADL::AbstractFeature {
							in item :>> type : feature_points_to_type::b;
						}

						out port f2 : AADL::AbstractFeature {
							out item :>> type : feature_points_to_type::b;
						}

						inout port f3 : AADL::AbstractFeature {
							inout item :>> type : feature_points_to_type::b;
						}
					}

					part def b :> AADL::Bus;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testFeaturePointsToTypeInOtherFile() throws Exception {
		var featurePointsToTypeInOtherFile = """
				package feature_points_to_type_in_other_file
				public
					with package_with_bus;

					system s
						features
							f1: in feature package_with_bus::b;
					end s;
				end feature_points_to_type_in_other_file;
				""";
		var packageWithBus = """
				package package_with_bus
				public
					bus b
					end b;
				end package_with_bus;
				""";
		var sysml = """
				package feature_points_to_type_in_other_file {
					part def s :> AADL::System {
						in port f1 : AADL::AbstractFeature {
							in item :>> type : package_with_bus::b;
						}
					}
				}""";
		var parsed = testHelper.parseString(featurePointsToTypeInOtherFile, packageWithBus);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testFeaturePointsToTypeInOtherNestedPackage() throws Exception {
		var featurePointsToTypeInOtherNestedPackage = """
				package feature_points_to_type_in_other_nested_package
				public
					with a::b::c;

					system s
						features
							f1: in feature a::b::c::b;
					end s;
				end feature_points_to_type_in_other_nested_package;
				""";
		var nestedPackage = """
				package a::b::c
				public
					bus b
					end b;
				end a::b::c;
				""";
		var sysml = """
				package feature_points_to_type_in_other_nested_package {
					part def s :> AADL::System {
						in port f1 : AADL::AbstractFeature {
							in item :>> type : 'a::b::c'::b;
						}
					}
				}""";
		var parsed = testHelper.parseString(featurePointsToTypeInOtherNestedPackage, nestedPackage);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testFeaturePointsToImpl() throws Exception {
		var aadl = """
				package feature_points_to_impl
				public
					system s
						features
							f1: in feature b.i;
					end s;

					bus b
					end b;

					bus implementation b.i
					end b.i;
				end feature_points_to_impl;
				""";
		var sysml = """
				package feature_points_to_impl {
					part def s :> AADL::System {
						in port f1 : AADL::AbstractFeature {
							in item :>> type : 'b.i';
						}
					}

					part def b :> AADL::Bus;
					part def 'b.i' :> b;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testFeaturePointsToImplInOtherFile() throws Exception {
		var featurePointsToImplInOtherFile = """
				package feature_points_to_impl_in_other_file
				public
					with package_with_bus_impl;

					system s
						features
							f1: in feature package_with_bus_impl::b.i;
					end s;
				end feature_points_to_impl_in_other_file;
				""";
		var packageWithBusImpl = """
				package package_with_bus_impl
				public
					bus b
					end b;

					bus implementation b.i
					end b.i;
				end package_with_bus_impl;
				""";
		var sysml = """
				package feature_points_to_impl_in_other_file {
					part def s :> AADL::System {
						in port f1 : AADL::AbstractFeature {
							in item :>> type : package_with_bus_impl::'b.i';
						}
					}
				}""";
		var parsed = testHelper.parseString(featurePointsToImplInOtherFile, packageWithBusImpl);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testFeaturePointsToImplInOtherNestedPackage() throws Exception {
		var featurePointsToImplInOtherNestedPackage = """
				package feature_points_to_impl_in_other_nested_package
				public
					with a::b::c::d;

					system s
						features
							f1: in feature a::b::c::d::b.i;
					end s;
				end feature_points_to_impl_in_other_nested_package;
				""";
		var nestedPackage = """
				package a::b::c::d
				public
					bus b
					end b;

					bus implementation b.i
					end b.i;
				end a::b::c::d;
				""";
		var sysml = """
				package feature_points_to_impl_in_other_nested_package {
					part def s :> AADL::System {
						in port f1 : AADL::AbstractFeature {
							in item :>> type : 'a::b::c::d'::'b.i';
						}
					}
				}""";
		var parsed = testHelper.parseString(featurePointsToImplInOtherNestedPackage, nestedPackage);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testFeaturePointsToComponentPrototype() throws Exception {
		var aadl = """
				package feature_points_to_component_prototype
				public
					system s
						prototypes
							p1: bus;
						features
							f1: in feature p1;
					end s;
				end feature_points_to_component_prototype;
				""";
		var sysml = """
				package feature_points_to_component_prototype {
					part def s :> AADL::System {
						// WARNING: Reference to prototype 'p1' not translated.
						in port f1 : AADL::AbstractFeature;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testFeaturePointsToFeaturePrototype() throws Exception {
		var aadl = """
				package feature_points_to_feature_prototype
				public
					system s
						prototypes
							p1: in feature;
						features
							f1: in prototype p1;
					end s;
				end feature_points_to_feature_prototype;
				""";
		var sysml = """
				package feature_points_to_feature_prototype {
					part def s :> AADL::System {
						// WARNING: Reference to prototype 'p1' not translated.
						in port f1 : AADL::AbstractFeature;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testDataPorts() throws Exception {
		var dataPorts = """
				package data_ports
				public
					with a::b::c::d::e;
					with a::b::c::d::e::f;
					with package_with_data;
					with package_with_data_impl;

					system s
						prototypes
							proto: data;
						features
							simple_in: in data port;
							simple_out: out data port;
							simple_in_out: in out data port;

							points_to_type: in data port d;
							points_to_type_in_other_file: in data port package_with_data::d;
							points_to_type_in_other_nested_package: in data port a::b::c::d::e::d;

							points_to_impl: in data port d.i;
							points_to_impl_in_other_file: in data port package_with_data_impl::d.i;
							points_to_impl_in_other_nested_package: in data port a::b::c::d::e::f::d.i;

							points_to_prototype: in data port proto;
					end s;

					data d
					end d;

					data implementation d.i
					end d.i;
				end data_ports;
				""";
		var nestedPackageWithDataType = """
				package a::b::c::d::e
				public
					data d
					end d;
				end a::b::c::d::e;
				""";
		var nestedPackageWithDataImpl = """
				package a::b::c::d::e::f
				public
					data d
					end d;

					data implementation d.i
					end d.i;
				end a::b::c::d::e::f;
				""";
		var packageWithData = """
				package package_with_data
				public
					data d
					end d;
				end package_with_data;
				""";
		var packageWithDataImpl = """
				package package_with_data_impl
				public
					data d
					end d;

					data implementation d.i
					end d.i;
				end package_with_data_impl;
				""";
		var sysml = """
				package data_ports {
					part def s :> AADL::System {
						in port simple_in : AADL::DataPort;
						out port simple_out : AADL::DataPort;
						inout port simple_in_out : AADL::DataPort;

						in port points_to_type : AADL::DataPort {
							in item :>> type : data_ports::d;
						}

						in port points_to_type_in_other_file : AADL::DataPort {
							in item :>> type : package_with_data::d;
						}

						in port points_to_type_in_other_nested_package : AADL::DataPort {
							in item :>> type : 'a::b::c::d::e'::d;
						}

						in port points_to_impl : AADL::DataPort {
							in item :>> type : 'd.i';
						}

						in port points_to_impl_in_other_file : AADL::DataPort {
							in item :>> type : package_with_data_impl::'d.i';
						}

						in port points_to_impl_in_other_nested_package : AADL::DataPort {
							in item :>> type : 'a::b::c::d::e::f'::'d.i';
						}

						// WARNING: Reference to prototype 'proto' not translated.
						in port points_to_prototype : AADL::DataPort;
					}

					part def d :> AADL::Data;
					part def 'd.i' :> d;
				}""";
		var parsed = testHelper.parseString(dataPorts, nestedPackageWithDataType, nestedPackageWithDataImpl,
				packageWithData, packageWithDataImpl);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testEventDataPort() throws Exception {
		var eventDataPorts = """
				package event_data_ports
				public
					with a::b::c::d::e;
					with a::b::c::d::e::f;
					with package_with_data;
					with package_with_data_impl;

					system s
						prototypes
							proto: data;
						features
							simple_in: in event data port;
							simple_out: out event data port;
							simple_in_out: in out event data port;

							points_to_type: in event data port d;
							points_to_type_in_other_file: in event data port package_with_data::d;
							points_to_type_in_other_nested_package: in event data port a::b::c::d::e::d;

							points_to_impl: in event data port d.i;
							points_to_impl_in_other_file: in event data port package_with_data_impl::d.i;
							points_to_impl_in_other_nested_package: in event data port a::b::c::d::e::f::d.i;

							points_to_prototype: in event data port proto;
					end s;

					data d
					end d;

					data implementation d.i
					end d.i;
				end event_data_ports;
				""";
		var nestedPackageWithDataType = """
				package a::b::c::d::e
				public
					data d
					end d;
				end a::b::c::d::e;
				""";
		var nestedPackageWithDataImpl = """
				package a::b::c::d::e::f
				public
					data d
					end d;

					data implementation d.i
					end d.i;
				end a::b::c::d::e::f;
				""";
		var packageWithData = """
				package package_with_data
				public
					data d
					end d;
				end package_with_data;
				""";
		var packageWithDataImpl = """
				package package_with_data_impl
				public
					data d
					end d;

					data implementation d.i
					end d.i;
				end package_with_data_impl;
				""";
		var sysml = """
				package event_data_ports {
					part def s :> AADL::System {
						in port simple_in : AADL::EventDataPort;
						out port simple_out : AADL::EventDataPort;
						inout port simple_in_out : AADL::EventDataPort;

						in port points_to_type : AADL::EventDataPort {
							in item :>> type : event_data_ports::d;
						}

						in port points_to_type_in_other_file : AADL::EventDataPort {
							in item :>> type : package_with_data::d;
						}

						in port points_to_type_in_other_nested_package : AADL::EventDataPort {
							in item :>> type : 'a::b::c::d::e'::d;
						}

						in port points_to_impl : AADL::EventDataPort {
							in item :>> type : 'd.i';
						}

						in port points_to_impl_in_other_file : AADL::EventDataPort {
							in item :>> type : package_with_data_impl::'d.i';
						}

						in port points_to_impl_in_other_nested_package : AADL::EventDataPort {
							in item :>> type : 'a::b::c::d::e::f'::'d.i';
						}

						// WARNING: Reference to prototype 'proto' not translated.
						in port points_to_prototype : AADL::EventDataPort;
					}

					part def d :> AADL::Data;
					part def 'd.i' :> d;
				}""";
		var parsed = testHelper.parseString(eventDataPorts, nestedPackageWithDataType, nestedPackageWithDataImpl,
				packageWithData, packageWithDataImpl);
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(parsed));
	}

	@Test
	public void testEventPort() throws Exception {
		var aadl = """
				package event_ports
				public
					system s
						features
							simple_in: in event port;
							simple_out: out event port;
							simple_in_out: in out event port;
					end s;
				end event_ports;
				""";
		var sysml = """
				package event_ports {
					part def s :> AADL::System {
						in port simple_in : AADL::EventPort;
						out port simple_out : AADL::EventPort;
						inout port simple_in_out : AADL::EventPort;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testRequiresDataAccess() throws Exception {
		var aadl = """
				package requires_data_access
				public
					system s
						features
							rda1: requires data access;
							rda2: requires data access d;
					end s;

					data d
					end d;
				end requires_data_access;
				""";
		var sysml = """
				package requires_data_access {
					part def s :> AADL::System {
						in port rda1 : AADL::DataAccess;

						in port rda2 : AADL::DataAccess {
							in ref :>> type : requires_data_access::d;
						}
					}

					part def d :> AADL::Data;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testProvidesDataAccess() throws Exception {
		var aadl = """
				package provides_data_access
				public
					system s
						features
							pda1: provides data access;
							pda2: provides data access d;
					end s;

					data d
					end d;
				end provides_data_access;
				""";
		var sysml = """
				package provides_data_access {
					part def s :> AADL::System {
						out port pda1 : AADL::DataAccess;

						out port pda2 : AADL::DataAccess {
							out ref :>> type : provides_data_access::d;
						}
					}

					part def d :> AADL::Data;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testBusAccess() throws Exception {
		var aadl = """
				package bus_access
				public
					system s
						features
							pba1: provides bus access;
							pba2: provides bus access b;
							rba1: requires bus access;
							rba2: requires bus access b;
					end s;

					bus b
					end b;
				end bus_access;
				""";
		var sysml = """
				package bus_access {
					part def s :> AADL::System {
						out port pba1 : AADL::BusAccess;

						out port pba2 : AADL::BusAccess {
							out ref :>> type : bus_access::b;
						}

						in port rba1 : AADL::BusAccess;

						in port rba2 : AADL::BusAccess {
							in ref :>> type : bus_access::b;
						}
					}

					part def b :> AADL::Bus;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testVirtualBusAccess() throws Exception {
		var aadl = """
				package virtual_bus_access
				public
					system s
						features
							pvba1: provides virtual bus access;
							pvba2: provides virtual bus access vb;
							rvba1: requires virtual bus access;
							rvba2: requires virtual bus access vb;
					end s;

					virtual bus vb
					end vb;
				end virtual_bus_access;
				""";
		var sysml = """
				package virtual_bus_access {
					part def s :> AADL::System {
						out port pvba1 : AADL::VirtualBusAccess;

						out port pvba2 : AADL::VirtualBusAccess {
							out ref :>> type : virtual_bus_access::vb;
						}

						in port rvba1 : AADL::VirtualBusAccess;

						in port rvba2 : AADL::VirtualBusAccess {
							in ref :>> type : virtual_bus_access::vb;
						}
					}

					part def vb :> AADL::VirtualBus;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testSubprogramAccess() throws Exception {
		var aadl = """
				package subprogram_access
				public
					system s
						features
							psubpa1: provides subprogram access;
							psubpa2: provides subprogram access subp;
							rsubpa1: requires subprogram access;
							rsubpa2: requires subprogram access subp;
					end s;

					subprogram subp
					end subp;
				end subprogram_access;
				""";
		var sysml = """
				package subprogram_access {
					part def s :> AADL::System {
						out port psubpa1 : AADL::SubprogramAccess;

						out port psubpa2 : AADL::SubprogramAccess {
							out ref :>> type : subprogram_access::subp;
						}

						in port rsubpa1 : AADL::SubprogramAccess;

						in port rsubpa2 : AADL::SubprogramAccess {
							in ref :>> type : subprogram_access::subp;
						}
					}

					part def subp :> AADL::Subprogram;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testSubprogramGroupAccess() throws Exception {
		var aadl = """
				package subprogram_group_access
				public
					system s
						features
							psubpga1: provides subprogram group access;
							psubpga2: provides subprogram group access subpg;
							rsubpga1: requires subprogram group access;
							rsubpga2: requires subprogram group access subpg;
					end s;

					subprogram group subpg
					end subpg;
				end subprogram_group_access;
				""";
		var sysml = """
				package subprogram_group_access {
					part def s :> AADL::System {
						out port psubpga1 : AADL::SubprogramGroupAccess;

						out port psubpga2 : AADL::SubprogramGroupAccess {
							out ref :>> type : subprogram_group_access::subpg;
						}

						in port rsubpga1 : AADL::SubprogramGroupAccess;

						in port rsubpga2 : AADL::SubprogramGroupAccess {
							in ref :>> type : subprogram_group_access::subpg;
						}
					}

					part def subpg :> AADL::SubprogramGroup;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testUntranslatedFeatures() throws Exception {
		var aadl = """
				package untranslated_features
				public
					system s
						features
							feature_group: feature group;
					end s;

					subprogram subp
						features
							param: in parameter;
					end subp;
				end untranslated_features;
				""";
		var sysml = """
				package untranslated_features {
					part def s :> AADL::System {
						// WARNING: 'feature_group' not translated.
					}

					part def subp :> AADL::Subprogram {
						// WARNING: 'param' not translated.
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testFeatureRefinements() throws Exception {
		var aadl = """
				package feature_refinements
				public
					system s1
						features
							f: in feature;
					end s1;

					system s2 extends s1
						features
							f: refined to in event port;
					end s2;
				end feature_refinements;
				""";
		var sysml = """
				package feature_refinements {
					part def s1 :> AADL::System {
						in port f : AADL::AbstractFeature;
					}

					part def s2 :> s1 {
						// WARNING: 'f' not translated.
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}
}