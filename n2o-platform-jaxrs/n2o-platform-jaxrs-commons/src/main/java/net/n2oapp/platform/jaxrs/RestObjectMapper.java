package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;

/**
 * @author RMakhmutov
 * @since 14.01.2019
 */
public class RestObjectMapper extends ObjectMapper {
    public RestObjectMapper(List<MapperConfigurer> mapperConfigurers) {
        this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.registerModule(new SpringDataModule());
        this.registerModule(new JavaTimeModule());
        this.setDateFormat(new StdDateFormat());
        if(mapperConfigurers != null) {
            mapperConfigurers.forEach(preparer -> preparer.configure(this));
        }
    }
}
