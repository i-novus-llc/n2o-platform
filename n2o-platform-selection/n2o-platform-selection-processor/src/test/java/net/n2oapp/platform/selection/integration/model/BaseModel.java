package net.n2oapp.platform.selection.integration.model;

import net.n2oapp.platform.selection.api.Selective;

import javax.persistence.*;

@Selective
@MappedSuperclass
public class BaseModel {

    @Id
    @SequenceGenerator(name = "seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
    @Access(AccessType.PROPERTY)
    private Integer id;

    protected BaseModel() {
    }

    public BaseModel(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}
