package com.spenserkao.addressbook.exception;

/**
* <h1>PhoneMalformedException</h1>
* 
* @author  Spenser Kao (SpenserKao@optusnet.com.au)
* @version 1.0
* @since   2018-12-09 
*/
public class PhoneMalformedException extends Exception{

    public PhoneMalformedException (String message, Exception fault) {
        super(message, fault);
    }

    public static PhoneMalformedException create(String message, Exception e) {
        System.out.println(message + e.getMessage());
        return new PhoneMalformedException(message, e);
    }

    public static PhoneMalformedException create(String message) {
        System.out.println(message);
        return new PhoneMalformedException(message, new Exception(message));
    }
}
