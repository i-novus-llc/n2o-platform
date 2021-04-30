package net.n2oapp.platform.seek;

import org.springframework.data.jpa.repository.JpaRepository;

@NullabilityProvided(by = TestParentEntityNullabilityProvider.class)
public interface TestParentEntityRepository extends JpaRepository<TestParentEntity, Integer>, SeekableRepository<TestParentEntity> {
}
