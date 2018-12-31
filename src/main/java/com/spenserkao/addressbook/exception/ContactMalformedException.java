package com.spenserkao.addressbook.exception;

/**
* <h1>ContactMalformedException</h1>
* 
* @author  Spenser Kao (SpenserKao@optusnet.com.au)
* @version 1.0
* @since   2018-12-09 
*/
public class ContactMalformedException extends Exception{

    public ContactMalformedException (String message, Exception fault) {
        super(message, fault);
    }

    public static ContactMalformedException create(String message, Exception e) {
        System.out.println(message + e.getMessage());
        return new ContactMalformedException(message, e);
    }

    public static ContactMalformedException create(String message) {
        System.out.println(message);
        return new ContactMalformedException(message, new Exception(message));
    }
}
