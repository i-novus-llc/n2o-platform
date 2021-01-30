package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.api.Joined;
import net.n2oapp.platform.selection.api.Selective;

import javax.persistence.*;
import java.util.List;

@Entity
@Selective
public class Employee extends BaseModel {

    @Column
    public String name;

    @Joined
    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    public Organisation organisation;

    @Joined(withNestedJoiner = false)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    public List<Contact> contacts;

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

}
