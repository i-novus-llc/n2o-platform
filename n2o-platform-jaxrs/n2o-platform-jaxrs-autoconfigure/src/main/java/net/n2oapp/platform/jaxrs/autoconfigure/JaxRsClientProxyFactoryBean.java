package net.n2oapp.platform.jaxrs.autoconfigure;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxrs.client.spring.JaxRsProxyClientConfiguration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

public class JaxRsClientProxyFactoryBean extends JaxRsProxyClientConfiguration implements FactoryBean {
    private Class<?> serviceClass;

    @Override
    public Object getObject() throws Exception {
        return createClient();
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
