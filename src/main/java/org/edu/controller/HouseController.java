package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.HouseDTO;
import org.edu.service.HouseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/houses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Houses", description = "Manage school houses and colours")
@SecurityRequirement(name = "bearerAuth")
public class HouseController {

    private final HouseService houseService;

    @PostMapping
    @Operation(summary = "Create a house")
    public HouseDTO createHouse(@Valid @RequestBody HouseDTO dto) {
        return houseService.createHouse(dto);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a house")
    public HouseDTO updateHouse(@PathVariable Long id, @Valid @RequestBody HouseDTO dto) {
        return houseService.updateHouse(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a house")
    public void deleteHouse(@PathVariable Long id) {
        houseService.deleteHouse(id);
    }

    @GetMapping
    @Operation(summary = "List houses")
    public Page<HouseDTO> getHouses(Pageable pageable) {
        return houseService.getAllHouses(pageable);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "List active houses")
    public List<HouseDTO> getActiveHouses() {
        return houseService.getActiveHouses();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a house by id")
    public HouseDTO getHouseById(@PathVariable Long id) {
        return houseService.getHouseById(id);
    }
}
