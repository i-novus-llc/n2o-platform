package net.n2oapp.platform.selection.core.domain;

import net.n2oapp.platform.selection.api.Joined;
import net.n2oapp.platform.selection.api.Selective;

import javax.persistence.*;

@Entity
@Selective(prefix = "")
public class Organisation extends BaseModel {

    @JoinColumn
    @Joined(withNestedJoiner = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Address legalAddress;

    @JoinColumn
    @Joined(withNestedJoiner = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Address factualAddress;

    @Column
    public String name;

    public Organisation() {
    }

    public Organisation(Integer id) {
        super(id);
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
