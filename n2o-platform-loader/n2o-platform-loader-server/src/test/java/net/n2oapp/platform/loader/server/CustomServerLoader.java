package net.n2oapp.platform.loader.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
class CustomServerLoader extends BaseServerLoader<TestModel, TestEntity> {
    @Autowired
    private TestRepository repository;

    @Override
    protected List<TestEntity> map(List<TestModel> models, String subject) {
        List<TestEntity> entities = new ArrayList<>();
        for (TestModel model : models) {
            TestEntity entity = new TestEntity();
            entity.setClient(subject);
            entity.setName(model.getName());
            entity.setCode(model.getCode());
            entities.add(entity);
        }
        return entities;
    }

    @Override
    protected List<TestEntity> findAllBySubject(String subject) {
        return repository.findAllByClient(subject);
    }

    @Override
    protected boolean contains(List<TestEntity> entities, TestEntity candidate) {
        for (TestEntity entity : entities) {
            if (entity.getCode().equals(candidate.getCode()))
                return true;
        }
        return false;
    }

    @Override
    protected void create(List<TestEntity> entities) {
        repository.saveAll(entities);
    }

    @Override
    protected void update(List<TestEntity> entities) {
        repository.saveAll(entities);
    }

    @Override
    protected void delete(List<TestEntity> entities) {
        repository.deleteAll(entities);
    }

    @Override
    public String getTarget() {
        return "load3";
    }

    @Override
    public Class<TestModel> getDataType() {
        return TestModel.class;
    }
}
