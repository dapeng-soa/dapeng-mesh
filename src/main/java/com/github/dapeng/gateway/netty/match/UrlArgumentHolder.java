package com.github.dapeng.gateway.netty.match;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * desc: UrlArgumentHolder
 *
 * @author hz.lei
 * @since 2018年08月24日 下午2:49
 */
public class UrlArgumentHolder {
    private String lastPath;

    private Map<String, String> argumentMap = new HashMap<>();


    public void setArgument(String key, String value) {
        this.argumentMap.put(key, value);
    }

    public void setLastPath(String lastPath) {
        this.lastPath = lastPath;
    }

    public String getLastPath() {
        return lastPath;
    }

    public Map<String, String> getArgumentMap() {
        return argumentMap;
    }

    public static UrlArgumentHolder onlyPathCreator(String path) {
        UrlArgumentHolder holder = new UrlArgumentHolder();
        holder.setLastPath(path);
        return holder;
    }

    public static UrlArgumentHolder nonPropertyCreator() {
        UrlArgumentHolder holder = new UrlArgumentHolder();
        return holder;
    }

    @Override
    public String toString() {
        return "UrlArgumentHolder{" +
                "lastPath='" + lastPath + '\'' +
                ", arguments=" + argumentMap.entrySet().stream().map(argument -> "KV:[" + argument.getKey() + " -> " + argument.getValue() + "]").collect(Collectors.joining(",")) +
                '}';
    }
}
