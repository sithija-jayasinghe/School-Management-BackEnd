package org.edu.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.HouseDTO;
import org.edu.entity.House;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.HouseMapper;
import org.edu.repository.HouseRepository;
import org.edu.repository.StudentRepository;
import org.edu.service.HouseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class HouseServiceImpl implements HouseService {

    private final HouseRepository houseRepository;
    private final StudentRepository studentRepository;
    private final HouseMapper houseMapper;

    @Override
    public HouseDTO createHouse(HouseDTO dto) {
        if (houseRepository.existsByNameIgnoreCase(dto.getName().trim())) {
            throw new IllegalStateException("House already exists with name: " + dto.getName());
        }

        House house = houseMapper.toEntity(dto);
        house.setName(dto.getName().trim());
        house.setColour(dto.getColour().trim());
        house.setActive(true);
        return toDTO(houseRepository.save(house));
    }

    @Override
    public HouseDTO updateHouse(Long id, HouseDTO dto) {
        House house = getActiveHouse(id);
        houseRepository.findByNameIgnoreCase(dto.getName().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalStateException("House already exists with name: " + dto.getName());
                });

        houseMapper.updateEntityFromDTO(dto, house);
        house.setName(house.getName().trim());
        house.setColour(house.getColour().trim());
        return toDTO(house);
    }

    @Override
    public void deleteHouse(Long id) {
        House house = houseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("House not found with id: " + id));
        if (!house.isActive()) {
            throw new IllegalStateException("House already inactive");
        }
        house.setActive(false);
    }

    @Override
    @Transactional(readOnly = true)
    public HouseDTO getHouseById(Long id) {
        return toDTO(getActiveHouse(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HouseDTO> getAllHouses(Pageable pageable) {
        return houseRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HouseDTO> getActiveHouses() {
        return houseRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toDTO)
                .toList();
    }

    private House getActiveHouse(Long id) {
        return houseRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("House not found with id: " + id));
    }

    private HouseDTO toDTO(House house) {
        HouseDTO dto = houseMapper.toDTO(house);
        dto.setStudentCount(studentRepository.countByAssignedHouseIdAndActiveTrue(house.getId()));
        return dto;
    }
}
