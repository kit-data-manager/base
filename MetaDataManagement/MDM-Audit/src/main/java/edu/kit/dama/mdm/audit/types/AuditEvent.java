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
package edu.kit.dama.mdm.audit.types;

import edu.kit.dama.mdm.audit.exception.FormatException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author jejkal
 */
@Entity
public class AuditEvent {

    public enum TYPE {

        CREATION,
        INGESTION,
        METADATA_MODIFICATION,
        CONTENT_MODIFICATION,
        CONTENT_REMOVAL,
        MIGRATION,
        REPLICATION,
        VALIDATION,
        DERIVATIVE_CREATION,
        DELETION,
        DEACCESSION;
    }

    public enum TRIGGER {
        INTERNAL,
        EXTERNAL;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventDate;
    private String pid;
    private String resource;
    @Enumerated(EnumType.STRING)
    private TYPE eventType;
    @Enumerated(EnumType.STRING)
    private TRIGGER eventTrigger;// INTERNAL or EXTERNAL
    private String category;//e.g. audit.digitalObject
    private String agent;
    private String owner;
    //@Column(columnDefinition = "LONGTEXT")//details character varying(1024),
    @Lob
    private String details;
    @Transient
    private final List<AuditDetail> detailsList = new ArrayList<>();

    /**
     * Default constructor.
     */
    public AuditEvent() {
    }

    /**
     * Factory an audit event of the provided type in the provided category.
     * Inside this call, the event date is set to now().
     *
     * @param type The event type.
     * @param category The event category.
     *
     * @return The audit event.
     */
    public static AuditEvent factoryAuditEvent(TYPE type, String category) {
        AuditEvent entry = new AuditEvent();
        entry.setCategory(category);
        entry.setEventType(type);

        entry.setEventDate(new Date());
        return entry;
    }

    /**
     * Create a JSON representation of this audit event.
     *
     * @return The JSON representation.
     */
    public String toJson() {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        StringBuilder json = new StringBuilder();
        json.append("{");
        if (getId() != null) {
            json.append("\"id\"").append(":").append(getId()).append(",");
        }
        json.append("\"owner\"").append(":\"").append(getOwner()).append("\"").append(",");
        json.append("\"eventType\"").append(":\"").append(getEventType()).append("\"").append(",");
        json.append("\"eventTrigger\"").append(":\"").append(getEventTrigger()).append("\"").append(",");
        json.append("\"agent\"").append(":\"").append(getAgent()).append("\"").append(",");
        json.append("\"resource\"").append(":\"").append(getResource()).append("\"").append(",");
        json.append("\"pid\"").append(":\"").append(getPid()).append("\"").append(",");
        json.append("\"category\"").append(":\"").append(getCategory()).append("\"").append(",");
        json.append("\"eventDate\"").append(":\"").append(df.format(getEventDate())).append("\"").append(",");

        json.append("\"details\"").append(":").append(getDetails()).append("").append("}");

        return json.toString();
    }

    /**
     * Create an AuditEvent from the provided JSON string.
     *
     * @param jsonString The json string.
     *
     * @return The AuditEvent
     *
     * @throws FormatException If the format of the event date does not match
     * the pattern yyyy-MM-dd'T'HH:mm:ss'Z'
     */
    public static AuditEvent fromJson(String jsonString) throws FormatException {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        JSONObject o = new JSONObject(jsonString);
        AuditEvent e = new AuditEvent();
        long lId = o.optLong("id", 0);
        if (lId > 0) {
            e.setId(lId);
        }
        e.setOwner(o.optString("owner", null));
        e.setAgent(o.optString("agent", null));
        e.setResource(o.optString("resource", null));
        e.setEventType(TYPE.valueOf(o.getString("eventType")));
        e.setEventTrigger(TRIGGER.valueOf(o.getString("eventTrigger")));
        e.setPid(o.getString("pid"));
        e.setCategory(o.getString("category"));
        String date = o.getString("eventDate");
        try {
            e.setEventDate(df.parse(date));
        } catch (ParseException ex) {
            //invalid date
            throw new FormatException("Format of eventDate " + date + " does not match pattern yyyy-MM-dd'T'HH:mm:ss'Z'", ex);
        }

        JSONArray detailsArray = o.optJSONArray("details");
        if (detailsArray != null) {
            List<AuditDetail> auditDetails = new ArrayList<>();
            for (int i = 0; i < detailsArray.length(); i++) {
                JSONObject detail = detailsArray.getJSONObject(i);
                AuditDetail adetail = AuditDetail.initFromJson(detail);
                auditDetails.add(adetail);
            }
            e.addAuditDetails(auditDetails.toArray(new AuditDetail[]{}));
        }
        return e;
    }

    /**
     * Get the id.
     *
     * @return The id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the id.
     *
     * @param id The id.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the event date.
     *
     * @return The event date.
     */
    public Date getEventDate() {
        return eventDate;
    }

    /**
     * Set the event date.
     *
     * @param eventDate The event date.
     */
    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    /**
     * Get the pid.
     *
     * @return The pid.
     */
    public String getPid() {
        return pid;
    }

    /**
     * Set the pid.
     *
     * @param pid The pid.
     */
    public void setPid(String pid) {
        this.pid = pid;
    }

    /**
     * Set the resource.
     *
     * @param resource The resource.
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * Get the resource.
     *
     * @return The resource.
     */
    public String getResource() {
        return resource;
    }

    /**
     * Get the event type.
     *
     * @return The event type.
     */
    public TYPE getEventType() {
        return eventType;
    }

    /**
     * Set the event type.
     *
     * @param eventType The event type.
     */
    public void setEventType(TYPE eventType) {
        this.eventType = eventType;
    }

    public void setEventTrigger(TRIGGER eventTrigger) {
        this.eventTrigger = eventTrigger;
    }

    public TRIGGER getEventTrigger() {
        return eventTrigger;
    }

    /**
     * Get the category.
     *
     * @return The category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Set the category.
     *
     * @param category The category.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Get the owner.
     *
     * @return The owner.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Set the owner.
     *
     * @param owner The owner.
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Set the agent.
     *
     * @param agent The agent.
     */
    public void setAgent(String agent) {
        this.agent = agent;
    }

    /**
     * Get the agent.
     *
     * @return The agent.
     */
    public String getAgent() {
        return agent;
    }

    /**
     * Get event details as JSON string.
     *
     * @return The event details.
     */
    public String getDetails() {
        return details;
    }

    /**
     * Set event details as JSON string.
     *
     * @param details The event details.
     */
    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * Add one or more audit details.
     *
     * @param details An array of audit details.
     */
    public void addAuditDetails(AuditDetail... details) {
        if (details != null) {
            detailsList.addAll(Arrays.asList(details));
            buildDetails();
        }
    }

    /**
     * Add one audit details.
     *
     * @param detail An audit details.
     */
    public void addAuditDetail(AuditDetail detail) {
        addAuditDetails(detail);
    }

    /**
     * Add an audit detail of type ARGUMENT. Therefor, at least type and name
     * should be provided.
     *
     * @param type The argument type.
     * @param name The argument name.
     */
    public void addArgumentDetail(String type, String name) {
        addAuditDetail(AuditDetail.factoryArgumentDetail(type, name));
    }

    /**
     * Add an audit detail of type ARGUMENT. Therefor, at least type and name
     * should be provided. The value is options.
     *
     * @param type The argument type.
     * @param name The argument name.
     * @param value The argument value.
     */
    public void addArgumentDetail(String type, String name, String value) {
        addAuditDetail(AuditDetail.factoryArgumentDetail(type, name, value));
    }

    /**
     * Add an audit detail of type COMMENT. Therefor, only value must be
     * provided.
     *
     * @param value The comment value.
     */
    public void addCommentDetail(String value) {
        addAuditDetail(AuditDetail.factoryCommentDetail(value));
    }

    /**
     * Convert a list of audit details to a JSON representation.
     *
     * @param details The list of details.
     *
     * @return The JSON string.
     */
    public static String detailsToJson(List<AuditDetail> details) {
        JSONArray a = new JSONArray();
        for (AuditDetail detail : details) {
            a.put(detail.toJson());
        }
        return a.toString();
    }

    /**
     * Build the json representation of the details list.
     */
    private void buildDetails() {
        setDetails(detailsToJson(detailsList));
    }

    @Override
    public String toString() {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        StringBuilder b = new StringBuilder();
        b.append("Pid: ").append(getPid()).append("\n");
        b.append("EventType: ").append(getEventType()).append("\n");
        b.append("EventDate: ").append(df.format(getEventDate())).append("\n");
        b.append("Category: ").append(getCategory()).append("\n");
        b.append("Agent: ").append(getAgent()).append("\n");
        b.append("Owner@Resource: ").append(getOwner()).append("@").append(getResource()).append("\n");
        b.append("Details: ").append(getDetails());
        return b.toString();
    }

}
