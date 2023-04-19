package net.n2oapp.platform.userinfo.mapper;

import com.google.gson.Gson;
import net.n2oapp.platform.userinfo.UserInfoModel;
import net.n2oapp.security.auth.common.OauthUser;

public class OauthPrincipalToJsonMapper extends PrincipalToJsonAbstractMapper<OauthUser> {

    protected Gson gson;

    public OauthPrincipalToJsonMapper() {
        gson = new Gson();
    }

    public OauthPrincipalToJsonMapper(Gson gson) {
        this.gson = gson;
    }

    public String map(OauthUser principal) {
        UserInfoModel userInfo = userInfoUserNameOnly ? new UserInfoModel(principal.getUsername()) : new UserInfoModel(principal);
        return gson.toJson(userInfo);
    }
}
