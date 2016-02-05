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
package edu.kit.dama.ui.admin.filter;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class UserDataFilter implements Filter {
    
    private static final Logger LOGGER
            = LoggerFactory.getLogger(UserDataFilter.class);

    private final String filterExpression;
    private final String propertyId;
    private final SearchSpace searchSpace;
    
    public enum SearchSpace {
        CONTAINS("Contains"),
        STARTS("Starts With"),
        ENDS("Ends With");
        
        public final String caption;

        private SearchSpace(String caption) {
            this.caption = caption;
        }
    }

    public UserDataFilter(String filterExpression, String propertyId, 
            SearchSpace searchSpace) throws IllegalArgumentException {
        this.filterExpression = filterExpression.toLowerCase();
        this.propertyId = propertyId;
        this.searchSpace = searchSpace;
    }

    @Override
    public boolean passesFilter(Object itemId, Item item) 
            throws UnsupportedOperationException {
        boolean filterPassed = false;
        String itemValue;
        if(item.getItemProperty(propertyId).getValue().getClass().equals(Long.class)) {
            itemValue = Long.toString((long) item.getItemProperty(propertyId).getValue());
        } else {
            itemValue = (String) item.getItemProperty(propertyId).getValue();
        }
        String haystack = (itemValue).toLowerCase();
        switch (searchSpace) {
            case CONTAINS:
                filterPassed = haystack.contains(filterExpression);
                break;
            case STARTS:
                filterPassed = haystack.startsWith(filterExpression);
                break;
            case ENDS:
                filterPassed = haystack.endsWith(filterExpression);
                break;
            default:
                LOGGER.error("Failed to check whether the passed cell content '" 
                        + haystack + "' contains the filtering expression ");
                break;
        }
        return filterPassed;
    }

    @Override
    public boolean appliesToProperty(Object propertyId) {
        return true;
    }
}
