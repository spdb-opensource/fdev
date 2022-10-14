/*
 * 文件： HttpUtils.java
 * 创建日期 2018年12月11日
 *
 */
package com.auto.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @date： (2018年12月11日 下午4:29:24)
 * @author: xxx
 * 
 * Modified history
 * 
 * 	Modified date: 	
 * 	Modifier user: 		
 * 	description:	
 * 
 * */
public class HttpUtils {
	
	private static final int BUFFER_SIZE = 2 * 1024;

	private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	//	String url = "http://iams.t-guz.minikube/api/setCurrCase";
//		method(url, "777777");
		runCmd("zip -r D:\\work_payfee\\testmanage-auto\\output.zip D:\\work_payfee\\testmanage-auto\\test-output\\");
	}
	
	public void traverseFolder(String path) {
		
	}
	
	@SuppressWarnings("deprecation")
	public static void method(String url, String caseId){
		HttpClient htppclient = new HttpClient();
		PostMethod method = new PostMethod(url);
		String body = "{\"caseId\":\"" + caseId + "\"}";
		method.setRequestBody(body);
		try {
			int statusCode = htppclient.executeMethod(method);
			
			if(statusCode != HttpStatus.SC_OK){
				System.err.println("Method failed:" + method.getStatusLine());
			}
			
			byte[] responseBody = method.getResponseBody();
			System.out.println(new String(responseBody, "utf-8"));
			System.out.println("当前接口案例：" + caseId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			method.releaseConnection();
		}
	}
	
	
	public static Object runCmd(String cmd) {
		String[] cmdA = {"/bin/sh", "-c", cmd};
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(cmdA);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try(LineNumberReader br = new LineNumberReader(new InputStreamReader(process.getInputStream()))) {
			StringBuffer sb = new StringBuffer();
			String line;
			while((line = br.readLine()) != null) {
				System.out.println(line);
				sb.append(line).append("\n");
			}
			return sb.toString();
		} catch (IOException e) {
			System.out.println("执行错误");
		}
		return null;
	}

	public static void toZip(String srcDir, String outDir, boolean KeepDirStructure) throws Exception{
		logger.info("toZip========start");
		try(FileOutputStream out = new FileOutputStream(outDir);
			ZipOutputStream zos = new ZipOutputStream(out)){
			File sourceFile = new File(srcDir);
			compress(sourceFile, zos, sourceFile.getName(), KeepDirStructure);
		}catch(Exception e){
			logger.info("Exception========start");
			throw new Exception(e);
		}
	}
	
	public static void compress(File sourceFile, ZipOutputStream zos, String name, boolean KeepDirStructure) {


		try {
			logger.info("compress========start");
			byte[] buf = new byte[BUFFER_SIZE];
			logger.info(sourceFile.getPath()+"");
			if(sourceFile.isFile()){

				logger.info("sourceFile========start" + sourceFile.getName());
				zos.putNextEntry(new ZipEntry(name));
				int len;
				FileInputStream in = new FileInputStream(sourceFile);
				while((len = in.read(buf)) != -1){
					zos.write(buf, 0, len);

					logger.info("zoslen========" + len);
				}
				zos.closeEntry();
				in.close();
			}else{


				logger.info("sourceFile========else" );
				File[] listFiles = sourceFile.listFiles();
//			logger.info("-------"+listFiles.length+"");
				if(listFiles == null || listFiles.length == 0){
					if(KeepDirStructure){
						zos.putNextEntry(new ZipEntry(name + File.separator));
						zos.closeEntry();
					}
				}else{
					for(File file : listFiles){

						logger.info("file========getpath"+ file.getPath() );
						if(KeepDirStructure){
							compress(file, zos, name + File.separator + file.getName(), KeepDirStructure);
						}else{
							compress(file, zos, file.getName(), KeepDirStructure);
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error("compress file error", e);
		} finally {
			if(zos != null) {
				try {
					zos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
   



}

