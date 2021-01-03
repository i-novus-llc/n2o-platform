package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.api.Selective;

@Selective
public class Organisation extends BaseModel {

    public Address legalAddress;
    public Address factualAddress;

    public Address getLegalAddress() {
        return legalAddress;
    }

    public void setLegalAddress(Address legalAddress) {
        this.legalAddress = legalAddress;
    }

    public Address getFactualAddress() {
        return factualAddress;
    }

    public void setFactualAddress(Address factualAddress) {
        this.factualAddress = factualAddress;
    }

}
