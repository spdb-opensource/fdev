package com.spdb.quartz;


import org.quartz.JobDetail;
import org.quartz.JobPersistenceException;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerKey;
import org.quartz.impl.jdbcjobstore.StdJDBCDelegate;
import org.quartz.spi.OperableTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.quartz.TriggerKey.triggerKey;

/**
 * @description: 重写StdJDBCDelegate部分方法
 * @author: lizz
 * @since: 2019-12-05 20:51
 **/

public class MyJDBCDelegate extends StdJDBCDelegate {
    private static final Logger logger = LoggerFactory.getLogger(MyJDBCDelegate.class);


    String TABLE_EXECUTION_PLAN = "EXECUTION_PLAN";
    String TABLE_PAUSED_STRATEGY_JOB = "PAUSED_STRATEGY_JOB";
    String TABLE_PAUSED_STRATEGY = "PAUSED_STRATEGY";
    String COL_JOB_STATE = "JOB_STATE";
    String COL_IDENTIFICATION_ID = "IDENTIFICATION_ID";
    String COL_ACQUIRED_TIME = "ACQUIRED_TIME";
    String COL_CREATE_DATE = "CREATE_DATE";
    String COL_START_PAUSED_DATE = "START_PAUSED_DATE";
    String COL_END_PAUSED_DATE = "END_PAUSED_DATE";
    String COL_PAUSED_DATE_ID = "PAUSED_DATE_ID";
    String COL_STATE_TYPE = "STATE_TYPE";
    String COL_ID = "ID";
    String COL_JOB_ID = "JOB_ID";
    String TABLE_PREFIX_SUBST_NEW = "SPDB_";

    String SELECT_TRIGGERS_FOR_JOBID = "SELECT "
            + COL_JOB_ID
            + " FROM "
            + TABLE_PREFIX_SUBST + TABLE_TRIGGERS
            + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? "
            + " AND " + COL_TRIGGER_GROUP + " = ? ";

    String INSERT_EXECUTION_PLAN = "INSERT INTO "
            + TABLE_PREFIX_SUBST_NEW + TABLE_EXECUTION_PLAN + " ("
            + COL_SCHEDULER_NAME + ", "
            + COL_INSTANCE_NAME + ", "
            + COL_IDENTIFICATION_ID + ", "
            + COL_TRIGGER_NAME + ", "
            + COL_TRIGGER_GROUP + ", "
            + COL_JOB_NAME + ", "
            + COL_JOB_GROUP + ", "
            + COL_SCHED_TIME + ", "
            + COL_ACQUIRED_TIME + ", "
            + COL_JOB_STATE + ", "
            + COL_CREATE_DATE + ") "
            + " VALUES(" + SCHED_NAME_SUBST + ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    String SELECT_EXECUTION_PLAN_STATE = "SELECT "
            + COL_JOB_STATE
            + " FROM "
            + TABLE_PREFIX_SUBST_NEW + TABLE_EXECUTION_PLAN
            + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND "
            + COL_TRIGGER_NAME + " = ?"
            + " AND "
            + COL_TRIGGER_GROUP + " = ?"
            + " AND "
            + COL_SCHED_TIME + " = ?";

    String SELECT_EXECUTION_PLAN_EXITS = "SELECT "
            + COL_ID
            + " FROM "
            + TABLE_PREFIX_SUBST_NEW + TABLE_EXECUTION_PLAN
            + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND "
            + COL_TRIGGER_NAME + " = ?"
            + " AND "
            + COL_TRIGGER_GROUP + " = ?"
            + " AND "
            + COL_JOB_STATE + " = ?"
            + " AND "
            + COL_SCHED_TIME + " = ?";

    String SELECT_EXECUTION_PLAN_SIMPLETRIGGER = "SELECT "
            +  COL_ID
            + " FROM "
            + TABLE_PREFIX_SUBST_NEW + TABLE_EXECUTION_PLAN
            + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND "
            + COL_TRIGGER_NAME + " = ?"
            + " AND "
            + COL_TRIGGER_GROUP + " = ?";

    String SELECT_EXECUTION_PLAN_TOP_ONE = "SELECT "
            + COL_ID
            + " FROM "
            + TABLE_PREFIX_SUBST_NEW + TABLE_EXECUTION_PLAN
            + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND "
            + COL_TRIGGER_NAME + " = ?"
            + " AND "
            + COL_TRIGGER_GROUP + " = ?"
            + " AND "
            + COL_JOB_STATE + " = ?"
            + " AND "
            + COL_SCHED_TIME + " = ?";

    String SELECT_EXECUTION_PLAN_TOP_ONE_SIMPLETRIGGER = "SELECT "
            + COL_ID
            + " FROM "
            + TABLE_PREFIX_SUBST_NEW + TABLE_EXECUTION_PLAN
            + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND "
            + COL_TRIGGER_NAME + " = ?"
            + " AND "
            + COL_TRIGGER_GROUP + " = ?";


    String SELECT_NEXT_TRIGGER_TO_ACQUIRE_INNER_PAUSED_STRATEGY = "SELECT "
            + COL_TRIGGER_NAME + ", "
            + COL_TRIGGER_GROUP + ", "
            + COL_NEXT_FIRE_TIME + ", "
            + COL_PRIORITY
            + " FROM "
            + TABLE_PREFIX_SUBST
            + TABLE_TRIGGERS
            + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_STATE + " = ? AND "
            + COL_NEXT_FIRE_TIME + " <= ? "
            + "AND (" + COL_MISFIRE_INSTRUCTION + " = -1 OR (" + COL_MISFIRE_INSTRUCTION + " != -1 AND " + COL_NEXT_FIRE_TIME + " >= ?)) "
            + " AND " + " (SELECT COUNT(*) " + " FROM " + TABLE_PREFIX_SUBST_NEW + TABLE_PAUSED_STRATEGY_JOB + "," + TABLE_PREFIX_SUBST_NEW + TABLE_PAUSED_STRATEGY
            + " WHERE "
            + TABLE_PREFIX_SUBST_NEW + TABLE_PAUSED_STRATEGY_JOB + "." + COL_PAUSED_DATE_ID + " = " + TABLE_PREFIX_SUBST_NEW + TABLE_PAUSED_STRATEGY + "." + COL_ID
            + " AND " + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + "." + COL_TRIGGER_GROUP + " = " + TABLE_PREFIX_SUBST_NEW + TABLE_PAUSED_STRATEGY_JOB + "." + COL_TRIGGER_GROUP
            + " AND " + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + "." + COL_TRIGGER_NAME + " = " + TABLE_PREFIX_SUBST_NEW + TABLE_PAUSED_STRATEGY_JOB + "." + COL_TRIGGER_NAME
            + " AND " + TABLE_PREFIX_SUBST_NEW + TABLE_PAUSED_STRATEGY + "." + COL_START_PAUSED_DATE + " <= ? "
            + " AND " + TABLE_PREFIX_SUBST_NEW + TABLE_PAUSED_STRATEGY + "." + COL_END_PAUSED_DATE + " >= ? "
            + " AND " + TABLE_PREFIX_SUBST_NEW + TABLE_PAUSED_STRATEGY_JOB + "." + COL_STATE_TYPE + " = 0 )=0"
            + " ORDER BY " + COL_NEXT_FIRE_TIME + " ASC, " + COL_PRIORITY + " DESC";

    String UPDATE_EXECUTION_PLAN = " UPDATE "
            + TABLE_PREFIX_SUBST_NEW + TABLE_EXECUTION_PLAN
            + " SET "
            + COL_INSTANCE_NAME + " = ?,"
            + COL_IDENTIFICATION_ID + " = ?,"
            // + COL_SCHED_TIME + " = ?,"
            + COL_ACQUIRED_TIME + " = ?,"
            + COL_JOB_STATE + " = ?"
            + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND "
            + COL_TRIGGER_NAME + " = ?"
            + " AND "
            + COL_TRIGGER_GROUP + " = ?"
            + " AND "
            + COL_ID + " = ?";

    String UPDATE_TRIGGER_NEXTFIRETIME = " UPDATE "
            + TABLE_PREFIX_SUBST + TABLE_TRIGGERS
            + " SET "
            + COL_NEXT_FIRE_TIME + " = ?,"
            + COL_PREV_FIRE_TIME + " = ?"
            + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND "
            + COL_TRIGGER_NAME + " = ?"
            + " AND "
            + COL_TRIGGER_GROUP + " = ?";

    /**
     * <p>
     * Insert a fired trigger.
     * </p>
     *
     * @param conn    the DB Connection
     * @param trigger the trigger
     * @param state   the state that the trigger should be stored in
     * @return the number of rows inserted
     */
    @Override
    public int insertFiredTrigger(Connection conn, OperableTrigger trigger,
                                  String state, JobDetail job) throws SQLException {
        int rows = 0;
        if (isExitsJobInfo(conn, trigger)) {
            long jobId = selectJobOperationRecordTopOne(conn, trigger);
            rows = updateJobOperationRecord(conn, trigger, state, jobId);
        } else {
            rows = insertJobOperationRecord(conn, trigger, state, job);
        }
        super.insertFiredTrigger(conn, trigger, state, job);
        return rows;
    }

    /**
     * 查询监控表中最先插入的job
     *
     * @param conn
     * @param trigger
     * @return
     * @throws SQLException
     */
    public Long selectJobOperationRecordTopOne(Connection conn, OperableTrigger trigger) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            Long jobId = selectJobTrigger(conn, trigger);
            if (jobId == 0) {
                if (trigger instanceof SimpleTrigger) {
                    ps = conn.prepareStatement(rtp(SELECT_EXECUTION_PLAN_TOP_ONE_SIMPLETRIGGER));
                    ps.setString(1, trigger.getKey().getName());
                    ps.setString(2, trigger.getKey().getGroup());
                } else {
                    ps = conn.prepareStatement(rtp(SELECT_EXECUTION_PLAN_TOP_ONE));
                    ps.setString(1, trigger.getKey().getName());
                    ps.setString(2, trigger.getKey().getGroup());
                    ps.setString(3, "WAITING");
                    ps.setLong(4, trigger.getNextFireTime().getTime());
                }

                rs = ps.executeQuery();
                while (rs.next()) {
                    jobId = rs.getLong(COL_ID);
                }
            }

            return jobId;
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
        }
    }

    /**
     * 更新监控表中job状态
     *
     * @param conn
     * @param trigger
     * @param state
     * @param jobId
     * @return
     * @throws SQLException
     */
    public int updateJobOperationRecord(Connection conn, OperableTrigger trigger, String state, long jobId) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(rtp(UPDATE_EXECUTION_PLAN));
            ps.setString(1, instanceId);
            ps.setString(2, trigger.getFireInstanceId());
            //ps.setLong(3, trigger.getNextFireTime().getTime());
            ps.setLong(3, System.currentTimeMillis());
            ps.setString(4, state);
            ps.setString(5, trigger.getKey().getName());
            ps.setString(6, trigger.getKey().getGroup());
            ps.setLong(7, jobId);
            return ps.executeUpdate();
        } finally {
            closeStatement(ps);
        }
    }

    /**
     * job是否在监控表里存在
     *
     * @param conn
     * @param trigger
     * @return
     * @throws SQLException
     */
    public boolean isExitsJobInfo(Connection conn, OperableTrigger trigger) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        if (selectJobTrigger(conn, trigger) != 0) {
            return true;
        }
        try {
            if (trigger instanceof SimpleTrigger) {
                ps = conn.prepareStatement(rtp(SELECT_EXECUTION_PLAN_SIMPLETRIGGER));
                ps.setString(1, trigger.getKey().getName());
                ps.setString(2, trigger.getKey().getGroup());
            } else {
                ps = conn.prepareStatement(rtp(SELECT_EXECUTION_PLAN_EXITS));
                ps.setString(1, trigger.getKey().getName());
                ps.setString(2, trigger.getKey().getGroup());
                ps.setString(3, "WAITING");
                ps.setLong(4, trigger.getNextFireTime().getTime());
            }
            rs = ps.executeQuery();
            if (!rs.next()) {
                return false;
            }
            return getBoolean(rs, COL_ID);
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
        }
    }


    /**
     * job在监控表里存在的编号
     *
     * @param conn
     * @param trigger
     * @return
     * @throws SQLException
     */
    public Long selectJobTrigger(Connection conn, OperableTrigger trigger) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(rtp(SELECT_TRIGGERS_FOR_JOBID));
            ps.setString(1, trigger.getKey().getName());
            ps.setString(2, trigger.getKey().getGroup());

            rs = ps.executeQuery();
            Long jobId = null;
            while (rs.next()) {
                jobId = rs.getLong(COL_JOB_ID);
            }
            return jobId;
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
        }
    }

    /**
     * 添加监控列表
     *
     * @param conn
     * @param trigger
     * @param state
     * @param job
     * @return
     * @throws SQLException
     */
    public int insertJobOperationRecord(Connection conn, OperableTrigger trigger, String state, JobDetail job) throws SQLException {
        PreparedStatement ps = null;
        int insertResult = 0;

        try {
            ps = conn.prepareStatement(rtp(INSERT_EXECUTION_PLAN));
            ps.setString(1, instanceId);
            ps.setString(2, trigger.getFireInstanceId());
            ps.setString(3, trigger.getKey().getName());
            ps.setString(4, trigger.getKey().getGroup());
            ps.setString(5, trigger.getJobKey().getName());
            ps.setString(6, trigger.getJobKey().getGroup());
            ps.setLong(7, trigger.getNextFireTime().getTime());
            ps.setLong(8, System.currentTimeMillis());
            ps.setString(9, state);
            ps.setLong(10, System.currentTimeMillis());

            insertResult = ps.executeUpdate();
        } finally {
            closeStatement(ps);
        }
        return insertResult;
    }

    @Override
    public int countMisfiredTriggersInState(
            Connection conn, String state1, long ts) throws SQLException {
        return super.countMisfiredTriggersInState(conn, state1, ts);
    }

    /**
     * <p>
     * Select the next trigger which will fire to fire between the two given timestamps
     * in ascending order of fire time, and then descending by priority.
     * </p>
     *
     * @param conn          the DB Connection
     * @param noLaterThan   highest value of <code>getNextFireTime()</code> of the triggers (exclusive)
     * @param noEarlierThan highest value of <code>getNextFireTime()</code> of the triggers (inclusive)
     * @param maxCount      maximum number of trigger keys allow to acquired in the returning list.
     * @return A (never null, possibly empty) list of the identifiers (Key objects) of the next triggers to be fired.
     */
    @Override
    public List<TriggerKey> selectTriggerToAcquire(Connection conn, long noLaterThan, long noEarlierThan, int maxCount)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<TriggerKey> nextTriggers = new LinkedList<>();
        try {
            ps = conn.prepareStatement(rtp(SELECT_NEXT_TRIGGER_TO_ACQUIRE_INNER_PAUSED_STRATEGY));

            // Set max rows to retrieve
            if (maxCount < 1) {
                // we want at least one trigger back.
                maxCount = 1;
            }
            ps.setMaxRows(maxCount);

            // Try to give jdbc driver a hint to hopefully not pull over more than the few rows we actually need.
            // Note: in some jdbc drivers, such as MySQL, you must set maxRows before fetchSize, or you get exception!
            ps.setFetchSize(maxCount);

            ps.setString(1, STATE_WAITING);
            ps.setBigDecimal(2, new BigDecimal(String.valueOf(noLaterThan)));
            ps.setBigDecimal(3, new BigDecimal(String.valueOf(noEarlierThan)));
            ps.setLong(4, System.currentTimeMillis());
            ps.setLong(5, System.currentTimeMillis());
            rs = ps.executeQuery();

            while (rs.next() && nextTriggers.size() <= maxCount) {
                nextTriggers.add(triggerKey(
                        rs.getString(COL_TRIGGER_NAME),
                        rs.getString(COL_TRIGGER_GROUP)));
            }
            Iterator<TriggerKey> iterator = nextTriggers.iterator();
            while (iterator.hasNext()) {
                TriggerKey triggerKey = iterator.next();
                OperableTrigger nextTrigger = super.selectTrigger(conn, triggerKey);
                boolean flag = selectJobState(conn, nextTrigger);
                if (!flag) {
                    updateTriggerNextFireTime(conn, nextTrigger);
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JobPersistenceException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
        }
        return nextTriggers;
    }

    /**
     * job是否在监控表里存在
     *
     * @param conn
     * @param trigger
     * @return
     * @throws SQLException
     */
    public boolean selectJobState(Connection conn, OperableTrigger trigger) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(rtp(SELECT_EXECUTION_PLAN_STATE));
            ps.setString(1, trigger.getKey().getName());
            ps.setString(2, trigger.getKey().getGroup());
            ps.setLong(3, trigger.getNextFireTime().getTime());
            rs = ps.executeQuery();

            LinkedList<String> list = new LinkedList<>();
            while (rs.next()) {
                String jobState = rs.getString(COL_JOB_STATE);
                list.add(jobState);
            }
            boolean flag = true;
            if (!list.isEmpty()) {
                flag = "WAITING".equals(list.get(0));
            }
            return flag;
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
        }
    }


    /**
     * 更新trigger表中job下次触发时间
     *
     * @param conn
     * @param trigger
     * @return
     * @throws SQLException
     */
    public int updateTriggerNextFireTime(Connection conn, OperableTrigger trigger) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(rtp(UPDATE_TRIGGER_NEXTFIRETIME));
            ps.setLong(1, trigger.getFireTimeAfter(trigger.getNextFireTime()).getTime());
            ps.setLong(2, trigger.getNextFireTime().getTime());
            ps.setString(3, trigger.getKey().getName());
            ps.setString(4, trigger.getKey().getGroup());
            return ps.executeUpdate();
        } finally {
            closeStatement(ps);
        }
    }
}
