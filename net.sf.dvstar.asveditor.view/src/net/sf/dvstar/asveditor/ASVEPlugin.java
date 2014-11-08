package net.sf.dvstar.asveditor;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class ASVEPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.sf.dvstar.asveditor"; //$NON-NLS-1$

	// The shared instance
	private static ASVEPlugin plugin;
	
    //Resource bundle.
    private ResourceBundle resourceBundle;
	
	/**
	 * The constructor
	 */
	public ASVEPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		//Locale.setDefault(new Locale("en"));
        resourceBundle = Platform.getResourceBundle(getBundle());
		plugin = this;
/*		
        try {
            URL messagesUrl = 
                    find(new Path("$nl$/bundle.properties")); //$NON-NLS-1$
            if(messagesUrl != null) {
                resourceBundle = new PropertyResourceBundle(
                        messagesUrl.openStream());
            }
        } catch (IOException x) {
            resourceBundle = null;
        }
*/		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ASVEPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	
	
    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     * @param key the key for which to fetch a localized text
     * @return localized string corresponding to key
     */
    public static String getString(String key) {
        ResourceBundle bundle = 
        		ASVEPlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     * @param key the key for which to fetch a localized text
     * @param arg1 default value
     * @return localized string corresponding to key
     */
    public static String getString(String key, String arg1) {
    	String ret = getString(key);
    	if(ret.equals(key)) ret = arg1;
        //return MessageFormat.format(, new String[]{arg1});
    	return ret;		
    }
    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     * @param key the key for which to fetch a localized text
     * @param arg1 runtime first argument to replace in key value
     * @param arg2 runtime second argument to replace in key value
     * @return localized string corresponding to key
     */
    public static String getString(String key, String arg1, String arg2) {
        return MessageFormat.format(
                getString(key), new String[]{arg1, arg2});
    }
    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     * @param key the key for which to fetch a localized text
     * @param arg1 runtime argument to replace in key value 
     * @param arg2 runtime second argument to replace in key value
     * @param arg3 runtime third argument to replace in key value
     * @return localized string corresponding to key
     */
    public static String getString(
            String key, String arg1, String arg2, String arg3) {
        return MessageFormat.format(
                getString(key), new String[]{arg1, arg2, arg3});
    }
    
    /**
     * Returns the plugin's resource bundle.
     * @return resource bundle
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
	
}
