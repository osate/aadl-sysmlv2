/* SysML v2 Domain Library for AADL:
 * references: 
 * SAE AS-5506D AADL appendix A1
 * OMG SysML v2 release 2024-02 */
 
 standard library package Deployment_Properties {
	
	import AADL_Project::*;
	
	allocation def Actual_Processor_Binding {
		end part sw : AADL::Component; 
		end part hw : AADL::Component; 
		}
	
	attribute def Scheduling_Protocol :> AADL::Property, Supported_Scheduling_Protocols;
	
}