package cn.scut.aicodesmell;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.scut.aicodesmell.mapper")
public class AiCodeSmellApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCodeSmellApplication.class, args);
    }

}
