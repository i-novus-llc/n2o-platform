package net.n2oapp.platform.userinfo.mapper;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
@Setter
@Getter
public abstract class PrincipalToJsonAbstractMapper<T> {

    @Value("${n2o.platform.userinfo.username-only:false}")
    protected Boolean userInfoUserNameOnly;

    public abstract String map(T principal);
}
