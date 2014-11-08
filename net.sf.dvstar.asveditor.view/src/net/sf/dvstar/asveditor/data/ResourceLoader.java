package net.sf.dvstar.asveditor.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ResourceLoader {

	public static final int MODE_XML_NONE = 0;
	public static final int MODE_XML_MAIN = 1;
	public static final int MODE_XML_SLAVE = 2;
	public static final int MODE_XML_ALL = 3;

	private List<ResourceLoader.ResourceEntry> mValuesList;
	private Map<String, ChangedResourceEntry> mChangedResourceEntryValues;

	Document mMainDocument;
	Document mSlavDocument;
	private File mMainFile;
	private File mSlavFile;
	private boolean mHaveChanges = false;

	public Map<String, ChangedResourceEntry> getChangedResourceEntryValues() {
		return mChangedResourceEntryValues;
	}

	public ResourceLoader() {
		mValuesList = new LinkedList<ResourceLoader.ResourceEntry>();
		mChangedResourceEntryValues = new HashMap<String, ResourceLoader.ChangedResourceEntry>();
	}

	public String getSlaveFileLocale() {
		String ret = "en";
		if (mSlavFile != null) {
			ret = mSlavFile.getParent().substring(
					mSlavFile.getParent().indexOf('-') + 1);
		}
		return ret;
	}

	public void saveAllDocuments() {
		mHaveChanges = true;// mChangedResourceEntryValues.size()>0?true:false;
		if (mHaveChanges && mMainFile != null && mSlavFile != null) {
			Collections.sort(mValuesList);
			saveDocument(MODE_XML_MAIN);
			saveDocument(MODE_XML_SLAVE);
			mChangedResourceEntryValues.clear();
		}
	}

	public void saveDocument(int aMode) {
		if (!canSaveFile(aMode))
			return;
		if (!backupFile(aMode))
			return;
		// if (!mHaveChamges)
		// return;

		if (mMainFile != null && mSlavFile != null) {

			try {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				// root elements
				Document doc = docBuilder.newDocument();

				Element rootElement = doc.createElement("resources");
				doc.appendChild(rootElement);

				for (int index = 0; index < mValuesList.size(); index++) {

					ResourceEntry entry = mValuesList.get(index);

					String key = entry.getValueKey();
					String val = "";
					if (aMode == MODE_XML_MAIN) {
						val = entry.getValueMain();
					}
					if (aMode == MODE_XML_SLAVE) {
						val = entry.getValueSlave();
					}
					System.out.println(key + "=" + val);
					if (val != null && val.length() > 0) {
						Element string = doc.createElement("string");
						Attr attr = doc.createAttribute("name");
						attr.setValue(key);
						string.setAttributeNode(attr);
						string.appendChild(doc.createTextNode(val));
						rootElement.appendChild(string);
					}
				}

				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(
						"{http://xml.apache.org/xslt}indent-amount", "4");
				DOMSource source = new DOMSource(doc);

				String oFileName = getOutputFileName(aMode);

				new File(oFileName).getParentFile().mkdirs();

				StreamResult result = new StreamResult(new File(oFileName));
				transformer.transform(source, result);
				System.out.println("Done");

			} catch (ParserConfigurationException pce) {
				pce.printStackTrace();
			} catch (TransformerException tfe) {
				tfe.printStackTrace();
			}
		}

	}

	private String getOutputFileName(int aMode) {
		String ret = mMainFile.getPath();
		if (aMode == MODE_XML_MAIN) {
			ret = mMainFile.getPath();
		}
		if (aMode == MODE_XML_SLAVE) {
			ret = mSlavFile.getPath();
		}
		return ret;
	}

	public static void copyFileUsingChannel(File source, File dest)
			throws IOException {
		FileChannel sourceChannel = null;
		FileChannel destChannel = null;
		try {
			sourceChannel = new FileInputStream(source).getChannel();
			destChannel = new FileOutputStream(dest).getChannel();
			destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
		} finally {
			sourceChannel.close();
			destChannel.close();
		}
	}

	private boolean backupFile(int mode) {
		boolean ret = false;
		File bFile = getBackupFile(mode);
		ret = bFile.exists();
		return ret;
	}

	private File getBackupFile(int aMode) {
		File ret = null;
		File bak = null;
		switch (aMode) {
		case ResourceLoader.MODE_XML_MAIN: {
			bak = mMainFile;
		}
			break;
		case ResourceLoader.MODE_XML_SLAVE: {
			bak = mSlavFile;
		}
			break;
		}

		String backupName = getNextBackupFileName(bak);
		ret = new File(backupName);
		bak.renameTo(ret);

		return ret;
	}

	private String getNextBackupFileName(File bak) {
		String ret = bak.getPath() + ".0";
		int count = 1;
		boolean exist = true;
		do {
			String suf = "." + count;
			File check = new File(bak.getPath() + suf);
			ret = check.getPath();
			exist = check.exists();
			count++;
		} while (exist);
		return ret;
	}

	private boolean canSaveFile(int aMode) {
		boolean ret = false;
		switch (aMode) {
		case ResourceLoader.MODE_XML_MAIN: {
			ret = mMainFile != null ? true : false;
		}
			break;
		case ResourceLoader.MODE_XML_SLAVE: {
			ret = mSlavFile != null ? true : false;
		}
			break;
		}
		return ret;
	}

	public void parceDocument(File aXmlFile, int aMode) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Document mWorkDocument;

		try {
			dBuilder = dbFactory.newDocumentBuilder();
			mWorkDocument = dBuilder.parse(aXmlFile);

			switch (aMode) {
			case ResourceLoader.MODE_XML_MAIN: {
				mValuesList.clear();
				mMainDocument = mWorkDocument;
				mMainFile = aXmlFile;
			}
				break;
			case ResourceLoader.MODE_XML_SLAVE: {
				mSlavDocument = mWorkDocument;
				mSlavFile = aXmlFile;
			}
				break;
			}
			fillContent(mWorkDocument, aMode);
		} catch (ParserConfigurationException ex) {
			Logger.getLogger(ResourceLoader.class.getName()).log(Level.SEVERE,
					null, ex);
		} catch (SAXException ex) {
			Logger.getLogger(ResourceLoader.class.getName()).log(Level.SEVERE,
					null, ex);
		} catch (IOException ex) {
			Logger.getLogger(ResourceLoader.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	private void fillContent(Document aWorkDocument, int vMode) {
		NodeList nList;
		System.out.println("Root element :"
				+ aWorkDocument.getDocumentElement().getNodeName());
		nList = aWorkDocument.getElementsByTagName("string");
		System.out.println("----------------------------");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			String key = nNode.getAttributes().getNamedItem("name")
					.getNodeValue();
			String val = nNode.getTextContent();
			System.out.println(nNode.getNodeName() + " " + key + "=" + val);

			switch (vMode) {
			case ResourceLoader.MODE_XML_MAIN: {
				ResourceEntry scVal = new ResourceEntry(key, val);
				mValuesList.add(scVal);
				System.out.println("-:" + scVal);
			}
				break;
			case ResourceLoader.MODE_XML_SLAVE: {
				ResourceEntry scVal = new ResourceEntry(key, val);
				int index = mValuesList.indexOf(scVal);
				if (index >= 0) {
					System.out.println(index + ":" + scVal);
					scVal = mValuesList.get(index);
					scVal.setValueSlave(val);
					mValuesList.set(index, scVal);
				}
			}
				break;
			}

		}
		System.out.println("----------------------------");
	}

	public int getResourceEntryIndex(ResourceEntry entry) {
		return mValuesList.indexOf(entry);
	}

	public static class ChangedResourceEntry {
		public ChangedResourceEntry(int selected, ResourceEntry oldValue2,
				ResourceEntry newValue2) {
			index = Integer.valueOf(selected);
			newValue = newValue2;
			oldValue = oldValue2;
		}

		public Integer index;
		public ResourceEntry oldValue;
		public ResourceEntry newValue;
	}

	/**
	 * ResourceEntry
	 */
	public static class ResourceEntry implements Comparable<ResourceEntry> {
		private String valueKey;
		private String valueMain;
		private String valueSlave;

		public ResourceEntry(String key, String value) {
			this.valueKey = key;
			this.valueMain = value;
			this.valueSlave = "";
		}

		public ResourceEntry(String key, String valMain, String valSlave) {
			this.valueKey = key;
			this.valueMain = valMain;
			this.valueSlave = valSlave;
		}

		@Override
		public boolean equals(Object m) {
			//System.out.println("equals=" + m);
			if (m instanceof ResourceEntry) {
				if (this.valueKey.equals(((ResourceEntry) m).valueKey))
					return true;
			}
			return false;
		}

		public int getEnfryHashCode() {
			return (valueKey + valueMain + valueSlave).hashCode();
		}

		/**
		 * @return the valueMain
		 */
		public String getValueMain() {
			return valueMain;
		}

		/**
		 * @param valueMain
		 *            the valueMain to set
		 */
		public void setValueMain(String valueMain) {
			this.valueMain = valueMain;
		}

		/**
		 * @return the valueSlave
		 */
		public String getValueSlave() {
			return valueSlave;
		}

		/**
		 * @param valueSlave
		 *            the valueSlave to set
		 */
		public void setValueSlave(String valueSlave) {
			this.valueSlave = valueSlave;
		}

		/**
		 * @return the valueKey
		 */
		public String getValueKey() {
			return valueKey;
		}

		/**
		 * @param valueKey
		 *            the valueKey to set
		 */
		public void setValueKey(String valueKey) {
			this.valueKey = valueKey;
		}

		public String toString() {
			return "[" + valueKey + "](" + valueMain + ")(" + valueSlave + ")";
		}

		@Override
		public int compareTo(ResourceEntry aResourceEntry) {
			return valueKey.compareTo(aResourceEntry.getValueKey());
		}

	}

	public List<ResourceEntry> getValuesList() {
		return mValuesList;
	}

	/*
	 * public void updateChangedResourceEntryValue(int selected, ResourceEntry
	 * newValue) { mValuesList.set(selected, newValue);
	 * //mValuesList.remove(selected); //mValuesList.add(selected, newValue); }
	 */
}
