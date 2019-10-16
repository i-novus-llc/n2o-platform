package net.n2oapp.platform.loader.server;

import lombok.Data;
import org.springframework.context.annotation.Primary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
class TestEntity {
    @Id
    private String code;
    @Column
    private String name;
    @Column
    private String client;
}
