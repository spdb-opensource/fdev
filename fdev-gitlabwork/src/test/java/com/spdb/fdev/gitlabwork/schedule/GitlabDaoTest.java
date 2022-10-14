package com.spdb.fdev.gitlabwork.schedule;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.gitlabwork.dao.IGitLabUserDao;
import com.spdb.fdev.gitlabwork.dao.IGitlabCommitDao;
import com.spdb.fdev.gitlabwork.dao.IGitlabProjectDao;
import com.spdb.fdev.gitlabwork.entiy.GitlabCommit;
import com.spdb.fdev.gitlabwork.entiy.GitlabProject;
import com.spdb.fdev.gitlabwork.entiy.GitlabUser;
import com.spdb.fdev.gitlabwork.util.Util;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class GitlabDaoTest {

    @Autowired
    private IGitlabCommitDao gitlabCommitDao;

    @Autowired
    private IGitLabUserDao gitLabUserDao;

    @Resource
    private MongoTemplate mongoTemplate;

    @Autowired
    IGitlabProjectDao gitlabProjectDao;

    @Test
    public void getAllCommitList() {
        String committed_date = "2019-07-22";
        GitlabUser gitlabUser = gitLabUserDao.selectByUserId("5cd13321954ad500064ebcfc");
        List<GitlabCommit> gitlabCommitList = gitlabCommitDao.selectGitlabCommitInfo(gitlabUser.getUsername(), gitlabUser.getName(), gitlabUser.getGituser(), gitlabUser.getConfigname(), committed_date);
        for (GitlabCommit commit : gitlabCommitList) {
            System.out.println("name=" + commit.getShort_id());
        }
    }


    @Test
    public void getCommitListByProjectName() {
        GitlabUser gitlabUser = gitLabUserDao.selectByUserId("5d3ffa18606eeb000a22d373");
        String committed_date = "2019-09-25";
        List<String> projectlist = new ArrayList<>();
        projectlist.add("mspmk-web-finance");
        projectlist.add("mspmk-web-geek");
        List<GitlabCommit> gitlabCommitList = gitlabCommitDao.selectGitlabCommitInfo(gitlabUser.getUsername(), gitlabUser.getName(), gitlabUser.getGituser(), gitlabUser.getConfigname(), committed_date, projectlist);
        for (GitlabCommit commit : gitlabCommitList) {
            System.out.println("name=" + commit.getShort_id());
        }
    }

    @Test
    public void getCommits() {
        Query query = new Query();
        String userName = "xxx";
        String committed_date = "2019-07-02";
        //query.addCriteria(new Criteria().orOperator(Criteria.where(Dict.COMMITTER_NAME).regex(userName, "i").and(Dict.COMMITTED_DATE).is(committed_date)));
        query.addCriteria(new Criteria().orOperator(Criteria.where(Dict.COMMITTER_NAME).is(userName).and(Dict.COMMITTED_DATE).is(committed_date)));
        List<GitlabCommit> gitlabCommitList = mongoTemplate.find(query, GitlabCommit.class);
        for (GitlabCommit commit : gitlabCommitList) {
            System.out.println(commit.getCommitter_name() + commit.getShort_id());
        }

    }

    @Test
    public void getCommit() {
        GitlabUser gitlabUser = gitLabUserDao.selectByUserId("5d3ffa18606eeb000a22d373");
        List<GitlabProject> gitlabProjectList = gitlabProjectDao.selectBySign(2);
        List<String> projectList = new ArrayList();
        for (GitlabProject gitlabProject : gitlabProjectList) {
            String projectName = gitlabProject.getNameEn();
            projectList.add(projectName);
        }
        //String committed_date = Util.getDateBefore();
        String committed_date = "2019-09-29";
        List<GitlabCommit> gitlabCommitList = gitlabCommitDao.selectGitlabCommitInfo(gitlabUser.getName(), gitlabUser.getUsername(), gitlabUser.getGituser(), gitlabUser.getConfigname(), committed_date, projectList);
        System.out.println(gitlabCommitList.size());
    }

    @Test
    public void testPattertIngore() throws ParseException {
        String startDate = "2019-07-01";
        String endDate = "2019-10-28";
        GitlabUser gitlabUser = gitLabUserDao.selectByUserId("5d3ffa18606eeb000a22d373");
        List<String> dateList = Util.getBetweenDates(startDate, endDate);
        List<GitlabCommit> result = new ArrayList<>();
        for (String committed_date : dateList) {
            List<GitlabCommit> gitlabCommitList = gitlabCommitDao.selectGitlabCommitInfo(gitlabUser.getName(), gitlabUser.getUsername(), gitlabUser.getGituser(), gitlabUser.getConfigname(), committed_date);
            result.addAll(gitlabCommitList);
        }
        System.out.println(result.size());
        for (GitlabCommit commit : result) {
            System.out.print(commit.getCommitter_name() + "\t");
        }
    }
}
