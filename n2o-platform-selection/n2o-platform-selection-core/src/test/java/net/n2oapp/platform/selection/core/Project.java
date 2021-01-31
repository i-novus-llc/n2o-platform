package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.api.Joined;
import net.n2oapp.platform.selection.api.Selective;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
@Selective
public class Project extends BaseModel {

    @Joined
    @ManyToMany(mappedBy = "projects")
    private Set<Employee> workers = new HashSet<>();

    @Column
    private String name;

    public Set<Employee> getWorkers() {
        return workers;
    }

    public void setWorkers(Set<Employee> workers) {
        this.workers = workers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
