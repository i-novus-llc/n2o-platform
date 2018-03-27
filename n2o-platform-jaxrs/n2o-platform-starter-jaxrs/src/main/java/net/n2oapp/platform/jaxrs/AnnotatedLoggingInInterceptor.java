package net.n2oapp.platform.jaxrs;

import org.apache.cxf.interceptor.LoggingInInterceptor;

/**
 * {@inheritDoc}
 */
@org.apache.cxf.annotations.Provider(value = org.apache.cxf.annotations.Provider.Type.InInterceptor)
public class AnnotatedLoggingInInterceptor extends LoggingInInterceptor {
}
