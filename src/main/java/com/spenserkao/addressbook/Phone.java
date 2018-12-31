package com.spenserkao.addressbook;

/**
* <h1>Phone Class</h1>
* <p>
* The class valiates the input phone number.
* It throws exception if invalid.
* 
* @author  Spenser Kao (SpenserKao@optusnet.com.au)
* @version 1.0
* @since   2018-12-09 
*/

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.spenserkao.addressbook.exception.PhoneMalformedException;

public class Phone {
	String number;
	boolean valid;
	
	/**
	 * <p>The constructor of class Phone.
	 * @param no the input phone number
	 * @throws PhoneMalformedException on malformedness
	 */	 
	public Phone (String no) throws PhoneMalformedException {
		number = no;

		String landlineRule = "([0][2-9])?(\\s)?[0-9]{4}(\\s)?[0-9]{4}";
		String mobileRule = "([0][4])(\\s)?((\\s)?[0-9]){8}";	
		String internationRule = "[+][0-9]*";
		Pattern ptrn = Pattern.compile(landlineRule + "|" + mobileRule + "|" + internationRule);	
		valid = ptrn.matcher(number).matches();	

		if (!valid) {
			throw PhoneMalformedException.create("Exception in validating a phone number " + number);
		}		
	} 

	/**
	 * <p>This method tells if the pertaining Phone instance is valid
	 * @return true is valid; otherwise invalid
	 */
	public boolean isValid() {
		return valid;
	}
	
	/**
	 * <p>This method return string content of the pertaing Phone instance
	 * @return string content
	 */	
	public String toString() {
		return number;
	}
}