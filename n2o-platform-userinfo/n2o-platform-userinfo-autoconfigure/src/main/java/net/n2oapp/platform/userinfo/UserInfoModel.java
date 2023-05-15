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

@Getter
@Setter
public class UserInfoModel {

    private String surname;
    private String firstName;
    private String patronymic;
    private String email;
    private String organization;
    private String region;
    private String department;
    private String departmentName;
    private String userLevel;
    private String accountId;
    private String username;

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
        this.surname = userInfo.getSurname();
        this.firstName = userInfo.getFirstName();
        this.patronymic = userInfo.getPatronymic();
        this.email = userInfo.getEmail();
        this.organization = userInfo.getOrganization();
        this.region = userInfo.getRegion();
        this.department = userInfo.getDepartment();
        this.departmentName = userInfo.getDepartmentName();
        this.userLevel = userInfo.getUserLevel();
        this.accountId = userInfo.getAccountId();
        this.username = userInfo.getUsername();
        this.roles = new HashSet<>(userInfo.getRoles());
        this.systems = new HashSet<>(userInfo.getSystems());
        this.permissions = new HashSet<>(userInfo.getPermissions());
        this.authorities = new HashSet<>(userInfo.getAuthorities());
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
