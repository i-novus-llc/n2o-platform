package net.n2oapp.platform.selection.integration.repository;

import net.n2oapp.platform.selection.integration.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
}
