package net.n2oapp.platform.web;

import net.n2oapp.criteria.dataset.DataSet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

@RestController
public class TokenController {
    @GetMapping("/token")
    public DataSet token(HttpServletRequest request) {
        return new DataSet("token", Collections.singletonList(Map.of("auth", request.getHeader("authorization"))));
    }
}
