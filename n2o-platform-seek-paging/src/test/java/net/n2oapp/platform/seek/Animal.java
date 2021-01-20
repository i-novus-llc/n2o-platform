package net.n2oapp.platform.seek;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Animal {

    @Id
    @SequenceGenerator(name = "seq1", sequenceName = "animal_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq1")
    private Integer id;

    @Column
    private String name;

    @Column
    private LocalDate birthDate;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Animal parent;

    @Column
    private BigInteger height;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "favorite_food_id")
    private Food favoriteFood;

    protected Animal() {
    }

    public Animal(Integer id, String name, LocalDate birthDate, Animal parent, BigInteger height, Food favoriteFood) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.parent = parent;
        this.height = height;
        this.favoriteFood = favoriteFood;
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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Animal getParent() {
        return parent;
    }

    public void setParent(Animal parent) {
        this.parent = parent;
    }

    public BigInteger getHeight() {
        return height;
    }

    public void setHeight(BigInteger height) {
        this.height = height;
    }

    public Food getFavoriteFood() {
        return favoriteFood;
    }

    public void setFavoriteFood(Food favoriteFood) {
        this.favoriteFood = favoriteFood;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Animal that = (Animal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Animal{" +
            "getId=" + id +
            ", name='" + name + '\'' +
            ", birthDate=" + birthDate +
            ", parentId=" + parent.getId() +
            ", height=" + height +
            ", favoriteFood=" + favoriteFood +
            '}';
    }

}
