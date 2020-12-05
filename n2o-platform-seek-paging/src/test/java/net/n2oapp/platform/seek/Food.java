package net.n2oapp.platform.seek;

import javax.persistence.*;

@Entity
public class Food {

    @Id
    @SequenceGenerator(name = "seq2", sequenceName = "food_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq2")
    private Integer id;

    @Column
    private String name;

    protected Food() {
    }

    public Food(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Food{" +
            "id=" + id +
            ", name='" + name + '\'' +
            '}';
    }

}
