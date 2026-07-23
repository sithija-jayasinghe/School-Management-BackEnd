package org.edu.repository;

import java.util.List;
import java.util.Optional;
import org.edu.entity.House;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseRepository extends JpaRepository<House, Long> {

    Optional<House> findByIdAndActiveTrue(Long id);

    Optional<House> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<House> findByActiveTrueOrderByNameAsc();
}
