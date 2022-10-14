package fdev.release;

import java.util.Map;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.release.FdevReleaseApplication;
import com.spdb.fdev.release.service.IAppService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.spdb.fdev.release.service.IReleaseTaskService;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes= {FdevReleaseApplication.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ReleaseTaskTests {
	@Autowired
	private IReleaseTaskService releaseTaskService;
	@Autowired
	private IAppService appService;
	@Test
	public void ReleaseTask() throws Exception {
		String application_id = "5d897c551b6f94000fb914e3";
		Map<String, Object> application = appService.queryAPPbyid(application_id);
		String network = application.get(Dict.NETWORK).toString();
		if(network.indexOf(",")!= -1) {
			network ="dmz";
		}
		String uat_env_name = appService.queryByLabelsFuzzy(Dict.UAT_LOWER, network);
		String rel_env_name = appService.queryByLabelsFuzzy(Dict.REL.toLowerCase(), network);
		System.out.println(uat_env_name);
		System.out.println(rel_env_name);
	}
}
