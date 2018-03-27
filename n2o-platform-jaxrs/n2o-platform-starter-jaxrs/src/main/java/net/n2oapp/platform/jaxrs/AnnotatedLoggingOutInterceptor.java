package net.n2oapp.platform.jaxrs;

import org.apache.cxf.interceptor.LoggingOutInterceptor;

/**
 * {@inheritDoc}
 */
@org.apache.cxf.annotations.Provider(value = org.apache.cxf.annotations.Provider.Type.OutInterceptor)
public class AnnotatedLoggingOutInterceptor extends LoggingOutInterceptor {
}
