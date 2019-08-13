package net.n2oapp.platform.security.autoconfigure;

import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.util.JsonParser;
import org.springframework.security.oauth2.common.util.JsonParserFactory;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;

import java.util.Map;

/**
 * Простой конвертер токенов доступа с методом декодирования токена
 */
public class PlatformAccessTokenConverter extends DefaultAccessTokenConverter {
    private JsonParser objectMapper = JsonParserFactory.create();

    /**
     * Декодирование токена доступа
     * @param token Токен доступа
     * @return Значения токена доступа
     */
    public Map<String, Object> decode(String token) {
        try {
            Jwt jwt = JwtHelper.decode(token);
            String claimsStr = jwt.getClaims();
            Map<String, Object> claims = objectMapper.parseMap(claimsStr);
            if (claims.containsKey(EXP) && claims.get(EXP) instanceof Integer) {
                Integer intValue = (Integer) claims.get(EXP);
                claims.put(EXP, Long.valueOf(intValue));
            }
            return claims;
        }
        catch (Exception e) {
            throw new InvalidTokenException("Cannot convert access token to JSON", e);
        }
    }
}
