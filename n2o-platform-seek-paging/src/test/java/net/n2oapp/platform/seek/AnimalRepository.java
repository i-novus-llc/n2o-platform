package net.n2oapp.platform.seek;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalRepository extends JpaRepository<AnimalEntity, Integer>, SeekableRepository<AnimalEntity> {
}
