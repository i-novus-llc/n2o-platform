package net.n2oapp.platform.initializr;

import io.spring.initializr.metadata.*;
import io.spring.initializr.web.support.DefaultInitializrMetadataProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class N2oInitializrMetadataProvider implements InitializrMetadataProvider {
    private final InitializrMetadata metadata;
    private final RestTemplate restTemplate;

    public N2oInitializrMetadataProvider(InitializrMetadata metadata, RestTemplate restTemplate) {
        this.metadata = metadata;
        this.restTemplate = restTemplate;
    }

    @Cacheable(value = "initializr.metadata", key = "'metadata'")
    public InitializrMetadata get() {
        this.updateInitializrMetadata(this.metadata);
        return this.metadata;
    }

    private void updateInitializrMetadata(InitializrMetadata metadata) {
        List<DependencyGroup> groups = fetchSpringIoDependencies();
        metadata.getDependencies().merge(groups);
    }

    private List<DependencyGroup> fetchSpringIoDependencies() {
        List<DependencyGroup> dependencyGroups = new ArrayList<>();
        JSONObject json = new JSONObject(restTemplate.getForObject("https://start.spring.io", Map.class));
        JSONArray groups = json.getJSONObject("dependencies").getJSONArray("values");
        for (int i = 0; i < groups.length(); i++) {
            JSONObject jsonGroup = groups.getJSONObject(i);
            DependencyGroup dependencyGroup = new DependencyGroup();
            dependencyGroup.setName(jsonGroup.getString("name"));
            JSONArray jsonDependencies = jsonGroup.getJSONArray("values");
            for (int j = 0; j < jsonDependencies.length(); j++) {
                JSONObject jsonDependency = jsonDependencies.getJSONObject(j);
                Dependency dependency = new Dependency();
                dependency.setId(jsonDependency.getString("id"));
                dependency.setName(jsonDependency.optString("name"));
                dependency.setDescription(jsonDependency.optString("description"));
                dependency.setVersionRange(jsonDependency.optString("versionRange"));
                dependencyGroup.getContent().add(dependency);
            }
            dependencyGroups.add(dependencyGroup);
        }
        return dependencyGroups;
    }
}
