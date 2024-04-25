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
package org.osate.sysml2aadl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
import org.eclipse.xtext.EcoreUtil2;
import org.omg.sysml.expressions.util.EvaluationUtil;
import org.omg.sysml.lang.sysml.AttributeUsage;
import org.omg.sysml.lang.sysml.BindingConnectorAsUsage;
import org.omg.sysml.lang.sysml.Classifier;
import org.omg.sysml.lang.sysml.ConnectionDefinition;
import org.omg.sysml.lang.sysml.ConnectionUsage;
import org.omg.sysml.lang.sysml.ConnectorAsUsage;
import org.omg.sysml.lang.sysml.Element;
import org.omg.sysml.lang.sysml.FeatureDirectionKind;
import org.omg.sysml.lang.sysml.FeatureReferenceExpression;
import org.omg.sysml.lang.sysml.ItemUsage;
import org.omg.sysml.lang.sysml.LiteralInteger;
import org.omg.sysml.lang.sysml.Namespace;
import org.omg.sysml.lang.sysml.OperatorExpression;
import org.omg.sysml.lang.sysml.Package;
import org.omg.sysml.lang.sysml.PartDefinition;
import org.omg.sysml.lang.sysml.PartUsage;
import org.omg.sysml.lang.sysml.PortDefinition;
import org.omg.sysml.lang.sysml.PortUsage;
import org.omg.sysml.lang.sysml.Relationship;
import org.omg.sysml.lang.sysml.Type;
import org.omg.sysml.util.FeatureUtil;
import org.osate.aadl2.Aadl2Factory;
import org.osate.aadl2.Aadl2Package;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.AbstractFeature;
import org.osate.aadl2.Access;
import org.osate.aadl2.AccessType;
import org.osate.aadl2.BusAccess;
import org.osate.aadl2.ComponentClassifier;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.ComponentType;
import org.osate.aadl2.ConnectedElement;
import org.osate.aadl2.Connection;
import org.osate.aadl2.ConnectionEnd;
import org.osate.aadl2.ContainedNamedElement;
import org.osate.aadl2.ContainmentPathElement;
import org.osate.aadl2.Context;
import org.osate.aadl2.DirectedFeature;
import org.osate.aadl2.Feature;
import org.osate.aadl2.IntegerLiteral;
import org.osate.aadl2.ListValue;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.NumberType;
import org.osate.aadl2.PackageSection;
import org.osate.aadl2.Property;
import org.osate.aadl2.PropertyAssociation;
import org.osate.aadl2.PropertyType;
import org.osate.aadl2.RangeType;
import org.osate.aadl2.RangeValue;
import org.osate.aadl2.Subcomponent;
import org.osate.aadl2.UnitLiteral;
import org.osate.aadl2.UnitsType;
import org.osate.aadl2.modelsupport.scoping.Aadl2GlobalScopeUtil;
import org.osate.sysml.util.ProxyUtil;

/**
 * This class contains the functionality to convert EMF resources for SysML files
 * to EMF resources for AADL. It makes use of the SysML library for AADL.
 */
public class SysML2AADLConverter {

	final Logger logger = Logger.getLogger(SysML2AADLConverter.class);

	private IProgressMonitor monitor;

	/** The resource set for all AADL and SysML resources. */
	private ResourceSet resourceSet;

	/** Resources for the AADL library files. */
	private Set<Resource> aadlLibraryResources;

	/** Table to look up AADL library elements by ID. */
	private Map<String, Element> aadlLibraryElements = new HashMap<>();

	/** List of all created resources for AADL packages. */
	private List<Resource> created = new ArrayList<>();

	/** Store the resource for the AADL model created for a SysML package. */
	private Map<Package, XMLResource> packageToResourceMap = new HashMap<>();

	/** Map to track references across AADL packages, used to generate with statements. */
	private Map<XMLResource, Set<XMLResource>> usedResources = new HashMap<>();

	private EObject propertyLookupContext = null;

	public SysML2AADLConverter(ResourceSet rs, Set<Resource> aadlLibraryResources, EObject propertyLookupContext) {
		this(rs, aadlLibraryResources, propertyLookupContext, new NullProgressMonitor());
	}

	public SysML2AADLConverter(ResourceSet rs, Set<Resource> aadlLibraryResources, EObject propertyLookupContext,
			IProgressMonitor monitor) {
		this.monitor = monitor;
		this.aadlLibraryResources = aadlLibraryResources;
		this.propertyLookupContext = propertyLookupContext;
		setResourceSet(rs);
		initLibraryElements();
	}

	/**
	 * Set the resource set to use and register a resource factory for file extension 'xml' that
	 * stores IDs for its content.
	 * 
	 * AADL resources are initially created as XML resources such that IDs from SysML elements can
	 * be used for proxy creation and resolution. That way EMF does all the work of resolving 
	 * references between generated AADL elements.  
	 * 
	 * @param rs the resource set to use
	 */
	private void setResourceSet(ResourceSet rs) {
		resourceSet = rs;

		// will use extrinsic IDs for generated AADL to handle proxies
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", new XMLResourceFactoryImpl() {

			@Override
			public Resource createResource(URI uri) {
				return new XMLResourceImpl(uri) {

					@Override
					protected boolean useIDs() {
						return true;
					}

				};
			}
		});
	}

	/**
	 * Put named AADL library elements in a lookup table
	 */
	private void initLibraryElements() {
		for (var r : aadlLibraryResources) {
			for (var iter = EcoreUtil.<Element>getAllContents(r, true); iter.hasNext();) {
				var e = iter.next();
				if (!(e instanceof Relationship)) {
					if (e.getElementId() != null && e.getDeclaredName() != null) {
						aadlLibraryElements.putIfAbsent(e.getElementId(), e);
					}
				}
			}
		}
	}

	/**
	 * Convert a collection of SysML resources to AADL
	 * @param sysmlResources the resources to convert
	 * @return
	 */
	public List<Resource> convert(Collection<Resource> sysmlResources) {
		var xmlResources = new ArrayList<Resource>();
		var todo = sysmlResources.size();

		monitor.beginTask("Conversion to AADL", todo);
		try {
			for (var r : sysmlResources) {
				monitor.subTask(r.getURI().toFileString());
				xmlResources.addAll(convert(r));
				monitor.worked(1);
			}

			for (var r : xmlResources) {
				EcoreUtil.resolveAll(r);
			}

			var aadlResources = new ArrayList<Resource>();
			for (var xmlResource : xmlResources) {
				// add with statement
				var used = usedResources.get(xmlResource);
				if (used != null) {
					var pkg = (AadlPackage) xmlResource.getContents().get(0);
					for (var res : used) {
						if (!res.getContents().isEmpty()) {
							pkg.getOwnedPublicSection().getImportedUnits().add((AadlPackage) res.getContents().get(0));
						}
					}
				}
				if (!xmlResource.getContents().isEmpty()) {
					var aadlResource = resourceSet
							.createResource(xmlResource.getURI().trimFileExtension().appendFileExtension("aadl"));
					aadlResource.getContents().add(EcoreUtil.copy(xmlResource.getContents().get(0)));
					aadlResources.add(aadlResource);
				}
			}
			return aadlResources;
		} catch (Error e) {
			return Collections.emptyList();
		} finally {
			monitor.done();
		}
	}

	/**
	 * Convert the content of a SysML resource to AADL
	 * @param sysmlResource the resource to convert
	 * @return
	 */
	public List<Resource> convert(Resource sysmlResource) {
		var ns = (Namespace) sysmlResource.getContents().get(0);

		for (var m : ns.getOwnedMember()) {
			if (m instanceof Package p) {
				convertPackage(p);
			}
		}
		return created;
	}

	/**
	 * Convert a SusML package to one or more AADL packages.
	 * AADL packages cannot be nested.
	 * @param sp the package to convert
	 */
	private void convertPackage(Package sp) {
		logger.info("converting package " + sp.getQualifiedName());
		XMLResource res = getPackageResource(sp);
		var ap = Aadl2Factory.eINSTANCE.createAadlPackage();
		res.getContents().add(ap);
		ap.setName(sp.getQualifiedName());
		res.setID(ap, sp.getElementId());

		var pub = ap.createOwnedPublicSection();

		for (var m : sp.getOwnedMember()) {
			if (m instanceof PartDefinition pd) {
				convertPartDefinition(res, pub, pd);
			} else if (m instanceof Package p) {
				convertPackage(p);
			}
		}
	}

	/**
	 * Convert a SysML part definition to an AADL classifier.
	 * @param res the resource containing the generated AADL model
	 * @param section the package section containing the new component classifier
	 * @param pd the part definition to convert
	 */
	private void convertPartDefinition(XMLResource res, PackageSection section, PartDefinition pd) {
		if (pd.specializesFromLibrary("AADL::Component")) {
			logger.info("converting part definition " + pd.getQualifiedName());
			// testCategory(pd);
			var c = createComponentClassifier(pd);
			if (c != null) {
				section.getOwnedClassifiers().add(c);
				res.setID(c, pd.getElementId());
				if (c instanceof ComponentType ctype) {
					addExtension(res, ctype, pd);
					for (var m : pd.getOwnedMember()) {
						if (m instanceof PortUsage p) {
							convertPortUsage(res, ctype, p);
						}
					}
					for (var f : pd.getOwnedFeature()) {
						if (f instanceof AttributeUsage a) {
							convertAttributeUsage(res, ctype, a);
						}
					}
				}
				if (c instanceof ComponentImplementation cimpl) {
					addExtension(res, cimpl, pd);
					addRealization(res, cimpl, pd);
					for (var f : pd.getOwnedFeature()) {
						if (f instanceof PartUsage p) {
							convertPartUsage(res, cimpl, p);
						}
						if (f instanceof ConnectionUsage cu) {
							convertConnectionUsage(res, cimpl, cu);
						}
						if (f instanceof BindingConnectorAsUsage cu) {
							convertBindingConnectorAsUsage(res, cimpl, cu);
						}
						if (f instanceof AttributeUsage a) {
							convertAttributeUsage(res, cimpl, a);
						}
					}
				}
			} else {
				logger.error("cannot create AADL classifier for " + pd.getQualifiedName());
			}
		} else {
			logger.warn("skipping part definition " + pd.getQualifiedName());
		}
	}

	/**
	 * Convert a SysML port usage to an AADL feature.
	 * @param res the resource containing the generated AADL model
	 * @param ctype the component implementation containing the new feature
	 * @param port the port usage to convert
	 */
	private void convertPortUsage(XMLResource res, ComponentType ctype, PortUsage port) {
		port.getPortDefinition().stream().filter(pd -> pd.specializesFromLibrary("AADL::Feature")).findFirst()
				.ifPresent(pd -> {
					var f = createFeature(res, port, pd);
					if (f != null) {
						logger.info("converting port usage " + port.getQualifiedName());
						if (addFeature(ctype, f)) {
							res.setID(f, port.getElementId());
						}
					} else {
						logger.warn("skipping port usage " + port.getQualifiedName());
					}
				});
	}

	/**
	 * Convert a SysML part usage to an AADL subcomponent.
	 * @param res the resource containing the generated AADL model
	 * @param cimpl the component implementation containing the new subcomponent
	 * @param part the part usage to convert
	 */
	private void convertPartUsage(XMLResource res, ComponentImplementation cimpl, PartUsage part) {
		part.getPartDefinition().stream().filter(pd -> pd.specializesFromLibrary("AADL::Component")).findFirst()
				.ifPresent(pd -> {
					var s = createSubcomponent(res, part, pd);
					if (s != null) {
						logger.info("converting part usage " + part.getQualifiedName());
						if (addSubcomponent(cimpl, s)) {
							res.setID(s, part.getElementId());
						}
					} else {
						logger.warn("skipping part usage " + part.getQualifiedName());
					}
				});
	}

	/**
	 * Convert a SysML connection usage to an AADL connection.
	 * @param res the resource containing the generated AADL model
	 * @param cimpl the component implementation containing the new connection
	 * @param conn the connection usage to convert
	 */
	private void convertConnectionUsage(XMLResource res, ComponentImplementation cimpl, ConnectionUsage conn) {
		conn.getConnectionDefinition().stream().filter(cd -> cd.specializesFromLibrary("AADL::Connection")).findFirst()
				.ifPresent(cd -> {
					var c = createConnection(res, conn, (ConnectionDefinition) cd);
					if (c != null) {
						logger.info("converting connection usage " + conn.getQualifiedName());
						if (addConnection(cimpl, c)) {
							res.setID(c, conn.getElementId());
						}
					} else {
						logger.warn("skipping connection usage " + conn.getQualifiedName());
					}
				});
		conn.getConnectionDefinition().stream().filter(cd -> cd.specializesFromLibrary("AADL::ActualBinding"))
				.findFirst().ifPresent(cd -> {
					var name = conn.getQualifiedName();
					if (name == null) {
						name = "<unnamed>";
					}
					var b = createActualBinding(res, conn, (ConnectionDefinition) cd);
					if (b != null) {
						logger.info("converting connection usage " + name);
						if (addPropertyAssociation(cimpl, b)) {
							res.setID(b, conn.getElementId());
						}
					} else {
						logger.warn("skipping connection usage " + name);
					}
				});
	}

	/**
	 * Convert SysML binding connector ar usage to an AADL connection declaration.
	 * This is the first/last segment of an access connection.
	 * @param res the resource containing the generated AADL model
	 * @param cimpl the component implementation containing the new connection
	 * @param cu the binding connector as usage
	 */
	private void convertBindingConnectorAsUsage(XMLResource res, ComponentImplementation cimpl,
			BindingConnectorAsUsage cu) {
		cu.getDefinition().stream().filter(cl -> cl.specializesFromLibrary("AADL::Connection")).findFirst()
				.ifPresent(cl -> {
					var c = createConnection(res, cu, cl);
					if (c != null) {
						logger.info("converting binding connector as usage " + cu.getQualifiedName());
						if (addConnection(cimpl, c)) {
							res.setID(c, cu.getElementId());
						}
					} else {
						logger.warn("skipping binding connector as usage " + cu.getQualifiedName());
					}
				});
	}

	/**
	 * Convert SysML attribute usage to an AADL property association. 
	 * @param res
	 * @param cc
	 * @param au
	 */
	private void convertAttributeUsage(XMLResource res, ComponentClassifier cc, AttributeUsage au) {
		au.getDefinition().stream().filter(cl -> cl.specializesFromLibrary("AADL::Property")).findFirst()
				.ifPresent(cl -> {
					var pa = createPropertyAssociation(res, au);
					if (pa != null) {
						logger.info("converting attribute usage " + au.getQualifiedName());
						if (addPropertyAssociation(cc, pa)) {
							res.setID(pa, au.getElementId());
						}
					} else {
						logger.warn("skipping attribute usage " + au.getQualifiedName());
					}
				});
	}

	/**
	 * Get the resource containing the AADL model generated from a given SysML package.
	 * Creates a new resource if necessary.
	 * @param p the SysML package
	 * @return the resource for the generated AADL model
	 */
	private XMLResource getPackageResource(Package p) {
		var r = packageToResourceMap.get(p);
		if (r == null) {
			var uri = URI.createFileURI(p.getQualifiedName()).appendFileExtension("xml");
			r = (XMLResource) resourceSet.createResource(uri);
			created.add(r);
			packageToResourceMap.put(p, r);
		}
		return r;
	}

//	private void testCategory(PartDefinition pd) {
//		{
//			// results in NPE with current pilot implementation:
//			// KerML QualifiedNameConverter is not set because of a bug
//			var feature = (org.omg.sysml.lang.sysml.Feature) pd.resolve("category").getMemberElement();
//			var expr = FeatureUtil.getValueExpressionFor(feature);
//			var result = ModelLevelExpressionEvaluator.INSTANCE.evaluate((Expression) expr, pd);
//			System.out.println(result);
//		}
//		var optf = pd.getFeature().stream().filter(f -> "category".equals(f.getName())).findFirst();
//		optf.ifPresent(f -> {
//			f.getMember().stream().filter(Expression.class::isInstance).forEach(e -> {
//				var result = ModelLevelExpressionEvaluator.INSTANCE.evaluate((Expression) e, pd);
//				System.out.println(result);
//			});
//		});
//	}

	/**
	 * Add an extension to an AADL component implementation created from the given part definition.
	 * The extended classifier is a component implementation created from a direct super classifier 
	 * of the given part definition.
	 * @param res the resource containing the generated AADL model
	 * @param cimpl the component implementation creted from pd
	 * @param pd the part definition
	 */
	private void addExtension(XMLResource res, ComponentImplementation cimpl, PartDefinition pd) {
		pd.getOwnedSubclassification().stream().map(s -> s.getSuperclassifier())
				.filter(c -> !aadlLibraryElements.containsKey(c.getElementId())
						&& c.specializesFromLibrary("AADL::Component") && !isAADLType(c))
				.findFirst().ifPresent(c -> {
					var e = cimpl.createOwnedExtension();
					var extended = (ComponentImplementation) getReferencedEObject(res,
							Aadl2Package.eINSTANCE.getComponentImplementation(), c);
					e.setExtended(extended);
				});
	}

	/**
	 * Add an extension to an AADL component type created from the given part definition.
	 * The extended classifier is a component type created from a direct super classifier 
	 * of the given part definition.
	 * @param res the resource containing the generated AADL model
	 * @param ctype the component type created from pd
	 * @param pd the part definition
	 */
	private void addExtension(XMLResource res, ComponentType ctype, PartDefinition pd) {
		pd.getOwnedSubclassification().stream().map(s -> s.getSuperclassifier())
				.filter(c -> !aadlLibraryElements.containsKey(c.getElementId())
						&& c.specializesFromLibrary("AADL::Component") && isAADLType(c))
				.findFirst().ifPresent(c -> {
					var e = ctype.createOwnedExtension();
					var extended = (ComponentType) getReferencedEObject(res, Aadl2Package.eINSTANCE.getComponentType(),
							c);
					e.setExtended(extended);
				});
	}

	/**
	 * Add a realization to an AADL component implementation created from the given part definition.
	 * The realized classifier is a component type created from a direct super classifier
	 * of the given part definition.
	 * @param res the resource containing the generated AADL model
	 * @param cimpl the component implementation created from pd
	 * @param pd the part definition.
	 */
	private void addRealization(XMLResource res, ComponentImplementation cimpl, PartDefinition pd) {
		pd.getOwnedSubclassification().stream().map(s -> s.getSuperclassifier())
				.filter(c -> c.specializesFromLibrary("AADL::Component") && isAADLType(c)).findFirst().ifPresent(c -> {
					var r = cimpl.createOwnedRealization();
					var implemented = (ComponentType) getReferencedEObject(res,
							Aadl2Package.eINSTANCE.getComponentType(), c);
					r.setImplemented(implemented);
				});
		if (cimpl.getOwnedRealization() == null) {
			var ext = cimpl.getExtended();
			if (ext != null) {
				var r = cimpl.createOwnedRealization();
				r.setImplemented(ext.getOwnedRealization().getImplemented());
			}
		}
	}

	/**
	 * Check if a SysML type represents an AADL component type.
	 * This is the case when the SysML name does not contain a '.' character.
	 * @param pd the SysML type
	 * @return true iff pd represents an AADL component type
	 */
	private boolean isAADLType(Type pd) {
		return pd.getName().indexOf('.') == -1;
	}

	/** The names of AADL feature categorie. */
	static final String[] aadlFeatureCategories = { "AbstractFeature", "DataPort", "EventDataPort", "EventPort",
			"BusAccess", "VirtualBusAccess", "DataAccess", "SubprogramAccess", "SubprogramGroupAccess", "Parameter",
			"FeatureGroup" };
	/**
	 * The names of AADL component categories.
	 * "Abstract" must be the last entry in this array!
	 */
	static final String[] aadlComponentCategories = { "Bus", "Device", "Data", "Memory", "Process", "Processor",
			"Subprogram", "SubprogramGroup", "System", "Thread", "ThreadGroup", "VirtualBus", "VirtualProcessor",
			"Abstract" };

	/** The names of AADL connection categories. */
	static final String[] aadlConnectionCategories = { "Feature", "Access", "Port", "Parameter", "FeatureGroup" };

	/** The names of AADL binding categories. */
	static final String[] aadlBindingCategories = { "Function", "Connection", "Processor", "Memory" };

	/**
	 * Create an AADL component classifier from a given part definition.
	 * @param pd the part definition
	 * @return the generated AADL component classifier
	 */
	private ComponentClassifier createComponentClassifier(PartDefinition pd) {
		for (var cname : aadlComponentCategories) {
			var lname = "AADL::" + cname;
			if (pd.specializesFromLibrary(lname)) {
				var eClass = (EClass) Aadl2Package.eINSTANCE
						.getEClassifier(cname + (isAADLType(pd) ? "Type" : "Implementation"));
				var s = (ComponentClassifier) Aadl2Factory.eINSTANCE.create(eClass);
				s.setName(pd.getName());
				return s;
			}
		}
		return null;
	}

	/**
	 * Create an AADL feature from a SysML port.
	 * @param res the resource containing the generated AADL model
	 * @param p the port usage
	 * @param pd the port definition for p
	 * @return the generated AADL feature
	 */
	private Feature createFeature(XMLResource res, PortUsage p, PortDefinition pd) {
		for (var cn : aadlFeatureCategories) {
			var vba = cn.equals("VirtualBusAccess");
			var cname = vba ? "BusAccess" : cn;
			var n = "AADL::" + cname;
			if (n.equals(pd.getQualifiedName()) || pd.specializesFromLibrary(n)) {
				var eClass = (EClass) Aadl2Package.eINSTANCE.getEClassifier(cname);
				var f = (Feature) Aadl2Factory.eINSTANCE.create(eClass);
				f.setName(p.getName());
				if (vba) {
					((BusAccess) f).setVirtual(true);
				}
				if (!setDirection(f, p.getDirection())) {
					logger.error("inout port " + p.getQualifiedName() + "cannot be translated to access feature");
					return null;
				}
				p.getUsage().stream().filter(ItemUsage.class::isInstance).map(ItemUsage.class::cast).findFirst()
						.ifPresent(iu -> {
							var type = iu.getType().get(0);
							if (type.libraryNamespace() != null && "AADL".equals(type.libraryNamespace().getName())) {
								return;
							}

							logger.debug("setting classifier for feature " + p.getQualifiedName());

							var fType = getReferencedEObject(res, Aadl2Package.eINSTANCE.getComponentClassifier(),
									type);
							String sfname = null;
							if (List.of("AbstractFeature").contains(cname)) {
								sfname = "abstractFeatureClassifier";
							} else if (List.of("Parameter", "DataPort", "EventDataPort", "DataAccess")
									.contains(cname)) {
								sfname = "dataFeatureClassifier";
							} else if (List.of("BusAccess").contains(cname)) {
								sfname = "busFeatureClassifier";
							} else if (List.of("SubprogramAccess").contains(cname)) {
								sfname = "subprogramFeatureClassifier";
							} else if (List.of("SubprogramGroupAccess").contains(cname)) {
								sfname = "subprogramGroupFeatureClassifier";
							}
							if (sfname != null) {
								var sf = f.eClass().getEStructuralFeature(sfname);
								f.eSet(sf, fType);
							}
						});
				return f;
			}
		}
		return null;
	}

	/**
	 * Set the direction of an AADL feature based on a SysML feature direction kind.
	 * @param f the AADL feature
	 * @param dk The feature direction kind
	 * @return true iff the direction is valid for the AADL feature
	 */
	private boolean setDirection(Feature f, FeatureDirectionKind dk) {
		if (f instanceof AbstractFeature af) {
			if (!dk.equals(FeatureDirectionKind.INOUT)) {
				if (dk.equals(FeatureDirectionKind.IN)) {
					af.setIn(true);
				}
				if (dk.equals(FeatureDirectionKind.OUT)) {
					af.setOut(true);
				}
			}
		} else if (f instanceof DirectedFeature df) {
			if (dk.equals(FeatureDirectionKind.IN) || dk.equals(FeatureDirectionKind.INOUT)) {
				df.setIn(true);
			}
			if (dk.equals(FeatureDirectionKind.OUT) || dk.equals(FeatureDirectionKind.INOUT)) {
				df.setOut(true);
			}
		} else if (f instanceof Access a) {
			if (dk.equals(FeatureDirectionKind.INOUT)) {
				return false;
			}
			if (dk.equals(FeatureDirectionKind.IN)) {
				a.setKind(AccessType.REQUIRES);
			}
			if (dk.equals(FeatureDirectionKind.OUT)) {
				a.setKind(AccessType.PROVIDES);
			}
		}
		return true;
	}

	/**
	 * Create an AADL subcomponent from a part.
	 * @param res the resource containing the generated AADL model
	 * @param p the part usage
	 * @param pd the part definition of p
	 * @return the generated AADL subcomponent
	 */
	private Subcomponent createSubcomponent(XMLResource res, PartUsage p, PartDefinition pd) {
		for (var cname : aadlComponentCategories) {
			var n = "AADL::" + cname;
			var setClassifier = pd.specializesFromLibrary(n);
			if (n.equals(pd.getQualifiedName()) || setClassifier) {
				var eClass = (EClass) Aadl2Package.eINSTANCE.getEClassifier(cname + "Subcomponent");
				var s = (Subcomponent) Aadl2Factory.eINSTANCE.create(eClass);
				s.setName(p.getName());
				if (setClassifier) {
					var subType = getReferencedEObject(res, Aadl2Package.eINSTANCE.getComponentClassifier(), pd);
					var fname = cname.toLowerCase() + "SubcomponentType";
					var f = s.eClass().getEStructuralFeature(fname);
					s.eSet(f, subType);
				}
				var redefs = p.getOwnedRedefinition();
				if (redefs.size() == 1) {
					var redef = redefs.get(0).getRedefinedFeature();
					if (p.getDeclaredName() == null || p.getDeclaredName() == redef.getName()) {
						s.setRefined((Subcomponent) getReferencedEObject(res, Aadl2Package.eINSTANCE.getSubcomponent(),
								redef));
					} else {
						logger.warn("ignoring redefinition with renaming for part usage " + p.getQualifiedName());
					}
				} else {
					logger.warn("ignoring multiple redefinitions for part usage " + p.getQualifiedName());
				}
				return s;
			}
		}
		return null;
	}

	/**
	 * Create an AADL connection from a SysML connection usage and connection definition.
	 * @param res the resource containing the generated AADL model
	 * @param cu the connection usage
	 * @param cd the connection definition
	 * @return the generated AADL connection, or null in case of an error
	 */
	private Connection createConnection(XMLResource res, ConnectionUsage cu, ConnectionDefinition cd) {
		for (var cn : aadlConnectionCategories) {
			var cname = cn + "Connection";
			var n = "AADL::" + cname;
			if (n.equals(cd.getQualifiedName()) || cd.specializesFromLibrary(n)) {
				var eClass = (EClass) Aadl2Package.eINSTANCE.getEClassifier(cname);
				var c = (Connection) Aadl2Factory.eINSTANCE.create(eClass);
				c.setName(cu.getName());
				var success = fillConnectedElement(c.createSource(), res, cu, cu.getSourceFeature(), "source");
				var tf = cu.getTargetFeature();
				if (tf.size() > 1) {
					logger.error(
							"connection usage " + cu.getQualifiedName() + " does not have exactly one target feature");
					return null;
				}
				success &= fillConnectedElement(c.createDestination(), res, cu, cu.getTargetFeature().get(0), "target");
				return success ? c : null;
			}
		}
		return null;
	}

	/**
	 * Create an AADL connection from a SysML binding connector as usage and classifier.
	 * @param res the resource containing the generated AADL model
	 * @param cu the binding connector as usage
	 * @param cd the classifier
	 * @return the generated AADL connection, or null in case of an error
	 */
	private Connection createConnection(XMLResource res, BindingConnectorAsUsage cu, Classifier cd) {
		for (var cn : aadlConnectionCategories) {
			var cname = cn + "Connection";
			var n = "AADL::" + cname;
			if (n.equals(cd.getQualifiedName()) || cd.specializesFromLibrary(n)) {
				var eClass = (EClass) Aadl2Package.eINSTANCE.getEClassifier(cname);
				var c = (Connection) Aadl2Factory.eINSTANCE.create(eClass);
				c.setName(cu.getName());
				var success = fillConnectedElement(c.createSource(), res, cu, cu.getSourceFeature(), "source");
				var tf = cu.getTargetFeature();
				if (tf.size() > 1) {
					logger.error("binding connection as usage " + cu.getQualifiedName()
							+ " does not have exactly one target feature");
					return null;
				}
				success &= fillConnectedElement(c.createDestination(), res, cu, cu.getTargetFeature().get(0), "target");
				return success ? c : null;
			}
		}
		return null;
	}

	/**
	 * Create an AADL binding property from a SysML connection usage.
	 * @param res the resource containing the generated AADL model
	 * @param cu the connection usage
	 * @param cd the connection definition
	 * @return the generated property association, or null in case of an error
	 */
	private PropertyAssociation createActualBinding(XMLResource res, ConnectionUsage cu, ConnectionDefinition cd) {
		for (var bn : aadlBindingCategories) {
			var bname = "Actual" + bn + "Binding";
			var n = "AADL::" + bname;
			if (n.equals(cd.getQualifiedName()) || cd.specializesFromLibrary(n)) {
				var pa = (PropertyAssociation) Aadl2Factory.eINSTANCE.createPropertyAssociation();
				var pn = "Deployment_Properties::Actual_" + bn + "_Binding";
				Property prop = findProperty(pn);
				pa.setProperty(prop);
				var success = fillContainedNamedElement(pa.createAppliesTo(), res, cu, cu.getSourceFeature(), "source");

				var value = pa.createOwnedValue();
				var list = (ListValue) value.createOwnedValue(Aadl2Package.eINSTANCE.getListValue());

				int i = 0;
				for (var tf : cu.getTargetFeature()) {
					var rv = Aadl2Factory.eINSTANCE.createReferenceValue();
					success &= fillContainedNamedElement(rv, res, cu, tf, "target[" + i++ + "]");
					list.getOwnedListElements().add(rv);
				}
				return success ? pa : null;
			}
		}
		return null;
	}

	/**
	 * Create a property association for property with the same name as a given attribute usage.
	 * Only 'Period' and 'Compute_Execution_Time'
	 * @param res the resource for the generated AADL model
	 * @param au the attribute usage
	 * @return a completely filled property association or null
	 */
	private PropertyAssociation createPropertyAssociation(XMLResource res, AttributeUsage au) {
		var pn = au.getName();
		var pa = (PropertyAssociation) Aadl2Factory.eINSTANCE.createPropertyAssociation();
		Property prop = findProperty(pn);
		if (pn == null) {
			logger.error("missing property definition for " + pn);
			return null;
		}
		pa.setProperty(prop);

		var value = pa.createOwnedValue();
		var success = true;
		if (pn.equals("Period")) {
			var intValue = (IntegerLiteral) value.createOwnedValue(Aadl2Package.eINSTANCE.getIntegerLiteral());
			success &= fillIntegerLiteral(au, prop.getQualifiedName(), prop.getPropertyType(), intValue);
		} else if (pn.equals("Compute_Execution_Time")) {
			var rangeValue = (RangeValue) value.createOwnedValue(Aadl2Package.eINSTANCE.getRangeValue());
			success &= fillRangeValue(au, prop, rangeValue);
		}

		return success ? pa : null;
	}

	/**
	 * Fill an AADL integer literal from a SysML feature
	 * @param f the SysML feature
	 * @param propName the name of the property
	 * @param pt the type of the property
	 * @param intValue the integer literal
	 * @return if the value could be filled successfully
	 */
	private boolean fillIntegerLiteral(org.omg.sysml.lang.sysml.Feature f, String propName, PropertyType pt, IntegerLiteral intValue) {
		boolean success = false;
		try {
			var v = EvaluationUtil.evaluate(FeatureUtil.getValueExpressionFor(f), f).get(0);
			var neg = false;
			var ival = 0;
			var unit = "";
			if (v instanceof OperatorExpression oe) {
				if ("-".equals(oe.getOperator())) {
					neg = true;
					oe = (OperatorExpression) oe.getOperand().get(0);
				}
				if ("[".equals(oe.getOperator())) {
					ival = ((LiteralInteger) oe.getOperand().get(0)).getValue();
					unit = ((FeatureReferenceExpression) oe.getOperand().get(1)).getOwnedMembership().get(0)
							.getMemberElement().getShortName();
				}
			}

			intValue.setValue(neg ? -ival : ival);
			var unitLiteral = findUnit(pt, unit);
			if (unitLiteral != null) {
				intValue.setUnit(unitLiteral);
				success = true;
			} else {
				logger.error("invalid unit " + unit + " for property type " + pt.getQualifiedName());
			}
		} catch (Exception e) {
			logger.error("could not extract value for " + propName);
		}
		return success;
	}

	/**
	 * Fill an AADL range value from a SysML attribute usage
	 * @param au the attribute usage
	 * @param prop the property
	 * @param rangeValue the empty range value
	 * @return if the value could be filled successfully
	 */
	private boolean fillRangeValue(AttributeUsage au, Property prop, RangeValue rangeValue) {
		boolean success = true;
		try {
			for (var f : au.getFeature()) {
				switch (f.getName()) {
				case "minimum": {
					var intVal = (IntegerLiteral) rangeValue.createMinimum(Aadl2Package.eINSTANCE.getIntegerLiteral());
					var pt = ((RangeType) prop.getPropertyType()).getNumberType();
					success &= fillIntegerLiteral(f, prop.getName() + ".minimum", pt , intVal);
					break;
				}
				case "maximum": {
					var intVal = (IntegerLiteral) rangeValue.createMaximum(Aadl2Package.eINSTANCE.getIntegerLiteral());
					var pt = ((RangeType) prop.getPropertyType()).getNumberType();
					success &= fillIntegerLiteral(f, prop.getName() + ".maximum", pt , intVal);
					break;
				}
				case "delta": {
					var intVal = (IntegerLiteral) rangeValue.createDelta(Aadl2Package.eINSTANCE.getIntegerLiteral());
					var pt = ((RangeType) prop.getPropertyType()).getNumberType();
					success &= fillIntegerLiteral(f, prop.getName() + ".delta", pt , intVal);
					break;
				}
				default:
					// ignore
				}
			}
		} catch (Exception e) {
			logger.error("could not extract value for " + prop.getQualifiedName());
		}
		return success;
	}

	/** Names of predeclared AADL property sets */
	static final String[] predeclaredPS = { "Communication_Properties", "Deployment_Properties", "Memory_Properties",
			"Modeling_Properties", "Programming_Properties", "Thread_Properties", "Timing_Properties" };

	/**
	 * Find an AADL property definition.
	 * @param name the name of the property
	 * @return the property definition for the given name, or null
	 */
	private Property findProperty(String name) {
		Property result = null;
		if (name.contains("::")) {
			result = Aadl2GlobalScopeUtil.get(propertyLookupContext, Aadl2Package.eINSTANCE.getElement(), name);
		} else {
			for (var ps : predeclaredPS) {
				result = Aadl2GlobalScopeUtil.get(propertyLookupContext, Aadl2Package.eINSTANCE.getElement(),
						ps + "::" + name);
				if (result != null) {
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Find an AADL unit literal by name in a given units type.
	 * @param pt the units type
	 * @param name the name of the unit
	 * @return the uni literal, or null
	 */
	private UnitLiteral findUnit(PropertyType pt, String name) {
		// some time units have different names in SysML and AADL
		if ("Time".equalsIgnoreCase(pt.getName())) {
			if (name.equals("s")) {
				name = "sec";
			} else if (name.equals("h")) {
				name = "hr";
			}
		}
		if (pt instanceof NumberType nt) {
			pt = nt.getUnitsType();
			if (pt instanceof UnitsType ut) {
				for (var ul : ut.getOwnedLiterals()) {
					if (name.equals(ul.getName())) {
						return (UnitLiteral) ul;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Fill the connected element of an AADL connection from a SysML connection usage.
	 * @param ce the connection end
	 * @param res the resource containing the generated AADL model
	 * @param cu the connection usage
	 * @param f the SysML feature
	 * @param endName the name of the connection usage end: "source" or "target" 
	 * @return true iff the operation was successful
	 */
	private boolean fillConnectedElement(ConnectedElement ce, XMLResource res, ConnectionUsage cu,
			org.omg.sysml.lang.sysml.Feature f, String endName) {
		if (f instanceof PortUsage pu) {
			var end = (ConnectionEnd) getReferencedEObject(res, Aadl2Package.eINSTANCE.getConnectionEnd(), pu);
			ce.setConnectionEnd(end);
		} else {
			var fcs = f.getOwnedFeatureChaining();
			if (fcs.size() != 2) {
				logger.error("there are not exactly two feature chainings in " + endName + " end of connection usage "
						+ cu.getQualifiedName());
				return false;
			}
			var ctx = (Context) getReferencedEObject(res, Aadl2Package.eINSTANCE.getConnectionEnd(),
					fcs.get(0).getChainingFeature());
			var end = (ConnectionEnd) getReferencedEObject(res, Aadl2Package.eINSTANCE.getConnectionEnd(),
					fcs.get(1).getChainingFeature());
			ce.setContext(ctx);
			ce.setConnectionEnd(end);
		}
		return true;
	}

	/**
	 * Fill the connected element of an AADL connection from a SysML binding connector as usage.
	 * @param ce the connection end
	 * @param res the resource containing the generated AADL model
	 * @param cu the binding connector as usage
	 * @param f the SysML feature
	 * @param endName the name of the connection usage end: "source" or "target" 
	 * @return true iff the operation was successful
	 */
	private boolean fillConnectedElement(ConnectedElement ce, XMLResource res, BindingConnectorAsUsage cu,
			org.omg.sysml.lang.sysml.Feature f, String endName) {
		if (f instanceof PartUsage pu) {
			var end = (ConnectionEnd) getReferencedEObject(res, Aadl2Package.eINSTANCE.getConnectionEnd(), pu);
			ce.setConnectionEnd(end);
		} else {
			var fcs = f.getOwnedFeatureChaining();
			if (!(fcs.get(fcs.size() - 1).getChainingFeature() instanceof ItemUsage)) {
				logger.error("last feature is not an item usage in " + endName + " end of binding connector as usage "
						+ cu.getQualifiedName());
				return false;
			}
			ConnectionEnd end;
			Context ctx = null;
			if (fcs.size() == 2) {
				end = (ConnectionEnd) getReferencedEObject(res, Aadl2Package.eINSTANCE.getConnectionEnd(),
						fcs.get(0).getChainingFeature());
			} else if (fcs.size() == 3) {
				ctx = (Context) getReferencedEObject(res, Aadl2Package.eINSTANCE.getContext(),
						fcs.get(0).getChainingFeature());
				end = (ConnectionEnd) getReferencedEObject(res, Aadl2Package.eINSTANCE.getConnectionEnd(),
						fcs.get(1).getChainingFeature());
			} else {
				logger.error("there are not exactly two or three feature chainings in " + endName
						+ " end of binding connector as usage " + cu.getQualifiedName());
				return false;
			}
			ce.setContext(ctx);
			ce.setConnectionEnd(end);
		}
		return true;
	}

	/**
	 * Fill the contained named element of an AADL connection end from a SysML connector as usage.
	 * @param cne the contained named element
	 * @param res the resource containing the generated AADL model
	 * @param cu the connector as usage
	 * @param f the SysML feature
	 * @param endName the name of the connection usage end: "source" or "target" 
	 * @return true iff the operation was successful
	 */
	private boolean fillContainedNamedElement(ContainedNamedElement cne, XMLResource res, ConnectorAsUsage cu,
			org.omg.sysml.lang.sysml.Feature f, String endName) {
		if (f instanceof PartUsage pu) {
			var ne = (NamedElement) getReferencedEObject(res, Aadl2Package.eINSTANCE.getNamedElement(), pu);
			cne.createPath().setNamedElement(ne);
		} else {
			var fcs = f.getOwnedFeatureChaining();
			if (!(fcs.size() > 0)) {
				logger.error("there are no feature chainings in " + endName);
				return false;
			}
			ContainmentPathElement path = null;
			for (var fc : fcs) {
				path = (path == null) ? cne.createPath() : path.createPath();
				var cf = fc.getChainingFeature();
				var ne = (NamedElement) getReferencedEObject(res, Aadl2Package.eINSTANCE.getNamedElement(), cf);
				path.setNamedElement(ne);
			}
		}
		return true;
	}

	/**
	 * Add a feature to a component type.
	 * @param ctype the component type
	 * @param f the feature to add
	 * @return true iff the operation was successful
	 */
	private boolean addFeature(ComponentType ctype, Feature f) {
		var fname = "owned" + f.eClass().getName();
		var sf = ctype.eClass().getEStructuralFeature(fname);
		if (sf != null) {
			@SuppressWarnings("unchecked")
			var value = (List<EObject>) ctype.eGet(sf);
			value.add(f);
			return true;
		} else {
			logger.error("feature " + f.getName() + " not allowed in " + ctype.getQualifiedName());
			return false;
		}
	}

	/**
	 * Add a subcomponent to a component implementation.
	 * @param cimpl the component implementation
	 * @param s the subcomponent to add
	 * @return true iff the operation was successful
	 */
	private boolean addSubcomponent(ComponentImplementation cimpl, Subcomponent s) {
		var fname = "owned" + s.eClass().getName();
		var sf = cimpl.eClass().getEStructuralFeature(fname);
		if (sf != null) {
			@SuppressWarnings("unchecked")
			var value = (List<EObject>) cimpl.eGet(sf);
			value.add(s);
			return true;
		} else {
			logger.error("subcomponent " + s.getName() + " not allowed in " + cimpl.getQualifiedName());
			return false;
		}
	}

	/**
	 * Add a connection declaration to an AADL component implementation.
	 * @param cimpl the component implementation
	 * @param c the connection to add
	 * @return true iff the operation was successful
	 */
	private boolean addConnection(ComponentImplementation cimpl, Connection c) {
		var fname = "owned" + c.eClass().getName();
		var sf = cimpl.eClass().getEStructuralFeature(fname);
		if (sf != null) {
			@SuppressWarnings("unchecked")
			var value = (List<EObject>) cimpl.eGet(sf);
			value.add(c);
			return true;
		} else {
			logger.error("could not create connection " + c.getName() + " in " + cimpl.getQualifiedName());
			return false;
		}
	}

	/**
	 * Add a property association to an AADL element.
	 * @param ne the AADL element
	 * @param pa the property association to add
	 * @return true iff the operation was successful
	 */
	private boolean addPropertyAssociation(NamedElement ne, PropertyAssociation pa) {
		var fname = "ownedPropertyAssociation";
		var sf = ne.eClass().getEStructuralFeature(fname);
		if (sf != null) {
			@SuppressWarnings("unchecked")
			var value = (List<EObject>) ne.eGet(sf);
			value.add(pa);
			return true;
		} else {
			logger.error("could not create property association for " + pa.getProperty().getName() + " in "
					+ ne.getQualifiedName());
			return false;
		}
	}

	/**
	 * Find an element with the same ID as a given element in a resource.
	 * If there is no such element, create a proxy for it.
	 * @param res the resource
	 * @param eClass eClass for the proxy
	 * @param to element to look up
	 * @return
	 */
	private EObject getReferencedEObject(XMLResource res, EClass eClass, Element to) {
		var id = to.getElementId();
		var eo = res.getEObject(id);

		if (eo == null) {
			var pkg = EcoreUtil2.getContainerOfType(to, Package.class);
			if (pkg != null) {
				var toRes = getPackageResource(pkg);
				eo = ProxyUtil.createProxy(toRes, eClass, id);
				if (eo instanceof NamedElement ne) {
					ne.setName(to.getName());
				}
				res.getContents().add(eo);
				if (toRes != res) {
					usedResources.putIfAbsent(res, new LinkedHashSet<>());
					usedResources.get(res).add(toRes);
				}
			} else {
				logger.fatal("referenced element " + to.getQualifiedName() + " not in a package");
				throw new Error("SysML model cannot be converted to AADL");
			}
		}
		return eo;
	}

}
