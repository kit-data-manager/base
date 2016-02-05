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
package edu.kit.dama.scheduler;

import edu.kit.dama.scheduler.manager.ISchedulerManager;
import edu.kit.dama.scheduler.manager.SchedulerManagerImpl;
import edu.kit.dama.scheduler.manager.SecureSchedulerManager;
import java.util.Collection;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wq7203
 */
public final class SchedulerManagement {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerManagement.class);
    /**
     * Implementation of the SchedulerManagement. There should be only one
     * instance available (singleton).
     */
    private static final SchedulerManagement SINGLETON = new SchedulerManagement();

    /**
     * Get single instance of SchedulerManagement (singleton).
     *
     * @return instance of SchedulerManagement (singleton).
     */
    public static SchedulerManagement getSchedulerManagement() {
        return SINGLETON;
    }

    /**
     * Private default constructor.
     */
    private SchedulerManagement() {
    }

    /**
     * Get a single instance for managing the job scheduler
     *
     * @return Instance for managing the job scheduler.
     */
    public ISchedulerManager getSchedulerManager() {

        final Scheduler scheduler;
        try {
            Collection<Scheduler> schedulers;
            LOGGER.debug("Try to build a Quartz StdSchedulerFactory.");
            SchedulerFactory sf = new StdSchedulerFactory();

            LOGGER.debug("Try to get schedulers from SchedulerFactory.");
            schedulers = sf.getAllSchedulers();

            if (schedulers.size() == 1) {
                scheduler = schedulers.iterator().next();
                LOGGER.debug("One scheduler found with name '{}'.", scheduler.getSchedulerName());
            } else if (schedulers.size() >= 1) {
                scheduler = schedulers.iterator().next();
                LOGGER.error("Multiple schedulers found. Using first one with name '{}'.", scheduler.getSchedulerName());
            } else {
                throw new SchedulerManagerException("Cannot build a SchedulerManager, scheduler is not running.");
            }
        } catch (SchedulerException ex) {
            throw new SchedulerManagerException("Cannot build a SchedulerFactory, internal SchedulerException occurred.", ex);
        }
        return new SecureSchedulerManager(new SchedulerManagerImpl(scheduler));
    }
}
