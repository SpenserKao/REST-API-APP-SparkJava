package com.spenserkao.addressbook.exception;

/**
 * <p>Created by Spenser Kao (SpenserKao@optusnet.com.au) on 21/10/2018.
 */
public class DBException extends Exception{

    public DBException (String message, Exception fault) {
        super(message, fault);
    }

    public static DBException create(String message, Exception e) {
        System.out.println(message + e.getMessage());
        return new DBException(message, e);
    }

    public static DBException create(String message) {
        System.out.println(message);
        return new DBException(message, new Exception(message));
    }
}
