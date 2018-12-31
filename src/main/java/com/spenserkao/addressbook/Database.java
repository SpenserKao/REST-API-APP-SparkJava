package com.spenserkao.addressbook;

/**
* <h1>Database Class</h1>
* <p> 
* The class for constructing Database.
*
* @author  Spenser Kao (SpenserKao@optusnet.com.au)
* @version 1.0
* @since   2018-12-10
*/

import com.spenserkao.addressbook.exception.DBException;
import com.spenserkao.addressbook.exception.ContactMalformedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.lang.StringBuffer;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException; 
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.io.FileWriter;
import java.io.IOException;
import org.w3c.dom.*;
import java.nio.file.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.net.URI;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.nio.charset.Charset;

public class Database {
	String db_xml_file;
	String propertiesFile;
	HashMap<String, Contact> db = new HashMap<>();
	boolean valid = false;
	boolean changed=false; // true when creating or deleting a contact record.
	Contact contact;

	/**
	 * <p>The constructor to populate database from a properties file that contains the default database file
	 * @param propFile properties file that contains name of default XML-based db file.
	 */
	public Database (String propFile) {
		this.propertiesFile = propFile;

		System.out.println("DBG: Database: propfile=" + this.propertiesFile);
		/**
		 * Read the application properties file for the name of default database XML FIle
		 * 
		 * Situiation#1: properties file is inside jar file at run time or
		 * under resources/<package path> at design time. But we are not using it here,
		 * InputStream inputStreamProp = getClass().getResourceAsStream(propFile);
		 * 
		 * Situiation#2: at the same deploymenet directory, we need to convert relative path of properties file to
		 & absolute path first. 
		 */
		retrieveDefaultDBFileName(propFile);
		populateDB(this.db_xml_file);
		//populateDB("db/default-db.xml");
	}

	/**
	 * <p>The constructor to populate database from multiple xml files
	 * @param l List of xml-based database files
	 */
	public Database(List<String> l) {
    	if (!l.isEmpty()) {
			l.forEach((s) -> {
	    		System.out.println("\nFound one name on the list to populate for: " + s);
				populateDB(s);
			});
		}	
	}

	/**
	 * <p>The empty constructor.
	 */
	public Database() {
	}

	/**
	 * <p>gets the current number of contacts.
	 * @return expected number of contacts
	 */
	int getContactsCount() {
		return this.db.size();
	}

	/** 
	 * <p>The methods populates databse basing on input XML-based file.
	 * @param relPath relative path of XML-based db file.
	 */
	public void populateDB(String relPath) {
		try {
			valid = false;

			InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(relPath); 
	
		    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		    Document doc = docBuilder.parse(in);

		    // normalize text representation
		    doc.getDocumentElement().normalize();
		    System.out.println("Root element of the doc: " + doc.getDocumentElement().getNodeName());

	        NodeList listOfContacts = doc.getElementsByTagName("contact");
	        int totalContacts = listOfContacts.getLength();
	        System.out.println("Total # of Contacts: " + totalContacts);

			for(int s=0; s<listOfContacts.getLength() ; s++){
				Node firstContactNode = listOfContacts.item(s);
	            if(firstContactNode.getNodeType() == Node.ELEMENT_NODE){
	            	Element firstContactElement = (Element)firstContactNode;
	                String fullname = firstContactElement.getElementsByTagName("fullname").item(0).getChildNodes().item(0).getNodeValue(); 
	                String phone = firstContactElement.getElementsByTagName("phone").item(0).getChildNodes().item(0).getNodeValue(); 
	                System.out.println("DBG: populating a new Contact: " + fullname + " " + phone);
	                contact = new Contact (fullname, phone); 
	                db.put(fullname, contact);                 
	            }
			}

			in.close();
		} catch (Exception e) {
			System.out.println("DBG: populateDB:" + e.getStackTrace());
		} 
		changed = false;
		valid = true;
	}

	/**
	 * <p>This method searchs contact records with fullname as specified. Wildcard accepted.
	 * @param fn the fullname as base to search
	 * @return matched record; otherwise null
	 */
	public ArrayList<Contact> search(String fn){
		Set<String> namesSet = db.keySet();
		ArrayList<String> namesMatches = new ArrayList<>();
		ArrayList<Contact> contactsMatched = new ArrayList<>();

		// parepare regex rule
		String variableSizeStr = "[a-zA-Z,.\\s]*";
		boolean hasHeadtStar = fn.startsWith("*");
		boolean hasTailStar = fn.endsWith("*");	
		boolean hasMiddleStar = fn.substring(1, fn.length()-1).indexOf("*") >= 0;
		String[] criteria = fn.split("\\*");
		String rule = (hasHeadtStar ? variableSizeStr : "");

		for (int i=0; i<criteria.length; i++) {
		    rule = rule + (criteria[i] + (hasMiddleStar && (i<criteria.length-1) ? variableSizeStr : "") );
		}

		rule = rule + (hasTailStar ? variableSizeStr : "");
		System.out.println("regex rule=" + rule);
		Pattern ptrn = Pattern.compile(rule);

		// matching key set with regex rule
		namesSet.forEach(
			n -> {
				if (ptrn.matcher(n).matches()) {
					namesMatches.add(n);
				}
			}
		);
	    
		namesMatches.forEach(
			n -> {
				contactsMatched.add(db.get(n));				
			}
		);

		return contactsMatched;
	}

	/**
	 * <p>This method add contact record
	 * @param fn The fullname that will be used as the key to add into HashMap
	 * @param c The Contact instance to used as value to add into HashMap
	 */
	public void add(String fn, Contact c){
		db.put(fn, c);
		changed = true;
	}

	/**
	 * <p>This method deletes contact record with fullname as specified
	 * @param fn The fullname that will be used as the key to look up into HashMap	 	
	 */
	public void delete(String fn){
		db.remove(fn);
		changed = true;
	}

	/**
	 * <p>This method tells if the pertaining Database instance is valid
	 * @return true is valid; otherwise invalid
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * <p>This method tells if the pertaining Database has been changed since initial loading
	 * @return true when creation or deletion of database content has been done; otherwise not done.
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * <p>This method displays content of the pertaing Database instance
	 */	
	public void show() {
		System.out.println("\nThe HashMap-based database has " + this.db.size() + " contact records listed below ---"); 
		this.db.forEach( (n, c)->System.out.println(c.toString() ) );
	}

	/**
	 * <p>This method displays contacts of the pertaing Database instance
	 * @return string representation of existing database instance
	 */	
	public String listContacts() {
		StringBuffer allContacts = new StringBuffer("{ \"Contacts\": [");
		this.db.forEach( (n, c)-> {
			allContacts.append(c.toJSON()).append(","); 
		});
		// remove the last "," of the appended string 
		if (this.db.size() > 0) {
			allContacts.replace(allContacts.length()-1, allContacts.length(), "");
		}
		allContacts.append("]}");
		System.out.println("DBG: allContacts=" + allContacts.toString());
		return allContacts.toString();
	}

	/**
	 * <p>This method shows names in the list
	 * @param l List to traverse
	 */
    static void showList(List<String> l) {
    	if (!l.isEmpty()) {
	    	System.out.println("\nFound following names on the list:"); 
			l.forEach((d) -> {
				System.out.println(d);
			});
		}
	}

	/**
	 *<p>THis method writes database content to XNLL file.
	 *<pre>References:
	 * 	https://docs.oracle.com/javase/7/docs/api/java/util/Properties.html
	 * 	https://docs.oracle.com/javase/7/docs/api/javax/xml/transform/OutputKeys.html
	 * 	https://docs.oracle.com/javase/7/docs/api/javax/xml/transform/Transformer.html#setOutputProperty(java.lang.String,%20java.lang.String)
	 * </pre>
	 * @return true sucessfull writeback; false failed writeback.
	 */
	public boolean writeToXmlFile() {

		try {
			System.out.println("Writing content back to " + this.db_xml_file);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();				
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.newDocument();
			Element contacts = doc.createElement("contacts");	
			doc.appendChild(contacts);

			db.forEach( 
				(n, c)-> {
					contacts.appendChild(c.toXML(doc));
				}
			);

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");	// assure Carriage Return is suffixed to each element
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");

			System.out.println("DBG: writeToXmlFile: done preparation before writing to XML file");

			DOMSource source = new DOMSource(doc);

			// Next line disabled for replacement block further down
			//StreamResult result = new StreamResult(new File(this.db_xml_file)); 

			/**
			 * begining of replacement block
			 */
			URI uri = Thread.currentThread().getContextClassLoader().getResource(this.db_xml_file).toURI();
			System.out.println("DBG: writeToXmlFile: uri.toString()=" + uri.toString());

			/**
			 * The solution to 'FileSystemNotFoundException' run-time error
			 * Ref: the 3rd last suggestion of following link:
			 * https://stackoverflow.com/questions/22605666/java-access-files-in-jar-causes-java-nio-file-filesystemnotfoundexception
			 */
			if ("jar".equals(uri.getScheme())){
			    for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
			        if (provider.getScheme().equalsIgnoreCase("jar")) {
			            try {
			                provider.getFileSystem(uri);
			            } catch (FileSystemNotFoundException e) {
			                // in this case we need to initialize it first:
			                provider.newFileSystem(uri, Collections.emptyMap());
			            }
			        }
			    }
			}

			Path path = Paths.get(uri);		
			System.out.println("DBG: writeToXmlFile: path.toString()=" + path.toString());
			
			//BufferedWriter bufferedWriter = Files.newBufferedWriter(path);
			/**
			 * The solution to 'FileSystemNotFoundException' run-time error: the file needs to be writable and to be truncated.
			 * Ref: the 3rd last suggestion of following link:
			 * https://stackoverflow.com/questions/22605666/java-access-files-in-jar-causes-java-nio-file-filesystemnotfoundexception
			 * Note: one adjustment required:
			 * StandardOpenOption.CREATE needs to be avoided from the options fed to newBufferedWriter method, as the file, 
			 * db/default-db.xml already exists in the jar file.
			 */
			BufferedWriter bufferedWriter = Files.newBufferedWriter(
				path, 
				Charset.forName("UTF-8"),
				StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

			StreamResult result = new StreamResult(bufferedWriter);
			/**
			 * end of replacement block
			 */

			transformer.transform(source, result);
			changed = false;
		
			bufferedWriter.close();

			return true;
		} catch (TransformerException te) {
			System.out.println("DBG: writeToXmlFile: encountered TransformerException");
			te.printStackTrace();
			return false;
		} catch (Exception e) {
			System.out.println("DBG: writeToXmlFile: encountered Exception detailed below:");
			e.printStackTrace();
			return false;
		}
	}			

	/**
	 *<p>This method returns the name of XML-based default db file.
	 *@return the expected name of file
	 */
	public String getDefaultDBFileName() {
		return this.db_xml_file;
	}

	/**
	 *<p>This method returns the name of XML-based default db file.</p>
	 *<p>Once done, attribute db_xml_file will be assigned.</p>
	 *@param propFile properties file that points to XML-based default db file.
	 */
	public void retrieveDefaultDBFileName(String propFile) {
		try {
			Properties prop = new Properties();

			InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(propFile);
			prop.load(in);	

			this.db_xml_file = prop.getProperty("DEFAULT_DATABASE_XML_FILE");
			in.close();
		} catch (FileNotFoundException fnfe) {
			System.out.println("DBG: FileNotFoundException:" + fnfe.getMessage());
		} catch (IOException ioe) {
			System.out.println("DBG: IOException:" + ioe.getMessage());			
			System.out.println(ioe.getMessage());
		} catch (Exception e) {
			System.out.println("DBG: Exception:" + e.getMessage());					
		} 
	}
}