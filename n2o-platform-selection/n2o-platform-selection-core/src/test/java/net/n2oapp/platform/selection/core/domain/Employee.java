package net.n2oapp.platform.selection.core.domain;

import net.n2oapp.platform.selection.api.Joined;
import net.n2oapp.platform.selection.api.Selective;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Selective
public class Employee extends BaseModel {

    @Column
    private String name;

    @Joined
    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private Organisation organisation;

    @Joined(withNestedJoiner = false)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private List<Contact> contacts;

    @JoinTable
    @Joined(withNestedJoiner = false)
    @ManyToMany
    private Set<Project> projects = new HashSet<>();

    @JoinColumn
    @Joined(withNestedJoiner = false)
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Passport passport;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    public Passport getPassport() {
        return passport;
    }

    public void setPassport(Passport passport) {
        this.passport = passport;
    }

}
