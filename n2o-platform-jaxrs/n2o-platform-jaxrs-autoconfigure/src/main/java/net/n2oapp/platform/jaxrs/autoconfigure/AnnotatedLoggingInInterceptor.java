package net.n2oapp.platform.jaxrs.autoconfigure;

import org.apache.cxf.ext.logging.LoggingInInterceptor;

/**
 * {@inheritDoc}
 */
@org.apache.cxf.annotations.Provider(value = org.apache.cxf.annotations.Provider.Type.InInterceptor)
public class AnnotatedLoggingInInterceptor extends LoggingInInterceptor {
}
