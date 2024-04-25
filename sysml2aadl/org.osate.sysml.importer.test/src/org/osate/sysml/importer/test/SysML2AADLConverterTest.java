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
package org.osate.sysml.importer.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SysML2AADLConverterTest {

	static SysMLTestHelper helper;

	@BeforeAll
	static void loadLibraries() {
		helper = new SysMLTestHelper();
		helper.initialize();
		helper.setVerbose(false);
	}

	@AfterEach
	void reset() {
		helper.cleanResourceSet();
	}

	@Test
	void testComponents() {
		var path = "models/components/";
		var results = helper.testFiles(path + "Components.sysml");
		compareFiles(path, results);
	}

	@Test
	void testSubcomponents() {
		var path = "models/subcomponents/";
		var results = helper.testFiles(path + "Subcomponents.sysml");
		compareFiles(path, results);
	}

	@Test
	void testFeatures() {
		var path = "models/features/";
		var results = helper.testFiles(path + "Features.sysml");
		compareFiles(path, results);
	}

	@Test
	void testConnections() {
		var path = "models/connections/";
		var results = helper.testFiles(path + "Connections.sysml");
		compareFiles(path, results);
	}

	@Test
	void testPoperties() {
		var path = "models/properties/";
		var results = helper.testFiles(path + "Properties.sysml");
		compareFiles(path, results);
	}

	private void compareFiles(String path, List<Resource> resources) {
		for (var r : resources) {
			try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					PrintStream printStream = new PrintStream(outputStream)) {
				r.save(printStream, null);

				var aadl = outputStream.toString();
				var fileName = r.getURI().toString();
				var lines = Files.readAllLines(Paths.get(path + "out/" + fileName));
				var content = lines.stream() //
						.filter(s -> !s.startsWith("--")) // skip comments
						.collect(Collectors.joining());
				assertEquals(content.replaceAll("\\s+", ""), aadl.replaceAll("\\s+", ""), fileName + " differs");
			} catch (IOException e) {
				fail("error reading result file " + r.getURI());
			}
		}
	}
}
