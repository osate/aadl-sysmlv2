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
package org.osate.sysml.util;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * Utility class to create proxy objects with a given ID.
 */
public final class ProxyUtil {
	
	private ProxyUtil() {
	}

	/**
	 * Create an EMF proxy object with the given eCLass and ID.
	 * @param res the resource for the proxy object
	 * @param type the eClass of the proxy
	 * @param id the ID of the proxy
	 * @return
	 */
	public static EObject createProxy(Resource res, EClass type, String id) {
		var proxy = (EObject) EcoreUtil.create(getInstantiableClass(type));
		var uri = URI.createURI(res.getURI().toString()).appendFragment(id);
		((InternalEObject) proxy).eSetProxyURI(uri);
		return proxy;
	}

	/**
	 * Find an eClass the can be instantiated to serve as a proxy for the given eCLass.
	 * @param target the eClass
	 * @return the instantiable eClass, some concrete subtype of target
	 */
	public static EClass getInstantiableClass(EClass target) {
		if (!target.isAbstract())
			return target;
		for (var o : target.getEPackage().getEClassifiers()) {
			if (o instanceof EClass eClass) {
				if (!eClass.isAbstract() && target.isSuperTypeOf(eClass)) {
					return eClass;
				}
			}
		}
		return null;
	}

}
