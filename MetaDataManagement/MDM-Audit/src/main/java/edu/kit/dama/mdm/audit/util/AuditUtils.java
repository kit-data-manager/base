/*
 * Copyright 2016 Karlsruhe Institute of Technology.
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
package edu.kit.dama.mdm.audit.util;

import edu.kit.dama.mdm.audit.types.AuditDetail;
import edu.kit.dama.mdm.audit.types.AuditEvent;

/**
 *
 * @author jejkal
 */
public final class AuditUtils {

    /**
     * Entry point for AuditAspect. The implementation of the method is done
     * within the aspect, this method body is just to trigger capturing of audit
     * information in a generic way.
     *
     * @param pid The pid of the object to collect audit information for.
     * @param caller The calling user.
     * @param agent The used agent, e.g. software, hardware.
     * @param resource The affected resource, e.g. the URL of the REST endpoint.
     * @param category The category of the audit entry. This category can be
     * used to route audit entries to different sinks.
     * @param type The event type.
     * @param trigger The event trigger.
     * @param details Optional list of audit details, e.g. argument information
     * or comments.
     */
    public static void audit(String pid, String caller, String agent, String resource, String category, AuditEvent.TYPE type, AuditEvent.TRIGGER trigger, AuditDetail... details) {
        //empty body
    }

}
