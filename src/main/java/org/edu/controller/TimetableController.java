package org.edu.controller;

import jakarta.validation.Valid;
import org.edu.dto.TimetableDTO;
import org.edu.service.TimetableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@CrossOrigin(origins = "*")
public class TimetableController {

    @Autowired
    private TimetableService timetableService;

    @PostMapping
    public ResponseEntity<TimetableDTO> createTimetable(@Valid @RequestBody TimetableDTO dto) {
        return new ResponseEntity<>(timetableService.createTimetable(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimetableDTO> updateTimetable(@PathVariable Long id, @Valid @RequestBody TimetableDTO dto) {
        return ResponseEntity.ok(timetableService.updateTimetable(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimetable(@PathVariable Long id) {
        timetableService.deleteTimetable(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimetableDTO> getTimetableById(@PathVariable Long id) {
        return ResponseEntity.ok(timetableService.getTimetableById(id));
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<TimetableDTO>> getClassSchedule(@PathVariable Long classId) {
        return ResponseEntity.ok(timetableService.getClassSchedule(classId));
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<TimetableDTO>> getTeacherSchedule(@PathVariable Long staffId) {
        return ResponseEntity.ok(timetableService.getTeacherSchedule(staffId));
    }
}
