package com.spdb.fdev.freport.spdb.service.impl;

import com.spdb.fdev.freport.base.dict.DashboardDict;
import com.spdb.fdev.freport.base.dict.Dict;
import com.spdb.fdev.freport.base.utils.CommonUtils;
import com.spdb.fdev.freport.spdb.dao.GitDao;
import com.spdb.fdev.freport.spdb.dao.ReportDao;
import com.spdb.fdev.freport.spdb.dao.UserDao;
import com.spdb.fdev.freport.spdb.entity.git.Commit;
import com.spdb.fdev.freport.spdb.entity.sonar.SonarProjectRank;
import com.spdb.fdev.freport.spdb.entity.user.User;
import com.spdb.fdev.freport.spdb.service.RankingService;
import com.spdb.fdev.freport.spdb.vo.Ranking;
import com.spdb.fdev.freport.spdb.vo.RankingVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RankingServiceImpl implements RankingService {

    private static final Logger logger = LoggerFactory.getLogger(RankingServiceImpl.class);

    @Autowired
    private GitDao gitDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ReportDao reportDao;

    @Override
    public RankingVo queryUserCommit(String rankingType, String startDate, String endDate) throws Exception {
        List<Ranking> result = new ArrayList<>();//定义结果集
        List<User> users = userDao.find(new User() {{//git用户名-中文名 map
            setGroupId(CommonUtils.getSessionUser().getGroup_id());
            setStatus("0");//不查离职
        }});
        //list转换map
        Map<String, String> gitUserEmailCnMap = users.stream().collect(Collectors.toMap(User::getEmail, User::getUserNameCn, (x, y) -> {
            return x + "," + y;//合并策略，重复的时候属于脏数据
        }));
        //准备数据源
        if (!CommonUtils.isNullOrEmpty(gitUserEmailCnMap)) {
            List<Commit> commitList = gitDao.findByCommitterEmail(gitUserEmailCnMap.keySet(), startDate, endDate, null);
            Map<String, Integer> userCommitTotalMap = new HashMap<>();//定义user提交行数map
            Map<String, List<Commit>> userCommitMap = commitList.stream().collect(Collectors.groupingBy(Commit::getCommitterEmail));
            for (String gitlabUserEmail : userCommitMap.keySet()) {
                Integer userCommit = 0;
                for (Commit item : userCommitMap.get(gitlabUserEmail)) {
                    userCommit += item.getStats().getTotal();
                }
                userCommitTotalMap.put(gitUserEmailCnMap.get(gitlabUserEmail), userCommit);
            }
            for (String userCnName : userCommitTotalMap.keySet()) {
                if (userCnName != null) {
                    result.add(new Ranking() {{
                        setLabel(userCnName);
                        setValue(new BigDecimal(userCommitTotalMap.get(userCnName)));
                    }});
                }
            }
            switch (rankingType) {//根据条件过滤
                case Dict.TOP_TEN:
                default:
                    result = result.stream().sorted(Comparator.comparing(Ranking::getValue).reversed()).limit(10).collect(Collectors.toList());
                    break;
                case Dict.LAST_TEN:
                    result = result.stream().sorted(Comparator.comparing(Ranking::getValue)).limit(10).collect(Collectors.toList());
                    break;
            }
        }
        return new RankingVo(reportDao.findDashboardTips(DashboardDict.USER_COMMIT).getContent(), result);
    }

    @Override
    public RankingVo querySonarProjectRank(String code) throws Exception {
        SonarProjectRank sonarProjectRankByCode = reportDao.findSonarProjectRankByCode(code);
        return new RankingVo(reportDao.findDashboardTips(DashboardDict.SONAR_PROJECT_RANK + "_" + code).getContent(), !CommonUtils.isNullOrEmpty(sonarProjectRankByCode) ? sonarProjectRankByCode.getData() : new ArrayList<>());
    }
}