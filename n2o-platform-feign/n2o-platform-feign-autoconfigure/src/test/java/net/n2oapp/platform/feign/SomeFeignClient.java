package net.n2oapp.platform.feign;

import net.n2oapp.platform.jaxrs.api.SomeRest;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author RMakhmutov
 * @since 15.03.2019
 */
@FeignClient(name = "${feign.name}", url = "${feign.url}")
public interface SomeFeignClient extends SomeRest {
}
