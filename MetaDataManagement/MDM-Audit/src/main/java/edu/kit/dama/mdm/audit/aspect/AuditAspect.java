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
package edu.kit.dama.mdm.audit.aspect;

import edu.kit.dama.mdm.audit.impl.AuditManager;
import edu.kit.dama.mdm.audit.types.AuditEvent;
import edu.kit.dama.mdm.audit.types.AuditDetail;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * The audit aspect. This aspect is activated when calling
 * edu.kit.dama.mdm.audit.util.AuditUtils.audit() and it takes care, that the
 * audit event defined by this method is published to a configured receiver,
 * e.g. a RabbitMQ exchange, from where the event can be consumed.
 */
@Aspect
public class AuditAspect {

    @Pointcut("call(* edu.kit.dama.mdm.audit.util.AuditUtils.audit(..)) && args(pid, caller, agent, resource, category, type, trigger, details)")
    public void auditPointcut(final String pid, final String caller, final String agent, final String resource, final String category, final AuditEvent.TYPE type, final AuditEvent.TRIGGER trigger, final AuditDetail... details) {
    }

    /**
     * Implementation of handling audit information. This method is activated by
     * the pointcut above and contains the implementation for publishing the
     * received audit information.
     *
     * @param pid The pid of the object to collect audit information for.
     * @param caller The calling user.
     * @param agent The agent.
     * @param resource The affected resource, e.g. the URL of the REST endpoint.
     * @param category The category of the audit entry. This category can be
     * used to route audit entries to different sinks.
     * @param type The event type.
     * @param trigger The event trigger.
     * @param details Optional list of audit details, e.g. argument information
     * or comments.
     * @param jointPoint the jointpoint.
     *
     * @throws Throwable if something fails.
     */
    @Around("auditPointcut(pid, caller, agent, resource, category, type, trigger, details)")
    public void handleAuditInformation(final String pid, final String caller, final String agent, final String resource, final String category, final AuditEvent.TYPE type, final AuditEvent.TRIGGER trigger, final AuditDetail[] details, final JoinPoint jointPoint) throws Throwable {
        AuditEvent event = AuditEvent.factoryAuditEvent(type, category);
        event.setEventTrigger(trigger);
        event.setPid(pid);
        event.setOwner(caller);
        event.setAgent(agent);
        event.setResource(resource);
        event.addAuditDetails(details);

        AuditManager.getInstance().getPublisher().publish(event);
    }
}
