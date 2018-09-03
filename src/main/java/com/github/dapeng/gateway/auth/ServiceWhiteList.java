package com.github.dapeng.gateway.auth;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.Set;

/**
 * @author maple 2018.09.03 17:02
 */
@Root(name = "service-whitelist")
public class ServiceWhiteList {
    @ElementList(entry = "service", inline = true)
    private Set<String> service;

    public Set<String> getService() {
        return service;
    }
}
