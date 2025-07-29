package kh.springboot.member.controller;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import kh.springboot.member.model.exception.MemberException;
import kh.springboot.member.model.service.MemberService;
import kh.springboot.member.model.vo.Member;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor // final이 붙은 상수나 @NonNull이 붙은 변수만 가지고 생성자 생성자
@SessionAttributes("loginUser")
@RequestMapping("member") // 공용 URL
public class MemberController {

	//  필드 주입 org.springframework.beans.factory.annotation.Autowired;
	//	@Autowired : 스프링부트에서 제공
	//	private MemberService mService;
	
	
	
	// 생성자 주입 : 롬복에서 제공
	private final MemberService mservice;
	
	private final BCryptPasswordEncoder bcrypt;
	
	private final JavaMailSender mailSender;
	
	@GetMapping("signIn")
	public String signIn() {
		return "signIn";
	}
	
	@GetMapping("enroll")
	public String enroll() {
		return "enroll";
	}
	
	// 데이터를 view로 전달하는 방법
	// 1. Model 객체 이용: request영역에 담기는 Map형식(key-value)의 객체
//	@GetMapping("/member/myInfo")
//	public String myInfo(HttpSession session, Model model) {
//		Member loginUser = (Member)session.getAttribute("loginUser");
//		if(loginUser != null) {
//			String id = loginUser.getId();
//			ArrayList<HashMap<String, Object>> list = mservice.selectMyList(id);
//			model.addAttribute("list", list);
//		}
//		return "views/member/myInfo";
//	}
	
	// 2.ModelAndView 객체 이용
	// model에 데이터 저장하고 view에 forward할 뷰 정보를 담음
	@GetMapping("myInfo")
	public ModelAndView myInfo(HttpSession session, ModelAndView mv) {
		Member loginUser = (Member)session.getAttribute("loginUser");
		if(loginUser != null) {
			String id = loginUser.getId();
			ArrayList<HashMap<String, Object>> list = mservice.selectMyList(id);
			
			mv.addObject("list", list);
			mv.setViewName("myInfo");
		}
		return mv;
	}
	
	@GetMapping("edit")
	public String edit() {
		return "edit";
	}
	
	
//	@GetMapping("/member/logout")
//	public String logout(HttpSession session) {
//	    session.invalidate();	
//	    return "redirect:/home";
//	}
	
	// @SessionAttributes 추가 후 로그아웃
	@GetMapping("logout")
	public String logout(SessionStatus status) {
		status.setComplete();	
	    return "redirect:/home";
	}
	
	// 파라미터를 전송받는 방법
	// 1. HttpServletRequest 이용
//	@PostMapping("/member/signIn")
//	public void login(HttpServletRequest request) {
//		String id = request.getParameter("id");
//		String pwd = request.getParameter("pwd");
//		System.out.println(id);
//	}
	
	// 2. @RequestParam 이용
//	@PostMapping("/member/signIn")
//	public void login(@RequestParam(value  = "id", defaultValue = "hello") String id, 
//						@RequestParam(value="pwd") String pwd,
//						@RequestParam(value="test", required = false) String test) {
//		System.out.println("id: " + id);
//		System.out.println("pwd: " + pwd);
//		System.out.println("test: " + test);
//	}
	
	// 3. @RequestParam 생략 : 파라미터 명과 변수 명을 일치시켜서 자동 매핑되게 함 /  근데 sts에서는 안됨 인텔리제이에서는 됨
	
	// 4. @ModelAttribute 이용
//	@PostMapping("/member/signIn")
//	public void login(@ModelAttribute Member m) {
//		System.out.println("id: " + m.getId());
//		System.out.println("pwd: " + m.getPwd());
//	}
	
	// 5. @ModelAttribute 생략 / 그냥 Member로 받아와서 하면 된다
//	@PostMapping("/member/signIn")
//	public String login(Member m, HttpSession session) throws MemberException {
//		Member loginUser = mservice.login(m);
//		if(loginUser != null) {
//			session.setAttribute("loginUser", loginUser);
////			return "views/home"; 
//			return "redirect:/home";
//		} else {
//			throw new MemberException("로그인을 실패하였습니다.");
//		}
//	}
	
	// 암호화 후 로그인
//	@PostMapping("/member/signIn")
//	public String login(Member m, HttpSession session){
//		Member loginUser = mservice.login(m);
//			if(loginUser != null && bcrypt.matches(m.getPwd(), loginUser.getPwd())) { //평문 pwd, 암호화된 pwd
//				session.setAttribute("loginUser", loginUser);
//				return "redirect:/home";
//			} else {
//				throw new MemberException("로그인을 실패하였습니다.");
//			}
//	}
	
	// 3. session에 저장할 때 @SessionAttributes 이용
	//      model에 attribute가 추가될 때 자동으로 키 값을 찾아 세션에 등록하는 기능
	// 암호화 후 로그인 + @SessionAttributes
	@PostMapping("signIn")
	public String login(Member m, Model model){
		Member loginUser = mservice.login(m);
			if(loginUser != null && bcrypt.matches(m.getPwd(), loginUser.getPwd())) { //평문 pwd, 암호화된 pwd
				model.addAttribute("loginUser",loginUser);
				return "redirect:/home";
			} else {
				throw new MemberException("로그인을 실패하였습니다.");
			}
	}
	
	
	@PostMapping("enroll")
	public String insertMember(@RequestParam("emailId") String emailId,
	                           @RequestParam("emailDomain") String emailDomain,
	                           @ModelAttribute Member m) {
	    if(!emailId.trim().equals("")) {
		    m.setEmail(emailId + "@" + emailDomain);
	    }
	    
	    String encPwd = bcrypt.encode(m.getPwd());
	    m.setPwd(encPwd);

	    int result = mservice.insertMember(m);
	    
	    // 회원가입 성공시 home으로 가기
	    // 회원가입 실피 새 500에러 발생: 회원가입을 실패하였습니다
		if(result > 0) {
			return "redirect:/home";
		} else {
			throw new MemberException("회원가입을 실패하였습니다.");
		}
	}
	
	@PostMapping("edit")
	public String edit(@RequestParam("emailId") String emailId,
							 @RequestParam("emailDomain") String emailDomain,
							 Member m, Model model) {
	    if(!emailId.trim().equals("")) {
		    m.setEmail(emailId + "@" + emailDomain);
	    }
		
		int result = mservice.updateEdit(m);
		
		if(result > 0) {
			model.addAttribute("loginUser", mservice.login(m));
			return "redirect:/home";
		} else {
			throw new MemberException("회원정보 수정을 실패하였습니다.");
		}
	}
	
	@PostMapping("updatePassword")
	public String updatePassWord(@RequestParam("currentPwd") String pwd,  @RequestParam("newPwd") String newPwd,
								 Model model, SessionStatus status) {
		Member m = (Member)model.getAttribute("loginUser");
		if(bcrypt.matches(pwd, m.getPwd())) {
			m.setPwd(bcrypt.encode(newPwd));
			int result = mservice.updatePassWord(m);
			if(result > 0) {
				return "redirect:/home";
			}else {
				throw new MemberException("비밀번호 수정에 실패하였습니다");
			}
		} else {
			throw new MemberException("현재 비밀번호가 아닙니다");
		}

	}
	
	@GetMapping("delete")
	public String delete(Model model, SessionStatus status) {
		Member loginUser = (Member)model.getAttribute("loginUser");
		
		int result = mservice.delete(loginUser);
		
		if(result > 0) {
			status.setComplete();
			return "redirect:/home";
		} else {
			throw new MemberException("탈퇴 실패");
		}
		
	}
	
//	@GetMapping("checkId")
//	public void checkId(@RequestParam("id")String id, PrintWriter out) {
//		int result = mservice.checkId(id);
//		out.print(result);
//	}
	
	@GetMapping("checkValue")
	@ResponseBody
	public int nickNameCheckResult(@RequestParam("value")String value, @RequestParam("column")String column) {
		HashMap<String, String > map = new HashMap<String, String>();
		map.put("col", column);
		map.put("val", value);
		
		return mservice.checkValue(map);

	}
	
	@GetMapping("echeck")
	@ResponseBody
	public String echeckEmail(@RequestParam("email") String email) {
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		
		// 수신자, 제목, 본문설정
		String subject = "[SpringBoot] 이메일 확인";
		String body = "<h1 aling='center'>StringBoot 이메일 확인</h1> <br>";
		body += "<div style='border: 5px solid yellowgreen; text-align: center; font-size: 15px; '>";
		body += "본 메일은 이메일을 확인하기 위해 발송되었습니다.<br>";
		body += "아래 숫자를 인증번호 확인란에 작심하여 확인해주시기 바랍니다. <br><br>";
		
		String random = "";
		for(int i =0; i<5; i++) {
			random += (int)(Math.random() * 10);
			
			//System.out.println(random);
		}
		
		body += "<span style='font-size: 30px; text-decoration: underling;'><b>" + random + "</b></span></div>";
		
		MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
		try {
			mimeMessageHelper.setTo(email);
			mimeMessageHelper.setSubject(subject);
			mimeMessageHelper.setText(body, true);
			
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mailSender.send(mimeMessage); //email 전송
	
		return random;
	}
	
	@GetMapping("findIDPW") 
	public String findIDPW() {
		return "findIDPW";
	}
	
//	@PostMapping("fid") 
//	public String fID(@ModelAttribute Member m, Model model) {
//		String id = mservice.finId(m);
//		if(id != null) {
//			model.addAttribute("id",id);
//			return "findId";
//		} else {
//			throw new MemberException("존재하지 않는 회원입니다.");
//		}
//	}
//	
//	@PostMapping("fpw") 
//	public String fPW(@ModelAttribute Member m, Model model) {
//		Member member = mservice.findPw(m);
//		if(member != null) {
//			model.addAttribute("id", member.getId());
//			return "resetPw";
//		} else {
//			throw new MemberException("존재하지 않는 회원입니다.");
//		}
//		
//	}
	
	@PostMapping("finfo") 
	public String fPW(@ModelAttribute Member m, Model model) {
		Member member = mservice.findInfo(m);
		if(member != null) {
			model.addAttribute("id", member.getId());
			return m.getName() == null ? "resetPw" : "findId";
		} else {
			throw new MemberException("존재하지 않는 회원입니다.");
		}
		
	}
}
