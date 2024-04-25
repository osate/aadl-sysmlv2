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
package org.osate.aadl2sysml.ui;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.modelsupport.resources.OsateResourceUtil;
import org.osate.aadl2sysml.Aadl2SysmlTranslator;

public class Aadl2SysmlTranslatorHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (HandlerUtil.getCurrentStructuredSelection(event).getFirstElement() instanceof IFile file) {
			try {
				var hasError = false;
				for (var marker : file.findMarkers(null, true, IResource.DEPTH_ONE)) {
					if (marker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR) {
						hasError = true;
						break;
					}
				}
				if (hasError) {
					MessageDialog.openError(HandlerUtil.getActiveShell(event), "Errors in AADL File",
							"Cannot translate \"" + file.getName() + "\" to SysML because it has errors.");
				} else {
					translate(OsateResourceUtil.toResourceURI(file), file.getProject(),
							file.getLocation().removeFileExtension().lastSegment(), event);
				}
			} catch (CoreException | InterruptedException | InvocationTargetException e) {
				StatusManager.getManager().handle(Status.error("Error while translating to SysML.", e));
			}
		}
		return null;
	}

	private static void translate(URI aadlPackageURI, IProject project, String fileName, ExecutionEvent event)
			throws InvocationTargetException, InterruptedException {
		var resource = new ResourceSetImpl().getResource(aadlPackageURI, true);
		var aadlPackage = (AadlPackage) resource.getContents().get(0);
		var sysMLPackage = Aadl2SysmlTranslator.translateToSysML(aadlPackage);
		var operation = new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor)
					throws CoreException, InvocationTargetException, InterruptedException {
				var subMonitor = SubMonitor.convert(monitor, "Translating AADL to SysML", 2);
				var folderPath = project.getFullPath().append("SysML-gen");
				var folder = new ContainerGenerator(folderPath).generateContainer(subMonitor.split(1));
				var stream = new ByteArrayInputStream(sysMLPackage.getBytes());
				var file = folder.getFile(new Path(fileName + ".sysml"));
				if (file.exists()) {
					file.setContents(stream, false, true, subMonitor.split(1));
				} else {
					file.create(stream, false, subMonitor.split(1));
				}
			}
		};
		HandlerUtil.getActiveWorkbenchWindow(event).run(true, true, operation);
	}
}