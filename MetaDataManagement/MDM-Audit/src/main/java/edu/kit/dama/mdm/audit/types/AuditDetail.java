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

import org.json.JSONObject;

/**
 *
 * @author jejkal
 */
public final class AuditDetail {

    public enum TYPE {
        ARGUMENT,
        COMMENT;
    }
    private TYPE detailType;
    private String dataType;
    private String name;
    private String value;

    public static AuditDetail initFromJson(JSONObject object) {
        return AuditDetail.fromJson(object);
    }

    /**
     * Default constructor.
     */
    AuditDetail() {
    }

    /**
     * Create an audit detail to describe an argument.
     *
     * @param dataType The argument data type.
     * @param name The argument name.
     * @param value The (serialized) argument value.
     *
     * @return An AuditDetail object of type TYPE.ARGUMENT
     */
    public static AuditDetail factoryArgumentDetail(String dataType, String name, String value) {
        if (dataType == null || name == null) {
            throw new IllegalArgumentException("Arguments 'type' and 'name' must not be null for DETAIL_TYPE.ARGUMENT.");
        }
        AuditDetail result = new AuditDetail();
        result.setDetailType(TYPE.ARGUMENT);
        result.setDataType(dataType);
        result.setName(name);
        result.setValue(value);
        return result;
    }

    /**
     * Create an audit detail to describe an argument.
     *
     * @param dataType The argument data type.
     * @param name The argument name.
     *
     * @return An AuditDetail object of type TYPE.ARGUMENT
     */
    public static AuditDetail factoryArgumentDetail(String dataType, String name) {
        if (dataType == null || name == null) {
            throw new IllegalArgumentException("Arguments 'type' and 'name' must not be null for DETAIL_TYPE.ARGUMENT.");
        }
        AuditDetail result = new AuditDetail();

        result.setDetailType(TYPE.ARGUMENT);
        result.setDataType(dataType);
        result.setName(name);
        return result;
    }

    /**
     * Create an audit detail to describe a comment.
     *
     * @param value The value of the comment.
     *
     * @return An AuditDetail object of type TYPE.COMMENT.
     */
    public static AuditDetail factoryCommentDetail(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Arguments 'type' and 'name' must not be null for DETAIL_TYPE.COMMENT.");
        }
        AuditDetail result = new AuditDetail();
        result.setDetailType(TYPE.COMMENT);
        result.setValue(value);
        return result;
    }

    /**
     * Get the data type.
     *
     * @return The data type.
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Set the data type.
     *
     * @param type The data type.
     */
    public void setDataType(String type) {
        this.dataType = type;
    }

    /**
     * Get the name.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     *
     * @param name The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the value.
     *
     * @return The value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the name.
     *
     * @param value The value.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Set the detail type.
     *
     * @param detailType The detail type.
     */
    public void setDetailType(TYPE detailType) {
        this.detailType = detailType;
    }

    /**
     * Get the detail type.
     *
     * @return The detail type.
     */
    public TYPE getDetailType() {
        return detailType;
    }

    /**
     * Convert the AuditDetail into its JSON representation.
     *
     * @return The JSON object representing this AuditDetail.
     */
    public JSONObject toJson() {
        JSONObject fieldElement = new JSONObject();
        fieldElement.putOnce("detailType", detailType.toString());
        switch (detailType) {
            case ARGUMENT:
                fieldElement.putOnce("dataType", dataType);
                fieldElement.putOnce("name", name);
                fieldElement.putOpt("value", value);
                break;
            case COMMENT:
                fieldElement.putOnce("value", value);
                break;
        }
        return fieldElement;
    }

    /**
     * Convert the provided JSONObject into its AuditDetail representation.
     *
     * @param object The JSONObject.
     *
     * @return The AuditDetail.
     */
    public static AuditDetail fromJson(JSONObject object) {
        AuditDetail detail = new AuditDetail();
        detail.setDetailType(TYPE.valueOf(object.optString("detailType", null)));
        String dataType = null;
        String name = null;
        String value = null;
        switch (detail.getDetailType()) {
            case ARGUMENT:
                dataType = object.getString("dataType");
                name = object.getString("name");
                value = object.optString("value", null);
                break;
            case COMMENT:
                value = object.getString("value");
                break;
        }

        detail.setDataType(dataType);
        detail.setName(name);
        detail.setValue(value);

        return detail;
    }

    @Override
    public String toString() {
        return getDetailType() + ": " + getDataType() + ", " + getName() + ", " + getValue();
    }

}
