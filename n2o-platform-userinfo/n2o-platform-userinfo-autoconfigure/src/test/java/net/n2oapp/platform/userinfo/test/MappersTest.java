package net.n2oapp.platform.userinfo.test;

import net.n2oapp.platform.userinfo.UserInfoModel;
import net.n2oapp.platform.userinfo.mapper.JsonToPrincipalMapper;
import net.n2oapp.platform.userinfo.mapper.OauthPrincipalToJsonMapper;
import net.n2oapp.platform.userinfo.mapper.UserInfoToJsonMapper;
import net.n2oapp.security.auth.common.OauthUser;
import net.n2oapp.security.auth.common.authority.PermissionGrantedAuthority;
import net.n2oapp.security.auth.common.authority.RoleGrantedAuthority;
import net.n2oapp.security.auth.common.authority.SystemGrantedAuthority;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class MappersTest {

    private final String JSON_PRINCIPAL_FULL = "{\"surname\":\"testSurname\",\"firstName\":\"testFirstName\",\"email\":\"testEmail\",\"accountId\":\"1\",\"username\":\"testUsername\",\"systems\":[\"testSystemGrantedAuthority\"],\"roles\":[\"testRoleGrantedAuthority\"],\"permissions\":[\"testPermissionGrantedAuthority2\",\"testPermissionGrantedAuthority\"]}";

    @Test
    public void jsonToPrincipalMapperTest() {
        JsonToPrincipalMapper mapper = new JsonToPrincipalMapper();
        UserInfoModel userInfo = mapper.map(JSON_PRINCIPAL_FULL);
        assertThat(userInfo.getSurname(), is("testSurname"));
        assertThat(userInfo.getFirstName(), is("testFirstName"));
        assertThat(userInfo.getEmail(), is("testEmail"));
        assertThat(userInfo.getAccountId(), is("1"));
        assertThat(userInfo.getUsername(), is("testUsername"));
        assertThat(userInfo.getPatronymic(), is(nullValue()));
        assertThat(userInfo.getOrganization(), is(nullValue()));
        assertThat(userInfo.getRegion(), is(nullValue()));
        assertThat(userInfo.getDepartment(), is(nullValue()));
        assertThat(userInfo.getDepartmentName(), is(nullValue()));
        assertThat(userInfo.getUserLevel(), is(nullValue()));
        assertThat(userInfo.systems.size(), is(1));
        assertThat(userInfo.roles.size(), is(1));
        assertThat(userInfo.permissions.size(), is(2));
        assertThat(userInfo.authorities.size(), is(4));
        assertThat(userInfo.authorities.stream().filter(a -> a instanceof PermissionGrantedAuthority).collect(Collectors.toList()).size(), is(2));
        assertThat(userInfo.authorities.stream().filter(a -> a instanceof SystemGrantedAuthority).collect(Collectors.toList()).size(), is(1));
        assertThat(userInfo.authorities.stream().filter(a -> a instanceof RoleGrantedAuthority).collect(Collectors.toList()).size(), is(1));
    }

    @Test
    public void oauthPrincipalToJsonMapperTest() {
        OauthPrincipalToJsonMapper mapper = new OauthPrincipalToJsonMapper();
        mapper.setUserInfoUserNameOnly(false);
        OidcIdToken oidcIdToken = new OidcIdToken("test_token_value", Instant.MIN, Instant.MAX, Map.of("sub", "sub"));
        OauthUser oauthUser = new OauthUser("testUsername", List.of(new RoleGrantedAuthority("testRoleGrantedAuthority"), new SystemGrantedAuthority("testSystemGrantedAuthority"), new PermissionGrantedAuthority("testPermissionGrantedAuthority2"), new PermissionGrantedAuthority("testPermissionGrantedAuthority")), oidcIdToken);
        oauthUser.setEmail("testEmail");
        oauthUser.setSurname("testSurname");
        oauthUser.setFirstName("testFirstName");
        oauthUser.setAccountId("1");
        String json = mapper.map(oauthUser);
        assertThat(json, is(JSON_PRINCIPAL_FULL));
    }

    @Test
    public void userInfoToJsonMapperTest() {
        JsonToPrincipalMapper mapper = new JsonToPrincipalMapper();
        UserInfoModel userInfo = mapper.map(JSON_PRINCIPAL_FULL);
        UserInfoToJsonMapper userInfoToJsonMapper = new UserInfoToJsonMapper();
        userInfoToJsonMapper.setUserInfoUserNameOnly(false);
        assertThat(userInfoToJsonMapper.map(userInfo), is(JSON_PRINCIPAL_FULL));
    }
}
