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
public class BindingPropertiesTest {
	@Inject
	private TestHelper<AadlPackage> testHelper;

	@Test
	public void testSimpleProcessorBinding() throws Exception {
		var aadl = """
				package simple_processor_binding
				public
					system s
					end s;

					system implementation s.i
						subcomponents
							ps: process ps.i;
							proc: processor;
						properties
							Actual_Processor_Binding => (reference (proc)) applies to ps.t;
					end s.i;

					process ps
					end ps;

					process implementation ps.i
						subcomponents
							t: thread;
					end ps.i;
				end simple_processor_binding;
				""";
		var sysml = """
				package simple_processor_binding {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part ps : 'ps.i';
						part proc : AADL::Processor;
						connection : AADL::ActualProcessorBinding connect ps.t to proc;
					}

					part def ps :> AADL::Process;

					part def 'ps.i' :> ps {
						part t : AADL::Thread;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testMultipleThreads() throws Exception {
		var aadl = """
				package multiple_threads
				public
					system s
					end s;

					system implementation s.i
						subcomponents
							ps: process ps.i;
							proc: processor;
						properties
							Actual_Processor_Binding => (reference (proc)) applies to ps.t1, ps.t2, ps.t3;
					end s.i;

					process ps
					end ps;

					process implementation ps.i
						subcomponents
							t1: thread;
							t2: thread;
							t3: thread;
					end ps.i;
				end multiple_threads;
				""";
		var sysml = """
				package multiple_threads {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part ps : 'ps.i';
						part proc : AADL::Processor;
						connection : AADL::ActualProcessorBinding connect ps.t1 to proc;
						connection : AADL::ActualProcessorBinding connect ps.t2 to proc;
						connection : AADL::ActualProcessorBinding connect ps.t3 to proc;
					}

					part def ps :> AADL::Process;

					part def 'ps.i' :> ps {
						part t1 : AADL::Thread;
						part t2 : AADL::Thread;
						part t3 : AADL::Thread;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testMultipleProcessors() throws Exception {
		var aadl = """
				package multiple_processors
				public
					system s
					end s;

					system implementation s.i
						subcomponents
							ps: process ps.i;
							proc1: processor;
							proc2: processor;
							proc3: processor;
						properties
							Actual_Processor_Binding => (reference (proc1), reference (proc2), reference (proc3)) applies to ps.t;
					end s.i;

					process ps
					end ps;

					process implementation ps.i
						subcomponents
							t: thread;
					end ps.i;
				end multiple_processors;
				""";
		var sysml = """
				package multiple_processors {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part ps : 'ps.i';
						part proc1 : AADL::Processor;
						part proc2 : AADL::Processor;
						part proc3 : AADL::Processor;
						connection : AADL::ActualProcessorBinding connect (ps.t, proc1, proc2, proc3);
					}

					part def ps :> AADL::Process;

					part def 'ps.i' :> ps {
						part t : AADL::Thread;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testMultipleProcessorsAndThread() throws Exception {
		var aadl = """
				package multiple_processors_and_threads
				public
					system s
					end s;

					system implementation s.i
						subcomponents
							ps: process ps.i;
							proc1: processor;
							proc2: processor;
							proc3: processor;
						properties
							Actual_Processor_Binding => (reference (proc1), reference (proc2), reference (proc3))
								applies to ps.t1, ps.t2, ps.t3;
					end s.i;

					process ps
					end ps;

					process implementation ps.i
						subcomponents
							t1: thread;
							t2: thread;
							t3: thread;
					end ps.i;
				end multiple_processors_and_threads;
				""";
		var sysml = """
				package multiple_processors_and_threads {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part ps : 'ps.i';
						part proc1 : AADL::Processor;
						part proc2 : AADL::Processor;
						part proc3 : AADL::Processor;
						connection : AADL::ActualProcessorBinding connect (ps.t1, proc1, proc2, proc3);
						connection : AADL::ActualProcessorBinding connect (ps.t2, proc1, proc2, proc3);
						connection : AADL::ActualProcessorBinding connect (ps.t3, proc1, proc2, proc3);
					}

					part def ps :> AADL::Process;

					part def 'ps.i' :> ps {
						part t1 : AADL::Thread;
						part t2 : AADL::Thread;
						part t3 : AADL::Thread;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testModalProcessorBinding() throws Exception {
		var aadl = """
				package modal_processor_binding
				public
					system s
					end s;

					system implementation s.i
						subcomponents
							ps: process ps.i;
							proc1: processor;
							proc2: processor;
						properties
							Actual_Processor_Binding =>
								(reference (proc1)) in modes (m1),
								(reference (proc2)) in modes (m2)
								applies to ps.t;
					end s.i;

					process ps
					end ps;

					process implementation ps.i
						subcomponents
							t: thread t;
					end ps.i;

					thread t
						modes
							m1: initial mode;
							m2: mode;
					end t;
				end modal_processor_binding;
				""";
		var sysml = """
				package modal_processor_binding {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part ps : 'ps.i';
						part proc1 : AADL::Processor;
						part proc2 : AADL::Processor;
						// WARNING: 'Deployment_Properties::Actual_Processor_Binding' not translated.
					}

					part def ps :> AADL::Process;

					part def 'ps.i' :> ps {
						part t : modal_processor_binding::t;
					}

					part def t :> AADL::Thread;
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testEmptyProcessorBinding() throws Exception {
		var aadl = """
				package empty_processor_binding
				public
					system s
					end s;

					system implementation s.i
						subcomponents
							ps: process ps.i;
						properties
							Actual_Processor_Binding => () applies to ps.t;
					end s.i;

					process ps
					end ps;

					process implementation ps.i
						subcomponents
							t: thread;
					end ps.i;
				end empty_processor_binding;
				""";
		var sysml = """
				package empty_processor_binding {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part ps : 'ps.i';
						// WARNING: 'Deployment_Properties::Actual_Processor_Binding' not translated.
					}

					part def ps :> AADL::Process;

					part def 'ps.i' :> ps {
						part t : AADL::Thread;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testNoApplies() throws Exception {
		var aadl = """
				package no_applies
				public
					system s
					end s;

					system implementation s.i
						subcomponents
							proc: processor;
						properties
							Actual_Processor_Binding => (reference (proc));
					end s.i;
				end no_applies;
				""";
		var sysml = """
				package no_applies {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part proc : AADL::Processor;
						// WARNING: 'Deployment_Properties::Actual_Processor_Binding' not translated.
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testDeeplyNestedProcessorBinding() throws Exception {
		var aadl = """
				package deeply_nested_processor_binding
				public
					system s
					end s;

					system implementation s.i
						subcomponents
							ps: process ps.i;
							sub1: system sub1.i;
						properties
							Actual_Processor_Binding => (reference (sub1.sub2.sub3.proc1)) applies to ps.tg.t1;
							Actual_Processor_Binding => (reference (sub1.sub2.sub3.proc2), reference (sub1.sub2.sub3.proc3))
								applies to ps.tg.t2;
					end s.i;

					process ps
					end ps;

					process implementation ps.i
						subcomponents
							tg: thread group tg.i;
					end ps.i;

					thread group tg
					end tg;

					thread group implementation tg.i
						subcomponents
							t1: thread;
							t2: thread;
					end tg.i;

					system sub1
					end sub1;

					system implementation sub1.i
						subcomponents
							sub2: system sub2.i;
					end sub1.i;

					system sub2
					end sub2;

					system implementation sub2.i
						subcomponents
							sub3: system sub3.i;
					end sub2.i;

					system sub3
					end sub3;

					system implementation sub3.i
						subcomponents
							proc1: processor;
							proc2: processor;
							proc3: processor;
					end sub3.i;
				end deeply_nested_processor_binding;
				""";
		var sysml = """
				package deeply_nested_processor_binding {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part ps : 'ps.i';
						part sub1 : 'sub1.i';
						connection : AADL::ActualProcessorBinding connect ps.tg.t1 to sub1.sub2.sub3.proc1;
						connection : AADL::ActualProcessorBinding connect (ps.tg.t2, sub1.sub2.sub3.proc2, sub1.sub2.sub3.proc3);
					}

					part def ps :> AADL::Process;

					part def 'ps.i' :> ps {
						part tg : 'tg.i';
					}

					part def tg :> AADL::ThreadGroup;

					part def 'tg.i' :> tg {
						part t1 : AADL::Thread;
						part t2 : AADL::Thread;
					}

					part def sub1 :> AADL::System;

					part def 'sub1.i' :> sub1 {
						part sub2 : 'sub2.i';
					}

					part def sub2 :> AADL::System;

					part def 'sub2.i' :> sub2 {
						part sub3 : 'sub3.i';
					}

					part def sub3 :> AADL::System;

					part def 'sub3.i' :> sub3 {
						part proc1 : AADL::Processor;
						part proc2 : AADL::Processor;
						part proc3 : AADL::Processor;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testMemoryBinding() throws Exception {
		var aadl = """
				package memory_binding
				public
					system s
						features
							dp1: in data port;
							edp1: in event data port;
					end s;

					system implementation s.i
						subcomponents
							d1: data;
							d2: data;
							d3: data;
							m1: memory;
							m2: memory;
							m3: memory;
							m4: memory;
							m5: memory;
						properties
							Actual_Memory_Binding => (reference (m1)) applies to d1;
							Actual_Memory_Binding => (reference (m2), reference (m3)) applies to d2, d3;
							Actual_Memory_Binding => (reference (m4)) applies to dp1;
							Actual_Memory_Binding => (reference (m5)) applies to edp1;
					end s.i;
				end memory_binding;
				""";
		var sysml = """
				package memory_binding {
					part def s :> AADL::System {
						in port dp1 : AADL::DataPort;
						in port edp1 : AADL::EventDataPort;
					}

					part def 's.i' :> s {
						part d1 : AADL::Data;
						part d2 : AADL::Data;
						part d3 : AADL::Data;
						part m1 : AADL::Memory;
						part m2 : AADL::Memory;
						part m3 : AADL::Memory;
						part m4 : AADL::Memory;
						part m5 : AADL::Memory;
						connection : AADL::ActualMemoryBinding connect d1 to m1;
						connection : AADL::ActualMemoryBinding connect (d2, m2, m3);
						connection : AADL::ActualMemoryBinding connect (d3, m2, m3);
						connection : AADL::ActualMemoryBinding connect dp1 to m4;
						connection : AADL::ActualMemoryBinding connect edp1 to m5;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testMemoryBindingWithFeatureGroup() throws Exception {
		var aadl = """
				package memory_binding_with_feature_group
				public
					system s
						features
							fg1: feature group fgt1;
					end s;

					system implementation s.i
						subcomponents
							m1: memory;
						properties
							Actual_Memory_Binding => (reference (m1)) applies to fg1.dp1;
					end s.i;

					feature group fgt1
						features
							dp1: in data port;
					end fgt1;
				end memory_binding_with_feature_group;
				""";
		var sysml = """
				package memory_binding_with_feature_group {
					part def s :> AADL::System {
						// WARNING: 'fg1' not translated.
					}

					part def 's.i' :> s {
						part m1 : AADL::Memory;
						// WARNING: 'Deployment_Properties::Actual_Memory_Binding' not translated.
					}

					// WARNING: 'fgt1' not translated.
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testConnectionBinding() throws Exception {
		var aadl = """
				package connection_binding
				public
					system s
					end s;

					system implementation s.i
						subcomponents
							b1: bus;
							b2: bus;
							b3: bus;
							left: system left;
							right: system right;
						connections
							conn1: feature left.out1 -> right.in1;
							conn2: feature left.out2 -> right.in2;
							conn3: feature left.out3 -> right.in3;
						properties
							Actual_Connection_Binding => (reference (b1)) applies to conn1;
							Actual_Connection_Binding => (reference (b2), reference (b3)) applies to conn2, conn3;
					end s.i;

					system left
						features
							out1: out feature;
							out2: out feature;
							out3: out feature;
					end left;

					system right
						features
							in1: in feature;
							in2: in feature;
							in3: in feature;
					end right;
				end connection_binding;
				""";
		var sysml = """
				package connection_binding {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part b1 : AADL::Bus;
						part b2 : AADL::Bus;
						part b3 : AADL::Bus;
						part left : connection_binding::left;
						part right : connection_binding::right;
						connection conn1 : AADL::FeatureConnection connect left.out1 to right.in1;
						connection conn2 : AADL::FeatureConnection connect left.out2 to right.in2;
						connection conn3 : AADL::FeatureConnection connect left.out3 to right.in3;
						connection : AADL::ActualConnectionBinding connect conn1 to b1;
						connection : AADL::ActualConnectionBinding connect (conn2, b2, b3);
						connection : AADL::ActualConnectionBinding connect (conn3, b2, b3);
					}

					part def left :> AADL::System {
						out port out1 : AADL::AbstractFeature;
						out port out2 : AADL::AbstractFeature;
						out port out3 : AADL::AbstractFeature;
					}

					part def right :> AADL::System {
						in port in1 : AADL::AbstractFeature;
						in port in2 : AADL::AbstractFeature;
						in port in3 : AADL::AbstractFeature;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testConnectionBindingWithUntranslatedFeatures() throws Exception {
		var aadl = """
				package connection_binding_with_untranslated_features
				public
					subprogram subp
						features
							fg1: feature group;
							p1: in parameter;
					end subp;

					subprogram implementation subp.i
						subcomponents
							a1: abstract;
						properties
							Actual_Connection_Binding => (reference (a1)) applies to fg1;
							Actual_Connection_Binding => (reference (a1)) applies to p1;
					end subp.i;
				end connection_binding_with_untranslated_features;
				""";
		var sysml = """
				package connection_binding_with_untranslated_features {
					part def subp :> AADL::Subprogram {
						// WARNING: 'fg1' not translated.
						// WARNING: 'p1' not translated.
					}

					part def 'subp.i' :> subp {
						part a1 : AADL::Abstract;
						// WARNING: 'Deployment_Properties::Actual_Connection_Binding' not translated.
						// WARNING: 'Deployment_Properties::Actual_Connection_Binding' not translated.
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testConnectionBindingWithUntranslatedConnections() throws Exception {
		var aadl = """
				package connection_binding_with_untranslated_connections
				public
					abstract a
						features
							subpa: provides subprogram access;
							ep: out event port;
					end a;

					abstract implementation a.i
						subcomponents
							b: bus;
							left: system left;
							right: system right;
						internal features
							event_source: event;
						processor features
							subprogram_proxy: subprogram;
						connections
							conn1: feature left.out_fg -> right.in_fg;
							conn2: feature subprogram_proxy <-> subpa;
							conn3: feature event_source -> ep;
							conn4: feature group left.out_fg -> right.in_fg;
						properties
							Actual_Connection_Binding => (reference (b)) applies to conn1;
							Actual_Connection_Binding => (reference (b)) applies to conn2;
							Actual_Connection_Binding => (reference (b)) applies to conn3;
							Actual_Connection_Binding => (reference (b)) applies to conn4;
					end a.i;

					system left
						features
							out_fg: out feature group;
					end left;

					system right
						features
							in_fg: in feature group;
					end right;

					subprogram top_subprogram
						features
							in_parameter1: in parameter;
					end top_subprogram;

					subprogram implementation top_subprogram.i
						subcomponents
							a: abstract;
						calls
							sequence1: {
								call1: subprogram subprogram1;
								call2: subprogram subprogram2;
							};
						connections
							conn5: feature call1.out_parameter1 -> call2.in_parameter2;
							conn6: feature in_parameter1 -> call2.in_parameter2;
							conn7: parameter call1.out_parameter1 -> call2.in_parameter2;
						properties
							Actual_Connection_Binding => (reference (a)) applies to conn5;
							Actual_Connection_Binding => (reference (a)) applies to conn6;
							Actual_Connection_Binding => (reference (a)) applies to conn7;
					end top_subprogram.i;

					subprogram subprogram1
						features
							out_parameter1: out parameter;
					end subprogram1;

					subprogram subprogram2
						features
							in_parameter2: in parameter;
					end subprogram2;
				end connection_binding_with_untranslated_connections;
				""";
		var sysml = """
				package connection_binding_with_untranslated_connections {
					part def a :> AADL::Abstract {
						out port subpa : AADL::SubprogramAccess;
						out port ep : AADL::EventPort;
					}

					part def 'a.i' :> a {
						part b : AADL::Bus;
						part left : connection_binding_with_untranslated_connections::left;
						part right : connection_binding_with_untranslated_connections::right;
						// WARNING: 'conn1' not translated.
						// WARNING: 'conn2' not translated.
						// WARNING: 'conn3' not translated.
						// WARNING: 'conn4' not translated.
						// WARNING: 'Deployment_Properties::Actual_Connection_Binding' not translated.
						// WARNING: 'Deployment_Properties::Actual_Connection_Binding' not translated.
						// WARNING: 'Deployment_Properties::Actual_Connection_Binding' not translated.
						// WARNING: 'Deployment_Properties::Actual_Connection_Binding' not translated.
					}

					part def left :> AADL::System {
						// WARNING: 'out_fg' not translated.
					}

					part def right :> AADL::System {
						// WARNING: 'in_fg' not translated.
					}

					part def top_subprogram :> AADL::Subprogram {
						// WARNING: 'in_parameter1' not translated.
					}

					part def 'top_subprogram.i' :> top_subprogram {
						part a : AADL::Abstract;
						// WARNING: 'conn5' not translated.
						// WARNING: 'conn6' not translated.
						// WARNING: 'conn7' not translated.
						// WARNING: 'Deployment_Properties::Actual_Connection_Binding' not translated.
						// WARNING: 'Deployment_Properties::Actual_Connection_Binding' not translated.
						// WARNING: 'Deployment_Properties::Actual_Connection_Binding' not translated.
					}

					part def subprogram1 :> AADL::Subprogram {
						// WARNING: 'out_parameter1' not translated.
					}

					part def subprogram2 :> AADL::Subprogram {
						// WARNING: 'in_parameter2' not translated.
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testFunctionBinding() throws Exception {
		var aadl = """
				package function_binding
				public
					system s
					end s;

					system implementation s.i
						subcomponents
							functional_arch: system functional_arch.i;
							system_arch: system system_arch.i;
						properties
							Actual_Function_Binding => (reference (system_arch.sub_a)) applies to functional_arch.sub_1;
							Actual_Function_Binding => (reference (system_arch.sub_b), reference (system_arch.sub_c))
								applies to functional_arch.sub_2, functional_arch.sub_3;
							Actual_Function_Binding => (reference (system_arch.f2)) applies to functional_arch.f1;
					end s.i;

					system functional_arch
						features
							f1: feature;
					end functional_arch;

					system implementation functional_arch.i
						subcomponents
							sub_1: system;
							sub_2: system;
							sub_3: system;
					end functional_arch.i;

					system system_arch
						features
							f2: feature;
					end system_arch;

					system implementation system_arch.i
						subcomponents
							sub_a: system;
							sub_b: system;
							sub_c: system;
					end system_arch.i;
				end function_binding;
				""";
		var sysml = """
				package function_binding {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part functional_arch : 'functional_arch.i';
						part system_arch : 'system_arch.i';
						connection : AADL::ActualFunctionBinding connect functional_arch.sub_1 to system_arch.sub_a;
						connection : AADL::ActualFunctionBinding connect (functional_arch.sub_2, system_arch.sub_b, system_arch.sub_c);
						connection : AADL::ActualFunctionBinding connect (functional_arch.sub_3, system_arch.sub_b, system_arch.sub_c);
						connection : AADL::ActualFunctionBinding connect functional_arch.f1 to system_arch.f2;
					}

					part def functional_arch :> AADL::System {
						inout port f1 : AADL::AbstractFeature;
					}

					part def 'functional_arch.i' :> functional_arch {
						part sub_1 : AADL::System;
						part sub_2 : AADL::System;
						part sub_3 : AADL::System;
					}

					part def system_arch :> AADL::System {
						inout port f2 : AADL::AbstractFeature;
					}

					part def 'system_arch.i' :> system_arch {
						part sub_a : AADL::System;
						part sub_b : AADL::System;
						part sub_c : AADL::System;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testFunctionBindingWithUntranslatedFeatures() throws Exception {
		var aadl = """
				package function_binding_with_untranslated_features
				public
					system s
					end s;

					system implementation s.i
						subcomponents
							functional_arch: subprogram functional_arch.i;
							system_arch: subprogram system_arch.i;
						properties
							Actual_Function_Binding => (reference (system_arch.a3)) applies to functional_arch.fg1;
							Actual_Function_Binding => (reference (system_arch.a4)) applies to functional_arch.p1;
							Actual_Function_Binding => (reference (system_arch.fg2)) applies to functional_arch.a1;
							Actual_Function_Binding => (reference (system_arch.p2)) applies to functional_arch.a2;
					end s.i;

					subprogram functional_arch
						features
							fg1: feature group;
							p1: in parameter;
					end functional_arch;

					subprogram implementation functional_arch.i
						subcomponents
							a1: abstract;
							a2: abstract;
					end functional_arch.i;

					subprogram system_arch
						features
							fg2: feature group;
							p2: in parameter;
					end system_arch;

					subprogram implementation system_arch.i
						subcomponents
							a3: abstract;
							a4: abstract;
					end system_arch.i;
				end function_binding_with_untranslated_features;
				""";
		var sysml = """
				package function_binding_with_untranslated_features {
					part def s :> AADL::System;

					part def 's.i' :> s {
						part functional_arch : 'functional_arch.i';
						part system_arch : 'system_arch.i';
						// WARNING: 'Deployment_Properties::Actual_Function_Binding' not translated.
						// WARNING: 'Deployment_Properties::Actual_Function_Binding' not translated.
						// WARNING: 'Deployment_Properties::Actual_Function_Binding' not translated.
						// WARNING: 'Deployment_Properties::Actual_Function_Binding' not translated.
					}

					part def functional_arch :> AADL::Subprogram {
						// WARNING: 'fg1' not translated.
						// WARNING: 'p1' not translated.
					}

					part def 'functional_arch.i' :> functional_arch {
						part a1 : AADL::Abstract;
						part a2 : AADL::Abstract;
					}

					part def system_arch :> AADL::Subprogram {
						// WARNING: 'fg2' not translated.
						// WARNING: 'p2' not translated.
					}

					part def 'system_arch.i' :> system_arch {
						part a3 : AADL::Abstract;
						part a4 : AADL::Abstract;
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}
}