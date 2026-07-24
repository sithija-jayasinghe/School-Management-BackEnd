package org.edu.config;

import lombok.RequiredArgsConstructor;
import org.edu.entity.House;
import org.edu.repository.HouseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class HouseSeeder implements CommandLineRunner {

    private final HouseRepository houseRepository;

    @Override
    @Transactional
    public void run(String... args) {
        ensureHouse("Red", "#DC2626");
        ensureHouse("Blue", "#2563EB");
        ensureHouse("Green", "#16A34A");
        ensureHouse("Yellow", "#FACC15");
    }

    private void ensureHouse(String name, String colour) {
        houseRepository.findByNameIgnoreCase(name).ifPresentOrElse(house -> {
            house.setName(name);
            house.setColour(colour);
            house.setActive(true);
        }, () -> {
            House house = new House();
            house.setName(name);
            house.setColour(colour);
            house.setActive(true);
            houseRepository.save(house);
        });
    }
}
