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
public class PropertiesTest {
	@Inject
	private TestHelper<AadlPackage> testHelper;

	@Test
	public void testSimplePeriod() throws Exception {
		var aadl = """
				package simple_period
				public
					thread t
						properties
							Period => 2 sec;
					end t;
				end simple_period;
				""";
		var sysml = """
				package simple_period {
					part def t :> AADL::Thread {
						attribute :>> Period = 2 [SI::s];
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testModalPeriod() throws Exception {
		var aadl = """
				package modal_period
				public
					thread t
						modes
							m1: initial mode;
							m2: mode;
						properties
							Period => 10ms in modes (m1), 20ms in modes (m2);
					end t;
				end modal_period;
				""";
		var sysml = """
				package modal_period {
					part def t :> AADL::Thread {
						// WARNING: 'Timing_Properties::Period' not translated.
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testPeriodRefersToOtherProperty() throws Exception {
		var aadl = """
				package period_refers_to_other_property
				public
					thread t
						properties
							Period => Activate_Deadline;
							Activate_Deadline => 20ms;
					end t;
				end period_refers_to_other_property;
				""";
		var sysml = """
				package period_refers_to_other_property {
					part def t :> AADL::Thread {
						// WARNING: 'Timing_Properties::Period' not translated.
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testPeriodWithAppliesTo() throws Exception {
		var aadl = """
				package period_with_applies_to
				public
					process ps
					end ps;

					process implementation ps.i
						subcomponents
							t: thread;
						properties
							Period => 20ms applies to t;
					end ps.i;
				end period_with_applies_to;
				""";
		var sysml = """
				package period_with_applies_to {
					part def ps :> AADL::Process;

					part def 'ps.i' :> ps {
						part t : AADL::Thread;
						// WARNING: 'Timing_Properties::Period' not translated.
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testPeriodAllUnits() throws Exception {
		var aadl = """
				package period_all_units
				public
					thread t1
						properties
							Period => 1ps;
					end t1;

					thread t2
						properties
							Period => 2ns;
					end t2;

					thread t3
						properties
							Period => 3us;
					end t3;

					thread t4
						properties
							Period => 4ms;
					end t4;

					thread t5
						properties
							Period => 5sec;
					end t5;

					thread t6
						properties
							Period => 6min;
					end t6;

					thread t7
						properties
							Period => 7hr;
					end t7;
				end period_all_units;
				""";
		var sysml = """
				package period_all_units {
					part def t1 :> AADL::Thread {
						attribute :>> Period = 1 [AADL_Project::Time_Units::ps];
					}

					part def t2 :> AADL::Thread {
						attribute :>> Period = 2 [AADL_Project::Time_Units::ns];
					}

					part def t3 :> AADL::Thread {
						attribute :>> Period = 3 [AADL_Project::Time_Units::us];
					}

					part def t4 :> AADL::Thread {
						attribute :>> Period = 4 [AADL_Project::Time_Units::ms];
					}

					part def t5 :> AADL::Thread {
						attribute :>> Period = 5 [SI::s];
					}

					part def t6 :> AADL::Thread {
						attribute :>> Period = 6 [SI::min];
					}

					part def t7 :> AADL::Thread {
						attribute :>> Period = 7 [SI::h];
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testPeriodAllCategories() throws Exception {
		var aadl = """
				package period_all_categories
				public
					thread t
						properties
							Period => 1ms;
					end t;

					thread group tg
						properties
							Period => 2ms;
					end tg;

					process ps
						properties
							Period => 3ms;
					end ps;

					system s
						properties
							Period => 4ms;
					end s;

					device dev
						properties
							Period => 5ms;
					end dev;

					virtual processor vp
						properties
							Period => 6ms;
					end vp;

					virtual bus vb
						properties
							Period => 7ms;
					end vb;

					bus b
						properties
							Period => 8ms;
					end b;

					abstract a
						properties
							Period => 9ms;
					end a;
				end period_all_categories;
				""";
		var sysml = """
				package period_all_categories {
					part def t :> AADL::Thread {
						attribute :>> Period = 1 [AADL_Project::Time_Units::ms];
					}

					part def tg :> AADL::ThreadGroup {
						attribute :>> Period = 2 [AADL_Project::Time_Units::ms];
					}

					part def ps :> AADL::Process {
						attribute :>> Period = 3 [AADL_Project::Time_Units::ms];
					}

					part def s :> AADL::System {
						attribute :>> Period = 4 [AADL_Project::Time_Units::ms];
					}

					part def dev :> AADL::Device {
						attribute :>> Period = 5 [AADL_Project::Time_Units::ms];
					}

					part def vp :> AADL::VirtualProcessor {
						attribute :>> Period = 6 [AADL_Project::Time_Units::ms];
					}

					part def vb :> AADL::VirtualBus {
						attribute :>> Period = 7 [AADL_Project::Time_Units::ms];
					}

					part def b :> AADL::Bus {
						attribute :>> Period = 8 [AADL_Project::Time_Units::ms];
					}

					part def a :> AADL::Abstract {
						attribute :>> Period = 9 [AADL_Project::Time_Units::ms];
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testSignedPeriod() throws Exception {
		var aadl = """
				package signed_period
				public
					thread t1
						properties
							Period => +1ms;
					end t1;

					thread t2
						properties
							Period => -2ms;
					end t2;
				end signed_period;
				""";
		var sysml = """
				package signed_period {
					part def t1 :> AADL::Thread {
						attribute :>> Period = 1 [AADL_Project::Time_Units::ms];
					}

					part def t2 :> AADL::Thread {
						attribute :>> Period = -2 [AADL_Project::Time_Units::ms];
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testSimpleComputeExecutionTime() throws Exception {
		var aadl = """
				package simple_compute_execution_time
				public
					thread t1
						properties
							Compute_Execution_Time => 1ms .. 10ms;
					end t1;

					thread t2
						properties
							Compute_Execution_Time => 20ms .. 30ms delta 2ms;
					end t2;
				end simple_compute_execution_time;
				""";
		var sysml = """
				package simple_compute_execution_time {
					part def t1 :> AADL::Thread {
						attribute :>> Compute_Execution_Time {
							:>> minimum = 1 [AADL_Project::Time_Units::ms];
							:>> maximum = 10 [AADL_Project::Time_Units::ms];
						}
					}

					part def t2 :> AADL::Thread {
						attribute :>> Compute_Execution_Time {
							:>> minimum = 20 [AADL_Project::Time_Units::ms];
							:>> maximum = 30 [AADL_Project::Time_Units::ms];
							:>> delta = 2 [AADL_Project::Time_Units::ms];
						}
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testModalComputeExecutionTime() throws Exception {
		var aadl = """
				package modal_compute_execution_time
				public
					thread t
						modes
							m1: initial mode;
							m2: mode;
						properties
							Compute_Execution_Time => 1ms .. 10ms in modes (m1), 20ms .. 30ms in modes (m2);
					end t;
				end modal_compute_execution_time;
				""";
		var sysml = """
				package modal_compute_execution_time {
					part def t :> AADL::Thread {
						// WARNING: 'Timing_Properties::Compute_Execution_Time' not translated.
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testComputeExecutionTimeWithAppliesto() throws Exception {
		var aadl = """
				package compute_execution_time_with_applies_to
				public
					process ps
					end ps;

					process implementation ps.i
						subcomponents
							t: thread;
						properties
							Compute_Execution_Time => 1ms .. 10ms applies to t;
					end ps.i;
				end compute_execution_time_with_applies_to;
				""";
		var sysml = """
				package compute_execution_time_with_applies_to {
					part def ps :> AADL::Process;

					part def 'ps.i' :> ps {
						part t : AADL::Thread;
						// WARNING: 'Timing_Properties::Compute_Execution_Time' not translated.
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testComputeExecutionTimeAllUnits() throws Exception {
		var aadl = """
				package compute_execution_time_all_units
				public
					thread t1
						properties
							Compute_Execution_Time => 10ps .. 100ps delta 1ps;
					end t1;

					thread t2
						properties
							Compute_Execution_Time => 20ns .. 200ns delta 2ns;
					end t2;

					thread t3
						properties
							Compute_Execution_Time => 30us .. 300us delta 3us;
					end t3;

					thread t4
						properties
							Compute_Execution_Time => 40ms .. 400ms delta 4ms;
					end t4;

					thread t5
						properties
							Compute_Execution_Time => 50sec .. 500sec delta 5sec;
					end t5;

					thread t6
						properties
							Compute_Execution_Time => 60min .. 600min delta 6min;
					end t6;

					thread t7
						properties
							Compute_Execution_Time => 70hr .. 700hr delta 7hr;
					end t7;
				end compute_execution_time_all_units;
				""";
		var sysml = """
				package compute_execution_time_all_units {
					part def t1 :> AADL::Thread {
						attribute :>> Compute_Execution_Time {
							:>> minimum = 10 [AADL_Project::Time_Units::ps];
							:>> maximum = 100 [AADL_Project::Time_Units::ps];
							:>> delta = 1 [AADL_Project::Time_Units::ps];
						}
					}

					part def t2 :> AADL::Thread {
						attribute :>> Compute_Execution_Time {
							:>> minimum = 20 [AADL_Project::Time_Units::ns];
							:>> maximum = 200 [AADL_Project::Time_Units::ns];
							:>> delta = 2 [AADL_Project::Time_Units::ns];
						}
					}

					part def t3 :> AADL::Thread {
						attribute :>> Compute_Execution_Time {
							:>> minimum = 30 [AADL_Project::Time_Units::us];
							:>> maximum = 300 [AADL_Project::Time_Units::us];
							:>> delta = 3 [AADL_Project::Time_Units::us];
						}
					}

					part def t4 :> AADL::Thread {
						attribute :>> Compute_Execution_Time {
							:>> minimum = 40 [AADL_Project::Time_Units::ms];
							:>> maximum = 400 [AADL_Project::Time_Units::ms];
							:>> delta = 4 [AADL_Project::Time_Units::ms];
						}
					}

					part def t5 :> AADL::Thread {
						attribute :>> Compute_Execution_Time {
							:>> minimum = 50 [SI::s];
							:>> maximum = 500 [SI::s];
							:>> delta = 5 [SI::s];
						}
					}

					part def t6 :> AADL::Thread {
						attribute :>> Compute_Execution_Time {
							:>> minimum = 60 [SI::min];
							:>> maximum = 600 [SI::min];
							:>> delta = 6 [SI::min];
						}
					}

					part def t7 :> AADL::Thread {
						attribute :>> Compute_Execution_Time {
							:>> minimum = 70 [SI::h];
							:>> maximum = 700 [SI::h];
							:>> delta = 7 [SI::h];
						}
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testComputeExecutionTimeAllCategories() throws Exception {
		var aadl = """
				package compute_execution_time_all_categories
				public
					thread t
						properties
							Compute_Execution_Time => 1ms .. 2ms;
					end t;

					device dev
						properties
							Compute_Execution_Time => 3ms .. 4ms;
					end dev;

					subprogram subp
						properties
							Compute_Execution_Time => 5ms .. 6ms;
					end subp;

					abstract a
						properties
							Compute_Execution_Time => 7ms .. 8ms;
					end a;
				end compute_execution_time_all_categories;
				""";
		var sysml = """
				package compute_execution_time_all_categories {
					part def t :> AADL::Thread {
						attribute :>> Compute_Execution_Time {
							:>> minimum = 1 [AADL_Project::Time_Units::ms];
							:>> maximum = 2 [AADL_Project::Time_Units::ms];
						}
					}

					part def dev :> AADL::Device {
						attribute :>> Compute_Execution_Time {
							:>> minimum = 3 [AADL_Project::Time_Units::ms];
							:>> maximum = 4 [AADL_Project::Time_Units::ms];
						}
					}

					part def subp :> AADL::Subprogram {
						attribute :>> Compute_Execution_Time {
							:>> minimum = 5 [AADL_Project::Time_Units::ms];
							:>> maximum = 6 [AADL_Project::Time_Units::ms];
						}
					}

					part def a :> AADL::Abstract {
						attribute :>> Compute_Execution_Time {
							:>> minimum = 7 [AADL_Project::Time_Units::ms];
							:>> maximum = 8 [AADL_Project::Time_Units::ms];
						}
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testComputeExecutionTimeRefersToOtherProperty() throws Exception {
		var aadl = """
				package compute_execution_time_refers_to_other_property
				public
					thread t1
						properties
							Compute_Execution_Time => Activate_Execution_Time;
							Activate_Execution_Time => 1ms .. 2ms;
					end t1;

					thread t2
						properties
							Compute_Execution_Time => Period .. 4ms;
							Period => 3ms;
					end t2;

					thread t3
						properties
							Compute_Execution_Time => 5ms .. Period;
							Period => 6ms;
					end t3;

					thread t4
						properties
							Compute_Execution_Time => 7ms .. 8ms delta Period;
							Period => 9ms;
					end t4;
				end compute_execution_time_refers_to_other_property;
				""";
		var sysml = """
				package compute_execution_time_refers_to_other_property {
					part def t1 :> AADL::Thread {
						// WARNING: 'Timing_Properties::Compute_Execution_Time' not translated.
					}

					part def t2 :> AADL::Thread {
						// WARNING: 'Timing_Properties::Compute_Execution_Time' not translated.
						attribute :>> Period = 3 [AADL_Project::Time_Units::ms];
					}

					part def t3 :> AADL::Thread {
						// WARNING: 'Timing_Properties::Compute_Execution_Time' not translated.
						attribute :>> Period = 6 [AADL_Project::Time_Units::ms];
					}

					part def t4 :> AADL::Thread {
						// WARNING: 'Timing_Properties::Compute_Execution_Time' not translated.
						attribute :>> Period = 9 [AADL_Project::Time_Units::ms];
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}

	@Test
	public void testSignedComputeExecutionTime() throws Exception {
		var aadl = """
				package signed_compute_execution_time
				public
					thread t1
						properties
							Compute_Execution_Time => +1ms .. +2ms delta +3ms;
					end t1;

					thread t2
						properties
							Compute_Execution_Time => -6ms .. -5ms delta 4ms;
					end t2;
				end signed_compute_execution_time;
				""";
		var sysml = """
				package signed_compute_execution_time {
					part def t1 :> AADL::Thread {
						attribute :>> Compute_Execution_Time {
							:>> minimum = 1 [AADL_Project::Time_Units::ms];
							:>> maximum = 2 [AADL_Project::Time_Units::ms];
							:>> delta = 3 [AADL_Project::Time_Units::ms];
						}
					}

					part def t2 :> AADL::Thread {
						attribute :>> Compute_Execution_Time {
							:>> minimum = -6 [AADL_Project::Time_Units::ms];
							:>> maximum = -5 [AADL_Project::Time_Units::ms];
							:>> delta = 4 [AADL_Project::Time_Units::ms];
						}
					}
				}""";
		assertEquals(sysml, Aadl2SysmlTranslator.translateToSysML(testHelper.parseString(aadl)));
	}
}