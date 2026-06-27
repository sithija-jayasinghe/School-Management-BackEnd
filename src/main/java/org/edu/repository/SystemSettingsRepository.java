package org.edu.repository;

import java.util.Optional;
import org.edu.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {

    Optional<SystemSettings> findTopByOrderByIdAsc();
}
