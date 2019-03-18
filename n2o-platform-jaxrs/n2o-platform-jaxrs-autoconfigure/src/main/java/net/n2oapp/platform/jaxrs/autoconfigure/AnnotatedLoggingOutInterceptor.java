package net.n2oapp.platform.jaxrs.autoconfigure;

import org.apache.cxf.ext.logging.LoggingOutInterceptor;

/**
 * {@inheritDoc}
 */
@org.apache.cxf.annotations.Provider(value = org.apache.cxf.annotations.Provider.Type.OutInterceptor)
public class AnnotatedLoggingOutInterceptor extends LoggingOutInterceptor {
}
