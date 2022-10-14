package com.plan.util;

import com.plan.dao.WorkOrderMapper;
import com.plan.dict.Dict;
import com.plan.dict.ErrorConstants;
import com.plan.service.IUserService;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.rediscluster.RedisUtils;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

@Component
@RefreshScope
public class MyUtil {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtils redisUtils;
    @Value("${isSendMail}")
    private boolean isSendMail;
    @Autowired
    private MailUtil mailUtil;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private IUserService userService;
    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Autowired
    WebApplicationContext applicationContext;


    public void sendFastMail(HashMap model, Set<String> person, String key, String workNo) throws Exception {
        if(isSendMail){  //邮件开关
            Set<String> emails = new HashSet();
//            Set<String> fdevUsers = new HashSet();
//            //判断是否是fdev创建的工单
//            String workFlag = workOrderMapper.queryWorkFlagByWorkNo(workNo);
//            if(workFlag != null && workFlag.equals("1")) {
//                try {
//                    fdevUsers = getFdevUsersEmail(workOrderMapper.queryMainTaskNoByWorkNo(workNo));
//                }catch (Exception e){
//                    logger.error("邮件通知fdev人员失败");
//                    logger.error(e.getMessage());
//                }
//            }
//            person.addAll(fdevUsers);
            Map<String, String> ftmsPeople = workOrderMapper.queryFtmsPeopleByWorkNo(workNo);
            if(!Util.isNullOrEmpty(ftmsPeople)){
                Set<String> ftmsSet = new HashSet<>();
                ftmsSet.addAll(Arrays.asList(ftmsPeople.get(Dict.TESTERS).split(",")));
                ftmsSet.addAll(Arrays.asList(ftmsPeople.get(Dict.WORKLEADER).split(",")));
                ftmsSet.addAll(Arrays.asList(ftmsPeople.get(Dict.WORKMANAGER).split(",")));
                person.addAll(ftmsSet);
            }
            person.remove(null);
            for (String s : person) {
                Map sendData = new HashMap();
                sendData.put(Dict.USER_NAME_EN, s);
                sendData.put(Dict.REST_CODE, "fdev.fuser.queryByUserCoreData");
                ArrayList<LinkedHashMap> resData = (ArrayList) restTransport.submitSourceBack(sendData);
                if (!Util.isNullOrEmpty(resData)) {
                    emails.add( (String) resData.get(0).get(Dict.EMAIL));
                }
            }
            String[] to = emails.toArray(new String[emails.size()]);
            mailUtil.sendTaskMail(key, model, workNo, to);
        }
    }

    public Set<String> getFdevUsersEmail(String mainTaskNo){
        if (Util.isNullOrEmpty(mainTaskNo)) {
            logger.error("主任务编号为空，通知fdev开发人员失败");
            throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"主任务编号为空"});
        }
        Map mm = new HashMap<String, String>();
        mm.put(Dict.ID, mainTaskNo);
        mm.put(Dict.REST_CODE, "fdev.ftask.queryTaskDetail");
        try{
            mm = (LinkedHashMap) restTransport.submitSourceBack(mm);
        }catch (Exception e){
            logger.error("查询fdev任务失败");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"查询fdev任务失败"});
        }
        Set<String> fdevUsers = new HashSet<>();
        //获取 任务负责人
        makeFdevUserName(mm, fdevUsers, Dict.MASTER);
        //获取 行方管理员
        makeFdevUserName(mm, fdevUsers, Dict.SPDB_MASTER);
        //拼接 开发人员
        makeFdevUserName(mm, fdevUsers, Dict.DEVELOPER);

        return fdevUsers;
    }

    /**
     * 将 fdev任务明细中
     *  人员信息数据提取出来
     */
    public void makeFdevUserName(Map fdevDate, Set<String> fdevUsers, String key){
        for (Map k : (ArrayList<Map>) fdevDate.get(key)){
            fdevUsers.add((String) k.get(Dict.USER_NAME_EN));
        }
    }


    /**
     * 转换，把project 的int值转换成 String
     */
    public String getSysName(Integer name){
        if (name == null)
            return "";
        switch (name){
            case 1:
                return "测试1";
            case 5:
                return "mantis接口测试项目";
            case 7:
                return "互联板块";
            case 8:
                return "财富版块";
            case 9:
                return "微生活板块";
            case 10:
                return "转账板块";
            case 11:
                return "零售公共";
            case 12:
                return "APP板块";
            case 13:
                return "账户板块";
            case 14:
                return "生态板块";
            case 15:
                return "公司金融";
            case 16:
                return "支付商务";
            case 17:
                return "移动PAD";
            case 18:
                return "公共服务";
            case  28:
                return "新SIT测试缺陷";
            case  29:
                return "生产问题总结-new测试";
            case  30:
                return "新REL测试缺陷";
            case  31:
                return "生产问题总结-newRel测试";
            case  32:
                return "新生产问题总结";
            case  33:
                return "生产缺陷";
            default:
                return name+"";
        }
    }

    /**
     * 严重性，数字转中文
     */
    public String getChServerity(String serverity){
        if (serverity == null)
            return "";
        switch (serverity){
            case "10":
                return "新功能";
            case "20":
                return "细节";
            case "30":
                return "文字";
            case "40":
                return "小调整";
            case "50":
                return "小错误";
            case "60":
                return "很严重";
            case "70":
                return "崩溃";
            case "80":
                return "宕机";
            default:
                return  serverity;
        }
    }


    /**
     * 优先级， 数字转中文
     */
    public String getChPriority(String priority){
        if (priority == null)
            return "";
        switch (priority){
            case "10":
                return "无";
            case "20":
                return "低";
            case "30":
                return "中";
            case "40":
                return "高";
            case "50":
                return "紧急";
            case "60":
                return "非常紧急";
            default:
                return priority;
        }
    }

    /**
     * 将状态值 status 转为中文
     */
    public String getChStatus(String priority){
        if (priority == null)
            return "";
        switch (priority){
            case "10":
                return "新建";
            case "20":
                return "拒绝";
            case "30":
                return "确认拒绝";
            case "40":
                return "延迟修复";
            case "50":
                return "打开";
            case "80":
                return "已修复";
            case "90":
                return "关闭";
            default:
                return priority;
        }
    }

    /**
     * 根据fdev提供的groupId，得到玉衡对应的groupId
     * @param fdevGroupId
     * @return
     * @throws Exception
     */
    public Map<String, String> getFtmsGroupId(String fdevGroupId) throws Exception {
        Map<String, String> resultMap = new HashMap<>();
        //根据fdev组id获取玉衡组id
        String ftmsGroupId = workOrderMapper.queryFtmsGroupId(fdevGroupId);
        //如果查不到，发接口查fdev组的父组id，再用父组id查玉衡组id，循环直到查出玉衡组id
        while (Util.isNullOrEmpty(ftmsGroupId)) {
            Map<String, String> sendMap = new HashMap<>();
            sendMap.put(Dict.ID, fdevGroupId);
            sendMap.put(Dict.REST_CODE, "queryChildGroupById");
            try {
                List<Map<String,String>> resultList = (List<Map<String, String>>) restTransport.submit(sendMap);
                fdevGroupId = resultList.get(0).get(Dict.PARENTID);
            } catch (Exception e) {
                logger.info("根据fdev子组id查父组id失败");
                return resultMap;
            }
            ftmsGroupId = workOrderMapper.queryFtmsGroupId(fdevGroupId);
        }
        resultMap.put(Dict.FTMSGROUPID, ftmsGroupId);
        resultMap.put(Dict.FDEVGROUPID, fdevGroupId);
        return resultMap;
    }
    public  String isNewFtms() {
        if (applicationContext.getServletContext().getContextPath().contains("-new")) {
            return "1";
        }
        return "0";
    }
}
