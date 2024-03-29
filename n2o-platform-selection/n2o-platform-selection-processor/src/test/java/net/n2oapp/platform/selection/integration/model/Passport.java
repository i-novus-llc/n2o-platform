package net.n2oapp.platform.selection.integration.model;

import net.n2oapp.platform.selection.api.Selective;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
@Selective
public class Passport extends BaseModel {

    @Column
    private String series;

    @Column
    private String number;

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

}
