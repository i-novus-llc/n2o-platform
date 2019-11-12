package net.n2oapp.platform.jaxrs.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.n2oapp.platform.jaxrs.api.FeignJwtHeaderInterceptorRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;

/**
 * Реализация REST сервиса для теста FeignJwtHeaderInterceptor
 */
@Controller
public class FeignJwtHeaderInterceptorRestImpl implements FeignJwtHeaderInterceptorRest {

    @Autowired
    private HttpServletRequest request;

    @Override
    public String testJwtTokenHeaderInterceptor() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(request.getHeader("Authorization"));
    }
}
