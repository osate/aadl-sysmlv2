/* SysML v2 Domain Library for AADL:
 * example 0001
 * references: 
 * SAE AS-5506D AADL 2.3
 * OMG SysML v2 release 2024-02 */ 
 
 package AADL_Example_0001 {

    import AADL_Project::*;
    import AADL_Project::Time_Units::*;

    part def System_Def :> AADL::System {

        part Hardware: Hardware_Def;
        part Software: Software_Def;

        allocation SwToHw: Deployment_Properties::Actual_Processor_Binding 
            allocate Software to Hardware;
    }

    part def Hardware_Def :> AADL::Processor {
        attribute :>> Scheduling_Protocol = Supported_Scheduling_Protocols::HPF;
    }

    part def Software_Def :> AADL::Process {

        port Input: AADL::DataPort { in :>> type: Int; }
        port Output: AADL::DataPort { out :>> type: Int; }

        part Sender: Thread_100Hz_Def {
            attribute :>> Priority = 2;
            attribute :>> Compute_Execution_Time = 1 [ms]..2 [ms];
        }
        part Receiver: Thread_100Hz_Def {
            attribute :>> Priority = 1;
            attribute :>> Compute_Execution_Time = 2 [ms]..2 [ms];
        }

        connection ToSe: AADL::PortConnection
            connect Input to Sender.Input;
        connection SeToRe: AADL::PortConnection
            connect Sender.Output to Receiver.Input;
        connection ReTo: AADL::PortConnection
            connect Receiver.Output to Output;
            
    }

    part def Thread_100Hz_Def :> AADL::Thread {

        port Input: AADL::DataPort { in :>> type: Int; }
        port Output: AADL::DataPort { out :>> type: Int; }

        attribute :>> Dispatch_Protocol = Periodic;
        attribute :>> Period = 10 [ms];

        /* annotation language BA .... */
    }

    part def Int :> AADL::Data { 
    	attribute :>> value: ScalarValues::Integer;
    }
    
}