package net.n2oapp.platform.loader.server;

class TestMapper {
    static TestEntity map(TestModel model, String client) {
        TestEntity entity = new TestEntity();
        entity.setCode(model.getCode());
        entity.setName(model.getName());
        entity.setClient(client);
        return entity;
    }
}
