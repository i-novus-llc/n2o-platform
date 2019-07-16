package net.n2oapp.platform.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingService {
    @Autowired
    private OAuth2RestOperations restOperations;

    /**
     * REST сервис для примера
     */
    @GetMapping("/greeting")
    public String greeting(OAuth2Authentication authentication) {
        return "Hello " + authentication.getPrincipal();
    }

    @GetMapping("/proxy/{port}")
    public String proxy(@PathVariable("port") String port) {
        return restOperations.getForObject("http://localhost:" + port + "/greeting", String.class);
    }
}
