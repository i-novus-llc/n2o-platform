package net.n2oapp.platform.loader.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
class SimpleServerLoader implements ServerLoader<List<TestModel>> {
    @Autowired
    private TestRepository repository;

    @Override
    public void load(List<TestModel> data, String subject) {
        List<String> loaded = new ArrayList<>();
        for (TestModel model : data) {
            TestEntity entity = TestMapper.map(model, subject);
            repository.save(entity);
            loaded.add(entity.getCode());
        }
        for (TestEntity old : repository.findAllByClient(subject)) {
            if (!loaded.contains(old.getCode()))
                repository.deleteById(old.getCode());
        }

    }

}
