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

/**
 * 
 * @author dx6468
 * @param <X>
 * @param <Y> 
 */
public class FilterProperties<X, Y> {
    public final X filterExpression;
    public final Y searchSpace;

    public FilterProperties(X filterExpression, Y searchSpace) {
        this.filterExpression = filterExpression;
        this.searchSpace = searchSpace;
    }
}
