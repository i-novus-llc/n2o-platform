package net.n2oapp.platform.userinfo;

import lombok.Getter;
import lombok.Setter;
import net.n2oapp.security.auth.common.OauthUser;
import net.n2oapp.security.auth.common.authority.PermissionGrantedAuthority;
import net.n2oapp.security.auth.common.authority.RoleGrantedAuthority;
import net.n2oapp.security.auth.common.authority.SystemGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.nonNull;

public class UserInfoModel {

    public String surname;
    public String firstName;
    public String patronymic;
    public String email;
    public String organization;
    public String region;
    public String department;
    public String departmentName;
    public String userLevel;
    public String accountId;
    public String username;

    public Set<String> systems = new HashSet<>();
    public Set<String> roles = new HashSet<>();
    public Set<String> permissions = new HashSet<>();

    public transient Collection<? extends GrantedAuthority> authorities = new HashSet<>();

    public UserInfoModel(OauthUser user) {
        this.surname = user.getSurname();
        this.firstName = user.getFirstName();
        this.patronymic = user.getPatronymic();
        this.email = user.getEmail();
        this.organization = user.getOrganization();
        this.region = user.getRegion();
        this.department = user.getDepartment();
        this.departmentName = user.getDepartmentName();
        this.userLevel = user.getUserLevel();
        this.accountId = user.getAccountId();
        this.username = user.getUsername();
        this.authorities = user.getAuthorities();
        parseAuthorities(user);
    }

    public UserInfoModel(String username) {
        this.username = username;
    }

    public UserInfoModel(UserInfoModel userInfo) {
        this.surname = userInfo.surname;
        this.firstName = userInfo.firstName;
        this.patronymic = userInfo.patronymic;
        this.email = userInfo.email;
        this.organization = userInfo.organization;
        this.region = userInfo.region;
        this.department = userInfo.department;
        this.departmentName = userInfo.departmentName;
        this.userLevel = userInfo.userLevel;
        this.accountId = userInfo.accountId;
        this.username = userInfo.username;
        this.roles = new HashSet<>(userInfo.roles);
        this.systems = new HashSet<>(userInfo.systems);
        this.permissions = new HashSet<>(userInfo.permissions);
        this.authorities = new HashSet<>(userInfo.authorities);
    }

    private void parseAuthorities(OauthUser user) {
        if (nonNull(user.getAuthorities())) {
            for (GrantedAuthority authority : user.getAuthorities()) {
                if (authority instanceof RoleGrantedAuthority)
                    roles.add(((RoleGrantedAuthority) authority).getRole());
                if (authority instanceof PermissionGrantedAuthority)
                    permissions.add(((PermissionGrantedAuthority) authority).getPermission());
                if (authority instanceof SystemGrantedAuthority)
                    systems.add(((SystemGrantedAuthority) authority).getSystem());
            }
        }
    }
}
