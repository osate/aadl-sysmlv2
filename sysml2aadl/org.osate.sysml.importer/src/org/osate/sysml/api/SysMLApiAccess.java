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
package org.osate.sysml.api;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.omg.sysml.ApiClient;
import org.omg.sysml.ApiException;
import org.omg.sysml.api.BranchApi;
import org.omg.sysml.api.ElementApi;
import org.omg.sysml.api.ProjectApi;
import org.omg.sysml.lang.sysml.Element;
import org.omg.sysml.lang.sysml.Relationship;
import org.omg.sysml.model.Project;
import org.osate.sysml.util.ElementHelper;

import okhttp3.OkHttpClient;

/**
 * This class contains methods to read a project from a SysML repository via the REST API.
 * It uses the Java API client from the SysMLv2 pilot implementation.
 */
@SuppressWarnings("restriction")
public class SysMLApiAccess {

	private final ApiClient apiClient = new ApiClient();

	private final ProjectApi projectApi = new ProjectApi(apiClient);

	private final BranchApi branchApi = new BranchApi(apiClient);

	private final ElementApi elementApi = new ElementApi(apiClient);

	private final Pattern pattern = Pattern.compile(".*page\\[after\\]=([^&]*)");

	private IProgressMonitor monitor;

	private String baseURL;

	private ResourceSet resourceSet;

	private Resource sysmlResource;

	private Set<Resource> libraryResources;

	private Map<String, Element> libraryElements = new HashMap<>();

	private ElementHelper elementHelper;

	public SysMLApiAccess(String baseURL, ResourceSet resourceSet, Set<Resource> aadlLibraryResources,
			IProgressMonitor monitor) {
		this.baseURL = baseURL;
		this.resourceSet = resourceSet;
		this.libraryResources = aadlLibraryResources;
		this.monitor = monitor;
		initLibraryElements();
		elementHelper = new ElementHelper(libraryElements);
	}

	private void initLibraryElements() {
		for (var r : libraryResources) {
			for (var iter = EcoreUtil.<Element>getAllContents(r, true); iter.hasNext();) {
				var e = iter.next();
				if (!(e instanceof Relationship)) {
					if (e.getElementId() != null && e.getDeclaredName() != null) {
						libraryElements.putIfAbsent(e.getElementId(), e);
					}
				}
			}
		}
	}

	/**
	 * Find the project with the given name in the repository and import the elements fron
	 * the head commit of the default branch. It does so by recursively reading the contents
	 * of the project's root elements.
	 * @param projectName name of the project to import
	 * @return a list of created resources
	 */
	public List<Resource> importProject(String projectName) {
		List<Resource> sysmlResources = new ArrayList<>();

		apiClient.setBasePath(baseURL);
		apiClient.setDebugging(true);
		apiClient.setHttpClient(new OkHttpClient.Builder().connectTimeout(1, TimeUnit.HOURS)
				.readTimeout(1, TimeUnit.HOURS).writeTimeout(1, TimeUnit.HOURS).build());

		try {
			var project = findProject(projectName);
			if (project == null)
				throw new RuntimeException("no such project: " + projectName);
			var projectId = project.getAtId();
			var branchId = project.getDefaultBranch().getAtId();
			var branch = branchApi.getBranchesByProjectAndId(projectId, branchId);
			var commitId = branch.getHead().getAtId();

			sysmlResource = resourceSet.createResource(URI.createFileURI(projectName + ".sysml"));
			((ResourceImpl) sysmlResource).setIntrinsicIDToEObjectMap(new HashMap<>());

			String after = null;
			do {
				var response = elementApi.getRootsByProjectCommitWithHttpInfo(projectId, commitId, after, null, 10);
				for (var root : response.getData()) {
					var id = UUID.fromString((String) root.get("elementId"));
					processElement(null, projectId, commitId, id, "");
				}
				after = null;
				var links = response.getHeaders().get("link");
				if (links != null) {
					for (var link : links) {
						var m = pattern.matcher(link);
						if (m.find()) {
							after = m.group(1);
							after = URLDecoder.decode(after, StandardCharsets.UTF_8);
							break;
						}
					}
				}
			} while (after != null);
		} catch (ApiException ae) {
			throw new RuntimeException(ae);
		}
		// this should resolve all proxies.
		EcoreUtil.resolveAll(resourceSet);
		sysmlResources.add(sysmlResource);
		return sysmlResources;
	}

	/**
	 * Find the project via the API.
	 * @param name
	 * @return
	 * @throws ApiException
	 */
	protected Project findProject(String name) throws ApiException {
		String after = null;
		do {
			var response = projectApi.getProjectsWithHttpInfo(after, null, 10);
			var projects = response.getData();
			for (var p : projects) {
				if (p.getName().startsWith(name)) {
					return p;
				}
			}
			after = null;
			var links = response.getHeaders().get("link");
			if (links != null) {
				for (var link : links) {
					var m = pattern.matcher(link);
					if (m.find()) {
						after = m.group(1);
						break;
					}
				}
			}
		} while (after != null);
		return null;
	}

	/**
	 * Import an element and process its owned relationships.
	 * @param owner the owner of this element
	 * @param projectId
	 * @param commitId
	 * @param elementId the ID if this element
	 * @param indent indentation for progress reporting
	 * @return the created org.omg.sysml.lang.sysml.Element
	 * @throws ApiException
	 */
	@SuppressWarnings("unchecked")
	Element processElement(Element owner, UUID projectId, UUID commitId, UUID elementId, String indent)
			throws ApiException {

		// Fetch the element via API
		var me = getElement(projectId, commitId, elementId);
		var name = (String) me.get("name");

		// skip API "proxies"
		// these seem to be created for library elements
		if (owner == null && !"Namespace".equals(me.get("@type")))
			return null;

		// create eObject
		Element e = elementHelper.fromModel(sysmlResource, owner, me);

		System.out.println(indent + "- " + me.get("@type") + " " + name);

		for (var ownedRelationship : (List<Map<String, String>>) ElementHelper.forceList(me.get("ownedRelationship"))) {
			var relationshipId = UUID.fromString(ownedRelationship.get("@id"));
			processRelationship(e, projectId, commitId, relationshipId, indent + "  ");
		}
		return e;
	}

	/**
	 * Import a relationship and process its related elements.
	 * @param owner the owner of this relationship
	 * @param projectId
	 * @param commitId
	 * @param relationshipId the ID of this relationship
	 * @param indent indentation for progress reporting
	 * @return the created org.omg.sysml.lang.sysml.Relationship
	 * @throws ApiException
	 */
	@SuppressWarnings("unchecked")
	Relationship processRelationship(Element owner, UUID projectId, UUID commitId, UUID relationshipId, String indent)
			throws ApiException {

		// Fetch the relationship via the API
		var mr = getElement(projectId, commitId, relationshipId);
		var name = (String) mr.get("name");
		
		// create eObject
		Relationship r = (Relationship) elementHelper.fromModel(sysmlResource, owner, mr);

		System.out.println(indent + "- " + mr.get("@type") + " " + name);

		for (var ownedElement : (List<Map<String, String>>) ElementHelper.forceList(mr.get("ownedRelatedElement"))) {
			var elementId = UUID.fromString(ownedElement.get("@id"));
			if ("OwningMembership".equals(mr.get("@type"))) {
				processElement(r, projectId, commitId, elementId, indent + "  ");
			} else {
				processElement(owner, projectId, commitId, elementId, indent);
			}
		}
		return r;
	}

	/**
	 * Fetch an element via the API.
	 * @param projectId
	 * @param commitId
	 * @param elementId the ID of the element to fetch
	 * @return the model element returned form the API.
	 */
	org.omg.sysml.model.Element getElement(UUID projectId, UUID commitId, UUID elementId) {
		org.omg.sysml.model.Element element = null;
		try {
			element = elementApi.getElementByProjectCommitId(projectId, commitId, elementId);
		} catch (ApiException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return element;
	}

}
