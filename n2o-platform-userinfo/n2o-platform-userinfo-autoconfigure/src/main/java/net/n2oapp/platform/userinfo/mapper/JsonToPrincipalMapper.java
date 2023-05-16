package net.n2oapp.platform.userinfo.mapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.n2oapp.platform.userinfo.UserInfoModel;
import net.n2oapp.security.auth.common.authority.PermissionGrantedAuthority;
import net.n2oapp.security.auth.common.authority.RoleGrantedAuthority;
import net.n2oapp.security.auth.common.authority.SystemGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashSet;

public class JsonToPrincipalMapper extends JsonToPrincipalAbstractMapper<UserInfoModel> {

    public UserInfoModel map(String principal) {
        UserInfoModel userInfo = new Gson().fromJson(principal, new TypeToken<UserInfoModel>() {
        }.getType());
        HashSet<GrantedAuthority> authorities = new HashSet<>();
        for (String role : userInfo.roles)
            authorities.add(new RoleGrantedAuthority(role));
        for (String permission : userInfo.permissions)
            authorities.add(new PermissionGrantedAuthority(permission));
        for (String system : userInfo.systems)
            authorities.add(new SystemGrantedAuthority(system));
        userInfo.authorities = authorities;
        return userInfo;
    }
}
