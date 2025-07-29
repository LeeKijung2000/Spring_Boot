package kh.springboot.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class TemplateResolverConfig {

	@Bean
	public ClassLoaderTemplateResolver memberResolver() {
		ClassLoaderTemplateResolver mResolver = new ClassLoaderTemplateResolver();
		mResolver.setPrefix("templates/views/member/");
		mResolver.setSuffix(".html");
		mResolver.setTemplateMode(TemplateMode.HTML);
		mResolver.setCharacterEncoding("UTF-8");
		mResolver.setCacheable(false);
		mResolver.setCheckExistence(true);
		
		return mResolver;
	}
	
	@Bean
	public ClassLoaderTemplateResolver boardResolver() {
		ClassLoaderTemplateResolver mbesolver = new ClassLoaderTemplateResolver();
		mbesolver.setPrefix("templates/views/board/");
		mbesolver.setSuffix(".html");
		mbesolver.setTemplateMode(TemplateMode.HTML);
		mbesolver.setCharacterEncoding("UTF-8");
		mbesolver.setCacheable(false);
		mbesolver.setCheckExistence(true);
		
		return mbesolver;
	}
}
