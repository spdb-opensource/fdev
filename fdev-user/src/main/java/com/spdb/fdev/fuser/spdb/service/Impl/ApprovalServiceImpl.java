package com.spdb.fdev.fuser.spdb.service.Impl;

import java.util.*;
import javax.annotation.Resource;

import com.spdb.fdev.fuser.spdb.service.VacationService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fuser.base.dict.Constants;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.ApprovalDao;
import com.spdb.fdev.fuser.spdb.dao.UserDao;
import com.spdb.fdev.fuser.spdb.entity.user.Company;
import com.spdb.fdev.fuser.spdb.entity.user.Group;
import com.spdb.fdev.fuser.spdb.entity.user.NetApproval;
import com.spdb.fdev.fuser.spdb.entity.user.User;
import com.spdb.fdev.fuser.spdb.service.ApprovalService;
import com.spdb.fdev.fuser.spdb.service.MailService;
import com.spdb.fdev.fuser.spdb.service.RoleService;

@Service
@RefreshScope
public class ApprovalServiceImpl implements ApprovalService {

    private Logger logger = LoggerFactory.getLogger(ApprovalServiceImpl.class);

    @Value("${net.approval.kf.mail}")
    private List<String> kfMailList;

    @Value("${net.approval.kf.off.mail}")
    private List<String> kfOffMailList;

    @Value("${net.approval.kf.off.batch.mail}")
    private List<String> kfOffBatchMailList;

    @Value("${net.approval.vdi.mail}")
    private List<String> vdiMailList;

    @Value("${net.approval.vm.mail}")
    private List<String> vmMailList;

    @Value("${send.KfOff.email.domain}")
    private String sendKfOffEmailDomain;

    @Resource
    private ApprovalDao approvalDao;

    @Resource
    private UserDao userDao;

    @Resource
    private MailService mailService;

    @Resource
    private RoleService roleService;

    @Resource
    private VacationService vacationService;

    /**
     * 查询网络审核列表
     *
     * @throws Exception
     */
    @Override
    public Map queryApprovalList(Map param) throws Exception {
        List<String> ids = approvalDao.queryUserIdsByCompany(param);
        param.put(Dict.IDS, ids);
        Map map = approvalDao.queryApprovalList(param);
        if (null != map.get("list") && ((List<Map>) map.get("list")).size() > 0) {
            List<Map> list = (List<Map>) map.get("list");
            reformUser(list);
        }
        return map;
    }

    @Override
    public void updateApproval(Map param) throws Exception {
        List<Map> approvals = approvalDao.queryWaitApproveList(param);
        if (CollectionUtils.isEmpty(approvals)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"未查询到相关审核记录！"});
        }
        reformUser(approvals);
        String type = (String) param.get("type");
        approvalDao.updateApproval(param);

        Set<String> to = new HashSet<>();
        String refuseTemplate = "";

        if (Dict.KF_APPROVAL.equals(type)) {
            to.addAll(kfMailList);
            refuseTemplate = "kf_approval_refused";
        } else if (Dict.KF_OFF_APPROVAL.equals(type)) {
            to.addAll(kfOffMailList);
        } else if (Dict.KF_OFF_BATCH_APPROVAL.equals(type)) {
            to.addAll(kfOffBatchMailList);
        } else if (Dict.VDI_APPROVAL.equals(type)) {
            to.addAll(vdiMailList);
            refuseTemplate = "vdi_approval_refused";
        } else if (Dict.VM_APPROVAL.equals(type)) {
            to.addAll(vmMailList);
            refuseTemplate = "vm_approval_refused";
        }

        //申请人邮箱列表
        HashSet<String> applicantEmails = new HashSet<>();
        approvals.forEach(s -> {
            Map map = (Map) s.get("applicant");
            if (!StringUtils.isEmpty(map.get(Dict.EMAIL))) {
                applicantEmails.add((String) map.get(Dict.EMAIL));
            }
        });

        if (Dict.PASSED.equals(param.get(Dict.STATUS))) {//审核通过
            to.addAll(applicantEmails);
            //发送邮件提醒进行网络审核相关操作
            HashMap model = new HashMap<>();
            model.put("approvalList", approvals);
            mailService.sendEmail(Constants.NET_APPROVAL, type, model, to.toArray(new String[to.size()]));
        } else if (!CommonUtils.isNullOrEmpty(refuseTemplate)) {//审核不通过且需要发送邮件通知
            //发送邮件提醒申请人 审核被拒绝
            HashMap model = new HashMap<>();
            model.put(Dict.NAME, param.get(Dict.USER_NAME_CN));
            mailService.sendEmail(Constants.NET_APPROVAL, refuseTemplate, model, applicantEmails.toArray(new String[applicantEmails.size()]));
        }
    }

    private List<Map> reformUser(List<Map> list) throws Exception {
        for (Map approval : list) {
            User user = new User();
            user.setId((String) approval.get("applicant_id"));
            List<Map> users = userDao.getUser(user);
            User emptyUser = new User();
            Map emptyUserMap = CommonUtils.obj2Map(emptyUser);
            emptyUserMap.put(Dict.COMPANY, new Company());
            emptyUserMap.put(Dict.GROUP, new Group());
            if (!users.isEmpty()) {
                approval.put("applicant", users.get(0));
            } else {
                approval.put("applicant", emptyUserMap);
            }
            user.setId((String) approval.get("user_id"));
            List<Map> users1 = userDao.getUser(user);
            if (!users1.isEmpty()) {
                approval.put("user", users1.get(0));
            } else {
                approval.put("user", emptyUserMap);
            }
        }
        return list;
    }

    /**
     * 新增用户时，给approval新插入数据
     *
     * @param netApproval
     * @throws Exception
     */
    @Override
    public void addApprovalByUser(NetApproval netApproval) throws Exception {
        this.approvalDao.addApprovalByUser(netApproval);
    }

    /**
     * 查询approval数据，按条件查询
     *
     * @param netApproval
     * @return
     * @throws Exception
     */
    @Override
    public List<Map> queryApproval(NetApproval netApproval) throws Exception {
        return this.approvalDao.queryApproval(netApproval);
    }

    /**
     * 更新NetApproval数据，
     *
     * @param netApproval 其中的id为查找记录用的，其他字段为设置需要修改的字段所用的
     * @throws Exception
     */
    @Override
    public void updateApprovalStatus(NetApproval netApproval) throws Exception {
        this.approvalDao.updateApprovalStatus(netApproval);
    }

    /**
     * 给所有关闭标志字段增加1和邮件提醒网络审核员审核
     */
    @Override
    public void addOffFlagAndNotify() throws Exception {
        //查询被关闭kf网络申请的数据（off_flag字段大于等于2）
        if (!this.vacationService.checkTodayIsVacation()) {
            int count = approvalDao.addAllOffFlag();
            logger.info("kf网络白名单关闭标志字段自增数据条数：{}", count);
        }
        NetApproval netApproval = new NetApproval();
        netApproval.setType(Dict.KF_APPROVAL);
        netApproval.setOff_flag(6);
        netApproval.setStatus(Dict.PASSED);
        //将off_flag字段自增，如果达到6，就是表明已经过了4天没有点击继续使用，则提关闭申请
        List<Map> queryApproval = approvalDao.queryApproval(netApproval);
        if (!CommonUtils.isNullOrEmpty(queryApproval)) {
            for (Map map : queryApproval) {
                String openPassId = (String) map.get(Dict.ID);
                NetApproval closeApproval = new NetApproval();
                closeApproval.setId(openPassId);
                closeApproval.setStatus(Dict.WAIT_APPROVE);
                closeApproval.setType(Dict.KF_OFF_BATCH_APPROVAL);
                //closeApproval.setCreate_time(CommonUtils.formatDate(CommonUtils.INPUT_DATE));
                closeApproval.setUpdate_time(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
                approvalDao.updateApprovalStatus(closeApproval);
            }
        }
        //邮件提醒网络审核员
        //查询所有待审核
        NetApproval waitApprove = new NetApproval();
        waitApprove.setStatus(Dict.WAIT_APPROVE);
        List<Map> approvals = approvalDao.queryApproval(waitApprove);
        //发送邮件
        if (!CommonUtils.isNullOrEmpty(approvals)) {
            //查询角色为网络审核员的人员列表
            List<String> roleid = roleService.queryRoleid(Arrays.asList(Constants.NET_APPROVER));
            List<User> users = userDao.queryUserStatis(roleid, "0");
            if (!CommonUtils.isNullOrEmpty(users)) {
                List<String> emails = new ArrayList<>();
                for (User user : users) {
                    emails.add(user.getEmail());
                }
                HashMap model = new HashMap<>();
                model.put("count", approvals.size());
                mailService.sendEmail(Constants.NET_APPROVAL, "approval_notify", model, emails.toArray(new String[emails.size()]));
            }
        }
    }

    @Override
    public void sendKfOffEmail() throws Exception {
        //查询所有审核通过的手机kf网络审核记录
        NetApproval netApproval = new NetApproval();
        netApproval.setType(Dict.KF_APPROVAL);
        netApproval.setStatus(Dict.PASSED);
        List<Map> approvals = approvalDao.queryApproval(netApproval);
        if (!CommonUtils.isNullOrEmpty(approvals)) {
            //完善用户信息
            reformUser(approvals);
            for (Map map : approvals) {
                String id = (String) map.get(Dict.ID);
                Map user = (Map) map.get(Dict.USER);
                //用户邮箱不为空时发送邮件通知
                if (!StringUtils.isEmpty(user.get(Dict.EMAIL))) {
                    String email = (String) user.get(Dict.EMAIL);
                    HashMap model = new HashMap<>();
                    model.put("id", id);
                    model.put("sendKfOffEmailDomain", sendKfOffEmailDomain);
                    mailService.sendEmail(Constants.NET_APPROVAL, "batch_kf_off_notify", model, new String[]{email});
                    //邮件发送后将改记录关闭标识设置为2
                    NetApproval changeFlag = new NetApproval();
                    changeFlag.setId(id);
                    changeFlag.setOff_flag(2);
                    approvalDao.updateApprovalStatus(changeFlag);
                }
            }
        }
    }

}
