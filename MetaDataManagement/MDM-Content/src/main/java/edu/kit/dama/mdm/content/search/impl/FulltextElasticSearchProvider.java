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
public class FulltextElasticSearchProvider extends BaseElasticSearchProvider {

  private List<BaseSearchTerm> terms;

  public FulltextElasticSearchProvider() {
    super(null, null, null);
  }

  public FulltextElasticSearchProvider(String pHostName, String pIndex, String pObjectType) {
    super(pHostName, pIndex, pObjectType);
  }

  public FulltextElasticSearchProvider(String pCluster, String pHostName, String pIndex, String pObjectType) {
    super(pCluster, pHostName, pIndex, pObjectType);
  }

  @Override
  public final void initialize() {
    terms = new LinkedList<>();
     terms.add(new TextTerm("All", "_all"));
    //terms.add(new TextTerm("Fulltext", KITDataManagerSettings.getSingleton().getStringProperty(KITDataManagerSettings.ELASTIC_SEARCH_FULLTEXT_SEARCH_KEY_ID, "es.fulltext")));
    //terms.add(new TextTerm("abstract", "abstract"));
  }

  @Override
  public List<BaseSearchTerm> getSearchTerms() {
    return terms;
  }

//  public static void main(String[] args) {
//    FulltextElasticSearchProvider pro = new FulltextElasticSearchProvider("KITDataManager", "localhost", "kitdatamanager", "bibtex");
//
//    BaseSearchTerm term = pro.getSearchTerms().get(0);
//    term.setValue("Grid");
//   
//    pro.performSearch(Arrays.asList(term), AuthorizationContext.factorySystemContext());
//    System.out.println(pro.getResults());
//  }
}
