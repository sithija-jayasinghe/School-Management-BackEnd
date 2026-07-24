package org.edu.service;

import java.util.List;
import org.edu.dto.HouseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HouseService {

    HouseDTO createHouse(HouseDTO dto);

    HouseDTO updateHouse(Long id, HouseDTO dto);

    void deleteHouse(Long id);

    HouseDTO getHouseById(Long id);

    Page<HouseDTO> getAllHouses(Pageable pageable);

    List<HouseDTO> getActiveHouses();
}
