package com.github.dapeng.gateway.util;

import com.github.dapeng.core.metadata.*;
import com.github.dapeng.gateway.netty.request.RequestContext;
import com.github.dapeng.openapi.cache.ServiceCache;
import com.google.gson.Gson;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 获得接口mock data
 *
 * @author huyj
 * @Created 2018-10-24 16:53
 */
public class MockDataUtils {

    private static final int LIST_MAX_SIZE = 5;
    private static final List<String> STRING_DATA = Arrays.asList("Hello Word!", "dapeng-soa", "春生、夏长、秋收、冬藏", "Today今天梦想", "霜淇淋", "为梦想而战", "好炖", "三明治", "HB2121564321451123165", "WMS1234465432213185421321", "烧烤", "蒸包", "面包", "32165231532125432", "便当", "13568254892", "坚如磐石", "梦想", "13697524589", "稳如狗", "15896524553", "赋能用户", "啊飞", "小李飞刀", "快乐", "创造改变", "葵花宝典", "asdfghjklqwertyuiopzxcvbnm", "薯片", "可口可乐", "雪碧", "每一盏灯有温暖整个城市的梦想", "品牌", "团队共建", "简单信仰、傻傻坚持");

    public static String getJsonMockData(RequestContext requestContext) {
        //String jsonData = String.format("{\"success\":\"%s\", \"status\":1}", getMethodResponseData(requestContext));
        return getMethodResponseData(requestContext);
    }

    public static String getMethodResponseData(RequestContext requestContext) {
        String serviceName = requestContext.service().orElse("").substring(requestContext.service().get().lastIndexOf(".") + 1);
        String version = requestContext.version().orElse("1.0.0");
        String method = requestContext.method().orElse("");
        Service service = ServiceCache.getService(serviceName, version).getService();
        if (service == null) {
            return String.format("没有找到服务[%s:%s] 元数据", serviceName, version);
        }

        Method methodObj = null;
        for (Method methItem : service.getMethods()) {
            if (methItem.name.equalsIgnoreCase(method)) {
                methodObj = methItem;
                break;
            }
        }

        if (methodObj == null) {
            return String.format("没有找到服务对应的方法[%s:%s:%s]", serviceName, version, method);
        }

        //生成示范响应对象
        HashMap methodMockData = new HashMap();
        for (int index = 0; index < methodObj.response.fields.size(); index++) {
            Field field = methodObj.response.fields.get(index);
            methodMockData.put(field.name, getJsonSample(field.dataType, service));
        }
        methodMockData.put("status", 1);
        methodMockData.put("mockMode", true);
        return new Gson().toJson(methodMockData);
    }


    /**
     * //VOID,
     * <p>
     * <p>
     * BOOLEAN,
     * BYTE,
     * SHORT,
     * INTEGER,
     * LONG,
     * DOUBLE,
     * STRING,
     * BINARY,
     * MAP,
     * LIST,
     * SET,
     * ENUM,
     * STRUCT,
     * DATE,
     * BIGDECIMAL;
     *
     * @param dataType
     * @param service
     * @return
     */
    public static Object getJsonSample(DataType dataType, Service service) {

        switch (dataType.kind) {
            case STRING:
                return getSampleString();
            case INTEGER:
                return ThreadLocalRandom.current().nextInt(1000);
            case DOUBLE:
                return ThreadLocalRandom.current().nextDouble(9999.9999);
            case BOOLEAN:
                return Math.round(Math.random()) == 1 ? "true" : "false";
            case BYTE:
                return ThreadLocalRandom.current().nextInt(256) - 128;
            case BINARY:
                return "546869732049732041205465737420427974652041727261792E";
            case SHORT:
                return ThreadLocalRandom.current().nextInt(127);
            case LONG:
                return ThreadLocalRandom.current().nextInt(999999);
            case ENUM:
                for (int i = 0; i < service.getEnumDefinitions().size(); i++) {
                    TEnum tenum = service.getEnumDefinitions().get(i);
                    if (dataType.qualifiedName.equalsIgnoreCase(tenum.namespace + "." + tenum.name)) {
                        int tEmItemCount = tenum.getEnumItems().size();
                        return tenum.getEnumItems().get(ThreadLocalRandom.current().nextInt(tEmItemCount)).label;
                    }
                }
                return "";
            case MAP:
                HashMap map = new HashMap();
                String key = (String) getJsonSample(dataType.keyType, service);
                Object value = getJsonSample(dataType.valueType, service);
                map.put(key, value);
                return map;
            case LIST:
                //List<Object> list = Arrays.asList();
                List<Object> list = new ArrayList<>();
                //List<Object> list = Lists.newArrayList();
                int listSize = ThreadLocalRandom.current().nextInt(LIST_MAX_SIZE) + 1;
                for (int i = 0; i < listSize; i++) {
                    list.add(getJsonSample(dataType.valueType, service));
                    list.add(getJsonSample(dataType.valueType, service));
                }
                return list;
            case SET:
                //List<Object> set = Collections.emptyList();
                List<Object> set = new ArrayList<>();
               // List<Object> set = Lists.newArrayList();
                int setSize = ThreadLocalRandom.current().nextInt(LIST_MAX_SIZE) + 1;
                for (int i = 0; i < setSize; i++) {
                    set.add(getJsonSample(dataType.valueType, service));
                    set.add(getJsonSample(dataType.valueType, service));
                }
                return set;
            case STRUCT:
                HashMap structMap = new HashMap();
                for (int i = 0; i < service.getStructDefinitions().size(); i++) {
                    Struct struct = service.getStructDefinitions().get(i);
                    if (dataType.qualifiedName.equalsIgnoreCase(struct.namespace + "." + struct.name)) {
                        for (int index = 0; index < struct.fields.size(); index++) {
                            Field field = struct.fields.get(index);
                            structMap.put(field.name, getJsonSample(field.dataType, service));
                        }
                        return structMap;
                    }
                }
                return "";
            case DATE:
                return "2016/04/13 16:00";
            case BIGDECIMAL:
                return "1234567.123456789123456";
            default:
                return "";
        }
    }


    private static String getSampleString() {
        return STRING_DATA.get(ThreadLocalRandom.current().nextInt(STRING_DATA.size() - 1));
    }

}
