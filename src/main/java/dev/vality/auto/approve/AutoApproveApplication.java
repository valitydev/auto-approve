package dev.vality.auto.approve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class AutoApproveApplication extends SpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoApproveApplication.class, args);
    }

}
