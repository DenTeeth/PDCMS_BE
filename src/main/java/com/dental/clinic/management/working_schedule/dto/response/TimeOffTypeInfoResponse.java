package com.dental.clinic.management.working_schedule.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simplified time-off type info for nested responses (P5.2)
 */
public class TimeOffTypeInfoResponse {

    @JsonProperty("type_id")
    private String typeId;

    @JsonProperty("type_name")
    private String typeName;

    @JsonProperty("is_paid")
    private Boolean isPaid;

    public TimeOffTypeInfoResponse() {
    }

    public TimeOffTypeInfoResponse(String typeId, String typeName, Boolean isPaid) {
        this.typeId = typeId;
        this.typeName = typeName;
        this.isPaid = isPaid;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }
}
