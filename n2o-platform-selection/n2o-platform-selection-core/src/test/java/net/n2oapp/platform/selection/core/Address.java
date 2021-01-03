package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.api.Selective;

@Selective
public class Address extends BaseModel {

    public String postcode;
    public String region;

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
