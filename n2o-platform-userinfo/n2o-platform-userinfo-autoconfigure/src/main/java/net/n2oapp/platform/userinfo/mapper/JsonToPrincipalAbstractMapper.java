package net.n2oapp.platform.userinfo.mapper;

import net.n2oapp.platform.userinfo.UserInfoModel;
import net.n2oapp.security.auth.common.authority.PermissionGrantedAuthority;
import net.n2oapp.security.auth.common.authority.RoleGrantedAuthority;
import net.n2oapp.security.auth.common.authority.SystemGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;

public abstract class JsonToPrincipalAbstractMapper<T extends UserInfoModel> {

    public abstract T map(String principal);

    public Collection<GrantedAuthority> collectAuthority(T userInfo){
        HashSet<GrantedAuthority> authorities = new HashSet<>();
        for (String role : userInfo.roles)
            authorities.add(new RoleGrantedAuthority(role));
        for (String permission : userInfo.permissions)
            authorities.add(new PermissionGrantedAuthority(permission));
        for (String system : userInfo.systems)
            authorities.add(new SystemGrantedAuthority(system));
        return authorities;
    }
}
