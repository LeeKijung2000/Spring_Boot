package kh.springboot.board.conrtoller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kh.springboot.board.model.exception.BoardException;
import kh.springboot.board.model.service.BoardService;
import kh.springboot.board.model.vo.Attachment;
import kh.springboot.board.model.vo.Board;
import kh.springboot.board.model.vo.PageInfo;
import kh.springboot.common.Pagination;
import kh.springboot.member.model.vo.Member;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("attm")
@RequiredArgsConstructor
public class AttacHmentController {

	private final BoardService bService;

	@GetMapping("list")
	public String selectList(@RequestParam(value = "page", defaultValue = "1") int currentPage, Model model,
			HttpServletRequest request) {

		int listCount = bService.getListCount(2);
		PageInfo pi = Pagination.getPageInfo(currentPage, listCount, 9);

		// 첨부파일 게시판 = 게시글 내용 + 첨부파일
		ArrayList<Board> blist = bService.selectBoardList(pi, 2); // 게시글 내용
		ArrayList<Attachment> alist = bService.selectAttmBoardList(null); // 첨부파일

		if (blist != null) {
			model.addAttribute("bList", blist).addAttribute("aList", alist).addAttribute("pi", pi).addAttribute("loc",
					request.getRequestURI());
			return "views/attm/list";
		} else {
			throw new BoardException("첨부파일 게시글 조회를 실패하였습니다.");
		}
	}

	@GetMapping("write")
	public String writeAttm() {
		return "views/attm/write";
	}

	@PostMapping("insert")
	public String insertAttmBoard(@ModelAttribute Board b, @RequestParam("file") ArrayList<MultipartFile> files,
			HttpSession session) {
		String id = ((Member) session.getAttribute("loginUser")).getId();
		b.setBoardWriter(id);

		ArrayList<Attachment> list = new ArrayList<Attachment>();
		for (int i = 0; i < files.size(); i++) {
			MultipartFile upload = files.get(i);
			if (!upload.getOriginalFilename().equals("")) {
				String[] returnArr = saveFile(upload); // 파일 리네임 및 파일 저장소에 파일 저장
				if (returnArr[1] != null) {
					Attachment a = new Attachment();
					a.setOriginalName(upload.getOriginalFilename());
					a.setRenameName(returnArr[1]);
					a.setAttmPath(returnArr[0]);

					list.add(a);
				}
			}
		}

		for (int i = 0; i < list.size(); i++) {
			Attachment a = list.get(i);
			if (i == 0) {
				a.setAttmLevel(0);
			} else {
				a.setAttmLevel(1);
			}
		}

//		int result1 = 0;
//		int result2 = 0;
//		if(list.isEmpty()) {
//			b.setBoardType(1);
//			result1 = bService.insert(b);
//		} else {
//			b.setBoardType(2);
//			result2 = bService.insert(b);
//			
//			for(Attachment a : list) {
//				a.setRefBoardId(b.getBoardId());
//			}
//			result2 = bService.insertAttm(list);	
//		}
//		
//		if(result1 + result2 == list.size() + 1 ) { // 성공했을 때
//			if(result2 == 0) {
//				return "redirect:board/list";
//			} else {
//				return "redirect:/attm/list";
//			}
//		}else {
//			for(Attachment a : list) {
//				deleteFile(a.getRenameName());
//			}
//			throw new BoardException("첨부파일 게시글 작성을 실패하였습니다");
//		}

		boolean boardInserted = false;
		boolean filesInserted = false;

		if (list.isEmpty()) {
			b.setBoardType(1);
			boardInserted = bService.insert(b) > 0;
		} else {
			b.setBoardType(2);
			boardInserted = bService.insert(b) > 0;

			for (Attachment a : list) {
				a.setRefBoardId(b.getBoardId());
			}
			filesInserted = bService.insertAttm(list) > 0;
		}

		if (boardInserted && (list.isEmpty() || filesInserted)) {
			return list.isEmpty() ? "redirect:/board/list" : "redirect:/attm/list";
		} else {
			for (Attachment a : list) {
				deleteFile(a.getRenameName());
			}
			throw new BoardException("첨부파일 게시글 작성을 실패하였습니다");
		}

	}

	public String[] saveFile(MultipartFile upload) {
		String savePath = "c:\\uploadFiles";

		File folder = new File(savePath);
		if (!folder.exists()) {
			folder.mkdirs();
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyMMddHHmmssSSS");
		int ranNum = (int) (Math.random() * 100000);
		String originFileName = upload.getOriginalFilename();
		String renameFileName = sdf.format(new Date()) + ranNum
				+ originFileName.substring(originFileName.lastIndexOf("."));
		String renamePath = folder + "\\" + renameFileName;
		// "c:\\uploadFiles" \ 2025071513452732142873.png
		try {
			upload.transferTo(new File(renamePath));
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		} // try catch 해주기

		String[] returnArr = new String[2];
		returnArr[0] = savePath;
		returnArr[1] = renameFileName;

		return returnArr;
	}

	public void deleteFile(String renameName) {
		String savepath = "c:\\uploadFiles";

		File f = new File(savepath + "\\" + renameName);
		if (f.exists()) {
			f.delete();
		}
	}

	@GetMapping("{id}/{currentPage}")
	public String selectAttm(@PathVariable("id") int bId, @PathVariable("currentPage") int currentPage,
			HttpSession session, Model model) {
		Member loginUser = (Member) session.getAttribute("loginUser");

		Board b = bService.selectBoardList(bId, loginUser);
		// 첨부파일 게시글 상세보기를 하기 위한 첨부파일 select query
//		select *
//		from attachment
//		where attm_status = 'Y' and ref_board_id = ? 
//		order by attm_id
		ArrayList<Attachment> list = bService.selectAttmBoardList(bId);

		if (b != null) {
			model.addAttribute("b", b).addAttribute("page", currentPage).addAttribute("list", list);
			return "views/attm/detail";
		} else {
			throw new BoardException("첨부파일 게시글 상세보기를 실패하였습니다.");
		}
	}

	@PostMapping("updForm")
	public String updateForm(@RequestParam("boardId") int bId, @RequestParam("page") int page, Model model) {
		Board b = bService.selectBoardList(bId, null);
		ArrayList<Attachment> list = bService.selectAttmBoardList(bId);
		model.addAttribute("b", b).addAttribute("page", page);
		model.addAttribute("list", list);
		return "views/attm/edit";
	}

	@PostMapping("update")
	public String updateBoard(@ModelAttribute Board b, @RequestParam("file") ArrayList<MultipartFile> files,  
			@RequestParam("deleteAttm") String[] deleteAttm ,@RequestParam("page") int page) {
		
		System.out.println(b);
		System.out.println("삭제할 개수 : " + deleteAttm.length + "개" + Arrays.toString(deleteAttm));
		for(MultipartFile mf : files) {
			System.out.println("fileName : " + mf.getOriginalFilename());
		}

		//새파일이 있을때
		//1. 기존 파일을 모두 삭제할때 = 새파일에서 처음만든거 레벨 0으로 바꾸기
		//2. 기존 파일을 일부 삭제할때 = 가까운 이미지레벨을 0으로 바꾸기
		//3. 기존 파일을 모두 유지할때 = 그냥 리스트 추가

		//새파일이 없을때

		//1. 기존 파일을 모두 삭제할때 = 타입1로 바꾸기
		//2. 기존 파일을 일부 삭제할때 = 가까운 이미지레벨을 0으로 바꾸기
		//3. 기존 파일을 모두 유지할때 = 	
		
		
		// 1. 새 파일 O
		b.setBoardType(2);
		
		// 새로 넣는 파일이 있다면 ArrayList<Attachment> list에 옮겨담기
		ArrayList<Attachment> list = new ArrayList<Attachment>();
		for(int i=0; i < files.size(); i++) {
			MultipartFile upload = files.get(i);
			if(!upload.getOriginalFilename().equals("")) {
				String[] returnArr =  saveFile(upload);
				if(returnArr[1] != null) {
					Attachment a = new Attachment();
					a.setOriginalName(upload.getOriginalFilename());
					a.setRenameName(returnArr[1]);
					a.setAttmPath(returnArr[0]);
					a.setRefBoardId(b.getBoardId());
					
					list.add(a);
				}
			}
		}
		
		// 삭제한다는 파일이 있다면 삭제할 파일의 이름과 레벨을 각각 delRename과 delLevel에 옮겨담기
		ArrayList<String> delRename = new ArrayList<String>();
		ArrayList<Integer> delLevel = new ArrayList<Integer>();
		for(String rename : deleteAttm) {
			if(!rename.equals("")) {
				String[] split = rename.split("/");
				delRename.add(split[0]);
				delLevel.add(Integer.parseInt(split[1]));
			}
		}
		
	    int deleteAttmResult = 0;
	    boolean existBeforeAttm = true; //존재하냐 이전에 넣은 첨부파일이 존재하는지
		if(!delRename.isEmpty()) { 
			deleteAttmResult =  bService.deleteAttm(delRename);
			if(deleteAttmResult > 0) {
				for(String rename : delRename) {
					deleteFile(rename); //C드라이브에 있는 파일까지 같이 삭제
				}
			}
			
			
			if(deleteAttm.length == deleteAttmResult) { // 기존 파일을 모두 삭제할때
				existBeforeAttm = false;
				if(list.isEmpty()) { //새로 넣은 파일이 없을때   == 그럼 일반게시판 ( 모두 삭제하고 새로 넣은 파일도 없으니까 일반 게시판이다)
					b.setBoardType(1);
				}
			} else { // 기존 파일 중 일부 삭제
				//삭제된 파일들 중에 0이 있는지
				for(int level : delLevel) {
					if(level == 0) {
						bService.updateAttmLevel(b.getBoardId()); //가장 가까운걸 0으로 만들어버리기
						break;
					}
				}
			}		
		}
		
		// 새 파일이 있을 때 새 파일들에 대한 level 설정
		for(int i=0; i < list.size(); i++) {
			Attachment a = list.get(i);
			
			if(existBeforeAttm) {
				a.setAttmLevel(1);
			} else {
				if(i == 0) {
					a.setAttmLevel(0);
				} else {
					a.setAttmLevel(1);
				}
			}
		}
		
		int updateBoardResult = bService.updateBoard(b);
		
		int updateAttmResult = 0;
		if(!list.isEmpty()) {
			updateAttmResult = bService.insertAttm(list);
			
		}
		
		if(updateBoardResult + updateAttmResult == list.size() + 1) {
			if(deleteAttm.length != 0 && delRename.size() == deleteAttm.length && updateAttmResult == 0) {
				return "redirect:/board/list";
			} else {
				return String.format("redirect:/attm/%d/%d", b.getBoardId(), page);
			}
		} else {
			throw new BoardException("첨부파일 게시글 수정을 실패하였습니다.");
		}
	
	}
	
//	@PostMapping("delete")
//	public String deleteAttm(@RequestParam("boardId") int bId , @ModelAttribute Board b) {
//		int result1 = bService.delete(bId);
//		int result2 = bService.statusNAttm(bId);
//		
//		if(result1 > 0 && result2 > 0) {
//			return "redirect:/attm/list";
//		} else {
//			throw new BoardException("게시글 삭제를 실패했습니다.");
//		}
//	}
	
	
}
