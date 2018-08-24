package com.github.dapeng.gateway.netty.match;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * desc: UrlArgumentHolder
 *
 * @author hz.lei
 * @since 2018年08月24日 下午2:49
 */
public class UrlArgumentHolder {
    private String lastPath;

    private List<KV> arguments = new ArrayList<>();

    public static class KV {
        public String argKey;
        public String argValue;

        public KV(String argKey, String argValue) {
            this.argKey = argKey;
            this.argValue = argValue;
        }
    }

    public void setArgument(KV kv) {
        this.arguments.add(kv);
    }

    public void setLastPath(String lastPath) {
        this.lastPath = lastPath;
    }

    public String getLastPath() {
        return lastPath;
    }

    public List<KV> getArguments() {
        return arguments;
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
                ", arguments=" + arguments.stream().map(argument -> "KV:[" + argument.argKey + " -> " + argument.argValue + "]").collect(Collectors.joining(",")) +
                '}';
    }
}
