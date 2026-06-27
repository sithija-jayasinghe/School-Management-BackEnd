package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.edu.dto.TimetableDTO;
import org.edu.service.TimetableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Timetable", description = "Manage class schedules and teacher timetable views")
@SecurityRequirement(name = "bearerAuth")
public class TimetableController {

    @Autowired
    private TimetableService timetableService;

    @PostMapping
    @Operation(summary = "Create a timetable entry")
    public ResponseEntity<TimetableDTO> createTimetable(@Valid @RequestBody TimetableDTO dto) {
        return new ResponseEntity<>(timetableService.createTimetable(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a timetable entry")
    public ResponseEntity<TimetableDTO> updateTimetable(@PathVariable Long id, @Valid @RequestBody TimetableDTO dto) {
        return ResponseEntity.ok(timetableService.updateTimetable(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a timetable entry")
    public ResponseEntity<Void> deleteTimetable(@PathVariable Long id) {
        timetableService.deleteTimetable(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a timetable entry by id")
    public ResponseEntity<TimetableDTO> getTimetableById(@PathVariable Long id) {
        return ResponseEntity.ok(timetableService.getTimetableById(id));
    }

    @GetMapping("/class/{classId}")
    @Operation(summary = "Get the timetable for a class")
    public ResponseEntity<List<TimetableDTO>> getClassSchedule(@PathVariable Long classId) {
        return ResponseEntity.ok(timetableService.getClassSchedule(classId));
    }

    @GetMapping("/staff/{staffId}")
    @Operation(summary = "Get the timetable for a staff member")
    public ResponseEntity<List<TimetableDTO>> getTeacherSchedule(@PathVariable Long staffId) {
        return ResponseEntity.ok(timetableService.getTeacherSchedule(staffId));
    }
}
