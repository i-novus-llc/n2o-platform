package net.n2oapp.platform.loader.server;

import net.n2oapp.platform.loader.server.repository.RepositoryServerLoader;

public class TestRepositoryLoader extends RepositoryServerLoader<TestModel, TestEntity, String> {
    public TestRepositoryLoader(TestRepository repository) {
        super(repository, TestMapper::map, repository::findAllByClient, TestEntity::getCode);
    }

    @Override
    public Class<?> getTargetClass() {
        return TestRepositoryLoader.class;
    }
}
