package com.kosmo.k11mybatis;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import mybatis.MemberVO;
import mybatis.MyBoardDTO;
import mybatis.MybatisDAOImpl;
import mybatis.MybatisMemberImpl;
import util.PagingUtil;

@Controller
public class MybatisController {

	/*
	servlet-context.xml에서 생성한 빈을 자동으로 주입받아
	Mybatis를 사용할 준비를 한다.
	 */
	@Autowired
	private SqlSession sqlSession;
	
	//방명록 리스트
	@RequestMapping("/mybatis/list.do")
	public String list(Model model, HttpServletRequest req) {
		
		//게시물의 갯수를 카운트.
		/*
		서비스객체 역할을 하는 인터페이스에 정의된 추상메소드를 호출하면
		최종적으로 Mapper의 동일한 id속성값을 가진 엘리먼트의 쿼리문이
		실행되어 결과를 반환받게 된다.
		 */
		int totalRecordCount = 
				sqlSession.getMapper(MybatisDAOImpl.class).getTotalCount();
		//페이지 처리를 위한 설정값
		int pageSize = 4;
		int blockPage = 2;
		//전체페이지수 계산
		int totalPage = (int)Math.ceil((double)totalRecordCount/pageSize);
		
		//현재페이지 번호 가져오기
		int nowPage = req.getParameter("nowPage")==null? 1:
			Integer.parseInt(req.getParameter("nowPage"));
		//select할 게시물의 구간을 계산
		int start = (nowPage-1)*pageSize + 1;
		int end = nowPage * pageSize;
		
		//Mapper 호출
		ArrayList<MyBoardDTO> lists = sqlSession.getMapper(MybatisDAOImpl.class)
				.listPage(start, end);
		//페이지번호 처리
		String pagingImg = 
				PagingUtil.pagingImg(totalRecordCount, pageSize, blockPage, nowPage,
						req.getContextPath()+"/mybatis/list.do?");
		
		model.addAttribute("pagingImg", pagingImg);
		//게시물의 줄바꿈 처리
		for(MyBoardDTO dto : lists) {
			String temp = 
					dto.getContents().replace("\r\n", "<br/>");
			dto.setContents(temp);
		}
		
		model.addAttribute("lists", lists);
		return "07Mybatis/list";
	}
	
	@RequestMapping("/mybatis/write.do")
	public String write(Model model, HttpSession session, HttpServletRequest req)
	{
		if(session.getAttribute("siteUserInfo")==null) {
			model.addAttribute("backUrl", "07Mybatis/write");
			return "redirect:login.do";
		}
		
		return "07Mybatis/write";
	}
	
	@RequestMapping("/mybatis/login.do")
	public String login(Model model) {
		return "07Mybatis/login";
	}
	
	@RequestMapping("/mybatis/loginAction.do")
	public ModelAndView loginAction(HttpServletRequest req, HttpSession session) {
		
		MemberVO vo = sqlSession.getMapper(MybatisMemberImpl.class).login(
				req.getParameter("id"), req.getParameter("pass"));
		
		ModelAndView mv = new ModelAndView();
		if(vo==null) {
			mv.addObject("LoginNG", "아이디/패스워드가 틀렸습니다.");
			mv.setViewName("07Mybatis/login");
			return mv;
		}
		else {
			session.setAttribute("siteUserInfo", vo);
		}
		
		String backUrl = req.getParameter("backUrl");
		if(backUrl==null|| backUrl.equals("")) {
			mv.setViewName("07Mybatis/login");
		}
		else {
			mv.setViewName(backUrl);
		}
		return mv;
	}
	
	@RequestMapping(value="/mybatis/writeAction.do", method = RequestMethod.POST)
	public String writeAction(Model model, HttpServletRequest req,
			HttpSession session) {
		if(session.getAttribute("siteUserInfo")==null) {
			return "redirect:login.do";
		}
		//Mybatis 사용
		sqlSession.getMapper(MybatisDAOImpl.class).write(
				req.getParameter("name"), req.getParameter("contents"),
				((MemberVO)session.getAttribute("siteUserInfo")).getId());
		
		return "redirect:list.do";
	}
		
	
}
