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
import org.omg.sysml.api.QueryApi;
import org.omg.sysml.model.Project;
import org.omg.sysml.model.Query;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;

@SuppressWarnings("restriction")
public class TestRecurse {

	public static final String DEFAULT_BASE_PATH = "http://localhost:9001";

	private final ApiClient apiClient = new ApiClient();
	private final ProjectApi projectApi = new ProjectApi(apiClient);
	private final BranchApi branchApi = new BranchApi(apiClient);
	private final ElementApi elementApi = new ElementApi(apiClient);
	private final QueryApi queryApi = new QueryApi(apiClient);

	private final Pattern pattern = Pattern.compile(".*page\\[after\\]=([^&]*)");

	private final String json = """
			{
				'@type':'Query',
				'select': ['name','@id','@type','owner'],
				'where': {
					'@type': 'CompositeConstraint',
					'operator': 'and',
					'constraint': [
						{
							'@type': 'PrimitiveConstraint',
							'inverse': False,
							'operator': '=',
							'property': 'name',
							'value': '%s'
						},
						{
							'@type': 'PrimitiveConstraint',
							'inverse': False,
							'operator': '=',
							'property': '@type',
							'value': 'Package'
						}
					]
				}
			}""";

	Project findProject(String name) throws ApiException {
		String after = null;
		do {
			System.out.println("... loading projects");
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

	void getMemberFeatures(UUID projectId, UUID commitId, UUID elementId, String memberType, String indent)
			throws ApiException {

		// Fetch the element
		var element = elementApi.getElementByProjectCommitId(projectId, commitId, elementId);

		var name = element.get("name");
		var id = element.get("@id");
		var type = element.get("@type");

		if (type.equals(memberType)) {
			System.out.println(indent + " - " + name + " (id = " + id + ", type = " + type + ")");
			// Feature memberships
	        @SuppressWarnings("unchecked")
			var features = (List<Map<String, String>>)element.get("feature");
	        if (features.size() > 0) {
	            for (var feature : features) {
	            	var featureId = UUID.fromString(feature.get("@id"));
	                getMemberFeatures(projectId, commitId, featureId, memberType, indent + "  ");
	            }
	        }
		}
	}

	public void run() throws ApiException {
		apiClient.setBasePath(DEFAULT_BASE_PATH);
		apiClient.setDebugging(true);
		apiClient.setHttpClient(new OkHttpClient.Builder().connectTimeout(1, TimeUnit.HOURS)
				.readTimeout(1, TimeUnit.HOURS).writeTimeout(1, TimeUnit.HOURS).build());

		var project = findProject("1a-Parts Tree");
		if (project == null) {
			return;
		}
		var projectId = project.getAtId();
		var branchId = project.getDefaultBranch().getAtId();
		var branch = branchApi.getBranchesByProjectAndId(projectId, branchId);
		var commitId = branch.getHead().getAtId();
		var query = new Gson().fromJson(json.formatted("vehicle1"), Query.class);
		System.out.println(query);
		var elements = queryApi.getQueryResultsByProjectIdQueryPost(projectId, query, commitId);
		for (var e : elements) {
			var id = UUID.fromString((String) e.get("@id"));
			getMemberFeatures(projectId, commitId, id, "PartUsage", "");
		}

	}

	public static void main(String[] args) throws ApiException {
		new TestRecurse().run();
	}
}
