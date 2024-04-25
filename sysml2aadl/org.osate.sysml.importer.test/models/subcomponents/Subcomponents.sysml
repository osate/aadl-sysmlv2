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
package subcomps {

	part def a :> AADL::Abstract;

	part def 'a.i' :> a {
		part sub1 : AADL::Abstract;
		part sub2 : AADL::Bus;
		part sub3 : AADL::Data;
		part sub4 : AADL::Device;
		part sub5 : AADL::Memory;
		part sub6 : AADL::Process;
		part sub7 : AADL::Processor;
		part sub8 : AADL::System;
		part sub9 : AADL::Subprogram;
		part sub10 : AADL::SubprogramGroup;
		part sub11 : AADL::Thread;
		part sub12 : AADL::ThreadGroup;
		part sub13 : AADL::VirtualBus;
		part sub14 : AADL::VirtualProcessor;
	}
	
	part def 'a.i2' :> a {
		part s1: s;
		part s2: 's.i';
	}
	
	part def x :> AADL::Component;
	part def x1 :> AADL::Component;
	part def y;

		
	part def 's.i' :> s {
		part t: AADL::Component;
	}
	
	part def s :> AADL::System;

}

