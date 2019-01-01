package com.spenserkao.addressbook;

/**
* <h1>AddressBook Class</h1>
* <p>
* The class constructs the addressbook project.
* 
* @author  Spenser Kao (SpenserKao@optusnet.com.au)
* @version 1.3.0
* @since   2018-12-10
*/

import com.spenserkao.addressbook.utility.JSON2HTML;
import static spark.Spark.*;
import java.util.ArrayList;
import java.util.List;
import com.spenserkao.addressbook.exception.ContactMalformedException;

public class AddressBook {
	static Database db;	// null
	static JSON2HTML j2tt = new JSON2HTML();
    
    public static void main(String[] args) {
		List<String> dbNamesList = new ArrayList<>();
		//static Database db = null;
		boolean isDbParamToken=false;
		boolean helpMsgOnly = false;

		// what is port number available?
		String num = System.getProperty("server.port");
		System.out.println("DBG found server.port=" + num);
		int port_no = (num != null) ? Integer.parseInt(num) : 8000 ;

		port(port_no);	// run on port #8000
		staticFiles.location("/public"); // Static files, such as css file

        for (String s: args) {
        	switch (s) {
        		case "-d":
        			// desigante database(s)
        			isDbParamToken = true;
        			//db.show();
        			break;
        		case "-h":
        			// show help message
        			isDbParamToken = false;
        			helpMsgOnly = true;
        			System.out.println(localHelpMsg());
        			System.exit(0);
        			break;
        		default:
        			// for any option other than "-" option, display help message
        			if (s.startsWith("-")) {
	        			// show help message
	        			isDbParamToken = false;
	        			helpMsgOnly = true;
	        			System.out.println(localHelpMsg());
	        			System.exit(0);
        				break;
        			} else if (s.endsWith(".xml") && isDbParamToken) {      			
	        			// if still within DB Parameters token, get the DB names
	        			dbNamesList.add(s);
	        			break;
        			}
        	}
        }

        // Debugging purpose: Now process the argument(s)
		showList(dbNamesList);

		/**  
		 * Follow-up population of db
		 * if no explict db xml file(s) provided, go with default one
		 */
		if (!helpMsgOnly) {
			if (dbNamesList.isEmpty()) {
				// from properties file that points to default db
	    		db = new Database("AddressBook.properties");
			} else {
				// from user-spefified db(s)
				db = new Database(dbNamesList);
				db.retrieveDefaultDBFileName("AddressBook.properties");
			}
		}

        // Configure the routes - thru three parts: verb, path and callback.
    	get("/addressbook", (req, res) -> {
			return welcomeMsgEndpointsList();
    	}); 
        get("/addressbook/about", (req, res) -> {
			return about();
		});     
        get("/addressbook/add", (req, res) -> {
			return addContact(req.queryParams("fullname"), req.queryParams("phone"));
		});     
		get("/addressbook/search/*", (req, res) -> {
			return listSearchResult(req.splat()[0]);
		});  
		get("/addressbook/remove/*", (req, res) -> {
			return remove(req.splat()[0]);
		});    
		get("/addressbook/list", (req, res) -> { 
			return listContacts(); 
		});  		   
		get("/addressbook/save", (req, res) -> { 
			if (dbNamesList.isEmpty()) {
				return (db.writeToXmlFile() ? "Successful " : "Failed ") + " saving of contacts change back to default-db.xml if retrieved at start-up."; 
			} else {
				return "As current contacts weren't loaded from default XML-based file, to which no saving of contacts change will take place.";
			}				
		});  			
    }

    static String listContacts() {
		return j2tt.convert(db.listContacts(), "Contacts");    	
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
	 * <p>This method displays local help message
	 * @return help message
	 */	
	static String localHelpMsg() {
		return
		"Usage: java -jar build/libs/addressbook-all-1.0.jar [-options]\n" +
		"where options include:\n" +
		"    -d <dbFile1> [<dbFile2>... <dbFileN>]\n" +
		"\tSpecifies XML-based db files, which must be with 'xml' extension.\n" +
		"\tBy default, which is no '-d' option and associated db(s) specified, there will be a default dbFile remembered in AddressBook.peoperties.\n" +
		"    -h\n\tPrint this help message.\n";
	}    

	/**
	 * <p>This method displays welcome message and list of endpoints
	 * @return expected message and list
	 */	
	@SuppressWarnings("unchecked")	
	static String welcomeMsgEndpointsList() {
		return j2tt.convert("templates/endPoints.json", 
		"Welcome to the AddressBook - a SparkJava-based REST API application. There are endpoints created as follows.",
		"Registered Routes", 
		false);		// src/main/resources/
	}

	/**
	 * <p>This method displays welcome message and list of endpoints
	 * @param pattern of fullname of contact(s) to remove	 	
	 * @return expected message and list
	 */	
	static String listSearchResult(String pattern) {
		StringBuffer partContacts = new StringBuffer("{ \"Contacts\": [");		
		ArrayList<Contact> contactsMatched = db.search(pattern);
		if (contactsMatched.size() > 0) {			
			// contactsMatched.forEach( c -> {c.toString();} ) 
			for (Contact contact : contactsMatched) {
				partContacts.append(contact.toJSON()).append(",");
			}		
		}
		// remove the last "," of the appended string 
		if (contactsMatched.size() > 0) {
			partContacts.replace(partContacts.length()-1, partContacts.length(), "");
		}
		partContacts.append("]}");
		return j2tt.convert(partContacts.toString(), "Contacts");    			
	}

	/**
	 * <p>This method removes an existing contact
	 * @param pattern of fullname of contact(s) to remove
	 * @return result of the removal
	 */	
	static String remove(String pattern) {
		String contactContents = ""; 
		ArrayList<Contact> contactsMatched = db.search(pattern);
		if (contactsMatched.size() == 0) {
			return "found no matching contact";
		} else {
			// contactsMatched.forEach( c -> {c.toString();} ) 
			for (Contact contact : contactsMatched) {
		    	db.delete(contact.getFullname());
			}
			return "removed " + contactsMatched.size() + " matching contact(s)";					
		}
	}	

	/**
	 * <p>This method adds a new contact
	 * @param fn Fullname as part of a contact
	 * @param ph Phone as part of a contact	 	
	 * @return result of the removal
	 */	
	static String addContact (String fn, String ph) {
		String fullname="", phone="", result;
		try {
			Contact c = new Contact(fn, ph);
			db.add(fn, c);
			result = "Successful contact addition with fullname " + fn + " and phone " + ph;
		} catch ( ContactMalformedException emf) {
			result = "Failed contact addition due to " + emf.getMessage();
		}
		return result;
	}

	/**
	 * <p>This method shows about message
	 * @return HTMLised about message
	 */	
	@SuppressWarnings("unchecked")
	static String about() {
		return j2tt.convert("templates/releaseNote.json", 
		null, 
		"Release Note", 
		true);	// src/main/resources/
	}
}