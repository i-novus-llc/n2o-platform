package net.n2oapp.platform.loader.server;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface TestRepository extends CrudRepository<TestEntity, String> {
    List<TestEntity> findAllByClient(String client);
}
