package com.github.dapeng.gateway.auth;

import org.simpleframework.xml.Text;

/**
 * @author huyj
 * @Created 2019-04-02 10:20
 */
public class Method {

    @Text
   private String methodName;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
