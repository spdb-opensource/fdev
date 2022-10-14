package com.spdb.fdev.component.web;

import com.spdb.fdev.base.dict.Constants;
import org.apache.commons.lang.StringUtils;

public class Test {

    /**
     * 是否满足1.0.1-SNAPSHOT(RC,RELEASE)这种版本格式，不满足直接放行，新增版本时无限制
     * 满足再进行版本比较
     *
     * @param latestVersion
     * @return
     */
    public boolean isJoinCompare(String latestVersion) {
        if (StringUtils.isBlank(latestVersion)) {
            return false;
        }
        String[] split = latestVersion.split("-");
        String[] versionList = split[0].split("\\.");
        if (versionList.length != 3) {
            return false;
        }
        return true;
    }

    /**
     * 对版本进行比较，比当前版本要大，如仓库最新版本为10.0.0，那么新建版本必须为10.0.1
     *
     * @param versionList
     * @param version
     * @return
     */
    private Boolean compareVersion(String[] versionList, String[] version) {
        if (versionList.length != 3 && version.length != 3) {
            return false;
        }
        if (Integer.valueOf(version[0]).intValue() < Integer.valueOf(versionList[0]).intValue()) {
            return false;
        }
        if (Integer.valueOf(version[0]).intValue() == Integer.valueOf(versionList[0]).intValue()) {
            if (Integer.valueOf(version[1]).intValue() < Integer.valueOf(versionList[1]).intValue()) {
                return false;
            }
            if (Integer.valueOf(version[1]).intValue() == Integer.valueOf(versionList[1]).intValue()) {
                if (Integer.valueOf(version[2]).intValue() <= Integer.valueOf(versionList[2]).intValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
//        System.out.println(Integer.MAX_VALUE);
//        System.out.println(Integer.MIN_VALUE);
//        Test test = new Test();
//        while (true) {
//            Scanner scanner = new Scanner(System.in);
//            String latestVersion = scanner.nextLine();
//            String version = "10212.0.1";
//            if (test.isJoinCompare(latestVersion)) {
//                String[] split = latestVersion.split("-");
//                String[] versionList = split[0].split("\\.");
//                Boolean flag = test.compareVersion(versionList, version.split("\\."));
//                if (!flag) {
//                    throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{version, "目标版本号必须比" + split[0] + "大"});
//                }
//            }
//        }
//        String result = ShellUtils.cellShell("mbank-jsbridge", "", true);
//        System.out.println(result);
//        if (StringUtils.isNotBlank(result) && result.length() > 1) {
//            String[] versionArray = result.substring(1, result.length() - 1).split(",");
//            for (String version : versionArray) {
//                //version = version.replaceAll("[\\[|\\]]", "").trim();
//                version = version.replaceAll("[\\[|\\]|\\']", "").trim();
//                System.out.println(version);
//            }
//        }
        String ref = "v1.5.0-alpha.0";
        if (ref.startsWith(Constants.VERSION_TAG)) {
            ref = ref.substring(1);
        }
        System.out.println("ref:" + ref);
    }
}
