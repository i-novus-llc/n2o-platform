package net.n2oapp.platform.userinfo.mapper;

import org.springframework.beans.factory.annotation.Value;

public abstract class PrincipalToJsonAbstractMapper<T> {

    @Value("${n2o.platform.userinfo.username-only:false}")
    protected Boolean userInfoUserNameOnly;

    public abstract String map(T principal);

    public void setUserInfoUserNameOnly(Boolean userInfoUserNameOnly) {
        this.userInfoUserNameOnly = userInfoUserNameOnly;
    }
}
