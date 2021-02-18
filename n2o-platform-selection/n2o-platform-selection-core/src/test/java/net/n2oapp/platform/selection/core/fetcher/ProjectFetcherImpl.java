package net.n2oapp.platform.selection.core.fetcher;

import net.n2oapp.platform.selection.core.domain.Project;
import net.n2oapp.platform.selection.core.domain.ProjectFetcher;

public class ProjectFetcherImpl extends BaseModelFetcherImpl<Project> implements ProjectFetcher {

    public ProjectFetcherImpl(Project project) {
        super(project);
    }

    @Override
    public void fetchName(Project model) {
        model.setName(src.getName());
    }

    @Override
    public Project create() {
        return new Project();
    }

}
