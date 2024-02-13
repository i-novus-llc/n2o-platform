package net.n2oapp.platform.seek;

import jakarta.persistence.*;

@Entity
public class TestChildEntity {

    @Id
    @SequenceGenerator(name = "seq2", sequenceName = "seq2")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq2")
    private Integer id;

    @Column
    private Integer field1;

    protected TestChildEntity() {
    }

    public TestChildEntity(Integer id, Integer field1) {
        this.id = id;
        this.field1 = field1;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getField1() {
        return field1;
    }

    public void setField1(Integer field1) {
        this.field1 = field1;
    }

    @Override
    public String toString() {
        return "TestChildEntity{" +
                "id=" + id +
                ", field1=" + field1 +
                '}';
    }

}
