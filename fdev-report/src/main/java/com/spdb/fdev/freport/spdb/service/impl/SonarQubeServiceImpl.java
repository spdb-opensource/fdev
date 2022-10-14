package com.spdb.fdev.freport.spdb.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.dict.Dict;
import com.spdb.fdev.freport.base.dict.DashboardDict;
import com.spdb.fdev.freport.spdb.dao.ReportDao;
import com.spdb.fdev.freport.spdb.dto.sonar.ProjectsMeasures;
import com.spdb.fdev.freport.spdb.entity.sonar.SonarProjectRank;
import com.spdb.fdev.freport.spdb.service.SonarQubeService;
import com.spdb.fdev.freport.spdb.vo.Ranking;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

//我先声明，这个Sonar工具类并不是我写的，是我从Sonar服务中copy过来的代码
@Service
public class SonarQubeServiceImpl implements SonarQubeService {

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private ReportDao reportDao;

    @Override
    public void cacheSonarProject() throws Exception {
        querySonarProjectRank().forEach(x -> reportDao.upsertSonarProjectRank(x));
    }

    @Override
    public List<SonarProjectRank> querySonarProjectRank() throws Exception {
        List<ProjectsMeasures> measures = measuresSearch();
        List<Ranking> bugs = new ArrayList<>();
        List<Ranking> vulnerabilities = new ArrayList<>();
        List<Ranking> duplicatedLinesDensity = new ArrayList<>();
        List<Ranking> codeSmells = new ArrayList<>();
        for (ProjectsMeasures measure : measures) {
            Ranking ranking = new Ranking();
            //包名:项目名
            ranking.setLabel(measure.getComponent().contains(":") ? measure.getComponent().split(":")[1] : measure.getComponent());
            ranking.setValue(new BigDecimal(measure.getValue()));
            switch (measure.getMetric()) {
                case DashboardDict.BUGS:
                    bugs.add(ranking);
                    break;
                case DashboardDict.VULNERABILITIES:
                    vulnerabilities.add(ranking);
                    break;
                case DashboardDict.CODE_SMELLS:
                    codeSmells.add(ranking);
                    break;
                case DashboardDict.DUPLICATED_LINES_DENSITY:
                    ranking.setValue(ranking.getValue());
                    duplicatedLinesDensity.add(ranking);
                    break;
                default:
            }
        }
        Comparator<Ranking> comparator = (o1, o2) -> {//降序
            return o2.getValue().compareTo(o1.getValue());
        };
        bugs.sort(comparator);
        duplicatedLinesDensity.sort(comparator);
        codeSmells.sort(comparator);
        vulnerabilities.sort(comparator);
        return new ArrayList<SonarProjectRank>() {{
            add(new SonarProjectRank() {{
                setCode(DashboardDict.BUGS);
                setData(bugs.size() > 10 ? bugs.subList(0, 10) : bugs);
            }});
            add(new SonarProjectRank() {{
                setCode(DashboardDict.VULNERABILITIES);
                setData(vulnerabilities.size() > 10 ? vulnerabilities.subList(0, 10) : vulnerabilities);
            }});
            add(new SonarProjectRank() {{
                setCode(DashboardDict.CODE_SMELLS);
                setData(codeSmells.size() > 10 ? codeSmells.subList(0, 10) : codeSmells);
            }});
            add(new SonarProjectRank() {{
                setCode(DashboardDict.DUPLICATED_LINES_DENSITY);
                setData(duplicatedLinesDensity.size() > 10 ? duplicatedLinesDensity.subList(0, 10) : duplicatedLinesDensity);
            }});
        }};
    }

    private List<ProjectsMeasures> measuresSearch() throws Exception {
        return JSONObject.parseArray(JSONObject.toJSONString(restTransport.submit(
                new HashMap<String, Object>() {{
                    put(Dict.REST_CODE, "searchTotalProjectMeasures");
                }}
        )), ProjectsMeasures.class);
    }

}
