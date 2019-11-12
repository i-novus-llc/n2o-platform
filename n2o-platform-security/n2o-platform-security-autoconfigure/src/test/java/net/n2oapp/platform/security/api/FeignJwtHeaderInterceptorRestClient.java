package net.n2oapp.platform.security.api;

import net.n2oapp.platform.jaxrs.api.FeignJwtHeaderInterceptorRest;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "${feign.name}", url = "${feign.url}")
public interface FeignJwtHeaderInterceptorRestClient extends FeignJwtHeaderInterceptorRest {
}
