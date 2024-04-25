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
package sysml.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.omg.sysml.ApiClient;
import org.omg.sysml.ApiException;
import org.omg.sysml.api.BranchApi;
import org.omg.sysml.api.ElementApi;
import org.omg.sysml.api.ProjectApi;
import org.omg.sysml.model.Project;

import okhttp3.OkHttpClient;

@SuppressWarnings("restriction")
public class TestContainment {

	public static final String DEFAULT_BASE_PATH = "http://localhost:9001";

	private final ApiClient apiClient = new ApiClient();
	private final ProjectApi projectApi = new ProjectApi(apiClient);
	private final BranchApi branchApi = new BranchApi(apiClient);
	private final ElementApi elementApi = new ElementApi(apiClient);

	private final Pattern pattern = Pattern.compile(".*page\\[after\\]=([^&]*)");

	Project findProject(String name) throws ApiException {
		String after = null;
		do {
			//System.out.println("... loading projects");
			var response = projectApi.getProjectsWithHttpInfo(after, null, 10);
			var projects = response.getData();
			for (var p : projects) {
				if (p.getName().startsWith(name)) {
					return p;
				}
			}
			after = null;
			for (var link : response.getHeaders().get("link")) {
				var m = pattern.matcher(link);
				if (m.find()) {
					after = m.group(1);
					break;
				}
			}
		} while (after != null);
		return null;
	}

	@SuppressWarnings("unchecked")
	void getContainments(UUID projectId, UUID commitId, UUID elementId, String indent) throws ApiException {

		// Fetch the element
		var element = elementApi.getElementByProjectCommitId(projectId, commitId, elementId);

		var name = element.get("name");
		var type = element.get("@type");

		System.out.println(indent + " - " + name + " (type = " + type + ")");
		
		for (var ownedRelationship : (List<Map<String, String>>) element.get("ownedRelationship")) {
			var ownedId = UUID.fromString(ownedRelationship.get("@id"));
			var relationship = elementApi.getElementByProjectCommitId(projectId, commitId, ownedId);
			var targetId = UUID.fromString(((List<Map<String,String>>)relationship.get("target")).get(0).get("@id"));
			if (((List<?>)relationship.get("ownedRelatedElement")).isEmpty()) {
				var relationshipName = relationship.get("name");
				var relationshipType = relationship.get("@type");
				var target = elementApi.getElementByProjectCommitId(projectId, commitId, targetId);
				var targetName = target.get("qualifiedName");
				var targetType = target.get("@type");

				System.out.println(indent + "   - " + relationshipName + ":" + relationshipType + " -> " + targetName + ":" + targetType);
			} else {
				getContainments(projectId, commitId, targetId, indent + "  ");
			}
		}
	}

	public void run() throws ApiException {
		apiClient.setBasePath(DEFAULT_BASE_PATH);
		apiClient.setDebugging(true);
		apiClient.setHttpClient(new OkHttpClient.Builder().connectTimeout(1, TimeUnit.HOURS)
				.readTimeout(1, TimeUnit.HOURS).writeTimeout(1, TimeUnit.HOURS).build());

		//var project = findProject("AADL_Example_Bindings");
		var project = findProject("AADL_Example");
		if (project == null) {
			return;
		}
		var projectId = project.getAtId();
		var branchId = project.getDefaultBranch().getAtId();
		var branch = branchApi.getBranchesByProjectAndId(projectId, branchId);
		var commitId = branch.getHead().getAtId();
		var roots = elementApi.getRootsByProjectCommit(projectId, commitId, null, null, null);

		for (var r : roots) {
			var inLibrary = (Boolean)r.get("isLibraryElement");
			System.out.print(inLibrary ? "LIBRARY" : "ROOT");
			System.out.println(" " + r.get("qualifiedName"));
			var id = UUID.fromString((String) r.get("@id"));
			getContainments(projectId, commitId, id, "");
		}

	}

	public static void main(String[] args) throws ApiException {
		new TestContainment().run();
	}
}
