package net.n2oapp.platform.loader.server;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
class TestEntity {
    @Id
    private String code;
    @Column
    private String name;
    @Column
    private String client;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }
}
