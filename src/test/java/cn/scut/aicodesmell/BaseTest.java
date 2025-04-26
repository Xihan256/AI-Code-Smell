package cn.scut.aicodesmell;

import com.alibaba.fastjson2.JSON;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

/**
 * 所有测试的基类
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AiCodeSmellApplication.class)
public class BaseTest {
    public static void main(String[] args) throws IOException, InterruptedException {
//        String[] cmd = {
//                "java", "-jar",
//                "F:\\javaPlace\\AICodeSmell\\ArchitectureRecovery\\arcade_java11_forSYS.jar",
//                "ACDC",
//                "F:\\javaPlace\\AICodeSmell\\data\\testSYS",
//                "F:\\javaPlace\\AICodeSmell\\data\\testSYS\\output\\org_apache_tools_ant",
//                "abc",
//                "org.apache.tools.ant"
//        };
//        Process exec = Runtime.getRuntime().exec(cmd);
//
//        exec.waitFor();
        String jsonS = "[\"Storage Component\", \"E2E Component\", \"Client Component\", \"Test Driver\", \"test cases\", \"Driver\", \"main\", \"Common Component\", \"Driver Component\", \"Component tests\", \"E2E end component\", \"Common\", \"UI Component\", \"Storage\", \"component test cases\", \"UI\", \"Test\", \"Logic Component\", \"E2E\", \"end\", \"Logic\", \"Client\"]";
        Object parse = JSON.parse(jsonS);
        if (parse instanceof List<?>) {
            List<String> l = (List<String>) parse;
            System.out.println(l);
        }
    }
}
