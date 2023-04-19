package net.n2oapp.platform.userinfo.mapper;

import com.google.gson.Gson;
import net.n2oapp.platform.userinfo.UserInfoModel;

public class UserInfoToJsonMapper extends PrincipalToJsonAbstractMapper<UserInfoModel> {

    protected Gson gson;

    public UserInfoToJsonMapper() {
        gson = new Gson();
    }

    @Override
    public String map(UserInfoModel principal) {
        UserInfoModel userInfo = userInfoUserNameOnly ? new UserInfoModel(principal.username) : new UserInfoModel(principal);
        return gson.toJson(userInfo);
    }
}
