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
package simple_abstract_feature {
	part def s :> AADL::System {
		in port f1 : AADL::AbstractFeature;
		out port f2 : AADL::AbstractFeature;
		inout port f3 : AADL::AbstractFeature;
	}
}

package feature_points_to_type {
	part def s :> AADL::System {
		in port f1 : AADL::AbstractFeature {
			in item :>> type : feature_points_to_type::b;
		}

		out port f2 : AADL::AbstractFeature {
			out item :>> type : feature_points_to_type::b;
		}

		inout port f3 : AADL::AbstractFeature {
			inout item :>> type : feature_points_to_type::b;
		}
		
		in port p: PP;
	}

	part def b :> AADL::Bus;

	port def PP :> AADL::AbstractFeature {
		in item :>> type : feature_points_to_type::b;
	}

}

package event_ports {
	part def s :> AADL::System {
		in port simple_in : AADL::EventPort;
		out port simple_out : AADL::EventPort;
		inout port simple_in_out : AADL::EventPort;
	}
}
