package net.n2oapp.platform.jaxrs.autoconfigure;

import org.apache.cxf.Bus;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.client.spring.JaxRsProxyClientConfiguration;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * Поиск и регистрация JaxRS прокси клиентов в качестве Spring Beans
 */
public class JaxRsProxyClientFactoryBean extends JaxRsProxyClientConfiguration implements FactoryBean {
    private Class<?> serviceClass;

    private String connectionTimeout;
    private String receiveTimeout;

    @Override
    public Object getObject() throws Exception {
        Client client = createClient();
        HTTPClientPolicy httpClientPolicy = WebClient.getConfig(client).getHttpConduit().getClient();
        if (!StringUtils.isEmpty(connectionTimeout))
            httpClientPolicy.setConnectionTimeout(Long.valueOf(connectionTimeout));
        if (!StringUtils.isEmpty(receiveTimeout))
            httpClientPolicy.setReceiveTimeout(Long.valueOf(receiveTimeout));
        return client;
    }

    @Override
    public Class<?> getObjectType() {
        return serviceClass != null ? Proxy.getProxyClass(getClass().getClassLoader(), serviceClass) : null;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public void setBus(Bus bus) {
        setPrivate("bus", bus);
    }

    public void setAddress(String address) {
        setPrivate("address", address);
    }

    public void setThreadSafe(Boolean threadSafe) {
        setPrivate("threadSafe", threadSafe);
    }

    public void setAccept(String accept) {
        setPrivate("accept", accept);
    }

    public void setContentType(String contentType) {
        setPrivate("contentType", contentType);
    }

    public void setConnectionTimeout(String connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setReceiveTimeout(String receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    @Override
    protected Class<?> getServiceClass() {
        return serviceClass;
    }

    private void setPrivate(String fieldName, Object value) {
        Field field = ReflectionUtils.findField(this.getClass(), fieldName);
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, this, value);
    }
}
