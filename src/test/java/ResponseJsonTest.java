import com.github.dapeng.gateway.util.Constants;

/**
 * @author maple 2018.09.06 上午11:01
 */
public class ResponseJsonTest {


    public static void main(String[] args) {
        String failed = "{\"responseCode\":\"Err-Core-502\", \"responseMsg\":\"网关错误\", \"success\":\"{}\", \"status\":0}";

        String success = "{\"responseCode\":\"Err-Core-502\", \"responseMsg\":\"网关错误\", \"success\":\"{}\"}";


        String substring = failed.substring(failed.length() - 11, failed.length() - 1);

        boolean b = failed.endsWith("\"status\":0}");


        System.out.println("flag :" + b);

        System.out.println(substring);

        if (success.contains(Constants.RESP_STATUS)) {

        } else {
            String response = "{}".equals(failed) ? "{\"status\":1}" : success.substring(0, success.lastIndexOf('}')) + ",\"status\":1}";

            System.out.println(response);
        }


    }
}
