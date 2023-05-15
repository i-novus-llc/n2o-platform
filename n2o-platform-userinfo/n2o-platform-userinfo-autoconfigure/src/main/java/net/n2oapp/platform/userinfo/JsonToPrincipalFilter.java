package net.n2oapp.platform.userinfo;

import net.n2oapp.platform.userinfo.mapper.JsonToPrincipalAbstractMapper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

import static java.util.List.of;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

public class JsonToPrincipalFilter extends OncePerRequestFilter {

    private JsonToPrincipalAbstractMapper jsonToPrincipalMapper;

    private String userInfoHeaderName;

    public JsonToPrincipalFilter(JsonToPrincipalAbstractMapper jsonToPrincipalMapper, String userInfoHeaderName) {
        this.jsonToPrincipalMapper = jsonToPrincipalMapper;
        this.userInfoHeaderName = userInfoHeaderName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(userInfoHeaderName);
        UserInfoModel userInfo = jsonToPrincipalMapper.map(header);
        Collection<? extends GrantedAuthority> authorities = isEmpty(userInfo.authorities) ? of(new SimpleGrantedAuthority("dummyAuthority")) : userInfo.authorities;
        AnonymousAuthenticationToken authenticationToken = new AnonymousAuthenticationToken(userInfo.getUsername(), userInfo, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !hasText(request.getHeader(userInfoHeaderName));
    }
}
