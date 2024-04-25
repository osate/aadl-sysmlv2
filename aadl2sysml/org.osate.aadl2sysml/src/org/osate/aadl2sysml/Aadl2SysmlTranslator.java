/*
 * AADL translator to SysMLV2
 *
 * Copyright 2024 Carnegie Mellon University.
 *
 * NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE ENGINEERING INSTITUTE MATERIAL IS FURNISHED ON AN "AS-IS"
 * BASIS. CARNEGIE MELLON UNIVERSITY MAKES NO WARRANTIES OF ANY KIND, EITHER EXPRESSED OR IMPLIED, AS TO ANY MATTER
 * INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED
 * FROM USE OF THE MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT MAKE ANY WARRANTY OF ANY KIND WITH RESPECT TO FREEDOM
 * FROM PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 *
 * Licensed under an Eclipse Public License - v 2.0-style license, please see license.txt or contact
 * permission@sei.cmu.edu for full terms.
 *
 * [DISTRIBUTION STATEMENT A] This material has been approved for public release and unlimited distribution.  Please see
 * Copyright notice for non-US Government use and distribution.
 *
 * DM24-0312
 */
package org.osate.aadl2sysml;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.osate.aadl2.Aadl2Package;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.AbstractFeature;
import org.osate.aadl2.Access;
import org.osate.aadl2.AccessConnection;
import org.osate.aadl2.BusAccess;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ComponentCategory;
import org.osate.aadl2.ComponentClassifier;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.ComponentPrototype;
import org.osate.aadl2.ComponentType;
import org.osate.aadl2.ConnectedElement;
import org.osate.aadl2.Connection;
import org.osate.aadl2.ContainedNamedElement;
import org.osate.aadl2.ContainmentPathElement;
import org.osate.aadl2.DataAccess;
import org.osate.aadl2.DataPort;
import org.osate.aadl2.DirectedFeature;
import org.osate.aadl2.Element;
import org.osate.aadl2.EventDataPort;
import org.osate.aadl2.EventPort;
import org.osate.aadl2.Feature;
import org.osate.aadl2.FeatureConnection;
import org.osate.aadl2.FeatureGroup;
import org.osate.aadl2.FeatureGroupConnection;
import org.osate.aadl2.FeatureGroupType;
import org.osate.aadl2.IntegerLiteral;
import org.osate.aadl2.InternalFeature;
import org.osate.aadl2.ListValue;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.Parameter;
import org.osate.aadl2.ParameterConnection;
import org.osate.aadl2.PortConnection;
import org.osate.aadl2.ProcessorFeature;
import org.osate.aadl2.PropertyAssociation;
import org.osate.aadl2.Prototype;
import org.osate.aadl2.RangeValue;
import org.osate.aadl2.ReferenceValue;
import org.osate.aadl2.Subcomponent;
import org.osate.aadl2.SubprogramAccess;
import org.osate.aadl2.SubprogramCall;
import org.osate.aadl2.SubprogramGroupAccess;
import org.osate.aadl2.contrib.aadlproject.TimeUnits;
import org.osate.aadl2.contrib.deployment.DeploymentProperties;
import org.osate.aadl2.contrib.timing.TimingProperties;
import org.osate.aadl2.modelsupport.scoping.Aadl2GlobalScopeUtil;
import org.osate.pluginsupport.properties.IntegerWithUnits;
import org.stringtemplate.v4.ST;

public final class Aadl2SysmlTranslator {
	private Aadl2SysmlTranslator() {
	}

	public static String translateToSysML(AadlPackage aadlPackage) {
		var parts = new ArrayList<String>();
		var publicSection = aadlPackage.getOwnedPublicSection();
		if (publicSection != null) {
			publicSection.getOwnedClassifiers()
					.stream()
					.map(Aadl2SysmlTranslator::translateClassifier)
					.forEachOrdered(parts::add);
		}

		ST template;
		if (parts.isEmpty()) {
			template = new ST("package %name%;", '%', '%');
		} else {
			template = new ST("""
					package %name% {
						%parts%
					}""", '%', '%');
			template.add("parts", joinMembers(parts));
		}
		template.add("name", translateName(aadlPackage));
		return template.render();
	}

	/*
	 * TODO Finish classifiers.
	 *
	 * The following classifiers are not translated:
	 * - feature groups types
	 *
	 * The following are not currently translated for classifiers:
	 * - An abstract type or implementation being extended into a specific category.
	 */
	private static String translateClassifier(Classifier classifier) {
		var members = new ArrayList<String>();
		if (classifier instanceof ComponentType type) {
			type.getOwnedFeatures().stream().map(Aadl2SysmlTranslator::translateFeature).forEachOrdered(members::add);
		} else if (classifier instanceof ComponentImplementation impl) {
			impl.getOwnedSubcomponents()
					.stream()
					.map(Aadl2SysmlTranslator::translateSubcomponent)
					.forEachOrdered(members::add);
			impl.getOwnedConnections()
					.stream()
					.map(Aadl2SysmlTranslator::translateConnection)
					.forEachOrdered(members::add);
		} else if (classifier instanceof FeatureGroupType) {
			return "// WARNING: '" + classifier.getName() + "' not translated.";
		}
		for (var association : classifier.getOwnedPropertyAssociations()) {
			var property = association.getProperty();
			if (property == DeploymentProperties.getActualProcessorBinding_Property(association)
					|| property == DeploymentProperties.getActualMemoryBinding_Property(association)
					|| property == DeploymentProperties.getActualConnectionBinding_Property(association)
					|| property == DeploymentProperties.getActualFunctionBinding_Property(association)) {
				members.addAll(translateBindingProperty(association));
			} else if (property == TimingProperties.getPeriod_Property(association)) {
				members.add(translatePeriod(association));
			} else if (property == TimingProperties.getComputeExecutionTime_Property(association)) {
				members.add(translateComputeExecutionTime(association));
			}
		}

		ST template;
		if (members.isEmpty()) {
			template = new ST("part def %name% :> %subclassification%;", '%', '%');
		} else {
			template = new ST("""
					part def %name% :> %subclassification% {
						%members%
					}""", '%', '%');
			template.add("members", joinMembers(members));
		}
		template.add("name", translateName(classifier));
		template.add("subclassification", translateSubclassification(classifier));
		return template.render();
	}

	private static String translateSubclassification(Classifier classifier) {
		if (classifier instanceof ComponentType type) {
			var extended = type.getExtended();
			if (extended == null) {
				return translateCategory(type.getCategory());
			} else {
				return translateReference(type, extended, false);
			}
		} else if (classifier instanceof ComponentImplementation impl) {
			var type = impl.getType();
			var extended = impl.getExtended();
			if (extended == null) {
				return translateReference(impl, impl.getType(), false);
			} else if (type == extended.getType()) {
				return translateReference(impl, extended, false);
			} else {
				return translateReference(impl, type, false) + ", " + translateReference(impl, extended, false);
			}
		} else {
			throw new AssertionError("Unexpected class: " + classifier.getClass());
		}
	}

	/*
	 * TODO Finish features.
	 *
	 * The following features are not translated:
	 * - feature groups
	 * - parameters
	 *
	 * The following are not currently translated for features:
	 * - Feature refinement
	 * - References to Prototypes
	 * - Arrays
	 * - Properties
	 * - An abstract feature being refined into a specific feature.
	 */
	private static String translateFeature(Feature feature) {
		if (invalidFeature(feature) || feature.getRefined() != null) {
			return "// WARNING: '" + feature.getName() + "' not translated.";
		}
		ST template;
		var featureClassifier = feature.getFeatureClassifier();
		if (featureClassifier instanceof ComponentClassifier classifier) {
			template = new ST("""
					%direction% port %name% : AADL::%kind% {
						%direction% %member% :>> type : %type%;
					}""", '%', '%');
			template.add("type", translateReference(feature, classifier, classifier instanceof ComponentType));
			if (feature instanceof DirectedFeature) {
				template.add("member", "item");
			} else if (feature instanceof Access) {
				template.add("member", "ref");
			} else {
				throw new AssertionError("Unexpected class: " + feature.getClass());
			}
		} else {
			template = new ST("""
					%prototypeWarning%
					%direction% port %name% : AADL::%kind%;""", '%', '%');
			if (featureClassifier instanceof ComponentPrototype prototype) {
				template.add("prototypeWarning",
						"// WARNING: Reference to prototype '" + prototype.getName() + "' not translated.");
			} else if (feature instanceof AbstractFeature abstractFeature
					&& abstractFeature.getFeaturePrototype() != null) {
				template.add("prototypeWarning", "// WARNING: Reference to prototype '"
						+ abstractFeature.getFeaturePrototype().getName() + "' not translated.");
			} else {
				template.add("prototypeWarning", "");
			}
		}
		if (feature instanceof DirectedFeature directedFeature) {
			template.add("direction", switch (directedFeature.getDirection()) {
				case IN_OUT -> "inout";
				default -> directedFeature.getDirection();
			});
		} else if (feature instanceof Access access) {
			template.add("direction", switch (access.getKind()) {
				case PROVIDES -> "out";
				case REQUIRES -> "in";
			});
		} else {
			throw new AssertionError("Unexpected class: " + feature.getClass());
		}
		template.add("name", feature.getName());
		if (feature instanceof AbstractFeature) {
			template.add("kind", "AbstractFeature");
		} else if (feature instanceof DataPort) {
			template.add("kind", "DataPort");
		} else if (feature instanceof EventDataPort) {
			template.add("kind", "EventDataPort");
		} else if (feature instanceof EventPort) {
			template.add("kind", "EventPort");
		} else if (feature instanceof DataAccess) {
			template.add("kind", "DataAccess");
		} else if (feature instanceof BusAccess busAccess) {
			template.add("kind", busAccess.isVirtual() ? "VirtualBusAccess" : "BusAccess");
		} else if (feature instanceof SubprogramAccess) {
			template.add("kind", "SubprogramAccess");
		} else if (feature instanceof SubprogramGroupAccess) {
			template.add("kind", "SubprogramGroupAccess");
		} else {
			throw new AssertionError("Unexpected class: " + feature.getClass());
		}
		return template.render();
	}

	private static boolean invalidFeature(Feature feature) {
		return feature instanceof FeatureGroup || feature instanceof Parameter;
	}

	/*
	 * TODO Finish subcomponents.
	 *
	 * The following are not currently translated for subcomponents:
	 * - References to Prototypes
	 * - Prototype bindings
	 * - Arrays
	 * - Implementation reference list
	 * - Properties
	 * - Modes
	 * - An abstract type or implementation being listed as the classifier for a subcomponent with a specific category.
	 * - An abstract subcomponent being refined into a subcomponent with a specific category.
	 */
	private static String translateSubcomponent(Subcomponent subcomponent) {
		ST template;
		if (subcomponent.getRefined() == null) {
			template = new ST("""
					%prototypeWarning%
					part %name% : %partType%;""", '%', '%');
		} else {
			template = new ST("""
					%prototypeWarning%
					part : %partType% :>> %name%;""", '%', '%');
		}
		template.add("name", subcomponent.getName());
		var subcomponentType = subcomponent.getSubcomponentType();
		if (subcomponentType instanceof ComponentClassifier classifier) {
			template.add("prototypeWarning", "");
			template.add("partType", translateReference(subcomponent, classifier, classifier instanceof ComponentType));
		} else if (subcomponentType instanceof Prototype prototype) {
			template.add("prototypeWarning",
					"// WARNING: Reference to prototype '" + prototype.getName() + "' not translated.");
			template.add("partType", translateCategory(subcomponent.getCategory()));
		} else {
			template.add("prototypeWarning", "");
			template.add("partType", translateCategory(subcomponent.getCategory()));
		}
		return template.render();
	}

	/*
	 * TODO Finish connections.
	 *
	 * The following connections are not translated:
	 * - feature group connections
	 * - parameter connections
	 * - connections that refer to an untranslated connection end
	 *
	 * The following are not currently translated for connections:
	 * - Directionality (unidirectional and bidirectional connections are translated the same way)
	 * - Connection refinement
	 * - Properties
	 * - Modes
	 */
	private static String translateConnection(Connection connection) {
		if (invalidConnection(connection) || connection.getRefined() != null) {
			return "// WARNING: '" + connection.getName() + "' not translated.";
		}

		var template = new ST(
				"%usageKeyword% %name% : AADL::%connType% %connectorKeyword% %source% %connectorOperator% %destination%;",
				'%', '%');
		template.add("name", connection.getName());
		if (connection instanceof FeatureConnection) {
			template.add("connType", "FeatureConnection");
		} else if (connection instanceof PortConnection) {
			template.add("connType", "PortConnection");
		} else if (connection instanceof AccessConnection) {
			template.add("connType", "AccessConnection");
		} else {
			throw new AssertionError("Unexpected class: " + connection.getClass());
		}

		var lastSource = connection.getSource().getLastConnectionEnd();
		var lastDestination = connection.getDestination().getLastConnectionEnd();
		var sourceIsTypedFeature = lastSource instanceof Feature && !(lastSource instanceof EventPort);
		var destinationIsTypedFeature = lastDestination instanceof Feature && !(lastDestination instanceof EventPort);
		var source = getConnectionChain(connection.getSource()).stream()
				.map(NamedElement::getName)
				.collect(Collectors.joining("."));
		var destination = getConnectionChain(connection.getDestination()).stream()
				.map(NamedElement::getName)
				.collect(Collectors.joining("."));

		if (lastSource instanceof Subcomponent && destinationIsTypedFeature
				|| sourceIsTypedFeature && lastDestination instanceof Subcomponent) {
			template.add("usageKeyword", "binding");
			template.add("connectorKeyword", "bind");
			if (sourceIsTypedFeature) {
				source += ".type";
			}
			template.add("source", source);
			template.add("connectorOperator", "=");
			if (destinationIsTypedFeature) {
				destination += ".type";
			}
			template.add("destination", destination);
		} else {
			template.add("usageKeyword", "connection");
			template.add("connectorKeyword", "connect");
			template.add("source", source);
			template.add("connectorOperator", "to");
			template.add("destination", destination);
		}
		return template.render();
	}

	private static boolean invalidConnection(Connection connection) {
		var sourceChain = getConnectionChain(connection.getSource());
		var destinationChain = getConnectionChain(connection.getDestination());
		var invalidType = connection instanceof FeatureGroupConnection || connection instanceof ParameterConnection;
		var invalidEnd = Stream.concat(sourceChain.stream(), destinationChain.stream())
				.anyMatch(element -> element instanceof Feature feature && invalidFeature(feature)
						|| element instanceof ProcessorFeature || element instanceof InternalFeature
						|| element instanceof SubprogramCall);
		return invalidType || invalidEnd;
	}

	private static List<NamedElement> getConnectionChain(ConnectedElement connectedElement) {
		var chain = new ArrayList<NamedElement>();
		var current = connectedElement;
		while (current != null) {
			if (current.getContext() != null) {
				chain.add(current.getContext());
			}
			chain.add(current.getConnectionEnd());
			current = current.getNext();
		}
		return chain;
	}

	/*
	 * TODO Finish binding properties.
	 *
	 * The following binding properties are not translated:
	 * - modal properties
	 * - properties without applies to
	 * - properties with empty list values
	 * - reference values or applies to that refer to an untranslated element
	 *
	 * The following are not currently translated for binding properties:
	 * - overridden properties: To properly implement this, each SysML connection should be given a name and overriding
	 * bindings should be marked as redefined.
	 * - array subscripts in reference value and applies to
	 * - in binding
	 */
	private static List<String> translateBindingProperty(PropertyAssociation association) {
		var property = association.getProperty();
		var notTranslatedMessage = "// WARNING: '" + property.getQualifiedName() + "' not translated.";
		if (association.isModal() || association.getAppliesTos().isEmpty() || association.getOwnedValues().isEmpty()) {
			return List.of(notTranslatedMessage);
		}
		if (association.getOwnedValues().get(0).getOwnedValue() instanceof ListValue listValue) {
			if (listValue.getOwnedListElements().isEmpty()) {
				return List.of(notTranslatedMessage);
			}
			var destinations = new ArrayList<String>();
			for (var listElement : listValue.getOwnedListElements()) {
				if (listElement instanceof ReferenceValue referenceValue) {
					if (invalidCNE(referenceValue)) {
						return List.of(notTranslatedMessage);
					}
					destinations.add(translateCNE(referenceValue));
				} else {
					return List.of(notTranslatedMessage);
				}
			}
			var results = new ArrayList<String>();
			for (var appliesTo : association.getAppliesTos()) {
				if (invalidCNE(appliesTo)) {
					results.add(notTranslatedMessage);
				} else {
					ST template;
					var source = translateCNE(appliesTo);
					if (destinations.size() == 1) {
						template = new ST("connection : AADL::Actual%kind%Binding connect %source% to %destination%;",
								'%', '%');
						template.add("source", source);
						template.add("destination", destinations.get(0));
					} else {
						template = new ST("connection : AADL::Actual%kind%Binding connect (%endPoints%);", '%', '%');
						var endPoints = Stream.concat(Stream.of(source), destinations.stream());
						template.add("endPoints", endPoints.collect(Collectors.joining(", ")));
					}
					template.add("kind", property.getName().substring(7, property.getName().length() - 8));
					results.add(template.render());
				}
			}
			return results;
		} else {
			return List.of(notTranslatedMessage);
		}
	}

	private static boolean invalidCNE(ContainedNamedElement cne) {
		return cne.getContainmentPathElements()
				.stream()
				.map(ContainmentPathElement::getNamedElement)
				.anyMatch(element -> element instanceof Feature feature && invalidFeature(feature)
						|| element instanceof Connection connection && invalidConnection(connection));
	}

	private static String translateCNE(ContainedNamedElement cne) {
		return cne.getContainmentPathElements()
				.stream()
				.map(cpe -> cpe.getNamedElement().getName())
				.collect(Collectors.joining("."));
	}

	/*
	 * TODO Finish Period.
	 *
	 * The following Period properties are not translated:
	 * - modal properties
	 * - properties with applies to
	 * - properties with values that refer to the value of another property or property constant
	 * - curly properties directly on a subcomponent
	 *
	 * The following are not currently translated for Period properties:
	 * - in binding
	 *
	 * Things to consider:
	 * - overridden properties are currently handled due to the Period attribute usage being defined in the AADL library
	 * in every component that Period applies to. As a result of this, overriding a property value works automatically.
	 * However, we would like to remove the Period attribute from the component category part defs in the AADL library.
	 * When we do that, we will need to consider how to handle the overriding of values.
	 */
	private static String translatePeriod(PropertyAssociation association) {
		var notTranslatedMessage = "// WARNING: '" + association.getProperty().getQualifiedName() + "' not translated.";
		if (association.isModal() || !association.getAppliesTos().isEmpty()) {
			return notTranslatedMessage;
		}
		var timeUnits = Aadl2GlobalScopeUtil.get(association, Aadl2Package.eINSTANCE.getUnitsType(),
				"AADL_Project::Time_Units");
		if (association.getOwnedValues().get(0).getOwnedValue() instanceof IntegerLiteral integerLiteral
				&& EcoreUtil.isAncestor(timeUnits, integerLiteral.getUnit())) {
			return "attribute :>> Period = " + translateTime(integerLiteral);
		} else {
			return notTranslatedMessage;
		}
	}

	/*
	 * TODO Finish Compute_Execution_Time.
	 *
	 * The following Compute_Execution_Time properties are not translated:
	 * - modal properties
	 * - properties with applies to
	 * - properties with values that refer to the value of another property or property constant
	 * - curly properties directly on a subcomponent
	 * - properties on event ports or event data ports
	 *
	 * The following are not currently translated for Compute_Execution_Time properties:
	 * - in binding
	 *
	 * Things to consider:
	 * - overridden properties are currently handled due to the Compute_Execution_Time attribute usage being defined in
	 * the AADL library in every component that Compute_Execution_Time applies to. As a result of this, overriding a
	 * property value works automatically. However, we would like to remove the Compute_Execution_Time attribute from
	 * the component category part defs in the AADL library. When we do that, we will need to consider how to handle the
	 * overriding of values.
	 */
	private static String translateComputeExecutionTime(PropertyAssociation association) {
		var notTranslatedMessage = "// WARNING: '" + association.getProperty().getQualifiedName() + "' not translated.";
		if (association.isModal() || !association.getAppliesTos().isEmpty()) {
			return notTranslatedMessage;
		}
		var timeUnits = Aadl2GlobalScopeUtil.get(association, Aadl2Package.eINSTANCE.getUnitsType(),
				"AADL_Project::Time_Units");
		if (association.getOwnedValues().get(0).getOwnedValue() instanceof RangeValue rangeValue
				&& rangeValue.getMinimum() instanceof IntegerLiteral minimum
				&& EcoreUtil.isAncestor(timeUnits, minimum.getUnit())
				&& rangeValue.getMaximum() instanceof IntegerLiteral maximum
				&& EcoreUtil.isAncestor(timeUnits, maximum.getUnit())) {
			var template = new ST("""
					attribute :>> Compute_Execution_Time {
						:>> minimum = %minimum%
						:>> maximum = %maximum%
						%delta%
					}""", '%', '%');
			template.add("minimum", translateTime(minimum));
			template.add("maximum", translateTime(maximum));
			if (rangeValue.getDelta() == null) {
				template.add("delta", "");
			} else if (rangeValue.getDelta() instanceof IntegerLiteral delta
					&& EcoreUtil.isAncestor(timeUnits, delta.getUnit())) {
				template.add("delta", ":>> delta = " + translateTime(delta));
			} else {
				return notTranslatedMessage;
			}
			return template.render();
		} else {
			return notTranslatedMessage;
		}
	}

	private static String translateTime(IntegerLiteral time) {
		var timeWithUnits = new IntegerWithUnits<>(time, TimeUnits.class);
		var sysmlUnits = switch (timeWithUnits.getUnit()) {
		case PS -> "AADL_Project::Time_Units::ps";
		case NS -> "AADL_Project::Time_Units::ns";
		case US -> "AADL_Project::Time_Units::us";
		case MS -> "AADL_Project::Time_Units::ms";
		case SEC -> "SI::s";
		case MIN -> "SI::min";
		case HR -> "SI::h";
		};
		return timeWithUnits.getValue() + " [" + sysmlUnits + "];";
	}

	private static String translateName(AadlPackage aadlPackage) {
		var name = aadlPackage.getName();
		if (name.contains("::")) {
			return '\'' + name + '\'';
		} else {
			return name;
		}
	}

	private static String translateName(Classifier classifier) {
		if (classifier instanceof ComponentType) {
			return classifier.getName();
		} else if (classifier instanceof ComponentImplementation) {
			return '\'' + classifier.getName() + '\'';
		} else {
			throw new AssertionError("Unexpected class: " + classifier.getClass());
		}
	}

	private static String translateCategory(ComponentCategory category) {
		return switch (category) {
		case ABSTRACT -> "AADL::Abstract";
		case BUS -> "AADL::Bus";
		case DATA -> "AADL::Data";
		case DEVICE -> "AADL::Device";
		case MEMORY -> "AADL::Memory";
		case PROCESS -> "AADL::Process";
		case PROCESSOR -> "AADL::Processor";
		case SUBPROGRAM -> "AADL::Subprogram";
		case SUBPROGRAM_GROUP -> "AADL::SubprogramGroup";
		case SYSTEM -> "AADL::System";
		case THREAD -> "AADL::Thread";
		case THREAD_GROUP -> "AADL::ThreadGroup";
		case VIRTUAL_BUS -> "AADL::VirtualBus";
		case VIRTUAL_PROCESSOR -> "AADL::VirtualProcessor";
		};
	}

	private static String translateReference(Element from, ComponentClassifier to, boolean alwaysQualify) {
		var fromPackage = EcoreUtil2.getContainerOfType(from, AadlPackage.class);
		var toPackage = EcoreUtil2.getContainerOfType(to, AadlPackage.class);
		if (alwaysQualify || fromPackage != toPackage) {
			return translateName(toPackage) + "::" + translateName(to);
		} else {
			return translateName(to);
		}
	}

	private static String joinMembers(List<String> members) {
		var result = new StringBuilder();
		for (var i = 0; i < members.size(); i++) {
			result.append(members.get(i));
			if (i + 1 < members.size()) {
				result.append('\n');
				var previousIsMultiLine = members.get(i).indexOf('\n') != -1;
				var nextIsMultiLine = members.get(i + 1).indexOf('\n') != -1;
				if (previousIsMultiLine || nextIsMultiLine) {
					result.append('\n');
				}
			}
		}
		return result.toString();
	}
}