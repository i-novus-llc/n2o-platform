package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface MapperPreparer {

    void prepare(ObjectMapper mapper);

}
