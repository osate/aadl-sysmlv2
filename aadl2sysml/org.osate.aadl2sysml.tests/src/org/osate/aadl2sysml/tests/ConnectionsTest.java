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
public class ConnectionsTest {
	@Inject
	private TestHelper<AadlPackage> testHelper;

	@Test
	public void testSimpleAcrossFeatureConnection() throws Exception {
		var aadl = """
				package simple_across_feature_connection
				public
					system top
					end top;

					system implementation top.i
						subcomponents
							left: system left;
							right: system right;
						connections
							directional: feature left.left_feature -> right.right_feature;
							bidirectional: feature left.left_feature <-> right.right_feature;
					end top.i;

					system left
						features
							left_feature: feature;
					end left;

					system right
						features
							right_feature: feature;
					end right;
				end simple_across_feature_connection;
				""";
		var sysml = """
				package simple_across_feature_connection {
					part def top :> AADL::System;

					part def 'top.i' :> top {
						part left : simple_across_feature_connection::left;
						part right : simple_across_feature_connection::right;
						connection directional : AADL::FeatureConnection connect left.left_feature to right.right_feature;
						connection bidirectional : AADL::FeatureConnection connect left.left_feature to right.right_feature;
					}

					part def left :> AADL::System {
						inout port left_feature : AADL::AbstractFeature;
					}

					part def right :> AADL::System {
						inout port right_feature : AADL::AbstractFeature;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testDownFeatureConnection() throws Exception {
		var aadl = """
				package down_feature_connection
				public
					system outer
						features
							outer_feature: in feature;
					end outer;

					system implementation outer.i
						subcomponents
							inner: system inner;
						connections
							conn: feature outer_feature -> inner.inner_feature;
					end outer.i;

					system inner
						features
							inner_feature: in feature;
					end inner;
				end down_feature_connection;
				""";
		var sysml = """
				package down_feature_connection {
					part def outer :> AADL::System {
						in port outer_feature : AADL::AbstractFeature;
					}

					part def 'outer.i' :> outer {
						part inner : down_feature_connection::inner;
						connection conn : AADL::FeatureConnection connect outer_feature to inner.inner_feature;
					}

					part def inner :> AADL::System {
						in port inner_feature : AADL::AbstractFeature;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testUpFeatureConnection() throws Exception {
		var aadl = """
				package up_feature_connection
				public
					system outer
						features
							outer_feature: out feature;
					end outer;

					system implementation outer.i
						subcomponents
							inner: system inner;
						connections
							conn: feature inner.inner_feature -> outer_feature;
					end outer.i;

					system inner
						features
							inner_feature: out feature;
					end inner;
				end up_feature_connection;
				""";
		var sysml = """
				package up_feature_connection {
					part def outer :> AADL::System {
						out port outer_feature : AADL::AbstractFeature;
					}

					part def 'outer.i' :> outer {
						part inner : up_feature_connection::inner;
						connection conn : AADL::FeatureConnection connect inner.inner_feature to outer_feature;
					}

					part def inner :> AADL::System {
						out port inner_feature : AADL::AbstractFeature;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testPortConnections() throws Exception {
		var aadl = """
				package port_connections
				public
					system top
					end top;

					system implementation top.i
						subcomponents
							left_process: process left_process.i;
							right_process: process right_process.i;
						connections
							across: port left_process.process_out -> right_process.process_in;
					end top.i;

					process left_process
						features
							process_out: out data port d;
					end left_process;

					process implementation left_process.i
						subcomponents
							left_thread: thread left_thread;
						connections
							up: port left_thread.thread_out -> process_out;
					end left_process.i;

					thread left_thread
						features
							thread_out: out data port d;
					end left_thread;

					process right_process
						features
							process_in: in data port d;
					end right_process;

					process implementation right_process.i
						subcomponents
							right_thread: thread right_thread;
						connections
							down: port process_in -> right_thread.thread_in;
					end right_process.i;

					thread right_thread
						features
							thread_in: in data port d;
					end right_thread;

					data d
					end d;
				end port_connections;
				""";
		var sysml = """
				package port_connections {
					part def top :> AADL::System;

					part def 'top.i' :> top {
						part left_process : 'left_process.i';
						part right_process : 'right_process.i';
						connection across : AADL::PortConnection connect left_process.process_out to right_process.process_in;
					}

					part def left_process :> AADL::Process {
						out port process_out : AADL::DataPort {
							out item :>> type : port_connections::d;
						}
					}

					part def 'left_process.i' :> left_process {
						part left_thread : port_connections::left_thread;
						connection up : AADL::PortConnection connect left_thread.thread_out to process_out;
					}

					part def left_thread :> AADL::Thread {
						out port thread_out : AADL::DataPort {
							out item :>> type : port_connections::d;
						}
					}

					part def right_process :> AADL::Process {
						in port process_in : AADL::DataPort {
							in item :>> type : port_connections::d;
						}
					}

					part def 'right_process.i' :> right_process {
						part right_thread : port_connections::right_thread;
						connection down : AADL::PortConnection connect process_in to right_thread.thread_in;
					}

					part def right_thread :> AADL::Thread {
						in port thread_in : AADL::DataPort {
							in item :>> type : port_connections::d;
						}
					}

					part def d :> AADL::Data;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testPortConnectionToDataAccess() throws Exception {
		var aadl = """
				package port_connection_to_data_access
				public
					system top
					end top;

					system implementation top.i
						subcomponents
							left: system left;
							right: system right;
						connections
							conn: port left.dp1 -> right.da1;
					end top.i;

					system left
						features
							dp1: out data port d;
					end left;

					system right
						features
							da1: requires data access d;
					end right;

					data d
					end d;
				end port_connection_to_data_access;
				""";
		var sysml = """
				package port_connection_to_data_access {
					part def top :> AADL::System;

					part def 'top.i' :> top {
						part left : port_connection_to_data_access::left;
						part right : port_connection_to_data_access::right;
						connection conn : AADL::PortConnection connect left.dp1 to right.da1;
					}

					part def left :> AADL::System {
						out port dp1 : AADL::DataPort {
							out item :>> type : port_connection_to_data_access::d;
						}
					}

					part def right :> AADL::System {
						in port da1 : AADL::DataAccess {
							in ref :>> type : port_connection_to_data_access::d;
						}
					}

					part def d :> AADL::Data;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testPortConnectionToDataSubcomponent() throws Exception {
		var aadl = """
				package port_connection_to_data_subcomponent
				public
					system top
					end top;

					system implementation top.i
						subcomponents
							d: data d;
							inner: system inner;
						connections
							conn: port inner.dp1 -> d;
					end top.i;

					system inner
						features
							dp1: out data port d;
					end inner;

					data d
					end d;
				end port_connection_to_data_subcomponent;
				""";
		var sysml = """
				package port_connection_to_data_subcomponent {
					part def top :> AADL::System;

					part def 'top.i' :> top {
						part d : port_connection_to_data_subcomponent::d;
						part inner : port_connection_to_data_subcomponent::inner;
						binding conn : AADL::PortConnection bind inner.dp1.type = d;
					}

					part def inner :> AADL::System {
						out port dp1 : AADL::DataPort {
							out item :>> type : port_connection_to_data_subcomponent::d;
						}
					}

					part def d :> AADL::Data;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testDataAccessInTopLevel() throws Exception {
		var aadl = """
				package data_access_in_top_level
				public
					system top
					end top;

					system implementation top.i
						subcomponents
							d: data d;
							s1: system s1;
							s2: system s2;
							s3: system s3;
						connections
							conn1: data access s1.rda1 <-> d;
							conn2: data access s2.rda2 <-> d;
							conn3: data access s3.rda3 <-> d;
					end top.i;

					system s1
						features
							rda1: requires data access d;
					end s1;

					system s2
						features
							rda2: requires data access d;
					end s2;

					system s3
						features
							rda3: requires data access d;
					end s3;

					data d
					end d;
				end data_access_in_top_level;
				""";
		var sysml = """
				package data_access_in_top_level {
					part def top :> AADL::System;

					part def 'top.i' :> top {
						part d : data_access_in_top_level::d;
						part s1 : data_access_in_top_level::s1;
						part s2 : data_access_in_top_level::s2;
						part s3 : data_access_in_top_level::s3;
						binding conn1 : AADL::AccessConnection bind s1.rda1.type = d;
						binding conn2 : AADL::AccessConnection bind s2.rda2.type = d;
						binding conn3 : AADL::AccessConnection bind s3.rda3.type = d;
					}

					part def s1 :> AADL::System {
						in port rda1 : AADL::DataAccess {
							in ref :>> type : data_access_in_top_level::d;
						}
					}

					part def s2 :> AADL::System {
						in port rda2 : AADL::DataAccess {
							in ref :>> type : data_access_in_top_level::d;
						}
					}

					part def s3 :> AADL::System {
						in port rda3 : AADL::DataAccess {
							in ref :>> type : data_access_in_top_level::d;
						}
					}

					part def d :> AADL::Data;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testDataAccessInNested() throws Exception {
		var aadl = """
				package data_access_in_nested
				public
					system top
					end top;

					system implementation top.i
						subcomponents
							left_middle: system left_middle.i;
							right_middle: system right_middle.i;
						connections
							across: data access left_middle.middle_pda <-> right_middle.middle_rda;
					end top.i;

					system left_middle
						features
							middle_pda: provides data access d;
					end left_middle;

					system implementation left_middle.i
						subcomponents
							left_inner: system left_inner.i;
						connections
							middle_up: data access left_inner.inner_pda <-> middle_pda;
					end left_middle.i;

					system left_inner
						features
							inner_pda: provides data access d;
					end left_inner;

					system implementation left_inner.i
						subcomponents
							d: data d;
						connections
							inner_up: data access d <-> inner_pda;
					end left_inner.i;

					system right_middle
						features
							middle_rda: requires data access d;
					end right_middle;

					system implementation right_middle.i
						subcomponents
							right_inner: system right_inner;
						connections
							middle_down: data access middle_rda <-> right_inner.inner_rda;
					end right_middle.i;

					system right_inner
						features
							inner_rda: requires data access d;
					end right_inner;

					data d
					end d;
				end data_access_in_nested;
				""";
		var sysml = """
				package data_access_in_nested {
					part def top :> AADL::System;

					part def 'top.i' :> top {
						part left_middle : 'left_middle.i';
						part right_middle : 'right_middle.i';
						connection across : AADL::AccessConnection connect left_middle.middle_pda to right_middle.middle_rda;
					}

					part def left_middle :> AADL::System {
						out port middle_pda : AADL::DataAccess {
							out ref :>> type : data_access_in_nested::d;
						}
					}

					part def 'left_middle.i' :> left_middle {
						part left_inner : 'left_inner.i';
						connection middle_up : AADL::AccessConnection connect left_inner.inner_pda to middle_pda;
					}

					part def left_inner :> AADL::System {
						out port inner_pda : AADL::DataAccess {
							out ref :>> type : data_access_in_nested::d;
						}
					}

					part def 'left_inner.i' :> left_inner {
						part d : data_access_in_nested::d;
						binding inner_up : AADL::AccessConnection bind d = inner_pda.type;
					}

					part def right_middle :> AADL::System {
						in port middle_rda : AADL::DataAccess {
							in ref :>> type : data_access_in_nested::d;
						}
					}

					part def 'right_middle.i' :> right_middle {
						part right_inner : data_access_in_nested::right_inner;
						connection middle_down : AADL::AccessConnection connect middle_rda to right_inner.inner_rda;
					}

					part def right_inner :> AADL::System {
						in port inner_rda : AADL::DataAccess {
							in ref :>> type : data_access_in_nested::d;
						}
					}

					part def d :> AADL::Data;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testAccessCategories() throws Exception {
		var aadl = """
				package access_categories
				public
					system outer
					end outer;

					system implementation outer.i
						subcomponents
							middle: system middle.i;
							d: data d;
							b: bus b;
							vb: virtual bus vb;
							subp: subprogram subp;
							subpg: subprogram group subpg;
						connections
							data_across: data access middle.da1 <-> d;
							bus_across: bus access middle.ba1 <-> b;
							virtual_bus_across: virtual bus access middle.vba1 <-> vb;
							subprogram_across: subprogram access middle.subpa1 <-> subp;
							subprogram_group_across: subprogram group access middle.subpga1 <-> subpg;
					end outer.i;

					system middle
						features
							da1: requires data access d;
							ba1: requires bus access b;
							vba1: requires virtual bus access vb;
							subpa1: requires subprogram access subp;
							subpga1: requires subprogram group access subpg;
					end middle;

					system implementation middle.i
						subcomponents
							inner: system inner;
						connections
							data_up: data access inner.da2 <-> da1;
							bus_up: bus access inner.ba2 <-> ba1;
							virtual_bus_up: virtual bus access inner.vba2 <-> vba1;
							subprogram_up: subprogram access inner.subpa2 <-> subpa1;
							subprogram_group_up: subprogram group access inner.subpga2 <-> subpga1;
					end middle.i;

					system inner
						features
							da2: requires data access d;
							ba2: requires bus access b;
							vba2: requires virtual bus access vb;
							subpa2: requires subprogram access subp;
							subpga2: requires subprogram group access subpg;
					end inner;

					data d
					end d;

					bus b
					end b;

					virtual bus vb
					end vb;

					subprogram subp
					end subp;

					subprogram group subpg
					end subpg;
				end access_categories;
				""";
		var sysml = """
				package access_categories {
					part def outer :> AADL::System;

					part def 'outer.i' :> outer {
						part b : access_categories::b;
						part d : access_categories::d;
						part subp : access_categories::subp;
						part subpg : access_categories::subpg;
						part middle : 'middle.i';
						part vb : access_categories::vb;
						binding data_across : AADL::AccessConnection bind middle.da1.type = d;
						binding bus_across : AADL::AccessConnection bind middle.ba1.type = b;
						binding virtual_bus_across : AADL::AccessConnection bind middle.vba1.type = vb;
						binding subprogram_across : AADL::AccessConnection bind middle.subpa1.type = subp;
						binding subprogram_group_across : AADL::AccessConnection bind middle.subpga1.type = subpg;
					}

					part def middle :> AADL::System {
						in port ba1 : AADL::BusAccess {
							in ref :>> type : access_categories::b;
						}

						in port vba1 : AADL::VirtualBusAccess {
							in ref :>> type : access_categories::vb;
						}

						in port da1 : AADL::DataAccess {
							in ref :>> type : access_categories::d;
						}

						in port subpga1 : AADL::SubprogramGroupAccess {
							in ref :>> type : access_categories::subpg;
						}

						in port subpa1 : AADL::SubprogramAccess {
							in ref :>> type : access_categories::subp;
						}
					}

					part def 'middle.i' :> middle {
						part inner : access_categories::inner;
						connection data_up : AADL::AccessConnection connect inner.da2 to da1;
						connection bus_up : AADL::AccessConnection connect inner.ba2 to ba1;
						connection virtual_bus_up : AADL::AccessConnection connect inner.vba2 to vba1;
						connection subprogram_up : AADL::AccessConnection connect inner.subpa2 to subpa1;
						connection subprogram_group_up : AADL::AccessConnection connect inner.subpga2 to subpga1;
					}

					part def inner :> AADL::System {
						in port ba2 : AADL::BusAccess {
							in ref :>> type : access_categories::b;
						}

						in port vba2 : AADL::VirtualBusAccess {
							in ref :>> type : access_categories::vb;
						}

						in port da2 : AADL::DataAccess {
							in ref :>> type : access_categories::d;
						}

						in port subpga2 : AADL::SubprogramGroupAccess {
							in ref :>> type : access_categories::subpg;
						}

						in port subpa2 : AADL::SubprogramAccess {
							in ref :>> type : access_categories::subp;
						}
					}

					part def d :> AADL::Data;
					part def b :> AADL::Bus;
					part def vb :> AADL::VirtualBus;
					part def subp :> AADL::Subprogram;
					part def subpg :> AADL::SubprogramGroup;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testUntranslatedConnections() throws Exception {
		var aadl = """
				package untranslated_connections
				public
					system s1
					end s1;

					system implementation s1.i
						subcomponents
							left: system s2;
							right: system s2;
						connections
							feature_group_connection: feature group left.fg -> right.fg;
					end s1.i;

					system s2
						features
							fg: feature group;
					end s2;

					thread t
					end t;

					thread implementation t.i
						calls
							sequence1: {
								call1: subprogram subprogram1;
								call2: subprogram subprogram2;
							};
						connections
							parameter_connection: parameter call1.out_parameter -> call2.in_parameter;
					end t.i;

					subprogram subprogram1
						features
							out_parameter: out parameter;
					end subprogram1;

					subprogram subprogram2
						features
							in_parameter: in parameter;
					end subprogram2;
				end untranslated_connections;
				""";
		var sysml = """
				package untranslated_connections {
					part def s1 :> AADL::System;

					part def 's1.i' :> s1 {
						part left : untranslated_connections::s2;
						part right : untranslated_connections::s2;
						// WARNING: 'feature_group_connection' not translated.
					}

					part def s2 :> AADL::System {
						// WARNING: 'fg' not translated.
					}

					part def t :> AADL::Thread;

					part def 't.i' :> t {
						// WARNING: 'parameter_connection' not translated.
					}

					part def subprogram1 :> AADL::Subprogram {
						// WARNING: 'out_parameter' not translated.
					}

					part def subprogram2 :> AADL::Subprogram {
						// WARNING: 'in_parameter' not translated.
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testUntranslatedConnectionEnds() throws Exception {
		var aadl = """
				package untranslated_connection_ends
				public
					system top_system
						features
							subpa: provides subprogram access subprogram1;
							ep: out event port;
					end top_system;

					system implementation top_system.i
						subcomponents
							left_system: system left_system;
							right_system: system right_system;
						internal features
							event_source: event;
						processor features
							subprogram_proxy: subprogram;
							port_proxy1: port;
							port_proxy2: port;
						connections
							conn1: feature left_system.fg1 -> right_system.fg2;
							conn2: feature subprogram_proxy <-> subpa;
							conn3: port port_proxy1 -> port_proxy2;
							conn4: port event_source -> ep;
							conn5: subprogram access left_system.rsubpa1 <-> processor.subprogram_proxy;
							conn6: subprogram access left_system.rsubpa1 <-> subprogram_proxy;
					end top_system.i;

					system left_system
						features
							fg1: feature group;
							out_port: out data port;
							rsubpa1: requires subprogram access;
					end left_system;

					system right_system
						features
							fg2: feature group;
					end right_system;

					thread top_thread
					end top_thread;

					thread implementation top_thread.i
						calls
							sequence1: {
								call1: subprogram subprogram1;
								call2: subprogram subprogram2;
							};
						connections
							conn6: feature call1.out_parameter -> call2.in_parameter1;
					end top_thread.i;

					subprogram subprogram1
						features
							out_parameter: out parameter;
					end subprogram1;

					subprogram subprogram2
						features
							in_parameter1: in parameter;
					end subprogram2;

					subprogram top_subprogram
						features
							in_parameter2: in parameter;
					end top_subprogram;

					subprogram implementation top_subprogram.i
						calls
							sequence2: {
								call3: subprogram subprogram2;
							};
						connections
							conn7: feature in_parameter2 -> call3.in_parameter1;
					end top_subprogram.i;
				end untranslated_connection_ends;
				""";
		var sysml = """
				package untranslated_connection_ends {
					part def top_system :> AADL::System {
						out port subpa : AADL::SubprogramAccess {
							out ref :>> type : untranslated_connection_ends::subprogram1;
						}

						out port ep : AADL::EventPort;
					}

					part def 'top_system.i' :> top_system {
						part left_system : untranslated_connection_ends::left_system;
						part right_system : untranslated_connection_ends::right_system;
						// WARNING: 'conn5' not translated.
						// WARNING: 'conn6' not translated.
						// WARNING: 'conn1' not translated.
						// WARNING: 'conn2' not translated.
						// WARNING: 'conn3' not translated.
						// WARNING: 'conn4' not translated.
					}

					part def left_system :> AADL::System {
						// WARNING: 'fg1' not translated.
						out port out_port : AADL::DataPort;
						in port rsubpa1 : AADL::SubprogramAccess;
					}

					part def right_system :> AADL::System {
						// WARNING: 'fg2' not translated.
					}

					part def top_thread :> AADL::Thread;

					part def 'top_thread.i' :> top_thread {
						// WARNING: 'conn6' not translated.
					}

					part def subprogram1 :> AADL::Subprogram {
						// WARNING: 'out_parameter' not translated.
					}

					part def subprogram2 :> AADL::Subprogram {
						// WARNING: 'in_parameter1' not translated.
					}

					part def top_subprogram :> AADL::Subprogram {
						// WARNING: 'in_parameter2' not translated.
					}

					part def 'top_subprogram.i' :> top_subprogram {
						// WARNING: 'conn7' not translated.
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testConnectionRefinements() throws Exception {
		var aadl = """
				package connection_refinements
				public
					system top
					end top;

					system implementation top.i1
						subcomponents
							left: system left;
							right: system right;
						connections
							conn: feature left.out_port -> right.in_port;
					end top.i1;

					system implementation top.i2 extends top.i1
						connections
							conn: refined to port;
					end top.i2;

					system left
						features
							out_port: out event port;
					end left;

					system right
						features
							in_port: in event port;
					end right;
				end connection_refinements;
				""";
		var sysml = """
				package connection_refinements {
					part def top :> AADL::System;

					part def 'top.i1' :> top {
						part left : connection_refinements::left;
						part right : connection_refinements::right;
						connection conn : AADL::FeatureConnection connect left.out_port to right.in_port;
					}

					part def 'top.i2' :> 'top.i1' {
						// WARNING: 'conn' not translated.
					}

					part def left :> AADL::System {
						out port out_port : AADL::EventPort;
					}

					part def right :> AADL::System {
						in port in_port : AADL::EventPort;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}
}