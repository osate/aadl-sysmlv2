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

import java.util.concurrent.TimeUnit;

import org.omg.sysml.ApiClient;
import org.omg.sysml.ApiException;
import org.omg.sysml.api.BranchApi;
import org.omg.sysml.api.ElementApi;
import org.omg.sysml.api.ProjectApi;

import okhttp3.OkHttpClient;

@SuppressWarnings("restriction")
public class Test {

	public static final String DEFAULT_BASE_PATH = "http://localhost:9001";

	private final ApiClient apiClient = new ApiClient();
	private final ProjectApi projectApi = new ProjectApi(apiClient);
	private final BranchApi branchApi = new BranchApi(apiClient);
	private final ElementApi elementApi = new ElementApi(apiClient);

	public void run() throws ApiException {
		apiClient.setBasePath(DEFAULT_BASE_PATH);
		apiClient.setDebugging(true);
		apiClient.setHttpClient(new OkHttpClient.Builder().connectTimeout(1, TimeUnit.HOURS)
				.readTimeout(1, TimeUnit.HOURS).writeTimeout(1, TimeUnit.HOURS).build());

		var projects = projectApi.getProjects(null, null, null);
		for (var p: projects) {
			var projectId = p.getAtId();
			var name = p.getName();
			System.out.println(projectId + " (" + name + ")");
			
			if (name.startsWith("AADL_Example_Bindings")) {
				var branchId = p.getDefaultBranch().getAtId();
				var branch = branchApi.getBranchesByProjectAndId(projectId, branchId);
				var commitId = branch.getHead().getAtId();
				var roots = elementApi.getRootsByProjectCommit(projectId, commitId, null, null, null);

				System.out.println("ROOTS");
				for (var e : roots) {
					// System.out.println(e.toString());
					System.out.println(e.get("qualifiedName"));
				}
			}
		}
		
		
	}

	public static void main(String[] args) throws ApiException {
		new Test().run();
	}
}
