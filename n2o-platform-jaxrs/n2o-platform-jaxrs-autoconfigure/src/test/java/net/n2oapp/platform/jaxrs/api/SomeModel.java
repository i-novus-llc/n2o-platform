package net.n2oapp.platform.jaxrs.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Модель данных, используемая в демонстрационном REST сервисе
 */
@Getter
@Setter
@Validated
public class SomeModel {
    private Long id;
    private @NotNull String name;
    private @NotNull Date date;
    private @NotNull LocalDateTime dateEnd;

    @NotNull
    @Valid
    private StringModel stringModel;

    public SomeModel() {
    }

    public SomeModel(Long id) {
        this.id = id;
    }
}
