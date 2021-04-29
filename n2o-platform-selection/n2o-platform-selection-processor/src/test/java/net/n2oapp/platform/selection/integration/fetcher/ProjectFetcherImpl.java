package net.n2oapp.platform.selection.integration.fetcher;

import net.n2oapp.platform.selection.integration.model.Project;
import net.n2oapp.platform.selection.integration.model.ProjectFetcher;
import net.n2oapp.platform.selection.integration.model.ProjectSelection;
import org.springframework.lang.NonNull;

public class ProjectFetcherImpl extends BaseModelFetcherImpl<Project, ProjectSelection> implements ProjectFetcher<Project> {

    public ProjectFetcherImpl(Project project) {
        super(project);
    }

    @Override
    public @NonNull Project create() {
        return new Project();
    }

    @Override
    public String fetchName() {
        return src.getName();
    }

}
