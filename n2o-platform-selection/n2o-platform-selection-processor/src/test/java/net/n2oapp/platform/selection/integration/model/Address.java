package net.n2oapp.platform.selection.integration.model;

import net.n2oapp.platform.selection.api.Selective;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Selective(prefix = "a")
public class Address extends BaseModel {

    @Column
    private String postcode;

    @Column
    private String region;

    public Address() {
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
