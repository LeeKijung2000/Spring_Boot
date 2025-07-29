package kh.springboot.board.model.service;

import java.util.ArrayList;

import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;

import kh.springboot.board.model.mapper.BoardMapper;
import kh.springboot.board.model.vo.Attachment;
import kh.springboot.board.model.vo.Board;
import kh.springboot.board.model.vo.PageInfo;
import kh.springboot.board.model.vo.Reply;
import kh.springboot.member.model.vo.Member;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {
		
	private final BoardMapper mapper;
	
	public int getListCount(int i) {
		return mapper.getListCount(i);
	}

	public ArrayList<Board> selectBoardList(PageInfo pi, int i) {
		int offset = (pi.getCurrentPage()-1)*pi.getBoardLimit();		
		RowBounds rowBounds = new RowBounds(offset, pi.getBoardLimit());
		return mapper.selectBoardList(i, rowBounds);
		
	}

	public int insert(Board board) {
		return mapper.insert(board);
	}

	public Board selectBoardList(int bId, Member loginUser) {
		
		Board b = mapper.selectBoard(bId);
		if(b != null) {
			if(loginUser != null && !b.getBoardWriter().equals(loginUser.getId())) {
				int result = mapper.updateCount(bId);
				if(result > 0) {
					b.setBoardCount(b.getBoardCount() + 1);
				}
			}
		}
		

		
		return b;
	}


	public int updateBoard(Board b) {
		// TODO Auto-generated method stub
		return mapper.updateBoard(b);
	}

	public int delete(int b) {
		
		return mapper.delete(b);
	}

	public ArrayList<Attachment> selectAttmBoardList(Integer bId) {
		return mapper.selectAttmBoardList(bId);
	}

	public int insertAttm(ArrayList<Attachment> list) {
	
		
//		int result = 0;
//		for(int i =0; i < list.size(); i ++) {
//			Attachment a = list.get(i);
//			result += mapper.insertAttm(a);
//		}
		//return result;
		 
		
		return mapper.insertAttm(list);
	}

	public int deleteAttm(ArrayList<String> delRename) {
		return mapper.deleteAttm(delRename);
	}

	public void updateAttmLevel(int boardId) {
		mapper.updateAttmLevel(boardId);	
	}

	public ArrayList<Board> selectTop() {
		return mapper.selectTop();
	}

	public ArrayList<Reply> selectReplyList(int bId) {
		return mapper.selectReplyList(bId);
	}

	public int insertReply(Reply r) {
		return mapper.insertReply(r);
	}

	public int rdelete(int rId) {
		return mapper.rdelete(rId);
	}

	public int rupdate(Reply reply) {
		return mapper.rupdate(reply);
	}


//	public int statusNAttm(int bId) {
//		return mapper.statusNAttm(bId);
//	}

	
}
