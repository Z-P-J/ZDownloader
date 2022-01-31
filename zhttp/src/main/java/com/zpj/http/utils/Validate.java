package com.zpj.http.utils;

import com.zpj.http.exception.UnsupportedMimeTypeException;

import java.util.regex.Pattern;

/**
 * Simple validation methods. Designed for jsoup internal use
 */
public final class Validate {

    private static final Pattern xmlContentTypeRxp = Pattern.compile("(application|text)/\\w*\\+?xml.*");
    private static final Pattern htmlContentTypeRxp = Pattern.compile("(application|text)/\\w*\\+?html.*");
    private static final Pattern jsonContentTypeRxp = Pattern.compile("(application|text)/\\w*\\+?json.*");
    
    private Validate() {}

    public static void isXml(String contentType, String url) throws UnsupportedMimeTypeException {
        if (!xmlContentTypeRxp.matcher(contentType).matches()) {
            throw new UnsupportedMimeTypeException("Unhandled content type. Must be xml content type", contentType, url);
        }
    }

    public static void isHtml(String contentType, String url) throws UnsupportedMimeTypeException {
        if (!htmlContentTypeRxp.matcher(contentType).matches()) {
            throw new UnsupportedMimeTypeException("Unhandled content type. Must be html content type", contentType, url);
        }
    }

    public static void isJson(String contentType, String url) throws UnsupportedMimeTypeException {
        if (!jsonContentTypeRxp.matcher(contentType).matches()) {
            throw new UnsupportedMimeTypeException("Unhandled content type. Must be json content type", contentType, url);
        }
    }

    /**
     * Validates that the object is not null
     * @param obj object to test
     */
    public static void notNull(Object obj) {
        if (obj == null)
            throw new IllegalArgumentException("Object must not be null");
    }

    /**
     * Validates that the object is not null
     * @param obj object to test
     * @param msg message to output if validation fails
     */
    public static void notNull(Object obj, String msg) {
        if (obj == null)
            throw new IllegalArgumentException(msg);
    }

    /**
     * Validates that the value is true
     * @param val object to test
     */
    public static void isTrue(boolean val) {
        if (!val)
            throw new IllegalArgumentException("Must be true");
    }

    /**
     * Validates that the value is true
     * @param val object to test
     * @param msg message to output if validation fails
     */
    public static void isTrue(boolean val, String msg) {
        if (!val)
            throw new IllegalArgumentException(msg);
    }

    /**
     * Validates that the value is false
     * @param val object to test
     */
    public static void isFalse(boolean val) {
        if (val)
            throw new IllegalArgumentException("Must be false");
    }

    /**
     * Validates that the value is false
     * @param val object to test
     * @param msg message to output if validation fails
     */
    public static void isFalse(boolean val, String msg) {
        if (val)
            throw new IllegalArgumentException(msg);
    }

    /**
     * Validates that the array contains no null elements
     * @param objects the array to test
     */
    public static void noNullElements(Object[] objects) {
        noNullElements(objects, "Array must not contain any null objects");
    }

    /**
     * Validates that the array contains no null elements
     * @param objects the array to test
     * @param msg message to output if validation fails
     */
    public static void noNullElements(Object[] objects, String msg) {
        for (Object obj : objects)
            if (obj == null)
                throw new IllegalArgumentException(msg);
    }

    /**
     * Validates that the string is not empty
     * @param string the string to test
     */
    public static void notEmpty(String string) {
        if (string == null || string.length() == 0)
            throw new IllegalArgumentException("String must not be empty");
    }

    /**
     * Validates that the string is not empty
     * @param string the string to test
     * @param msg message to output if validation fails
     */
    public static void notEmpty(String string, String msg) {
        if (string == null || string.length() == 0)
            throw new IllegalArgumentException(msg);
    }

    /**
     Cause a failure.
     @param msg message to output.
     */
    public static void fail(String msg) {
        throw new IllegalArgumentException(msg);
    }
}
