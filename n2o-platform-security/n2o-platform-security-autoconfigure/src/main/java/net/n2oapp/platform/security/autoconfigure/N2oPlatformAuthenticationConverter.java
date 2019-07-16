package net.n2oapp.platform.security.autoconfigure;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Получение данных о пользователе из токена
 */
public class N2oPlatformAuthenticationConverter implements UserAuthenticationConverter {

    private UserDetailsService userDetailsService;
    private String usernameKey = "username";
    private String authoritiesKey = "roles";
    private Collection<GrantedAuthority> defaultAuthorities = AuthorityUtils.createAuthorityList("ROLE_USER");

    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put(USERNAME, authentication.getName());
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            response.put(AUTHORITIES, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        }
        return response;
    }

    @Override
    public Authentication extractAuthentication(Map<String, ?> map) {
        if (map.containsKey(usernameKey)) {
            Object principal = map.get(usernameKey);
            Collection<? extends GrantedAuthority> authorities = getAuthorities(map);
            if (userDetailsService != null) {
                UserDetails user = userDetailsService.loadUserByUsername((String) map.get(usernameKey));
                authorities = user.getAuthorities();
                principal = user;
            }
            return new UsernamePasswordAuthenticationToken(principal, "N/A", authorities);
        }
        return null;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void setUsernameKey(String usernameKey) {
        this.usernameKey = usernameKey;
    }

    public void setAuthoritiesKey(String authoritiesKey) {
        this.authoritiesKey = authoritiesKey;
    }

    public void setDefaultAuthorities(Collection<GrantedAuthority> defaultAuthorities) {
        this.defaultAuthorities = defaultAuthorities;
    }

    protected Collection<GrantedAuthority> getAuthorities(Map<String, ?> map) {
        Object authorities = map.get(authoritiesKey);

        if (authorities == null) {
            return defaultAuthorities;
        }
        if (authorities instanceof String) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList((String) authorities);
        }
        if (authorities instanceof Collection) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList(StringUtils
                    .collectionToCommaDelimitedString((Collection<?>) authorities));
        }
        throw new IllegalArgumentException("Authorities must be either a String or a Collection");
    }
}
