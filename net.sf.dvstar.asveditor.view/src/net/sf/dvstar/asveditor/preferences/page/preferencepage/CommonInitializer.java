package net.sf.dvstar.asveditor.preferences.page.preferencepage;

import net.sf.dvstar.asveditor.ASVEPlugin;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class CommonInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
	    IPreferenceStore store = ASVEPlugin.getDefault().getPreferenceStore();
	    store.setDefault("URL", "http://www.stardust.in.ua");	
	    store.setDefault("DEFAULT_LOCALE", "re");	
    }

}
