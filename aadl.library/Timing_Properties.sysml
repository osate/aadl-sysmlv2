/* SysML v2 Domain Library for AADL:
 * references: 
 * SAE AS-5506D AADL appendix A3
 * OMG SysML v2 release 2024-02 */

standard library package Timing_Properties {
	
	import AADL_Project::*;
	
	attribute def Compute_Execution_Time :> AADL::Property, Time_Range;
	
	attribute def Period :> AADL::Property, Time;
	
}