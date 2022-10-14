package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Constants;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.unit.DealGitApiUnit;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.base.utils.Tuple;
import com.spdb.fdev.fdevtask.spdb.dao.IFdevTaskDao;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.service.GitApiService;
import com.spdb.fdev.fdevtask.spdb.service.IAppApi;
import com.spdb.fdev.fdevtask.spdb.service.IFdevTaskService;
import com.spdb.fdev.fdevtask.spdb.service.SonarQubeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
@RefreshScope
public class SonarQubeServiceImpl implements SonarQubeService {

    private Logger log = LoggerFactory.getLogger(SonarQubeService.class);
    @Autowired
    private IFdevTaskDao fdevTaskDao;

    @Resource
    private IAppApi iAppApi;

    @Value("${git.token}")
    private String token;

    @Resource
    private DealGitApiUnit dealGitApiUnit;

    @Autowired
    private GitApiService gitApiService;

    @Autowired
    private IFdevTaskService fdevTaskService;

    @Autowired
    private IAppApi appApi;
    private static final String TIPS_SCAN = "sonar正在扫描请稍等";

    @Override
    public Tuple.Tuple3 getSonarScanInfo(String task_id) throws Exception {
        Integer scanBugs = 0;
        String sonar_scan_switch = "0";
        //根据任务id获取任务所属应用,分支名字
        FdevTask fdevTask = fdevTaskDao.selectTaskById(task_id);
        // 非Java微服务、后端组件和后端骨架，不sonar卡点
        if(!Constants.APPLICATION_JAVA.equals(fdevTask.getApplicationType())
                && !Constants.COMPONENT_SERVER.equals(fdevTask.getApplicationType())
                && !Constants.ARCHETYPE_SERVER.equals(fdevTask.getApplicationType())){
            log.info("非Java微服务、后端组件和后端骨架，不sonar卡点");
            return Tuple.tuple(sonar_scan_switch, scanBugs, "");
        }
        String feature_branch = Optional.ofNullable(fdevTask.getFeature_branch())
                .orElseThrow(
                        () -> new FdevException(ErrorConstants.TASK_ERROR, new String[]{"请先创建分支"})
                );
        // 判断是否合并过sit sit_merge_id
        //获取应用类型
        String flag = Constants.BRANCH_SIT;
        Map<String, Object> projectInfo = fdevTaskService.getProjectInfo(fdevTask.getProject_id(), fdevTask.getApplicationType());
        String gitlabId = (String) projectInfo.get(Dict.GITLAB_ID);
        sonar_scan_switch = CommonUtils.isNotNullOrEmpty((String) projectInfo.get(Dict.SONARSCANSWITCH)) ? "0" : (String) projectInfo.get(Dict.SONARSCANSWITCH);
        if (Constants.APPLICATION_IOS.equals(fdevTask.getApplicationType())
                || Constants.APPLICATION_ANDROID.equals(fdevTask.getApplicationType())) {
            flag = feature_branch.replaceFirst("feature", "dev");
        }

        boolean merged = gitApiService.judgeMergeSit(feature_branch, gitlabId, flag);
        if (!merged) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"请先同步代码到sit"});
        }

        //判断是否没有扫描
        String scanTime = fdevTask.getScanTime();
        if (CommonUtils.isNullOrEmpty(scanTime)) {
            if ("1".equals(sonar_scan_switch)) {
                log.info("sonar没有扫描时间,还没扫描");
                return Tuple.tuple(sonar_scan_switch, scanBugs, "请先执行sonar扫描");
            }
            log.info("sonar扫描还未完成,但不做卡点");
            return Tuple.tuple(sonar_scan_switch, scanBugs, "");
        }
        //判断是否扫描完成
        String sonarId = fdevTask.getSonarId();
        if (CommonUtils.isNullOrEmpty(sonarId)) {
            if ("1".equals(sonar_scan_switch)) {
                if(((Long)System.currentTimeMillis() - (Long)CommonUtils.dateParse(scanTime,CommonUtils.DATE_TIME_PATTERN).getTime())/1000/60/60 >= 2){
                    log.info("sonar扫描时间超过2小时");
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append("sonar扫描超时,请至任务详情重新扫描,项目名:").append(fdevTask.getProject_name()).append(",分支名:").append(feature_branch);
                    return Tuple.tuple(sonar_scan_switch, scanBugs, stringBuffer.toString() );
                }
                log.info("sonar扫描还未完成");
                return Tuple.tuple(sonar_scan_switch, scanBugs, "sonar正在扫描中,请稍后再试！");
            }
            log.info("sonar扫描还未完成,但不做卡点");
            return Tuple.tuple(sonar_scan_switch, scanBugs, "");
        }
        //判断扫描是否出错
        if (sonarId.contains("fwebhook")) {
            if ("1".equals(sonar_scan_switch)) {
                log.info("扫描出错");
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("sonar扫描出错,请至任务详情重新扫描,项目名:").append(fdevTask.getProject_name()).append(",分支名:").append(feature_branch);
                return Tuple.tuple(sonar_scan_switch, scanBugs, stringBuffer.toString());
            }
            log.info("sonar扫描还未完成,但不做卡点");
            return Tuple.tuple(sonar_scan_switch, scanBugs, "");
        }
        //调应用模块 传递分支名字和project_id,sonarId
        Map scanInfo = iAppApi.getSonarScanInfo(fdevTask.getProject_id(), feature_branch, sonarId);
        if (CommonUtils.isNullOrEmpty(scanInfo) && "1".equals(sonar_scan_switch)) {
            return Tuple.tuple(sonar_scan_switch, scanBugs, "查询扫描结果失败，请尝试重新合并代码");
        }
        String status = (String) scanInfo.get("status");
        if ("SUCCESS".equals(status)) {
            scanBugs = Integer.valueOf(Optional.ofNullable((String) scanInfo.get("bugs")).orElse("0"));
            StringBuffer sb = new StringBuffer();
            if (scanBugs > 0) {
                sb.append("当前分支存在未修复的扫描红线问题:").append(scanBugs).append("个,详见任务详情里的代码分析。建议修复后再合并提测");
                return Tuple.tuple(sonar_scan_switch, scanBugs, sb.toString());
            }
            return Tuple.tuple(sonar_scan_switch, scanBugs, "");
        }
        //将返回结果返回给前段 正在扫描或者 扫描存在bug
        if ("PENDING".equals(status) && "1".equals(sonar_scan_switch)) {
            return Tuple.tuple(sonar_scan_switch, scanBugs, "sonar正在扫描,请耐心等待！");
        }
        if ("1".equals(sonar_scan_switch)) {
            return Tuple.tuple(sonar_scan_switch, scanBugs, "请重新执行sonar扫描！");
        }
        return Tuple.tuple(sonar_scan_switch, scanBugs, "");
    }

    @Override
    public String updateTaskSonarId(String featureId, String webNamEn, String sonarId, String scanTime) {
        long row = fdevTaskDao.updateTaskByBranchAndWebName(featureId, webNamEn, sonarId, scanTime);
        if (row > 0) {
            log.info("更新成功，影响行数为:{}", row);
            return "SUCCESS";
        }
        log.info("更新失败featureId：{}，webNamEn：{}，SonarId：{},scanTime:{}", featureId, webNamEn, sonarId, scanTime);
        return "FAIL";
    }

    @Override
    public Tuple.Tuple3 getScanProcess(String taskId) {
        FdevTask fdevTask = fdevTaskDao.selectTaskById(taskId);
        String scanTime = fdevTask.getScanTime();
        String sonarId = fdevTask.getSonarId();

        //判断分支是否存在
        String feature_branch = Optional.ofNullable(fdevTask.getFeature_branch())
                .orElseThrow(
                        () -> new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"请先创建分支"})
                );
        //存在time
        if (CommonUtils.isNotNullOrEmpty(scanTime) && CommonUtils.isApp(fdevTask.getApplicationType())) {
            //不存在sonar 返回正在扫描
            if (CommonUtils.isNullOrEmpty(sonarId)) {
                return Tuple.tuple(0, "正在执行于" + scanTime + "触发的sonar扫描，请耐心等待！", scanTime);
            }
            //存在 根据sornarid查询一下
            if (sonarId.contains("fwebhook")) {
                return Tuple.tuple(4, "在" + scanTime + "触发的sonar扫描出错", scanTime);
            }
            //调应用模块 传递分支名字和project_id,sonarId
            Map scanInfo = iAppApi.getSonarScanInfo(fdevTask.getProject_id(), feature_branch, sonarId);
            if (CommonUtils.isNullOrEmpty(scanInfo)) {
                return Tuple.tuple(2, "", scanTime);
            }
            String status = (String) scanInfo.get("status");
            if ("SUCCESS".equals(status)) {
                return Tuple.tuple(3, "", scanTime);
            }
            //将返回结果返回给前段 正在扫描或者 扫描存在bug
            if ("PENDING".equals(status)) {
                return Tuple.tuple("0", "sonar正在扫描,请耐心等待！", scanTime);
            }
            return Tuple.tuple("2", "", scanTime);
        }
        // 直接返回
        return Tuple.tuple(1, "当前分支未执行过sonar扫描", "");
    }

}
