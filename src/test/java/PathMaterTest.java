import com.github.dapeng.match.AntPathMatcher;

import java.util.Map;

/**
 * desc: PathMaterTest
 *
 * @author hz.lei
 * @since 2018年08月23日 下午1:45
 */
public class PathMaterTest {

    public static void main(String[] args) {
        AntPathMatcher matcher = new AntPathMatcher();
        boolean test = matcher.match("/api/{\\S}/{\\S}/{\\S}", "/api/rest/2/3");
        System.out.println(test);

//        Map<String, String> stringStringMap = matcher.extractUriTemplateVariables("/api/{name:\\S}/{vaule:\\S}/{age:\\S}", "/api/rest/2/3");
        Map<String, String> stringStringMap = matcher.extractUriTemplateVariables("/api/{name:[a-z]+}/{vaule:[a-z]+}/{age:[a-z]+}", "/api/rest/we/xz");

        Map<String, String> stringStringMap1 = matcher.extractUriTemplateVariables("/api/{name:[\\s\\S]*}/{vaule:[\\s\\S]*}/{age:[\\s\\S]*}", "/api/resds22--st/2/3");

        System.out.println(stringStringMap);
    }
}
