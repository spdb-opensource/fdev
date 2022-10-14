package com.spdb.fdev.base.dict;

public enum DeployTypeEnum {


    CAAS("CAAS",Dict.PRO_IMAGE_URI),
    SCC("SCC",Dict.PRO_SCC_IMAGE_URI);

    private String type;

    private String uri;

    DeployTypeEnum(String type, String uri) {
        this.type = type;
        this.uri = uri;
    }

    public static String getUri(String type) {
        for(DeployTypeEnum s:DeployTypeEnum.values()) {
            if(s.getType().equalsIgnoreCase(type)) {
                return s.getUri();
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
