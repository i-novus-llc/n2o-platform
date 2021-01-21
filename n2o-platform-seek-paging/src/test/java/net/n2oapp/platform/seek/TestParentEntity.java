package net.n2oapp.platform.seek;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class TestParentEntity {

    @Id
    @SequenceGenerator(name = "seq1", sequenceName = "seq1")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq1")
    private Integer id;

    @Column
    private Integer field1;

    @Column
    private Integer field2;

    @Fetch(FetchMode.SELECT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private TestParentEntity parent;

    @Column
    private Integer field3;

    @Fetch(FetchMode.SELECT)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "child_id")
    private TestChildEntity child;

    protected TestParentEntity() {
    }

    public TestParentEntity(Integer id, Integer field1, Integer field2, TestParentEntity parent, Integer field3, TestChildEntity child) {
        this.id = id;
        this.field1 = field1;
        this.field2 = field2;
        this.parent = parent;
        this.field3 = field3;
        this.child = child;
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

    public Integer getField2() {
        return field2;
    }

    public void setField2(Integer field2) {
        this.field2 = field2;
    }

    public TestParentEntity getParent() {
        return parent;
    }

    public void setParent(TestParentEntity parent) {
        this.parent = parent;
    }

    public Integer getField3() {
        return field3;
    }

    public void setField3(Integer field3) {
        this.field3 = field3;
    }

    public TestChildEntity getChild() {
        return child;
    }

    public void setChild(TestChildEntity child) {
        this.child = child;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestParentEntity that = (TestParentEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TestParentEntity{" +
                "id=" + id +
                ", field1=" + field1 +
                ", field2=" + field2 +
                ", parentId=" + (parent == null ? null : parent.id) +
                ", field3=" + field3 +
                ", child=" + child +
                '}';
    }

}