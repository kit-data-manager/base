/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
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
package edu.kit.dama.mdm.dataorganization.impl.jpa.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * A bulk persist implementation used to store multiple entities within one
 * transaction. After a definable number of persists the opened transaction is
 * committed and a new transaction is created.
 *
 * @author pasic
 */
public final class BulkPersist {

  /**
   * The entity manager used to persist.
   */
  private final EntityManager em;
  /**
   * The amount of entities written before the data is flushed to the database.
   */
  private int flushRate;
  /**
   * Teh currently written entity.f
   */
  private int count;
  /**
   * The current transaction.
   */
  private EntityTransaction transaction;

  /**
   * Default constructor.
   */
  public BulkPersist() {
    em = PersistenceFacade.getInstance().getEntityManagerFactory().createEntityManager();
  }

  /**
   * Start a bulk ingest. After start entities can be persisted using
   * persist(Object) within one transaction. After <i>flushrate</i> persists,
   * the data is flushed to the database and a new transaction is started.
   *
   * @param flushrate The number of persists before a flush() is exectuted.
   */
  public void startBulkIngest(int flushrate) {
    this.flushRate = flushrate;
    this.count = 0;
    transaction = em.getTransaction();
    transaction.begin();
  }

  /**
   * Persist the provided object using the current transaction. If the provided
   * flushRate is reached by this object, a flush() is executed and a new
   * transaction is started.
   *
   * @param o The object to persist.
   */
  public void persist(Object o) {
    em.persist(o);
    if (++count >= flushRate) {
      count = 0;
      em.flush();
      transaction.commit();
      transaction = em.getTransaction();
      transaction.begin();
    }
  }

  /**
   * Finish the bulk ingest. All data is flushed and the current transaction is
   * committed.
   */
  public void finish() {
    count = 0;
    em.flush();
    transaction.commit();
  }
}
