package kh.springboot.member.model.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import kh.springboot.member.model.mapper.MemberMapper;
import kh.springboot.member.model.vo.Member;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberMapper mapper;
	
	public Member login(Member m) {
		return mapper.login(m);
	}

	public int insertMember(Member m) {
		
		return mapper.insertMember(m);
	}

	public ArrayList<HashMap<String, Object>> selectMyList(String id) {
		return mapper.selectMyList(id);
	}

	public int updateEdit(Member m) {

		return mapper.updateEdit(m);
	}

	public int updatePassWord(Member m) {
		
		return mapper.updatePassWord(m);
	}

	public int delete(Member loginUser) {
		return mapper.delete(loginUser);
	}


//	public int checkId(String id) {	
//		return mapper.checkId(id);
//	}
//
//	public int nickNameCheckResult(String nickName) {
//		return mapper.nickNameCheckResult(nickName);
//	}

	public int checkValue(HashMap<String, String> map) {
		return mapper.checkValue(map);
	}

	public String finId(Member m) {
		return mapper.finId(m);
	}

	public Member findPw(Member m) {
		return mapper.findPw(m);
	}

	public Member findInfo(Member m) {
		return mapper.findInfo(m);
	}
	
}
