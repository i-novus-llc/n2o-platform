package net.n2oapp.platform.selection.core;

public class EmployeeSelection implements Selection<EmployeeDto> {

    @SelectionKey("name")
    public SelectionEnum selectName;

    @SelectionKey("surname")
    public SelectionEnum selectSurname;

    public SelectionEnum getSelectName() {
        return selectName;
    }

    public void setSelectName(SelectionEnum selectName) {
        this.selectName = selectName;
    }

    public SelectionEnum getSelectSurname() {
        return selectSurname;
    }

    public void setSelectSurname(SelectionEnum selectSurname) {
        this.selectSurname = selectSurname;
    }

}
