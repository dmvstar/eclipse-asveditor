package net.sf.dvstar.asveditor.views;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import net.sf.dvstar.asveditor.ASVEPlugin;
import net.sf.dvstar.asveditor.data.ResourceLoader;
import net.sf.dvstar.asveditor.data.ResourceLoader.ChangedResourceEntry;
import net.sf.dvstar.asveditor.data.ResourceLoader.ResourceEntry;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class ASVEditView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "net.sf.dvstar.asveditor.views.ASVEditView";
	public static final String PREF_PATH = "net.sf.dvstar.asveditor.preferences.paths";

	private TableViewer mViewerTableResource;
	private ResourceLoader mResourceLoader;
	private List<ResourceLoader.ResourceEntry> mResourceEntries;
	private int mLoadedFiles = ResourceLoader.MODE_XML_NONE;

	private Color colorRed;
	private Color colorBlu;

	protected boolean mNewResourceEntryValues = false;

	private Action actionLoadMain;
	private Action actionLoadSlav;
	private Action actionSaveAlls;
	private Action actionReloadMS;
	private Action doubleClickAction;
	private Action selectClickAction;

	private Text textValKey;
	private Text textValMain;
	private Text textValSlav;

	private Button btnDelValue;
	private Button btnNewValue;
	private Button btnUpdValue;

	private Label lblSlavValue;
	private Label lblKeynValue;
	private Label lblMainValue;
	protected ResourceEntry mLastEditResourceEntry;

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public ASVEditView() {
		mResourceEntries = new LinkedList<ResourceLoader.ResourceEntry>();
		mResourceLoader = new ResourceLoader();
		colorRed = new Color(Display.getCurrent(), 255, 0, 0);
		colorBlu = new Color(Display.getCurrent(), 0, 0, 255);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		/*
		 * for (int i = 1; i < 100; i++) { entries.add(new
		 * ResourceLoader.ResourceEntry("key_" + i, "value " + i)); }
		 */
		/*
		 * viewer.setContentProvider(new ViewContentProvider());
		 * viewer.setLabelProvider(new ViewLabelProvider());
		 */
		TableColumn col;
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));

		Composite compositeTable = new Composite(parent, SWT.NONE);
		compositeTable.setLayout(new GridLayout(5, false));
		mViewerTableResource = new TableViewer(compositeTable,
				SWT.FULL_SELECTION);

		mViewerTableResource
				.setContentProvider(new ResourceEntryContentProvider());
		mViewerTableResource.setLabelProvider(new ResourceEntryLabelProvider());

		mViewerTableResource.setSorter(new NameSorter());
		// viewer.setInput(getViewSite());

		mViewerTableResource.setInput(mResourceEntries);

		Table table = mViewerTableResource.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));

		col = new TableColumn(table, SWT.NONE);
		col.setText("Value Key");
		// col.setText(ASVEPlugin.getString("label.key.text", "String Key"));
		// col.setWidth(169);
		col = new TableColumn(table, SWT.NONE);
		col.setText("Value Main");
		// col.setText(ASVEPlugin.getString("label.main.text",
		// "Main Value (en)"));
		// col.setWidth(172);
		col = new TableColumn(table, SWT.NONE);
		col.setText("Value Slave");
		// col.setText(ASVEPlugin.getString("label.slav.text",
		// "Slave Value (xx)"));
		// col.setWidth(246);

		// new TableColumn(table, SWT.LEFT).setText("Value Main");
		// new TableColumn(table, SWT.LEFT).setText("Value Slave");
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		lblKeynValue = new Label(compositeTable, SWT.NONE);
		lblKeynValue.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));

		textValKey = new Text(compositeTable, SWT.BORDER);
		textValKey
				.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		textValKey.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1));
		new Label(compositeTable, SWT.NONE);

		btnUpdValue = new Button(compositeTable, SWT.NONE);
		btnUpdValue.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (mNewResourceEntryValues) {
					updateResourceEntryValue(ResourceLoader.MODE_XML_ALL);
					mNewResourceEntryValues = false;
					btnNewValue.setText(ASVEPlugin.getString("button.new.text",
							"New"));
					//setLastEditResourceEntry();
				} else {
					ISelection selection = mViewerTableResource.getSelection();
					Object obj = ((IStructuredSelection) selection)
							.getFirstElement();
					if (obj != null) {
						ResourceLoader.ResourceEntry entry = (ResourceEntry) obj;
						updateResourceEntryValue(entry);
					}
				}

			}
		});
		btnUpdValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));

		lblMainValue = new Label(compositeTable, SWT.NONE);
		lblMainValue.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));

		textValMain = new Text(compositeTable, SWT.BORDER);
		textValMain.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				updateResourceEntryValue(ResourceLoader.MODE_XML_MAIN);
			}
		});
		textValMain.setFont(SWTResourceManager
				.getFont("Segoe UI", 10, SWT.BOLD));
		textValMain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));

		btnNewValue = new Button(compositeTable, SWT.NONE);
		btnNewValue.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (!mNewResourceEntryValues) {
					clearResourceEntryTextValues();
					mNewResourceEntryValues = true;
					textValKey.setFocus();
					btnNewValue.setText(ASVEPlugin.getString("button.cnl.text",
							"Cancel"));
					mLastEditResourceEntry = getCurrentResourceEntry();
				} else {
					btnNewValue.setText(ASVEPlugin.getString("button.new.text",
							"New"));
					mNewResourceEntryValues = false;
					setLastEditResourceEntry();
				}
			}
		});

		btnNewValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));

		lblSlavValue = new Label(compositeTable, SWT.NONE);
		lblSlavValue.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));

		textValSlav = new Text(compositeTable, SWT.BORDER);
		textValSlav.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				updateResourceEntryValue(ResourceLoader.MODE_XML_SLAVE);
			}
		});
		textValSlav.setFont(SWTResourceManager
				.getFont("Segoe UI", 10, SWT.BOLD));
		textValSlav.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));

		btnDelValue = new Button(compositeTable, SWT.NONE);
		btnDelValue.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				deleteResourceEntryValue(-1);
				clearResourceEntryTextValues();
				setLastEditResourceEntry();
			}
		});
		btnDelValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));

		lblKeynValue.setText("String Key");
		lblMainValue.setText("Main Value (en)");
		lblSlavValue.setText("Slave Value (xx)");

		btnNewValue.setText("New");
		btnDelValue.setText("Del");
		btnUpdValue.setText("Update");

		// Create the help context id for the viewer's control
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(mViewerTableResource.getControl(),
						"net.sf.dvstar.asveditor.viewer");

		refresh();
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		hookClickAction();
		contributeToActionBars();

		initLocalizedStrings();
	}

	protected void setLastEditResourceEntry() {
		if (mLastEditResourceEntry != null) {
			setSelectedResourceEntry(mLastEditResourceEntry);
			mLastEditResourceEntry = null;
		} else {
			setSelectedResourceEntry(null);
		}
	}

	private void initLocalizedStrings() {
		btnNewValue.setText(ASVEPlugin.getString("button.new.text", "New"));
		btnDelValue.setText(ASVEPlugin.getString("button.del.text", "Del"));
		btnUpdValue.setText(ASVEPlugin.getString("button.upd.text", "Update"));

		lblKeynValue.setText(ASVEPlugin.getString("label.key.text",
				"String Key"));
		lblMainValue.setText(ASVEPlugin.getString("label.main.text",
				"Main Value (en)"));
		lblSlavValue.setText(ASVEPlugin.getString("label.slav.text",
				"Slave Value (xx)"));

		actionLoadMain.setText(ASVEPlugin.getString("actions.text.main",
				"Load Main XML resource file"));
		actionLoadMain.setToolTipText(ASVEPlugin.getString(
				"actions.tiptext.main", "Load Main XML resource file"));
		actionLoadSlav.setText(ASVEPlugin.getString("actions.text.slave",
				"Load Slave XML resource file"));
		actionLoadSlav.setToolTipText(ASVEPlugin.getString(
				"actions.tiptext.slave", "Load Slave XML resource file"));

		actionSaveAlls.setText(ASVEPlugin.getString("actions.text.saveall",
				"Save string resources"));
		actionSaveAlls.setToolTipText(ASVEPlugin.getString(
				"actions.text.saveall", "Save string resources"));
		actionReloadMS.setText(ASVEPlugin.getString("actions.text.reload",
				"Reload string resources"));
		actionReloadMS.setToolTipText(ASVEPlugin.getString(
				"actions.text.reload", "Reload string resources"));

		Table table = mViewerTableResource.getTable();
		TableColumn col;
		col = table.getColumn(0);
		col.setText(ASVEPlugin.getString("label.key.text", "String Key"));
		col = table.getColumn(1);
		col.setText(ASVEPlugin.getString("label.main.text", "Main Value (en)"));
		col = table.getColumn(2);
		col.setText(ASVEPlugin.getString("label.slav.text", "Slave Value (xx)"));

	}

	protected void clearResourceEntryTextValues() {
		setResourceEntryTextValues(null);
	}

	protected void deleteResourceEntryValue(int i) {
		int delIndex = -1;
		if (i < 0) {
			delIndex = getCurrentResourceEntryIndex();
		} else
			delIndex = i;
		mResourceLoader.getValuesList().remove(delIndex);
		refresh();
	}

	protected void updateResourceEntryValue(int mode) {
		if (mNewResourceEntryValues && mode == ResourceLoader.MODE_XML_ALL) {
			addResourceEntryValue();
			mNewResourceEntryValues = false;
		} else {
			ISelection selection = mViewerTableResource.getSelection();
			Object obj = ((IStructuredSelection) selection).getFirstElement();
			if (obj != null) {
				ResourceLoader.ResourceEntry entry = (ResourceEntry) obj;
				updateResourceEntryValue(entry);
			}
		}
	}

	private void addResourceEntryValue() {
		ResourceEntry addValue = getChangedResourceEntry(-1);
		if (addValue != null) {
			mResourceLoader.getValuesList().add(addValue);
			refresh();
			setSelectedResourceEntry(addValue);
		} else {
			setLastEditResourceEntry();
		}
	}

	private void setSelectedResourceEntry(ResourceEntry addValue) {
		if (addValue != null) {
			int selected = getResourceEntryIndex(addValue);
			if (selected >= 0) {
				/*
				 * Object o = mViewerTableResource.getElementAt(1); Object o1 =
				 * mViewerTableResource.getTable().getItem(1).getData();
				 * TableItem items[] =
				 * mViewerTableResource.getTable().getItems(); for(int i=0;
				 * i<items.length;i++ ) {
				 * if(items[i].getData().equals(addValue)){selected=i; break;} }
				 * o = mViewerTableResource.getElementAt(selected);
				 * //System.out.println(
				 * "selected["+selected+"] addValue ["+addValue+
				 * "] find ["+o+"] find1 ["+o1+"]" +" item "+items[selected]);
				 * // mViewerTableResource.setSelection(new StructuredSelection(
				 * addValue ),true); // mViewerTableResource.setSelection(new
				 * StructuredSelection
				 * (mViewerTableResource.getElementAt(selected)),true);
				 * //mViewerTableResource.getElementAt(selected);
				 */
				mViewerTableResource.setSelection(new StructuredSelection(
						addValue), true);
			}
		} else {
			mViewerTableResource.setSelection(new StructuredSelection(
					mViewerTableResource.getElementAt(0)), true);
		}
	}

	private int getResourceEntryIndex(ResourceEntry addValue) {
		int ret = -1;
		ret = mResourceLoader.getValuesList().indexOf(addValue);
		return ret;
	}

	protected void updateResourceEntryValue(ResourceEntry entry) {
		int selected = getCurrentResourceEntryIndex();
		ResourceEntry oldValue = getCurrentResourceEntry(selected);
		ResourceEntry newValue = getChangedResourceEntry(selected);
		ChangedResourceEntry changed = new ChangedResourceEntry(selected,
				oldValue, newValue);
		if (newValue != null && oldValue != null) {
			if (oldValue.getValueKey().equals(newValue.getValueKey())) {
				if (oldValue.getEnfryHashCode() != newValue.getEnfryHashCode()) {
					mResourceLoader.getChangedResourceEntryValues().put(
							newValue.getValueKey(), changed);
					mResourceLoader.getValuesList().set(selected, newValue);
					refresh();
					actionSaveAlls.setEnabled(true);
					actionReloadMS.setEnabled(true);
				} else {
					showMessage("Resource entry values is same !");
				}
			} else {
				if (!mNewResourceEntryValues) {
					showMessage("Key values is not same, revert changes !");
					setResourceEntryTextValues(oldValue);
				}
			}
		}
	}

	private ResourceEntry getChangedResourceEntry(int selected) {
		ResourceEntry ret = null;
		String key = textValKey.getText();
		String valMain = textValMain.getText();
		String valSlave = textValSlav.getText();
		if (key.length() > 0) {
			ret = new ResourceEntry(key, valMain, valSlave);
		}
		return ret;
	}

	private ResourceEntry getCurrentResourceEntry(int selected) {
		ResourceEntry ret = null;
		ret = mResourceLoader.getValuesList().get(selected);
		return ret;
	}

	private ResourceEntry getCurrentResourceEntry() {
		ResourceLoader.ResourceEntry entry = null;
		ISelection selection = mViewerTableResource.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		if (obj != null) {
			entry = (ResourceEntry) obj;
		}
		return entry;
	}

	private int getCurrentResourceEntryIndex() {
		int ret = -1;
		ISelection selection = mViewerTableResource.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		if (obj != null) {
			ResourceLoader.ResourceEntry entry = (ResourceEntry) obj;
			ret = mResourceLoader.getResourceEntryIndex(entry);
		}
		return ret;
	}

	private void refresh() {
		mViewerTableResource.refresh();
		Table table = mViewerTableResource.getTable();
		for (int i = 0, n = table.getColumnCount(); i < n; i++) {
			table.getColumn(i).pack();
		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ASVEditView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr
				.createContextMenu(mViewerTableResource.getControl());
		mViewerTableResource.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, mViewerTableResource);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(actionLoadMain);
		manager.add(new Separator());
		manager.add(actionLoadSlav);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(actionLoadMain);
		manager.add(actionLoadSlav);
		manager.add(actionSaveAlls);
		manager.add(actionReloadMS);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actionLoadMain);
		manager.add(actionLoadSlav);
		manager.add(actionSaveAlls);
		manager.add(actionReloadMS);
	}

	private void saveLastDirSelect(File fXml, int mode) {
		// We access the instanceScope
		IEclipsePreferences preferences = InstanceScope.INSTANCE
				.getNode(PREF_PATH);

		Preferences sub1 = (Preferences) preferences.node("paths");
		sub1.put("lastDir_" + mode, fXml.getParent());
		try {
			// forces the application to save the preferences
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	private String loadLastDirSelect(int mode) {
		String ret = ".";
		Preferences preferences = InstanceScope.INSTANCE.getNode(PREF_PATH);
		Preferences sub1 = preferences.node("paths");
		ret = sub1.get("lastDir_" + mode, ".");
		return ret;
	}

	private void makeActions() {

		ImageDescriptor myImageOpenM = ImageDescriptor
				.createFromURL(FileLocator.find(ASVEPlugin.getDefault()
						.getBundle(), new Path("icons/document-new-m.png"),
						null));

		ImageDescriptor myImageOpenS = ImageDescriptor
				.createFromURL(FileLocator.find(ASVEPlugin.getDefault()
						.getBundle(), new Path("icons/document-new-s.png"),
						null));

		ImageDescriptor myImageSave = ImageDescriptor.createFromURL(FileLocator
				.find(ASVEPlugin.getDefault().getBundle(), new Path(
						"icons/document-save.png"), null));

		ImageDescriptor myImageRelo = ImageDescriptor.createFromURL(FileLocator
				.find(ASVEPlugin.getDefault().getBundle(), new Path(
						"icons/document-refresh.png"), null));

		actionLoadMain = new Action() {
			public void run() {
				loadXmlFile(null, ResourceLoader.MODE_XML_MAIN);
			}
		};
		actionLoadMain.setText("Load Main XML resource file");
		actionLoadMain.setToolTipText("Load Main XML resource file");
		actionLoadMain.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		actionLoadMain.setImageDescriptor(myImageOpenM);

		actionLoadSlav = new Action() {
			public void run() {
				loadXmlFile(null, ResourceLoader.MODE_XML_SLAVE);
			}
		};

		actionLoadSlav.setText("Load Slave XML resource file");
		actionLoadSlav.setToolTipText("Load Slave XML resource file");
		actionLoadSlav.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		actionLoadSlav.setImageDescriptor(myImageOpenS);

		actionSaveAlls = new Action() {
			public void run() {
				mResourceLoader.saveAllDocuments();
				actionSaveAlls.setEnabled(false);
				actionReloadMS.setEnabled(false);
				refresh();
			}
		};
		actionSaveAlls.setText("Save string resources");
		actionSaveAlls.setToolTipText("Save string resources");
		actionSaveAlls.setImageDescriptor(myImageSave);
		actionSaveAlls.setEnabled(false);

		actionReloadMS = new Action() {
			public void run() {
				actionReloadMS.setEnabled(false);
			}
		};
		actionReloadMS.setText("Reload string resources");
		actionReloadMS.setToolTipText("Reload string resources");
		actionReloadMS.setImageDescriptor(myImageRelo);
		actionReloadMS.setEnabled(false);

		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = mViewerTableResource.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				showMessage("Double-click detected on " + obj.toString());
			}
		};

		selectClickAction = new Action() {
			public void run() {
				ISelection selection = mViewerTableResource.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				if (obj != null) {
					ResourceLoader.ResourceEntry entry = (ResourceEntry) obj;
					setResourceEntryTextValues(entry);
				}
			}
		};

	}

	protected void setResourceEntryTextValues(ResourceEntry entry) {
		if (entry == null) {
			textValKey.setText("");
			textValMain.setText("");
			textValSlav.setText("");
		} else {
			textValKey.setText(entry.getValueKey());
			textValMain.setText(entry.getValueMain());
			textValSlav.setText(entry.getValueSlave());
		}
	}

	private void hookDoubleClickAction() {
		mViewerTableResource.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void hookClickAction() {
		mViewerTableResource
				.addSelectionChangedListener(new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						selectClickAction.run();
					}
				});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(mViewerTableResource.getControl()
				.getShell(), "ASVEdit View ", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		mViewerTableResource.getControl().setFocus();
	}

	// ----------------------------------------------------------------------------------------------------------------

	class ResourceEntryLabelProvider implements ITableLabelProvider,
			ITableFontProvider, ITableColorProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			ResourceLoader.ResourceEntry ae = (ResourceLoader.ResourceEntry) element;
			switch (columnIndex) {
			case 0:
				return ae.getValueKey();
			case 1:
				return ae.getValueMain();
			case 2:
				return ae.getValueSlave();
			}
			return "";
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public Color getForeground(Object element, int columnIndex) {
			Color ret = mViewerTableResource.getTable().getForeground();

			if (isChangedEntryValue(element, columnIndex) && columnIndex > 0)
				ret = colorRed;
			if (isChangedEntryValue(element, columnIndex) && columnIndex == 0)
				ret = colorBlu;

			return ret;
		}

		private boolean isChangedEntryValue(Object element, int columnIndex) {
			boolean ret = false;
			ResourceEntry entryCheck = (ResourceEntry) element;
			ChangedResourceEntry entryFind = mResourceLoader
					.getChangedResourceEntryValues().get(
							entryCheck.getValueKey());
			if (entryFind != null) {
				switch (columnIndex) {
				case 0: {
					if (entryFind.oldValue.getValueMain().hashCode() != entryFind.newValue
							.getValueMain().hashCode()
							|| entryFind.oldValue.getValueSlave().hashCode() != entryFind.newValue
									.getValueSlave().hashCode())
						ret = true;
				}
					break;
				case 1: {
					if (entryFind.oldValue.getValueMain().hashCode() != entryFind.newValue
							.getValueMain().hashCode())
						ret = true;
				}
					break;
				case 2: {
					if (entryFind.oldValue.getValueSlave().hashCode() != entryFind.newValue
							.getValueSlave().hashCode())
						ret = true;
				}
					break;
				}
			}
			return ret;
		}

		@Override
		public Color getBackground(Object element, int columnIndex) {
			return mViewerTableResource.getTable().getBackground();
		}

		@Override
		public Font getFont(Object element, int columnIndex) {
			Font ret = mViewerTableResource.getTable().getFont();

			if (columnIndex == 0 || isChangedEntryValue(element, columnIndex)) {
				FontData fd[] = ret.getFontData();
				fd[0].setStyle(SWT.BOLD);
				ret = new Font(ret.getDevice(), fd[0]);
			}

			return ret;
		}
	}

	class ResourceEntryContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			// System.out.println("getElements(Object inputElement)="+inputElement);
			return ((List<?>) inputElement).toArray();
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/**
	 * Load XML strings from file
	 * 
	 * @param fileName
	 *            filename
	 * @param mode
	 *            main or slave file to load
	 */
	public void loadXmlFile(String fileName, int aMode) {
		String result = null;
		String message = "";

		int vMode = aMode;
		// showMessage("File name is " + fileName);
		result = fileName;
		setResourceEntryTextValues(null);
		switch (vMode) {
		case ResourceLoader.MODE_XML_MAIN: {
			message = "Load Main XML resource file executed";
		}
			break;
		case ResourceLoader.MODE_XML_SLAVE: {
			message = "Load Slave XML resource file executed";
			if ((mLoadedFiles & ResourceLoader.MODE_XML_MAIN) == 0) {
				showMessage("Main File not loaded !");
				result = null;
				vMode = ResourceLoader.MODE_XML_MAIN;
			}
		}
			break;
		}

		if (result != null) {
			File fXml = new File(result);
			if (!fXml.isFile() || !fXml.getAbsolutePath().endsWith(".xml")
					|| !fXml.getName().contains("string"))
				result = null;
		}
		if (result == null) {
			FileDialog dialog = new FileDialog(mViewerTableResource
					.getControl().getShell(), SWT.OPEN);
			dialog.setFilterNames(new String[] { "XML Files (*.xml)" });
			dialog.setFilterExtensions(new String[] { "*.xml" });
			dialog.setFilterPath(loadLastDirSelect(vMode));
			result = dialog.open();

		}
		if (result == null) {
			showMessage("File name is empty !");
		}

		if (result != null) {
			// showMessage(message + " " + result);
			File fXml = new File(result);
			if (fXml.exists()) {
				// showMessage("Open file " + result);
				saveLastDirSelect(fXml, vMode);

				mResourceLoader.parceDocument(fXml, vMode);
				if (vMode == ResourceLoader.MODE_XML_MAIN) {
					File fXmlSlave = getXmlSlave(fXml);
					if (fXmlSlave != null) {
						mResourceLoader.parceDocument(fXmlSlave,
								ResourceLoader.MODE_XML_SLAVE);
						mLoadedFiles |= vMode | ResourceLoader.MODE_XML_SLAVE;
					}
				}

				mResourceEntries = mResourceLoader.getValuesList();

				if ((mLoadedFiles & ResourceLoader.MODE_XML_SLAVE) == ResourceLoader.MODE_XML_SLAVE) {
					String suf = mResourceLoader.getSlaveFileLocale();
					String txt = lblSlavValue.getText();
					String rpl = txt.replace("xx", suf);
					lblSlavValue.setText(rpl);

					Table table = mViewerTableResource.getTable();
					TableColumn col = table.getColumn(2);
					txt = col.getText();
					rpl = txt.replace("xx", suf);
					col.setText(rpl);
					// col.setText(ASVEPlugin.getString("label.slav.text","Slave Value (xx)"));

				}

				mViewerTableResource.setInput(mResourceEntries);
				refresh();
				mLoadedFiles |= vMode;
			} else {
				showMessage("File not exists " + result);
			}
		}

	}

	/**
	 * Get File for slave locale
	 * 
	 * @param fXml
	 *            main locale
	 * @return slave locale
	 */
	private File getXmlSlave(File fXml) {
		String locale = ASVEPlugin.getDefault().getPreferenceStore()
				.getString("DEFAULT_LOCALE");
		String dlocale = locale.length() > 0 ? locale : "ru";
		String newFileName = fXml.getParentFile().getParent() + "/values-"
				+ dlocale + "/" + fXml.getName();
		// showMessage(newFileName);
		File ret = new File(newFileName);
		if (!ret.exists())
			ret = null;
		return ret;
	}

}

/*
 * http://www.vogella.com/tutorials/EclipsePlugIn/article.html
 * http://www.java2s.com/Code/JavaAPI/org.eclipse.jface.viewers/
 * implementsIStructuredContentProvider.htm
 * 
 * http://www.vogella.com/tutorials/EclipsePlugIn/article.html
 * http://basti1302.github.io/startexplorer/
 * http://www.eclipse.org/articles/Article-Table-viewer/table_viewer.html
 * http://www.vogella.com/tutorials/SWT/article.html
 */

