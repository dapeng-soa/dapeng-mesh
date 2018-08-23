package com.github.dapeng.util;

import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * desc: GateWayUtil
 *
 * @author hz.lei
 * @since 2018年08月23日 下午12:03
 */
public class GateWayUtil {

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static String[] tokenizeToStringArray(
            @Nullable String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {

        if (str == null) {
            return new String[0];
        }

        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (!ignoreEmptyTokens || token.length() > 0) {
                tokens.add(token);
            }
        }
        return tokens.toArray(new String[tokens.size()]);
    }
}
