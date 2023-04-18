package net.n2oapp.platform.userinfo.mapper;

import net.n2oapp.platform.userinfo.UserInfo;

public abstract class JsonToPrincipalAbstractMapper<T extends UserInfo> {

    public abstract T map(String principal);
}
