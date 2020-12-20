package net.n2oapp.platform.selection.core;

public class EmployeeEntity implements Mapper<EmployeeDto> {

    public final String name;
    public final String surname;

    public EmployeeEntity(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    @Override
    public EmployeeDto create() {
        return new EmployeeDto();
    }

    @SelectionKey("name")
    public void setName(EmployeeDto employee) {
        employee.setName(name);
    }

    @SelectionKey("surname")
    public void setSurname(EmployeeDto employee) {
        employee.setSurname(surname);
    }

}
