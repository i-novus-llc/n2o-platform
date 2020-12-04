package net.n2oapp.platform.seek;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class AnimalEntity {

    @Id
    @SequenceGenerator(name = "seq", sequenceName = "animal_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
    private Integer id;

    @Column
    private String name;

    @Column
    private LocalDate birthDate;

    protected AnimalEntity() {
    }

    public AnimalEntity(Integer id, String name, LocalDate birthDate) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
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

}
