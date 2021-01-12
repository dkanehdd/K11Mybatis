package mybatis;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

@Service
public interface MybatisDAOImpl {

	//게시물 갯수 카운트하기
	public int getTotalCount();
	//게시물을 select해서 List로 반환하기
	public ArrayList<MyBoardDTO> listPage(int s, int e);
	
	public void write(@Param("_name") String name,
			@Param("_contents") String contents,
			@Param("_id") String id);
}
