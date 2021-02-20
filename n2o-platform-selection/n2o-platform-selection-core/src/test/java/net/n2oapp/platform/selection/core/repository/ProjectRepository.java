package net.n2oapp.platform.selection.core.repository;

import net.n2oapp.platform.selection.core.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
}
