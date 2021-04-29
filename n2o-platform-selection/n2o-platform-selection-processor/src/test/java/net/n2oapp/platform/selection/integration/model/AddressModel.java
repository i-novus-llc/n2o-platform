package net.n2oapp.platform.selection.integration.model;

import net.n2oapp.platform.selection.api.Selective;

import java.util.List;

@Selective
public class AddressModel<A extends Address> extends AModel<A, A> {

    List<A> aList;

    public List<A> getAList() {
        return aList;
    }

    public void setAList(List<A> aList) {
        this.aList = aList;
    }

    @Selective
    static class AdddrModel<A extends Model<?, ?>> extends AddressModel<Address> {

        A abc;

        public A getAbc() {
            return abc;
        }

        public void setAbc(A abc) {
            this.abc = abc;
        }

    }

    @Selective
    static class AbcModel extends AdddrModel<AdddrModel<AdddrModel<?>>> {

    }

}
