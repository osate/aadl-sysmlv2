/**
 * AADL Library for SysMLv2
 *
 * Copyright 2024 Carnegie Mellon University.
 * 
 * NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE ENGINEERING
 * INSTITUTE MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON
 * UNIVERSITY MAKES NO WARRANTIES OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, AS TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF
 * FITNESS FOR PURPOSE OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS
 * OBTAINED FROM USE OF THE MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT
 * MAKE ANY WARRANTY OF ANY KIND WITH RESPECT TO FREEDOM FROM PATENT,
 * TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 * 
 * Licensed under a BSD (SEI)-style license, please see license.txt or
 * contact permission@sei.cmu.edu for full terms.
 *
 * [DISTRIBUTION STATEMENT A] This material has been approved for public
 * release and unlimited distribution.  Please see Copyright notice for
 * non-US Government use and distribution.
 * 
 * DM24-0309
*/

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
	
	abstract connection def ActualBinding;
	
	abstract connection def ActualProcessorBinding :> ActualBinding;
	abstract connection def ActualMemoryBinding :> ActualBinding;
	abstract connection def ActualConnectionBinding :> ActualBinding;
	abstract connection def ActualFunctionBinding :> ActualBinding;
	
	abstract attribute def Property;
}
