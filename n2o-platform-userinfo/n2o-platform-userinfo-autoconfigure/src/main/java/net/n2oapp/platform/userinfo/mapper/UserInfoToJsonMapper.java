package net.n2oapp.platform.userinfo.mapper;

import com.google.gson.Gson;
import net.n2oapp.platform.userinfo.UserInfo;

public class UserInfoToJsonMapper extends PrincipalToJsonAbstractMapper<UserInfo> {

    protected Gson gson;

    public UserInfoToJsonMapper() {
        gson = new Gson();
    }

    @Override
    public String map(UserInfo principal) {
        UserInfo userInfo = userInfoUserNameOnly ? new UserInfo(principal.username) : new UserInfo(principal);
        return gson.toJson(userInfo);
    }
}
