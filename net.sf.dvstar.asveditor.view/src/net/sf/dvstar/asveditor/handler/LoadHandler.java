package net.sf.dvstar.asveditor.handler;

import net.sf.dvstar.asveditor.data.ResourceLoader;
import net.sf.dvstar.asveditor.views.ASVEditView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class LoadHandler extends AbstractHandler {
	private QualifiedName path = new QualifiedName("xml", "path");

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		
		Shell shell = HandlerUtil.getActiveShell(event);
		ISelection sel = HandlerUtil.getActiveMenuSelection(event);
		IStructuredSelection selection = (IStructuredSelection) sel;

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage page = window.getActivePage();

		try {
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
					.showView(ASVEditView.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}

		ASVEditView view = (ASVEditView) page.findView(ASVEditView.ID);

		Object firstElement = selection.getFirstElement();
		// view.setTextKey("QWEQWEQWEQWEQWEQWEQWEQWE "+result);

		if (firstElement instanceof IFile) {
			IPath loc = ((IFile) firstElement).getLocation();

//			MessageDialog.openInformation(shell, "Info", "Select "
//					+ firstElement == null ? "null" : firstElement + " view "
//					+ view == null ? "null" : view.toString());

			view.loadXmlFile(loc.toOSString(), ResourceLoader.MODE_XML_MAIN);

		} else {
			MessageDialog.openInformation(shell, "Info",
					"Please select a XML string resource file");
		}

		/*
		 * if (firstElement instanceof ICompilationUnit) { //createOutput(shell,
		 * firstElement);
		 * 
		 * } else { MessageDialog.openInformation(shell, "Info",
		 * "Please select a Java source file"); }
		 */
		return null;
	}

}
