package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces({"application/xml", "application/*+xml", "text/xml"})
@Consumes({"application/xml", "application/*+xml", "text/xml"})
public class XmlProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

    private final XmlMapper xmlMapper;

    public XmlProvider(XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        JavaType javaType = xmlMapper.getTypeFactory().constructType(genericType);
        return xmlMapper.canDeserialize(javaType);
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {
        JavaType javaType = xmlMapper.getTypeFactory().constructType(genericType);
        return xmlMapper.readValue(entityStream, javaType);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return xmlMapper.canSerialize(type);
    }

    @Override
    public void writeTo(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
        xmlMapper.writeValue(entityStream, o);
    }

}
