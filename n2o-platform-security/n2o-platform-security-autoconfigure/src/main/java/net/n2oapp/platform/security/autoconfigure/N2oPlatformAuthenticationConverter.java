package net.n2oapp.platform.security.autoconfigure;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.mapping.Attributes2GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Получение данных о пользователе из токена
 */
public class N2oPlatformAuthenticationConverter implements UserAuthenticationConverter {

    private UserDetailsService userDetailsService;
    private String usernameKey;
    private String authoritiesKey;
    private Attributes2GrantedAuthoritiesMapper authoritiesMapper;
    private Collection<GrantedAuthority> defaultAuthorities = AuthorityUtils.createAuthorityList("ROLE_USER");

    public N2oPlatformAuthenticationConverter(String usernameKey, String authoritiesKey) {
        this.usernameKey = usernameKey;
        this.authoritiesKey = authoritiesKey;
    }

    public  N2oPlatformAuthenticationConverter(String usernameKey, String authoritiesKey, Attributes2GrantedAuthoritiesMapper authoritiesMapper) {
        this(usernameKey, authoritiesKey);
        this.authoritiesMapper = authoritiesMapper;
    }

    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put(usernameKey, authentication.getName());
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            response.put(authoritiesKey, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
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

    public void setAuthoritiesMapper(Attributes2GrantedAuthoritiesMapper authoritiesMapper) {
        this.authoritiesMapper = authoritiesMapper;
    }

    /** @noinspection WeakerAccess*/
    protected Collection<GrantedAuthority> getAuthorities(Map<String, ?> map) {
        Object authorities = map.get(authoritiesKey);

        if (authorities == null) {
            return defaultAuthorities;
        }

        if (!(authorities instanceof String || authorities instanceof Collection)) {
            throw new IllegalArgumentException("Authorities must be either a String or a Collection");
        }

        Collection<String> authorityColl =
            authorities instanceof String
                ? Arrays.asList(((String) authorities).split(","))
                : ((Collection<?>) authorities).stream().map(Object::toString).collect(Collectors.toList());

        //noinspection unchecked
        return (Collection<GrantedAuthority>) authoritiesMapper.getGrantedAuthorities(authorityColl);
    }
}
