package net.n2oapp.platform.userinfo.mapper;

import com.google.gson.Gson;
import net.n2oapp.platform.userinfo.UserInfoModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserInfoToJsonMapper<T extends UserInfoModel> extends PrincipalToJsonAbstractMapper<T> {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoToJsonMapper.class);

    protected Gson gson;

    public UserInfoToJsonMapper() {
        gson = new Gson();
    }

    public UserInfoToJsonMapper(Gson gson) {
        this.gson = gson;
    }

    @Override
    public String map(T principal) {
        T userInfo;
        try {
            userInfo = userInfoUserNameOnly ?
                    (T) principal.getClass().getDeclaredConstructor(String.class).newInstance(principal.username) :
                    principal;
        } catch (Exception e) {
            logger.error("Exception with deserialization of UserInfoModel");
            throw new RuntimeException(e);
        }
        return gson.toJson(userInfo);
    }
}
