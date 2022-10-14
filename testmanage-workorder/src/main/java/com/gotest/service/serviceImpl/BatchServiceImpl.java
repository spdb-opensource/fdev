package com.gotest.service.serviceImpl;

import com.gotest.dao.BatchMapper;
import com.gotest.dao.GroupMapper;
import com.gotest.dict.Dict;
import com.gotest.domain.WorkOrder;
import com.gotest.service.BatchService;
import com.gotest.service.IDemandService;
import com.test.testmanagecommon.rediscluster.RedisClusterConfig;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
@Service
public class BatchServiceImpl implements BatchService {

    @Autowired
    private BatchMapper batchMapper;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private IDemandService demandService;

    private static final Logger log = LoggerFactory.getLogger(RedisClusterConfig.class);

    @Override
    public void batchGroupManagerLeader() throws Exception {
        List<Map<String, String>> orders = batchMapper.queryOrder();
        Map sendTask;
        for(Map<String, String> order : orders){
            String taskNo = order.get(Dict.MAINTASKNO);
            String workNo = order.get(Dict.WORKNO);
            String workManager = order.get(Dict.WORKMANAGER);
            String workLeader = order.get(Dict.WORKLEADER);
            String fdevGroupId = order.get(Dict.FDEVGROUPID);
//            sendTask = new HashMap();
//            sendTask.put(Dict.ID, taskNo);
//            sendTask.put(Dict.REST_CODE, "queryTaskDetail");
//            try {
//                sendTask = (Map)restTransport.submitSourceBack(sendTask);
//                String fdevGroupId = String.valueOf(((Map)sendTask.get(Dict.GROUP)).get(Dict.ID));
                if(!Util.isNullOrEmpty(fdevGroupId)){
//                    batchMapper.setFdevGroupId(workNo, fdevGroupId);
                    Map<String,String> res = groupMapper.queryAutoWorkOrder(fdevGroupId);
                    if (!Util.isNullOrEmpty(res) ) {
                        if(Util.isNullOrEmpty(workManager)){
                            batchMapper.setWorkManager(workNo, res.get(Dict.WORKMANAGER));
                        }
                        if(Util.isNullOrEmpty(workLeader)){
                            batchMapper.setWorkLeader(workNo, res.get(Dict.GROUPLEADER));
                        }
                    }else{
                        List<Map> groups = userService.queryParentGroupById(fdevGroupId);
                        if(!Util.isNullOrEmpty(groups)){
                            //获取其父组
                            Map group = groups.get(1);
                            Map<String,String> result = groupMapper.queryAutoWorkOrder((String)group.get(Dict.ID));
                            if(!Util.isNullOrEmpty(result)){
                                if(Util.isNullOrEmpty(workManager)){
                                    batchMapper.setWorkManager(workNo, result.get(Dict.WORKMANAGER));
                                }
                                if(Util.isNullOrEmpty(workLeader)){
                                    batchMapper.setWorkLeader(workNo, result.get(Dict.GROUPLEADER));
                                }
                            }
                        }
                    }
                }
//            } catch (Exception e) {
//                log.error("fail to fetch task info" + taskNo);
//            }
        }
    }

    @Override
    public void batchRqrNoInSubmit() throws Exception {
        List<String> problemRqrmntNos = batchMapper.queryproblemRqrmntNos();
        for(String problemRqrmntNo : problemRqrmntNos){
            Map result = demandService.queryDemandById(problemRqrmntNo);
            if(!Util.isNullOrEmpty(result)){
                String oa_contact_no = String.valueOf(result.get(Dict.OA_CONTACT_NO));
                String oa_contact_name = String.valueOf(result.get(Dict.OA_CONTACT_NAME));
                batchMapper.setRqrNo(oa_contact_no+"/"+oa_contact_name, problemRqrmntNo);
            }
        }
    }

    @Override
    public void batchInsertDemandNo() throws Exception {
        List<WorkOrder> workOrders = batchMapper.queryFdevAllOrder();
        for (WorkOrder workOrder : workOrders) {
            String unit = workOrder.getUnit();
            Map map = demandService.queryByFdevNoAndDemandId(unit);
            String rqrmntNo = String.valueOf(((Map)map.get(Dict.DEMAND_BASEINFO)).get(Dict.OA_CONTACT_NO));
            batchMapper.setOrderDemandNo(workOrder.getWorkOrderNo(), rqrmntNo);
        }
    }

    @Override
    public void batchInsertDemandName() throws Exception {
        List<WorkOrder> workOrders = batchMapper.queryFdevAllOrder();
        for (WorkOrder workOrder : workOrders) {
            String unit = workOrder.getUnit();
            Map map = demandService.queryByFdevNoAndDemandId(unit);
            String rqrmntName = String.valueOf(((Map) map.get(Dict.DEMAND_BASEINFO)).get(Dict.OA_CONTACT_NAME));
            batchMapper.setOrderDemandName(workOrder.getWorkOrderNo(),rqrmntName);
        }
    }
}
