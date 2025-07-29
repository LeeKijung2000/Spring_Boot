package kh.springboot.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // Controller 역할의 bean(객체) 생성
public class HomeController {
	
	@GetMapping("home") // 핸들러맵핑
	public String home() {

		return "views/home";
	}
}
