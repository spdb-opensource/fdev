package com.spdb.fdev.fdemand.spdb.dto.testorder;

import java.util.Collection;

public class TestOrderQueryDto extends TestOrderDto {

    private Collection<String> ipmpNos;//研发单元编号集

    private Collection<String> implNos;//实施单元编号集

    private Collection<String> demandIds;//实施单元编号集

    public Collection<String> getIpmpNos() {
        return ipmpNos;
    }

    public void setIpmpNos(Collection<String> ipmpNos) {
        this.ipmpNos = ipmpNos;
    }

    public Collection<String> getImplNos() {
        return implNos;
    }

    public void setImplNos(Collection<String> implNos) {
        this.implNos = implNos;
    }

    public Collection<String> getDemandIds() {
        return demandIds;
    }

    public void setDemandIds(Collection<String> demandIds) {
        this.demandIds = demandIds;
    }
}
