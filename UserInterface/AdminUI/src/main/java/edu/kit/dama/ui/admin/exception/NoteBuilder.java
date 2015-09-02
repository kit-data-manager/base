/* 
 * Copyright 2015 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.ui.admin.exception;

/**
 *
 * @author dx6468
 */
public class NoteBuilder {

    public static final String CONTACT = "Please contact the system administrator. ";
    public static final String FILL_OUT_REQUEST
            = "Please fill out all required component tagged with a red star. ";
    public static final String UNAUTHORIZED_CTX = "Unauthorized request! ";
    public static final String NON_EXECUTABLE_COMMIT = "Non-executable commit! ";
    public static final String CAUSE = "Cause: ";
    private static final String UNAUTHORIZED = "You are NOT authorized to ";
    private static final String NOT_FOUND = " was not found in the database. ";
    private static final String EXISTS = " already exists in the database. ";

    /**
     * 
     * @param inexistentObject
     * @return 
     */
    public static String notFound(String inexistentObject) {
        return capitalizeFirstChar(inexistentObject)+ NOT_FOUND + CONTACT;
    }

    /**
     * 
     * @param existingObject
     * @return 
     */
    public static String alreadyExists(String existingObject) {
        return capitalizeFirstChar(existingObject) + EXISTS;
    }

    /**
     * 
     * @param requestedObject
     * @return 
     */
    public static String unauthorizedGetRequest(String requestedObject) {
        return UNAUTHORIZED + "get " + requestedObject + ". " + CONTACT;
    }

    /**
     * 
     * @param objectToAdd
     * @return 
     */
    public static String unauthorizedAddRequest(String objectToAdd) {
        return UNAUTHORIZED + "add " + objectToAdd + ". " + CONTACT;
    }

    /**
     * 
     * @param objectToRemove
     * @return 
     */
    public static String unauthorizedRemoveRequest(String objectToRemove) {
        return  UNAUTHORIZED + "remove " + objectToRemove + ". " + CONTACT;
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String unauthorizedChangeRequest(String object) {
        return UNAUTHORIZED + "change " + object + ". " + CONTACT;
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String unauthorizedSaveRequest(String object) {
        return "You are NOT authorized to save " + object + ". " + CONTACT;
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String unauthorizedCreateRequest(String object) {
        return "You are NOT authorized to create " + object + ". " + CONTACT;
    }

    /**
     * 
     * @return 
     */
    public static String unauthorizedContext() {
        return UNAUTHORIZED_CTX + CONTACT;
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String containerInitializationFailed(String object) {
        return String.valueOf(capitalizeFirstChar(object)) + " could not be loaded. " + CONTACT;
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String emptyValue(String object) {
        return capitalizeFirstChar(object) + " is empty. " + FILL_OUT_REQUEST;
    }

    /**
     *
     * @param input
     * @return
     */
    private static String capitalizeFirstChar(String input) {
        if (Character.isUpperCase(input.charAt(0))) {
            return input;
        }
        String output = Character.toString(input.charAt(0)).toUpperCase();
        return output.concat(input.substring(1));
    }
}