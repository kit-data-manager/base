/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.mdm.content.search.impl;

import java.util.Date;
import org.apache.commons.lang.math.LongRange;

/**
 *
 * @author niedermaier
 */
public class DateRangeTerm extends BaseSearchTerm<LongRange> {

  public DateRangeTerm(String label, String key) {
    super(label, key);
  }

  public void setFromDate(Date fromDate) {
    setValue(new LongRange(fromDate.getTime(), getValue().getMaximumLong()));
  }

  public Date getFromDate() {
    return new Date(getValue().getMinimumLong());
  }

  public void setToDate(Date toDate) {
    setValue(new LongRange(getValue().getMinimumLong(), toDate.getTime()));
  }

  public Date getToDate() {
    return new Date(getValue().getMaximumLong());
  }
}
