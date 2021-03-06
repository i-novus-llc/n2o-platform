package net.n2oapp.platform.seek;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompositePkEntityRepository extends JpaRepository<CompositePkEntity, CompositePkEntity.Id>, SeekableRepository<CompositePkEntity> {
}
