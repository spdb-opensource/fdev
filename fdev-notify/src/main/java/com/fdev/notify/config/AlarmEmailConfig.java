package com.fdev.notify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author xxx
 * @date 2019/9/10 10:37
 */
@Component
@ConfigurationProperties(prefix = "toalarmemail")
public class AlarmEmailConfig {

    private List<String> others;
    private List<String> fdevAppDownAlert;
    private List<String> fdevTaskAlert;
    private List<String> fdevReleaseAlert;
    private List<String> fdevInterfaceAlert;
    private List<String> fdevEnvConfigAlert;

    public List<String> getOthers() {
        return others;
    }

    public void setOthers(List<String> others) {
        this.others = others;
    }

    public List<String> getFdevAppDownAlert() {
        return fdevAppDownAlert;
    }

    public void setFdevAppDownAlert(List<String> fdevAppDownAlert) {
        this.fdevAppDownAlert = fdevAppDownAlert;
    }

    public List<String> getFdevTaskAlert() {
        return fdevTaskAlert;
    }

    public void setFdevTaskAlert(List<String> fdevTaskAlert) {
        this.fdevTaskAlert = fdevTaskAlert;
    }

    public List<String> getFdevReleaseAlert() {
        return fdevReleaseAlert;
    }

    public void setFdevReleaseAlert(List<String> fdevReleaseAlert) {
        this.fdevReleaseAlert = fdevReleaseAlert;
    }

    public List<String> getFdevInterfaceAlert() {
        return fdevInterfaceAlert;
    }

    public void setFdevInterfaceAlert(List<String> fdevInterfaceAlert) {
        this.fdevInterfaceAlert = fdevInterfaceAlert;
    }

    public List<String> getFdevEnvConfigAlert() {
        return fdevEnvConfigAlert;
    }

    public void setFdevEnvConfigAlert(List<String> fdevEnvConfigAlert) {
        this.fdevEnvConfigAlert = fdevEnvConfigAlert;
    }
}
