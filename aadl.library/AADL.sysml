/* SysML v2 Domain Library for AADL:
 * references: 
 * SAE AS-5506D AADL chapters 4 to 10
 * OMG SysML v2 release 2024-02 */ 

standard library package AADL {
	
	enum def ComponentCategory {
		enum Abstract;
		enum Bus;
		enum Data;
		enum Device;
		enum Memory;
		enum Process;
		enum Processor;
		enum System;
		enum Subprogram;
		enum SubprogramGroup;
		enum Thread;
		enum ThreadGroup;
		enum VirtualProcessor;
		enum VirtualBus;
	}

	abstract part def Component {
		attribute category : ComponentCategory;
 	}
	
	part def Abstract :> Component {
		attribute :>> category = ComponentCategory::Abstract;
		
		attribute Compute_Execution_Time : Timing_Properties::Compute_Execution_Time;
		attribute Period : Timing_Properties::Period;
	}
	
	part def Bus :> Component {
		attribute :>> category = ComponentCategory::Bus;
		
		attribute Period : Timing_Properties::Period;
	}
	
	part def Data :> Component {
		attribute :>> category = ComponentCategory::Data;
		
		attribute value: Base::DataValue;
	}
	
	part def Device :> Component {
		attribute :>> category = ComponentCategory::Device;
		
		attribute Compute_Execution_Time : Timing_Properties::Compute_Execution_Time;
		attribute Period : Timing_Properties::Period;
	}
	
	part def Memory :> Component {
		attribute :>> category = ComponentCategory::Memory;
	}
	
	part def Process :> Component {
		attribute :>> category = ComponentCategory::Process;
		
		attribute Period : Timing_Properties::Period;
	}
	
	part def Processor :> Component {
		attribute :>> category = ComponentCategory::Processor;
		
		attribute Scheduling_Protocol : Deployment_Properties::Scheduling_Protocol;
	}
	
	part def System :> Component {
		attribute :>> category = ComponentCategory::System;
		
		attribute Period : Timing_Properties::Period;
	}
	
	part def Subprogram :> Component {
		attribute :>> category = ComponentCategory::Subprogram;
		
		attribute Compute_Execution_Time : Timing_Properties::Compute_Execution_Time;
	}
	
	part def SubprogramGroup :> Component {
		attribute :>> category = ComponentCategory::SubprogramGroup;
	}
	
	part def Thread :> Component {
		attribute :>> category = ComponentCategory::Thread;
		
		attribute Compute_Execution_Time : Timing_Properties::Compute_Execution_Time;
		attribute Period : Timing_Properties::Period;
		attribute Dispatch_Protocol : Thread_Properties::Dispatch_Protocol;
		attribute Priority : Thread_Properties::Priority;
	}
	
	part def ThreadGroup :> Component {
		attribute :>> category = ComponentCategory::ThreadGroup;
		
		attribute Period : Timing_Properties::Period;
	}
	
	part def VirtualBus :> Component {
		attribute :>> category = ComponentCategory::VirtualBus;
		
		attribute Period : Timing_Properties::Period;
	}
	
	part def VirtualProcessor :> Component {
		attribute :>> category = ComponentCategory::VirtualProcessor;
		
		attribute Period : Timing_Properties::Period;
	}
	
	abstract port def Feature;
	
	port def AbstractFeature :> Feature {
		inout item type : Component;
	}
	
	port def DataPort :> Feature {
		inout item type : Component;
	}
	
	port def EventDataPort :> Feature {
		inout item type : Component;
	}
	
	port def EventPort :> Feature;
	
	port def DataAccess :> Feature {
		inout ref type : Component;
	}
	
	port def BusAccess :> Feature {
		inout ref type : Component;
	}
	
	port def VirtualBusAccess :> Feature {
		inout ref type : Component;
	}
	
	port def SubprogramAccess :> Feature {
		inout ref type : Component;
	}
	
	port def SubprogramGroupAccess :> Feature {
		inout ref type : Component;
	}
	
	abstract connection def Connection :> Connections::BinaryConnection;
	
	connection def FeatureConnection :> Connection {
		end source : Feature;
		end target : Feature;
	}
	
	connection def PortConnection :> Connection {
		end source : Feature;
		end target : Feature;
	}
	
	connection def AccessConnection :> Connection;
	connection def FeatureGroupConnection :> Connection;
	connection def ParameterConnection :> Connection;
	
	abstract attribute def Property;
	
}
