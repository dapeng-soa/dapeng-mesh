import com.github.dapeng.gateway.util.Constants;

import java.util.HashMap;

/**
 * @author maple 2018.09.29 1:41 PM
 */
public class CookiesTest {

    public static void main(String[] args) {
        String value = "cookie_storeId";
        String substring = value.substring(Constants.COOKIES_PREFIX.length());
        System.out.println(substring);

        HashMap<Object, Object> map = new HashMap<>();
        HashMap<Object, Object> map1 = new HashMap<>();
        map1.put("storeId","123");
        HashMap<Object, Object> map2 = new HashMap<>();
        map2.put("storeId","456");

        map.putAll(map1);
        map.putAll(map2);

        System.out.println(map.entrySet());



    }
}
