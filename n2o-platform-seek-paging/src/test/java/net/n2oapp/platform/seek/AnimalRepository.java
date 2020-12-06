package net.n2oapp.platform.seek;

import org.springframework.data.jpa.repository.JpaRepository;

@PivotProvided(by = AnimalPivotProvider.class)
public interface AnimalRepository extends JpaRepository<Animal, Integer>, SeekableRepository<Animal> {
}
