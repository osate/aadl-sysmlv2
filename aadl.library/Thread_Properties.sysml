/* SysML v2 Domain Library for AADL:
 * references: 
 * SAE AS-5506D AADL appendix A2
 * OMG SysML v2 release 2024-02 */
 
 standard library package Thread_Properties {
	
	import AADL_Project::*;
		
	attribute def Dispatch_Protocol :> AADL::Property, Supported_Dispatch_Protocols;
	
	attribute def Priority :> AADL::Property, ScalarValues::Integer;
	
}