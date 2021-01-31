package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.api.Selective;

import javax.persistence.Entity;

@Entity
@Selective
public class Contact extends BaseModel {

    public String phone;
    public String email;

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

}
