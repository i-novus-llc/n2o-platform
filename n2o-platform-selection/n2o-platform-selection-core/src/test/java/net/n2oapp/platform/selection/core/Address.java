package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.api.Selective;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Selective(prefix = "a")
public class Address extends BaseModel {

    @Column
    public String postcode;

    @Column
    public String region;

    protected Address() {
    }

    public Address(Integer id) {
        super(id);
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

}
