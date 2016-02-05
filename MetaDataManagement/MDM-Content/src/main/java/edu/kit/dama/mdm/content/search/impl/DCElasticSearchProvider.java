/*
 * Copyright 2014 Karlsruhe Institute of Technology.
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
package edu.kit.dama.mdm.content.search.impl;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author mf6319
 */
public class DCElasticSearchProvider extends BaseElasticSearchProvider {

    public final static String OBJECT_TYPE_DC = "dc";

    private List<BaseSearchTerm> terms;

    public DCElasticSearchProvider() {
        super(null, OBJECT_TYPE_DC);
    }

    public DCElasticSearchProvider(String pHostName, String pIndex, String pObjectType) {
        super(pHostName, pIndex, OBJECT_TYPE_DC);
    }

    public DCElasticSearchProvider(String pCluster, String pHostName, String pIndex, String pObjectType) {
        super(pCluster, pHostName, pIndex, OBJECT_TYPE_DC);
    }

    @Override
    public final void initialize() {
        terms = new LinkedList<BaseSearchTerm>();
        terms.add(new TextTerm("Creator", "dc\\:creator"));
        terms.add(new TextTerm("Publisher", "dc\\:publisher"));
        terms.add(new DateRangeTerm("Date", "dc\\:date"));
        terms.add(new TextTerm("Description", "dc\\:description"));
        terms.add(new TextTerm("Format", "dc\\:format"));
        terms.add(new TextTerm("Object Id", "dc\\:identifier"));
        terms.add(new TextTerm("Subject", "dc\\:subject"));
        terms.add(new TextTerm("Title", "dc\\:title"));
        terms.add(new TextTerm("Type", "dc\\:type"));
    }

    @Override
    public List<BaseSearchTerm> getSearchTerms() {
        return terms;
    }

//  public static void main(String[] args) {
//    DCElasticSearchProvider pro = new DCElasticSearchProvider("KITDataManager", "dama-virtualbox", "kitdatamanager", OBJECT_TYPE_DC);
//
//    BaseSearchTerm term = pro.getSearchTerms().get(6);
//    term.setValue("SampleInvestigation");
//
//    pro.performSearch(Arrays.asList(term), null);
//    System.out.println(pro.getResults());
//  }
}
