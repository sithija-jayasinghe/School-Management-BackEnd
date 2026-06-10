package org.edu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ParentStudentRequest {

    @NotBlank(message = "Relationship type is required")
    @Size(max = 50, message = "Relationship type must not exceed 50 characters")
    private String relationshipType;

    private boolean isPrimaryContact;
    private boolean isEmergencyContact;

    public ParentStudentRequest() {}

    public String getRelationshipType() { return relationshipType; }
    public void setRelationshipType(String relationshipType) { this.relationshipType = relationshipType; }

    public boolean isPrimaryContact() { return isPrimaryContact; }
    public void setPrimaryContact(boolean primaryContact) { isPrimaryContact = primaryContact; }

    public boolean isEmergencyContact() { return isEmergencyContact; }
    public void setEmergencyContact(boolean emergencyContact) { isEmergencyContact = emergencyContact; }
}
