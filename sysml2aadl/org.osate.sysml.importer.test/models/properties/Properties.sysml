package period_all_units {
	part def t1 :> AADL::Thread {
		attribute :>> Period = -1 [AADL_Project::Time_Units::ps];
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
}

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
			:>> minimum = -70 [SI::h];                                                             
			:>> maximum = 700 [SI::h];                                                            
			:>> delta = 7 [SI::h];                                                                
		}                                                                                      
	}                                                                                       
}                                                                                 