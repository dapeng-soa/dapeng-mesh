package com.github.dapeng.gateway.auth;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.Set;

/**
 * @author huyj
 * @Created 2019-04-02 10:17
 */
@Root(name = "service-whitelist")
public class AuthWhiteList {
    @ElementList(entry = "service", type = Service.class, inline = true, required = false)
    private Set<Service> services;

    public Set<Service> getServices() {
        return services;
    }

    public void setServices(Set<Service> services) {
        this.services = services;
    }
}
