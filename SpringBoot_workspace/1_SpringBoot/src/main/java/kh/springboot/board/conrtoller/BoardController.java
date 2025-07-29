package kh.springboot.board.conrtoller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kh.springboot.board.model.exception.BoardException;
import kh.springboot.board.model.service.BoardService;
import kh.springboot.board.model.vo.Board;
import kh.springboot.board.model.vo.PageInfo;
import kh.springboot.board.model.vo.Reply;
import kh.springboot.common.Pagination;
import kh.springboot.member.model.vo.Member;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("board")
@RequiredArgsConstructor
public class BoardController {
	
	private final BoardService bService;

	
	@GetMapping("list")
	public String selectList(@RequestParam(value= "page", defaultValue= "1") int currentPage, 
			Model model, HttpServletRequest request) {
		int listCount = bService.getListCount(1);
		
		PageInfo pi = Pagination.getPageInfo(currentPage, listCount, 5); // 현재페이지, 몇개가있는지, 몇개씩 보여질지
		
		ArrayList<Board> list =  bService.selectBoardList(pi, 1);
		
		model.addAttribute("list", list);
		model.addAttribute("pi", pi);
		model.addAttribute("loc",request.getRequestURL());
		
		return "list";
	}
	
	@GetMapping("write")
	public String write() {		
		return "write";
	}
	
	@PostMapping("insert")
	public String insert(@ModelAttribute Board board, HttpSession session) {		
		String boardWriter = ((Member)session.getAttribute("loginUser")).getId();
		board.setBoardWriter(boardWriter);
		board.setBoardType(1);
		int result = bService.insert(board);
		
		if(result > 0 ) {
			return "redirect:/board/list";
		} else {
			throw new BoardException("게시글 작성을 실패하였습니다.");
		}
	}
	
	// url을 통해 전달된 값을 파라미터로 받아오기
	// http://localhost:8080/board?id=10&page=1 => 쿼리스트링 이용하여 여러 개 값 전달
	// http://localhost:8080/board/10/1			=> 폴더이동 혹은 데이터 전달
	@GetMapping("/{id}/{page}")
	public String selectBoard(@PathVariable("id") int bId, @PathVariable("page") int page, HttpSession session, Model model) {
		
		
		Member loginUser = (Member)session.getAttribute("loginUser");
		Board b = bService.selectBoardList(bId,loginUser);
		ArrayList<Reply> list = bService.selectReplyList(bId);
		
		if(b != null) {
			model.addAttribute("b", b).addAttribute("list", list).addAttribute("page",page).addAttribute("loginUser", loginUser);;
			return "detail";
		} else {
			throw new BoardException("게시글 상세보기를 실패하였습니다.");
		}
	}
	
	@PostMapping("updForm")
	public String updateForm(@RequestParam("boardId") int bId, @RequestParam("page") int page, Model model) {
		Board b = bService.selectBoardList(bId, null);
		model.addAttribute("b",b).addAttribute("page",page);

		return "views/board/edit";
	}
	
	@PostMapping("update")
	public String updateBoard(@ModelAttribute Board b, @RequestParam("page") int page) {
		b.setBoardType(1);
		int result = bService.updateBoard(b);
		if(result > 0) {
			return "redirect:/board/" + b.getBoardId() + "/" + page;
		} else {
			throw new BoardException("게시글 수정을 실패했습니다.");
		}
	}
	
	@PostMapping("delete")
	public String delete(@RequestParam("boardId") int bId, HttpServletRequest request) {
		int result = bService.delete(bId);
		if(result > 0) {
			return "redirect:/" + (request.getHeader("referer").contains("board") ? "board" : "attm") + "/list";
		} else {
			throw new BoardException("게시글 삭제를 실패했습니다.");
		}
	}
	
	@GetMapping(value="top",produces = "application/json; charset=UTF-8")
	@ResponseBody
	public String selectTop(HttpServletResponse response) {
		ArrayList<Board> list =  bService.selectTop();
		
		 //json 버전
		 //Board => jsonobject / arraylist => jsonarray
		JSONArray array = new JSONArray();
		for(Board b : list) {
			JSONObject json = new JSONObject();
			json.put("boardId",b.getBoardId());
			json.put("boardTitle",b.getBoardTitle());
			json.put("nickName",b.getNickName());
			json.put("boardModifyDate",b.getBoardModifyDate());
			json.put("boardCount",b.getBoardCount());
			
			array.put(json);
		}
		
//		response.setContentType("application/json; charset=UTF-8");
		return array.toString();
		
//		response.setContentType("application/json; charset=UTF-8");
//		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
//		try {
//			gson.toJson(list, response.getWriter());
//		} catch (JsonIOException | IOException e) {
//			e.printStackTrace();
//		}
		
	}
	
//	@PostMapping(value = "rinsert",produces = "application/json; charset=UTF-8")
//	@ResponseBody
//	public String insertReply(@ModelAttribute Reply r) {
//		int result = bService.insertReply(r);
//		ArrayList<Reply> list = bService.selectReplyList(r.getRefBoardId()); // 잘못침 select인데 set으로 만듦
//		
//		JSONArray array = new JSONArray();
//		for(Reply reply : list ) {
//			JSONObject json = new JSONObject();
//			json.put("replyContent",reply.getReplyContent());
//			json.put("nickName",reply.getNickName());
//			json.put("replyModifyDate",reply.getReplyModifyDate());
//			array.put(json);
//		}
//		
//		return array.toString();
//	}
	
	// GSON
//	@PostMapping(value = "rinsert")
//	@ResponseBody
//	public void insertReply(@ModelAttribute Reply r, HttpServletResponse response) {
//		int result = bService.insertReply(r);
//		ArrayList<Reply> list = bService.selectReplyList(r.getRefBoardId()); // 잘못침 select인데 set으로 만듦
//		
//		response.setContentType("application/json; charset=UTF-8");
//		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
//		try {
//			gson.toJson(list,response.getWriter());
//		} catch (JsonIOException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
//	}
	
	//jackson 버전 : springboot에서 자동으로 라이브러리 제공
	@GetMapping("rinsert")
	@ResponseBody
	public String insertReply(@ModelAttribute Reply r, HttpServletResponse response) {
		int result =  bService.insertReply(r);
		ArrayList<Reply> list = bService.selectReplyList(r.getRefBoardId());
		
		ObjectMapper om = new ObjectMapper();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		om.setDateFormat(sdf);
//		om.getDateFormat();
		String str = null;
		try {
			str = om.writeValueAsString(list);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		response.setContentType("application/json; charset=UTF-8");
		return str;
	}
	
	@GetMapping("rdelete")
	@ResponseBody
	public int rdelete(@RequestParam("rId") int rId) {
		
		int result =  bService.rdelete(rId);
		
		return result;
	}
	
	@GetMapping("rupdate")
	@ResponseBody
	public int rupdate(@RequestParam("replyId") int replyId, @RequestParam("replyContent") String replyContent) {
		
		Reply reply = new Reply();
		
		reply.setReplyId(replyId);
		reply.setReplyContent(replyContent);

		int result = bService.rupdate(reply);
		
		return result;
	}
	
	
}
