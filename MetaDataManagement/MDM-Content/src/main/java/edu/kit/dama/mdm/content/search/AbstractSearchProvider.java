/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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
package edu.kit.dama.mdm.content.search;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.mdm.content.search.impl.BaseSearchTerm;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author mf6319
 * @param <C> Generic type to search for.
 *
 * @TODO Add lazy loading. Return all results by count method but only load e.g.
 * 100. getResults() then takes one additional argument for the max. index. If
 * it exceeds the cache size, additional elements have to be loaded.
 */
public abstract class AbstractSearchProvider<C> {

  private List<C> resultCache;
  private int resultCacheSize = 100;

  /**
   * Default constructor.
   */
  public AbstractSearchProvider() {
    initializeInternal();
  }

  /**
   * Set the result cache size.
   *
   * @param pCacheSize The cache size (Default: 100)
   */
  public void setResultCacheSize(int pCacheSize) {
    resultCacheSize = pCacheSize;
  }

  /**
   * Get the result cache size.
   *
   * @return The cache size.
   */
  public int getResultCacheSize() {
    return resultCacheSize;
  }

  /**
   * Internal initialization.
   */
  private void initializeInternal() {
    resultCache = new LinkedList<>();
    initialize();
  }

  /**
   * Initialize search provider, e.g. by initializing the list of available
   * search terms. This method is called inside the default constructor of
   * AbstractSearchProvider and should therefore not be used for any
   * dynamic/configurable initialization.
   */
  protected abstract void initialize();

  /**
   * Get a list of search terms supported for this panel.
   *
   * @return The list of supported search terms.
   */
  public abstract List<BaseSearchTerm> getSearchTerms();

  /**
   * Perform the search for max. <i>getResultCacheSize()</i> results. This
   * method first performs {@link #queryForResults(java.util.List)} and then may
   * filter the obtained results using {@link #filterResults(java.util.List, edu.kit.authorization.entities.IAuthorizationContext)
   * } or not. Afterwards, the results are written to the cache and can be
   * obtained using {@link #getResults() } or {@link #getResults(int, int) }.
   *
   * @param pTermValues A list of search terms including their values.
   * @param pContext The authorization context used to filter the results.
   */
  public final void performSearch(List<BaseSearchTerm> pTermValues, IAuthorizationContext pContext) {
    resultCache.clear();

    List<C> results = queryForResults(pTermValues);
    results = filterResults(results, pContext);
    for (C result : results) {
      resultCache.add(result);
    }
  }

  /**
   * The actual search action implemented by the concrete search provider. This
   * search returns all results that fit the provided search terms. The results
   * might be filtered later using {@link #filterResults(java.util.List, edu.kit.authorization.entities.IAuthorizationContext)
   * }.
   *
   * @param pTermValues A list of search terms including their values.
   *
   * @return The list of search results.
   */
  public abstract List<C> queryForResults(List<BaseSearchTerm> pTermValues);

  /**
   * Filter a list of previously obtained search results.
   *
   * @param pUnfilteredResults An unfiltered list of search results.
   * @param pContext The authorization context used to filter the results.
   *
   * @return The filtered list of search results.
   */
  public abstract List<C> filterResults(List<C> pUnfilteredResults, IAuthorizationContext pContext);

  /**
   * Returns all results located in the cache.
   *
   * @return All results.
   */
  public List<C> getResults() {
    return resultCache;
  }

  /**
   * Returns a part of all results located in the cache.
   *
   * @param pIndex The first index.
   * @param pResults The number of results.
   *
   * @return The requested elements.
   */
  public List<C> getResults(int pIndex, int pResults) {
    return resultCache.subList(pIndex, pResults);
  }

  /**
   * Returns the current number of results in the result cache.
   *
   * @return The number of results.
   */
  public final int getResultCount() {
    return resultCache.size();
  }
}
