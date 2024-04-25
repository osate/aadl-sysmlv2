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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.omg.sysml.lang.sysml.Element;
import org.omg.sysml.lang.sysml.Relationship;
import org.omg.sysml.lang.sysml.SysMLPackage;

/**
 * This class contains methods to convert objects of class org.omg.sysml.model.Element
 * to objects of class org.omg.sysml.lang.syml.Element.
 * It makes use of the ecore meta-model for SysML.
 */
public final class ElementHelper {

	/**
	 * A map from ID to library element with that ID. 
	 * Used to avoid creating proxies for library elements.
	 */
	private Map<String, Element> libraryElements;

	public ElementHelper(Map<String, Element> libraryElements) {
		this.libraryElements = libraryElements;
	}

	/**
	 * Convert an org.omg.sysml.model.Element to an org.omg.sysml.lang.sysml.Element.
	 * @param res the resource for the new element
	 * @param owner the owner for the new element
	 * @param me the element to convert
	 * @return the new element
	 */
	public Element fromModel(Resource res, Element owner, org.omg.sysml.model.Element me) {
		var type = (String) me.get("@type");
		EClass eClass = (EClass) SysMLPackage.eINSTANCE.getEClassifier(type);
		var e = (Element) EcoreUtil.create(eClass);

		if (owner == null) {
			res.getContents().add(e);
		} else if (owner instanceof Relationship r) {
			r.getOwnedRelatedElement().add(e);
		} else {
			owner.getOwnedRelationship().add((Relationship) e);
		}

		populateAttributes(e, me);
		populateReferences(res, e, me);

		return e;
	}

	/**
	 * Set the eAttributes of the new element.
	 * @param eo the new element
	 * @param me the model element
	 */
	@SuppressWarnings("unchecked")
	protected void populateAttributes(EObject eo, org.omg.sysml.model.Element me) {
		var eClass = eo.eClass();

		for (var attr : eClass.getEAllAttributes()) {
			if (attr.isChangeable() && !attr.isDerived()) {
				var name = attr.getName();
				var type = attr.getEAttributeType();
				var value = me.get(name);
				if (value != null) {
					if (attr.isMany()) {
						var attrList = (List<Object>) eo.eGet(attr);
						var valueList = (List<Object>) forceList(value);
						for (var v : valueList) {
							if (v instanceof String str) {
								v = EcoreUtil.createFromString(type, str);
							}
							try {
								attrList.add(v);
							} catch (UnsupportedOperationException uoe) {
								// can't set, ignore
							}
						}
					} else {
						if (!"String".equals(type.getName()) && value instanceof String str) {
							value = EcoreUtil.createFromString(type, str);
						}
						try {
							eo.eSet(attr, value);
						} catch (UnsupportedOperationException uoe) {
							// can't set, ignore
						}
					}
				}
			}
		}
	}

	private static final Pattern pattern = Pattern.compile(".* must be an instance of (.*)");

	/**
	 * Set all eReferences in the new element.
	 * @param res the resource containing the new element
	 * @param eo the new element
	 * @param me the model element
	 */
	@SuppressWarnings("unchecked")
	protected void populateReferences(Resource res, EObject eo, org.omg.sysml.model.Element me) {
		var eClass = eo.eClass();

		for (var ref : eClass.getEAllReferences()) {
			var name = ref.getName();
			var type = ref.getEReferenceType();

			if (type != null && ref.isChangeable() && !ref.isDerived()) {
				if (ref.isMany() && !"ownedRelationship".equals(name)) {
					var refList = (List<EObject>) eo.eGet(ref);
					var valueList = (List<Map<String, String>>) forceList(me.get(name));

					refList.clear();
					for (var value : valueList) {
						var id = (String) value.get("@id");
						var elem = libraryElements.get(id);

						if (elem == null) {
							elem = (Element) res.getEObject(id);
						}
						if (elem != null) {
							refList.add(elem);
						} else {
							addProxy(res, refList, ref, type, (String) value.get("@id"));
						}
					}
				}
				if (!ref.isMany() && !"ownedRelatedElement".equals(name)) {
					var obj = me.get(name);
					if (obj instanceof List<?> l) {
						if (l.size() < 1)
							return;
						obj = l.get(0);
					}
					var value = (Map<String, String>) obj;

					if (value != null) {
						var id = (String) value.get("@id");
						var elem = libraryElements.get(id);

						if (elem == null) {
							elem = (Element) res.getEObject(id);
						}
						if (elem != null) {
							eo.eSet(ref, elem);
						} else {
							addProxy(res, eo, ref, type, (String) value.get("@id"));
						}
					}
				}
			}
		}
	}

	/**
	 * Create a proxy for a reference to an element that is either in a different resource or
	 * in the same resource but has not yet been created.
	 * @param res the resource
	 * @param eo the new element that references the proxy
	 * @param ref the eReference to the other element
	 * @param type the type of the referenced element
	 * @param id the ID of the referenced element
	 */
	protected void addProxy(Resource res, EObject eo, EReference ref, EClass type, String id) {
		Element proxy = null;
		try {
			proxy = (Element) ProxyUtil.createProxy(res, type, id);
			eo.eSet(ref, proxy);
		} catch (IllegalArgumentException iae) {
			// different type because of redefinition
			// exception message contains name of expected type
			var m = pattern.matcher(iae.getMessage());
			if (m.find()) {
				var typeName = m.group(1);
				type = (EClass) SysMLPackage.eINSTANCE.getEClassifier(typeName);
				proxy = (Element) ProxyUtil.createProxy(res, type, id);
				eo.eSet(ref, proxy);
			}
		}
		if (proxy != null) {
			res.getContents().add(proxy);
		}
	}

	/**
	 * Create a proxy for a reference in a list to an element that is either in a different resource or
	 * in the same resource but has not yet been created.
	 * @param res the resource
	 * @param refList The list that contains the reference to the proxy
	 * @param ref the eReference to the other element
	 * @param type the type of the referenced element
	 * @param id the ID of the referenced element
	 */
	protected void addProxy(Resource res, List<EObject> refList, EReference ref, EClass type, String id) {
		Element proxy = null;
		try {
			proxy = (Element) ProxyUtil.createProxy(res, type, id);
			refList.add(proxy);
		} catch (IllegalArgumentException iae) {
			// different type because of redefinition
			// exception message contains name of expected type
			var m = pattern.matcher(iae.getMessage());
			if (m.find()) {
				var typeName = m.group(1);
				type = (EClass) SysMLPackage.eINSTANCE.getEClassifier(typeName);
				proxy = (Element) ProxyUtil.createProxy(res, type, id);
				refList.add(proxy);
			}
		} catch (UnsupportedOperationException uoe) {
			// can't set, ignore
		}
		if (proxy != null) {
			res.getContents().add(proxy);
		}
	}

	/**
	 * Turn the argument into a list:
	 * <ul>
	 *   <li>if it is a list, return it as is
	 *   <li>if it is null, return an empty list
	 *   <li>otherwise return a list with the argument as the single element
	 * <ul>
	 * @param <T> the type of the argument
	 * @param arg the argument to forse into a list
	 * @return a list
	 */
	public static <T> List<?> forceList(T arg) {
		if (arg instanceof List<?> l)
			return l;
		if (arg == null)
			return Collections.emptyList();
		return Collections.singletonList(arg);
	}

}
