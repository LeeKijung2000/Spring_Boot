package kh.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class}) //이 클래스가 main클래스 백엔드 클래스이다
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
