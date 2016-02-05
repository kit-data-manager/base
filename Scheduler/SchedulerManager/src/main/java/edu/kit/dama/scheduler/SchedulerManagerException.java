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

/**
 *
 * @author wq7203
 */
public class SchedulerManagerException extends RuntimeException {

    private static final long serialVersionUID = -7632876989874868927L;

    /**
     * Creates a new instance of <code>SchedulerManagerException</code> without
     * detail message.
     */
    public SchedulerManagerException() {
    }

    /**
     * Constructs an instance of <code>SchedulerManagerException</code> with the
     * specified detail message.
     *
     * @param message the detail message.
     */
    public SchedulerManagerException(String message) {
        super(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public SchedulerManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new runtime exception with the specified cause.
     *
     * @param cause the cause.
     */
    public SchedulerManagerException(Throwable cause) {
        super(cause);
    }
}
