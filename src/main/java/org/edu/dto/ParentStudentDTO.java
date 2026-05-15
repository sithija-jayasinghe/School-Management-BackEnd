package org.edu.dto;

public class ParentStudentDTO {
    private Long id;
    private Long parentId;
    private Long studentId;
    private String relationshipType;
    private boolean isPrimaryContact;
    private boolean isEmergencyContact;

    public ParentStudentDTO() {}

    public ParentStudentDTO(Long id, Long parentId, Long studentId, String relationshipType, boolean isPrimaryContact, boolean isEmergencyContact) {
        this.id = id;
        this.parentId = parentId;
        this.studentId = studentId;
        this.relationshipType = relationshipType;
        this.isPrimaryContact = isPrimaryContact;
        this.isEmergencyContact = isEmergencyContact;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getRelationshipType() { return relationshipType; }
    public void setRelationshipType(String relationshipType) { this.relationshipType = relationshipType; }

    public boolean isPrimaryContact() { return isPrimaryContact; }
    public void setPrimaryContact(boolean primaryContact) { isPrimaryContact = primaryContact; }

    public boolean isEmergencyContact() { return isEmergencyContact; }
    public void setEmergencyContact(boolean emergencyContact) { isEmergencyContact = emergencyContact; }
}
