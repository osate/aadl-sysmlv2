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
package function_binding {
	
	import AADL::*;
	
	part def s :> System;

	part def 's.i' :> s {
		part functional_arch : 'functional_arch.i';
		part system_arch : 'system_arch.i';
		connection : ActualFunctionBinding connect functional_arch.sub_1 to system_arch.sub_a;
		connection : ActualFunctionBinding connect (functional_arch.sub_2, system_arch.sub_b, system_arch.sub_c);
		connection : ActualFunctionBinding connect (functional_arch.sub_3, system_arch.sub_b, system_arch.sub_c);
		connection : ActualFunctionBinding connect functional_arch.f1 to system_arch.f2;
	}

	part def functional_arch :> System {
		inout port f1 : AbstractFeature;
	}

	part def 'functional_arch.i' :> functional_arch {
		part sub_1 : System;
		part sub_2 : System;
		part sub_3 : System;
	}

	part def system_arch :> AADL::System {
		inout port f2 : AbstractFeature;
	}

	part def 'system_arch.i' :> system_arch {
		part sub_a : System;
		part sub_b : System;
		part sub_c : System;
	}

}
