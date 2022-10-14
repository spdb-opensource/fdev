package com.spdb.fdev.fdevinterface.base.utils;

import com.jcraft.jsch.*;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @auther :gejunpeng
 * @date :2020/12/29 9:56
 * @location :shanghai
 * @description:
 */
public class SFTPClient {
    private String host;
    private String user;
    private String password;

    private ChannelSftp channelSftp;

    private void connect(String host, String user, String password) {
        JSch jSch = new JSch();
        Session session = null;
        try {
            session = jSch.getSession(user, host);
            session.setPassword(password);
            // 跳过检测
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            this.channelSftp = (ChannelSftp) channel;
        } catch (JSchException e) {
            throw new FdevException(ErrorConstants.SCP_ROUTE_DATA_ERROR, new String[]{e.getMessage() + "，ip地址为:" + host + "，用户名或密码不正确"});
        }
    }

    private void scpUp(String remoteDir, String tempPath, String host) {
        try {
            channelSftp.cd(remoteDir);
        } catch (SftpException e) {
            if (e.id == channelSftp.SSH_FX_NO_SUCH_FILE) {
                try {
                    channelSftp.mkdir(remoteDir);
                    channelSftp.cd(remoteDir);


                } catch (SftpException ex) {
                    throw new FdevException(ErrorConstants.SCP_ROUTE_DATA_ERROR, new String[]{ex.getMessage() + "，ip地址为:" + host});
                }
            }
        }
        File file = new File(tempPath);
        try (InputStream inputStream = new FileInputStream(file)) {
            channelSftp.put(inputStream, file.getName());
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.SCP_ROUTE_DATA_ERROR, new String[]{e.getMessage() + "，ip地址为:" + host});
        }
    }

    /**
     * @auth : gejunpeng
     * @Description: scp下载
     * @Return:
     * @Param:
     */
    private void scpDown(String remoteDir, String remoteFile, String localDir) {
        try {
            channelSftp.cd(remoteDir);
            channelSftp.get(remoteFile, localDir);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.SCP_ROUTE_DATA_ERROR);
        }
    }

    private void disConnect() {
        try {
            channelSftp.getSession().disconnect();
        } catch (JSchException e) {
            throw new FdevException(ErrorConstants.SCP_ROUTE_DATA_ERROR, new String[]{e.getMessage() + "ip地址为:" + host});
        }
        channelSftp.quit();
        channelSftp.disconnect();
    }

    /**
     * 上传配置文件到远端服务器
     *
     * @param remoteDir
     * @param tempPath
     */
    public void pushRomote(String remoteDir, String tempPath) {
        // 连接服务器
        connect(this.host, this.user, this.password);
        // 执行scp
        scpUp(remoteDir, tempPath, this.host);
        // 断开连接
        disConnect();
    }

    public void downRomote(String remoteDir, String remoteFile, String localDir) {
        // 连接服务器
        connect(this.host, this.user, this.password);
        // 执行scp

        scpDown(remoteDir, remoteFile, localDir);
        // 断开连接
        disConnect();
    }

    public SFTPClient(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
