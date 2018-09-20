import java.util.Scanner;

/**
 * @author maple 2018.09.20 下午3:53
 */
public class StringEscapeTest {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String parameter = scanner.next();

        parameter.replaceAll("\"", "\\\"");


        System.out.println(parameter);

    }

}
