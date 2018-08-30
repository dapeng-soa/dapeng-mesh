package com.github.dapeng.gateway.auth;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.Set;

/**
 * @author with struy.
 * Create by 2018/5/12 00:00
 * email :yq1724555319@gmail.com
 */
@Root(name = "service-whitelist")
public class ServiceWhitelist {
    @ElementList(entry = "service", inline = true)
    private Set<String> service;

    public Set<String> getService() {
        return service;
    }
}
