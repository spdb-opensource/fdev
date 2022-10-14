package fdev.release;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.spdb.fdev.base.dict.Dict;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.spdb.fdev.release.entity.ReleaseApplication;
import com.spdb.fdev.release.entity.ReleaseTask;
import com.spdb.fdev.release.service.IReleaseApplicationService;
@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ReleaseApplicationTests {
	@Autowired
	private IReleaseApplicationService releaseApplicationService;
	//@Test
	public void ReleaseApplication() throws Exception {
		Date date = new Date();
		System.out.print(date);
	}

	@Test
	public void test()throws Exception{
		String application_id =new String("ims-jnl-test-111"); //(String) requestParam.get(Dict.APPLICATION_ID);
		Integer gitlab_project_id =3230;// (Integer) requestParam.get(Dict.GITLAB_PROJECT_ID);
		String release_branch ="2222";//(String) requestParam.get(Dict.RELEASE_BRANCH);
		//若无应用id，则用git项目id查询其应用id
		application_id = releaseApplicationService.queryApplicationId(application_id, gitlab_project_id);
		//通过应用id 和 rel分支名查询 投产应用的 UAT环境
		//String uatEnv = releaseApplicationService.queryUatEnv(application_id, release_branch);
		//System.out.println(uatEnv);
	}

}
