package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * class provides functionality for customization ObjectMapper instance which uses in jax-rs server and jax-rs proxy client
 */
public interface MapperConfigurer {

    void configure(ObjectMapper mapper);

}
