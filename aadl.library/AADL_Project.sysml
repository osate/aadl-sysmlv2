/* SysML v2 Domain Library for AADL:
 * references: 
 * SAE AS-5506D AADL appendix A8
 * OMG SysML v2 release 2024-02 */ 

standard library package AADL_Project {
	
	enum def Supported_Dispatch_Protocols {
		/* Periodic, Sporadic, Aperiodic, Timed, Hybrid, Background, <project-specified> */
		enum Periodic;
		enum Sporadic;
		enum Aperiodic;
		enum Timed;
		enum Hybrid;
		enum Background;
	}
	
	alias Periodic for Supported_Dispatch_Protocols::Periodic;
	alias Sporadic for Supported_Dispatch_Protocols::Sporadic;
	alias Aperiodic for Supported_Dispatch_Protocols::Aperiodic;
	alias Timed for Supported_Dispatch_Protocols::Timed;
	alias Hybrid for Supported_Dispatch_Protocols::Hybrid;
	alias Background for Supported_Dispatch_Protocols::Background;
	
	enum def Supported_Scheduling_Protocols {
		/* FixedTimeline, Cooperative, RMS, EDF, SporadicServer, SlackServer, ARINC653 */
		enum RM;
		enum DM;
		enum EDF;
		enum HPF;
		enum ARINC653;
	}

	alias RM for Supported_Scheduling_Protocols::RM;
	alias DM for Supported_Scheduling_Protocols::DM;
	alias EDF for Supported_Scheduling_Protocols::EDF;
	alias HPF for Supported_Scheduling_Protocols::HPF;
	alias ARINC653 for Supported_Scheduling_Protocols::ARINC653;

	attribute def Time :> ISQ::DurationValue {
		attribute :>> num: ScalarValues::Integer;
	}
	
	attribute def Time_Range {
		attribute minimum : Time;
		attribute maximum : Time;
		attribute delta : Time;
	}
	
    package Time_Units {
		attribute <ps> picosecond : SI::DurationUnit {
			:>> unitConversion: MeasurementReferences::ConversionByPrefix {
				:>> prefix = SI::pico;
				:>> referenceUnit = SI::s;
			}
		}
		
		attribute <ns> nanosecond : SI::DurationUnit {
			:>> unitConversion: MeasurementReferences::ConversionByPrefix {
				:>> prefix = SI::nano;
				:>> referenceUnit = SI::s;
			}
		}
		
		attribute <us> microsecond : SI::DurationUnit {
			:>> unitConversion: MeasurementReferences::ConversionByPrefix {
				:>> prefix = SI::micro;
				:>> referenceUnit = SI::s;
			}
		}
		
		attribute <ms> millisecond : SI::DurationUnit {
			:>> unitConversion: MeasurementReferences::ConversionByPrefix {
				:>> prefix = SI::milli;
				:>> referenceUnit = SI::s;
			}
		}
	}
}