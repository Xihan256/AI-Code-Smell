package cn.scut.aicodesmell;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * 所有测试的基类
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AiCodeSmellApplication.class)
public class BaseTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        String[] cmd = {
                "java", "-jar",
                "F:\\javaPlace\\AICodeSmell\\ArchitectureRecovery\\arcade_java11_forSYS.jar",
                "ACDC",
                "F:\\javaPlace\\AICodeSmell\\data\\testSYS",
                "F:\\javaPlace\\AICodeSmell\\data\\testSYS\\output\\org_apache_tools_ant",
                "abc",
                "org.apache.tools.ant"
        };
        Process exec = Runtime.getRuntime().exec(cmd);

        exec.waitFor();
    }
}
