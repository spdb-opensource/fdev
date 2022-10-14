package com.spdb.fdev.base.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class zipUtils {
	/**
	 * 下载minio上的文件到本地
	 * @param zipPath
	 * @param localPath
	 * @throws IOException
	 */
	public static void upZipFiles(String zipPath, String localPath) throws IOException {
		upZipFiles(new File(zipPath), localPath);
	}

	@SuppressWarnings("rawtypes")
	public static void upZipFiles(File file, String localPath) {
		long start = System.currentTimeMillis();
		if(!file.exists()){
			throw new RuntimeException(file.getPath()+"所指文件不存在");
		}
		//开始解压
		ZipFile zipFile = null;
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			zipFile = new ZipFile(file, Charset.forName("GBK"));
			Enumeration<?> entries = zipFile.entries();
			while(entries.hasMoreElements()){
				ZipEntry entry = (ZipEntry) entries.nextElement();
				System.out.println("解压" + entry.getName());
				//如果是文件夹，就创建个文件夹
				if(entry.isDirectory()){
					String dirPath = localPath +"/" +entry.getName();
					File dir = new File(dirPath);
					dir.mkdirs();
				} else {
					//如果是文件，就先创建一个文件，然后用io流把内容写入
					File targetFile = new File(localPath+"/"+ entry.getName());
					if(!targetFile.getParentFile().exists()){
						targetFile.getParentFile().mkdirs();
					}
					targetFile.createNewFile();
					//将压缩文件内容写入文件
					is = zipFile.getInputStream(entry);
					fos = new FileOutputStream(targetFile);
					int len;
					byte[] buf = new byte[1024];
					while ((len = is.read(buf)) > 0) {
						fos.write(buf, 0, len);
					}
				}
			}
			long end = System.currentTimeMillis();
			System.out.println("解压完成，耗时："+(end-start)+"ms");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(zipFile !=null){
				try {
					zipFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				;
			}
		}
	}

	/**
	 * 根据byte数组，生成文件
	 * @param bytes
	 * @param filePath
	 * @param fileName
	 */
	public static void byteToFile(byte[] bytes, String filePath, String fileName) {
		File file = new File(filePath+fileName);
		try (
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
		){
			File dir = new File(filePath);
			if (!dir.exists() && !dir.isDirectory()) {
				dir.mkdirs();
			}

			bos.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 删除文件夹及其下面的子文件夹和文件
	 *
	 * @param file
	 */
	public static void deleteFile(File file) {
		try {
			if (file.isFile()) {
				file.delete();
			} else {
				File[] listFiles = file.listFiles();
				if (listFiles != null && listFiles.length > 0) {
					for (File subFile : listFiles) {
						deleteFile(subFile);
					}
				}
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
