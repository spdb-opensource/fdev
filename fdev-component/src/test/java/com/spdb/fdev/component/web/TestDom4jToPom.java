package com.spdb.fdev.component.web;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class TestDom4jToPom {

	public static void main(String[] args) throws DocumentException, IOException {
		String path = "D:/ideaproject/116/com-csiicopy-guard/pom.xml";
		File file = new File(path);
		Document document = null;
		Element root = null;
		SAXReader reader = new SAXReader();
		document = reader.read(path);
		root = document.getRootElement();
		Element distributionManagement = root.addElement("distributionManagement");
		// 增加repository节点
		Element repository = distributionManagement.addElement("repository");
		Element repositoryid = repository.addElement("id");
		repositoryid.setText("releases");
		Element repositoryurl = repository.addElement("url");
		repositoryurl.setText("${nexus.release.url}");
		// 增加snapshotRepository节点
		Element snapshotRepository = distributionManagement.addElement("snapshotRepository");
		Element snapshotRepositoryid = snapshotRepository.addElement("id");
		snapshotRepositoryid.setText("snapshots");
		Element snapshotRepositoryurl = snapshotRepository.addElement("url");
		snapshotRepositoryurl.setText("${nexus.snapshots.url}");
		// 写入pom文件
		OutputFormat outputFormat = OutputFormat.createPrettyPrint();
		outputFormat.setEncoding("UTF-8");
		XMLWriter xmlWriter = new XMLWriter(new FileWriter(file), outputFormat);
		xmlWriter.write(document);
		xmlWriter.close();
	}

}
