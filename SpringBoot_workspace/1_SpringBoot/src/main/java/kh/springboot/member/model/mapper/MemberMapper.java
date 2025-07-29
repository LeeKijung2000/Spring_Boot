package kh.springboot.member.model.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.annotations.Mapper;

import kh.springboot.member.model.vo.Member;

@Mapper // 인터페이스 구현을 xml로 하겠다는 의미
public interface MemberMapper {

	Member login(Member m);

	int insertMember(Member m);

	ArrayList<HashMap<String, Object>> selectMyList(String id);

	int updateEdit(Member m);

	int updatePassWord(Member m);

	int delete(Member loginUser);

//	int checkId(String id);
//
//	int nickNameCheckResult(String nickName);

	int checkValue(HashMap<String, String> map);

	String finId(Member m);

	Member findPw(Member m);

	Member findInfo(Member m);
	
}
