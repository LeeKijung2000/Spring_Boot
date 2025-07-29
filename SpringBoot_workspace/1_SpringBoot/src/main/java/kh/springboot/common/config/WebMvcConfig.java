package kh.springboot.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**")   // 매핑 uri 설정
					.addResourceLocations("file:///c:/uploadFiles/", "classpath:/static/"); // 정적 리소스 위치 정적파일에 있는것들은 이제 image로 불린다
	}
}
