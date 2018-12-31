package com.spenserkao.addressbook;

/**
 * <h1>Contact Class</h1>
 * <p>
 * Valiates the input properties before compositing a Contact class.
 * It throws exception if zny property is invalid.
 * Note: 
 * 	Among the parameters, only fullname is mandatory to create a Contact record. 
 *  The field can viewed like primary key of relational database.
 *  
 * @author  Spenser Kao (SpenserKao@optusnet.com.au)
 * @version 1.0
 * @since   2018-12-09
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.spenserkao.addressbook.exception.*;
import org.w3c.dom.Element;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;


public class Contact {
	String fullname;	
	Phone phone;	
	boolean valid = false;

	/**
	 *<p>The constructor of class Contact.
	 * @param 	name 		Contact person's fullname
	 * @param 	phone 	Contact person's workPhone
 	 * @throws 	ContactMalformedException on malformedness
	 */
	public Contact (String name, String phone) throws ContactMalformedException {
		try {
			if (name.isEmpty()) {
				throw ContactMalformedException.create("Error in processing name");
			} else {
				this.fullname = name;
			}

			this.phone = new Phone (phone);
			valid = true;
		} catch (PhoneMalformedException pme) {
			throw ContactMalformedException.create("Error in processing phone number");
		}  		
	} 

	/**
	 * <p>This method tells if the pertaining Email instance is valid
	 * @return true is valid; otherwise invalid
	 */
	public boolean isValid() {
		return valid;	
	}

	/**
	 * <p>This method returns fullname
	 * @return attribute fullanme
	 */
	public String getFullname () {
		return this.fullname;
	}

	/**
	 * <p>This method return string content of the pertaing Contact instance
	 * @return string content
	 */	
	public String toString() {
			return "\nOne contact -------" +
			"\n\tfullName: " + this.fullname + 		
			"\n\tphone: " + this.phone.toString();
	}

	/**
	 * <p>This method return JSONified content of the contact record
	 * @return the element with JSONified content of the contact record
	 * <p>Reference:
	 *
	 */		
	public String toJSON() {
		return "{" +
		"\"Fullname\": \"" + this.fullname + 
		"\", \"Phone\": \"" + this.phone.toString() +
		"\"}";
	}	

	/**
	 * <p>This method return XMLised content of the contact record
	 * @param doc link to Document tree, to avoid WRONG_DOCUMENT_ERR error. To assure that Contact elements and parental Contacts are of the same DOM tree.
	 * @return the element with XMLised content of the contact record
	 * <p>Reference:
	 *		https://tinyurl.com/yclelpux for solution of error WRONG_DOCUMENT_ERR
	 */		
	public Element toXML(Document doc) {
		Element contact = doc.createElement("contact");			

		Element fullname = doc.createElement("fullname");		
		fullname.appendChild(doc.createTextNode(this.fullname));
		Element phone = doc.createElement("phone");		
		phone.appendChild(doc.createTextNode(this.phone.toString()));

		contact.appendChild(fullname);	
		contact.appendChild(phone);		
		return contact;
	}
}
