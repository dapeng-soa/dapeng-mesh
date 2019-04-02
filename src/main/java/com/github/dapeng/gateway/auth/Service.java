package com.github.dapeng.gateway.auth;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import java.util.Set;

/**
 * @author huyj
 * @Created 2019-04-02 10:56
 */
public class Service {
    @Attribute
    private String serviceName;

    @ElementList(entry = "method", type = Method.class, inline = true, required = false)
    private Set<Method> methods;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Set<Method> getMethods() {
        return methods;
    }

    public void setMethods(Set<Method> methods) {
        this.methods = methods;
    }
}
