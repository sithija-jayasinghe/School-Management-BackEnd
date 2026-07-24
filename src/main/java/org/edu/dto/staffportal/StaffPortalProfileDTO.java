package org.edu.dto.staffportal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.EmploymentType;
import org.edu.util.StaffCategory;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffPortalProfileDTO {

    private Long staffId;
    private Long userId;
    private String staffCode;
    private String name;
    private String email;
    private String phoneNumber;
    private String designation;
    private StaffCategory staffCategory;
    private EmploymentType employmentType;
    private String department;
    private LocalDate joiningDate;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
