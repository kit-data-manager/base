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
package edu.kit.dama.scheduler.api.impl.test;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.dama.scheduler.api.trigger.ExpressionTrigger;

@XmlAccessorType(XmlAccessType.FIELD)
public class SimpleExpressionTrigger extends ExpressionTrigger {

    @XmlTransient
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleExpressionTrigger.class);

    private Date startDate;
    private Date endDate;
    private String expression;
    private Date nextFireTime;

    /**
     * Default constructor.
     */
    public SimpleExpressionTrigger() {
    }

    @Override
    public void setStartDate(Date startDate) {
        this.startDate = (startDate != null) ? (Date) startDate.clone() : null;
    }

    @Override
    public Date getStartDate() {
        return (this.startDate != null) ? (Date) this.startDate.clone() : null;
    }

    @Override
    public void setEndDate(Date endDate) {
        this.endDate = (endDate != null) ? (Date) endDate.clone() : null;
    }

    @Override
    public Date getEndDate() {
        return (this.endDate != null) ? (Date) this.endDate.clone() : null;
    }

    @Override
    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public String getExpression() {
        return this.expression;
    }

    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = (nextFireTime != null) ? (Date) nextFireTime.clone() : null;
    }

    @Override
    public Date getNextFireTime() {
        return (this.nextFireTime != null) ? (Date) this.nextFireTime.clone() : null;
    }
}
