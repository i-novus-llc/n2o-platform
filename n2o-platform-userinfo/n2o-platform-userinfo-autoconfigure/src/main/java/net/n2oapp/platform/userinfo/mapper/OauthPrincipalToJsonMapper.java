package net.n2oapp.platform.userinfo.mapper;

import com.google.gson.Gson;
import net.n2oapp.platform.userinfo.UserInfo;
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
        UserInfo userInfo = userInfoUserNameOnly ? new UserInfo(principal.getUsername()) : new UserInfo(principal);
        return gson.toJson(userInfo);
    }
}
