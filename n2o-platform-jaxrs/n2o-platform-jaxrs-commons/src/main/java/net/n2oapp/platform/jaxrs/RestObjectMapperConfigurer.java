package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;

import java.text.DateFormat;
import java.util.List;

/**
 * @author RMakhmutov
 * @since 14.01.2019
 */
public final class RestObjectMapperConfigurer {

    private static final Module SPRING_DATA_JSON_MODULE = new SpringDataModule.SpringDataJsonModule();
    private static final Module SPRING_DATA_XML_MODULE = new SpringDataModule.SpringDataXmlModule();
    private static final Module JACKSON_XML_MODULE = new JacksonXmlModule();
    private static final Module JAXB_MODULE = new JakartaXmlBindAnnotationModule();

    private static final Module JAVA_TIME_MODULE = new JavaTimeModule();

    private static final DateFormat STD_DATE_FORMAT = new StdDateFormat();

    private RestObjectMapperConfigurer() {
        throw new UnsupportedOperationException();
    }

    public static void configure(ObjectMapper objectMapper, List<MapperConfigurer> mapperConfigurers) {
        configureCommon(objectMapper, mapperConfigurers);
        objectMapper.registerModule(SPRING_DATA_JSON_MODULE);
    }

    public static void configure(XmlMapper xmlMapper, List<MapperConfigurer> mapperConfigurers) {
        configureCommon(xmlMapper, mapperConfigurers);
        xmlMapper.registerModule(SPRING_DATA_XML_MODULE);
        xmlMapper.registerModule(JACKSON_XML_MODULE);
        xmlMapper.registerModule(JAXB_MODULE);
    }

    private static void configureCommon(ObjectMapper objectMapper, List<MapperConfigurer> mapperConfigurers) {
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(JAVA_TIME_MODULE);
        objectMapper.setDateFormat(STD_DATE_FORMAT);
        if (mapperConfigurers != null) {
            mapperConfigurers.forEach(preparer -> preparer.configure(objectMapper));
        }
    }

}
