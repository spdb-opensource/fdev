package com.spdb.fdev.fdevenvconfig.base.utils;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xxx
 * @date 2020/6/10 15:54
 */
public class GetModelFieldUtil {

    private GetModelFieldUtil() {
    }

    /**
     * 从文件内容里解析出引用的实体属性
     *
     * @param fileContent
     * @return
     */
    public static String getModelField(String fileContent) {
        if (StringUtils.isEmpty(fileContent)) {
            new ArrayList<>();
        }
        if (fileContent.contains("\r\n")) {
            fileContent = fileContent.replace("\r\n", "\n");
        }
        String[] fileContentSplit = fileContent.split("\n");
        StringBuilder result = new StringBuilder();
        for (String line : fileContentSplit) {
            if (isReplaceContent(line)) {
                result.append(getContent(line));
            }
        }
        return result.toString();
    }

    private static StringBuilder getContent(String line) {
        StringBuilder result = new StringBuilder();
        char[] chars = line.toCharArray();
        List<Integer> resultFirst = new ArrayList<>();
        List<Integer> resultLast = new ArrayList<>();
        for (int i = 0; i < chars.length; i++) {
            if ('$' == chars[i] && '<' == chars[i + 1]) {
                // 得到'<'的下标
                resultFirst.add(i + 1);
            }
            if ('>' == chars[i]) {
                // 得到'>'的下标
                resultLast.add(i);
            }
        }
        List<List<Integer>> comData = combinationNum(resultFirst, resultLast);
        comData.forEach(data -> result.append(line, data.get(0) + 1, data.get(1)).append("\n"));
        return result;
    }

    /**
     * 判断该行是否包括占位符“$<>”
     *
     * @param line
     * @return
     */
    private static boolean isReplaceContent(String line) {
        boolean flag = false;
        if (StringUtils.isNotEmpty(line) && line.contains("$<") && line.contains(">")) {
            flag = true;
        }
        return flag;
    }

    /**
     * 将出现"$<"的'<'下标和出现的'>'下标进行两两组合，如[[2,5],[8,13]]
     *
     * @param resultFirst
     * @param resultLast
     * @return
     */
    private static List<List<Integer>> combinationNum(List<Integer> resultFirst, List<Integer> resultLast) {
        List<List<Integer>> resultList = new ArrayList<>();
        for (int i = 0; i < resultFirst.size(); i++) {
            for (int j = 0; j < resultLast.size(); j++) {
                if (i + 1 >= resultFirst.size() && resultFirst.get(i) < resultLast.get(j)) {
                    List<Integer> list = new ArrayList<>();
                    list.add(resultFirst.get(i));
                    list.add(resultLast.get(j));
                    resultList.add(list);
                    break;
                }
                if (resultFirst.get(i) < resultLast.get(j) && resultLast.get(j) < resultFirst.get(i + 1)) {
                    List<Integer> list = new ArrayList<>();
                    list.add(resultFirst.get(i));
                    list.add(resultLast.get(j));
                    resultList.add(list);
                    break;
                }
            }
        }
        return resultList;
    }

}
