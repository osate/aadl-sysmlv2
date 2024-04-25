/**
 * SysML API access examples and SysML to AADL translator
 *
 * Copyright 2024 Carnegie Mellon University.
 *
 * NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE ENGINEERING INSTITUTE MATERIAL IS
 * FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON UNIVERSITY MAKES NO WARRANTIES OF ANY KIND,
 * EITHER EXPRESSED OR IMPLIED, AS TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS
 * FOR PURPOSE OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE MATERIAL.
 * CARNEGIE MELLON UNIVERSITY DOES NOT MAKE ANY WARRANTY OF ANY KIND WITH RESPECT TO FREEDOM FROM
 * PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 *
 * Licensed under an Eclipse Public License - v 2.0-style license, please see license.txt or
 * contact permission@sei.cmu.edu for full terms.
 *
 * [DISTRIBUTION STATEMENT A] This material has been approved for public release and unlimited
 * distribution.  Please see Copyright notice for non-US Government use and distribution.
 *
 * This Software includes and/or makes use of Third-Party Software each subject to its own license.
 *
 * DM24-0393
 */
package port_connections {
	part def top :> AADL::System;

	part def 'top.i' :> top {
		part left_process : 'left_process.i';
		part right_process : 'right_process.i';
		connection across : AADL::PortConnection connect left_process.process_out to right_process.process_in;
	}

	part def left_process :> AADL::Process {
		out port process_out : AADL::DataPort {
			out item :>> type : port_connections::d;
		}
	}

	part def 'left_process.i' :> left_process {
		part left_thread : port_connections::left_thread;
		connection up : AADL::PortConnection connect left_thread.thread_out to process_out;
	}

	part def left_thread :> AADL::Thread {
		out port thread_out : AADL::DataPort {
			out item :>> type : port_connections::d;
		}
	}

	part def right_process :> AADL::Process {
		in port process_in : AADL::DataPort {
			in item :>> type : port_connections::d;
		}
	}

	part def 'right_process.i' :> right_process {
		part right_thread : port_connections::right_thread;
		connection down : AADL::PortConnection connect process_in to right_thread.thread_in;
	}

	part def right_thread :> AADL::Thread {
		in port thread_in : AADL::DataPort {
			in item :>> type : port_connections::d;
		}
	}

	part def d :> AADL::Data;
}

package port_connection_to_data_subcomponent {
	part def top :> AADL::System;

	part def 'top.i' :> top {
		part d : port_connection_to_data_subcomponent::d;
		part inner : port_connection_to_data_subcomponent::inner;
		binding conn : AADL::PortConnection bind inner.dp1.type = d;
	}

	part def inner :> AADL::System {
		out port dp1 : AADL::DataPort {
			out item :>> type : port_connection_to_data_subcomponent::d;
		}
	}

	part def d :> AADL::Data;
}

