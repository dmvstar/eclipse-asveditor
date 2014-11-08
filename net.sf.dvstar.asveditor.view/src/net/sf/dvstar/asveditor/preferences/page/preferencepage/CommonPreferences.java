package net.sf.dvstar.asveditor.preferences.page.preferencepage;

import net.sf.dvstar.asveditor.ASVEPlugin;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class CommonPreferences extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(ASVEPlugin.getDefault().getPreferenceStore());
		setDescription("ASVEdit preference page");

	}

	@Override
	protected void createFieldEditors() {

		addField(new DirectoryFieldEditor("MAIN_PATH", "&Directory preference:",
				getFieldEditorParent()));
		addField(new BooleanFieldEditor("BOOLEAN_CREATE_BACKUP",
				"&Create backup file", getFieldEditorParent()));
/*
		addField(new RadioGroupFieldEditor("CHOICE",
				"An example of a multiple-choice preference", 1,
				new String[][] { { "&Choice 1", "choice1" },
						{ "C&hoice 2", "choice2" } }, getFieldEditorParent()));
*/		
		addField(new StringFieldEditor("BACKUP_SUFF", "A &backup suffix:",
				getFieldEditorParent()));
		addField(new StringFieldEditor("DEFAULT_LOCALE", "A &default resource locale:",
				getFieldEditorParent()));

	}

}
