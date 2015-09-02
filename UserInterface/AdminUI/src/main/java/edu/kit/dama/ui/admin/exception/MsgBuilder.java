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
public class MsgBuilder {
    
    /**
     * 
     * @param object
     * @return 
     */
    public static String notFound(String object) {
        return "Failed to find " + object + " in the database. ";
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String alreadyExists(String object) {
        return capitalizeFirstChar(object) + " already exists in the database. ";
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String unauthorizedGetRequest(String object) {
        return "Unauthorized to get " + object + " from database. ";
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String unauthorizedAddRequest(String object) {
        return "Unauthorized to add " + object + " in the database. ";
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String unauthorizedRemoveRequest(String object) {
        return "Unauthorized to remove " + object + " from the database. ";
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String unauthorizedCreateRequest(String object) {
        return "Unauthorized to create " + object + " from the database. ";
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String unauthorizedChangeRequest(String object) {
        return "Unauthorized to change " + object + " in the database. ";
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String unauthorizedSaveRequest(String object) {
        return "Unauthorized to save " + object + " in the database. ";
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String containerInitializationFailed(String object) {
        return "Failed to initialize the container of following component: " + object + ". ";
    }

    /**
     * 
     * @return 
     */
    public static String unauthorizedContext() {
        return "Unauthorized context. ";
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String emptyValue(String object) {
        return "Value of '" + object + "' is empty. ";
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String updateFailed(String object) {
        return "Failed to update " + object + "'. ";
        
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String buildFailed(String object) {
        return "Failed to build " + object + "'.";
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String reloadFailed(String object) {
        return "Failed to reload " + object + "'. ";
    }

    /**
     * 
     * @param object
     * @return 
     */
    public static String commitFailed(String object) {
        return "Failed to commit " + object + "' to the database. ";
    }
    
    /**
     * 
     * @param object
     * @return 
     */
    public static String addFailed(String object) {
        return "Failed to add " + object + "' to the database. ";
    }
    
    /**
     * 
     * @param object
     * @return 
     */
    public static String deleteFailed(String object) {
        return "Failed to delete " + object + "' from the database. ";
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