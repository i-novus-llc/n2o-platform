package net.n2oapp.platform.jaxrs.api;

import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Модель данных, используемая в демонстрационном REST сервисе
 */
@Validated
public class SomeModel {
    private Long id;
    private String name;
    private Date date;
    private LocalDateTime dateEnd;

    private StringModel stringModel;

    public SomeModel() {
    }

    public SomeModel(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public @NotNull String getName() {
        return this.name;
    }

    public @NotNull Date getDate() {
        return this.date;
    }

    public @NotNull LocalDateTime getDateEnd() {
        return this.dateEnd;
    }

    public @NotNull @Valid StringModel getStringModel() {
        return this.stringModel;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public void setDate(@NotNull Date date) {
        this.date = date;
    }

    public void setDateEnd(@NotNull LocalDateTime dateEnd) {
        this.dateEnd = dateEnd;
    }

    public void setStringModel(@NotNull @Valid StringModel stringModel) {
        this.stringModel = stringModel;
    }
}
