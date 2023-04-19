package net.n2oapp.platform.userinfo.mapper;

import net.n2oapp.platform.userinfo.UserInfoModel;

public abstract class JsonToPrincipalAbstractMapper<T extends UserInfoModel> {

    public abstract T map(String principal);
}
