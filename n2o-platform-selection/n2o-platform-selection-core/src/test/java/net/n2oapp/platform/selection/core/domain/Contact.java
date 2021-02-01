package net.n2oapp.platform.selection.core.domain;

import net.n2oapp.platform.selection.api.SelectionIgnore;
import net.n2oapp.platform.selection.api.Selective;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Selective
public class Contact extends BaseModel {

    public String phone;
    public String email;

    @JoinColumn
    @SelectionIgnore
    @ManyToOne(optional = false)
    public Employee owner;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Employee getOwner() {
        return owner;
    }

    public void setOwner(Employee owner) {
        this.owner = owner;
    }

}
