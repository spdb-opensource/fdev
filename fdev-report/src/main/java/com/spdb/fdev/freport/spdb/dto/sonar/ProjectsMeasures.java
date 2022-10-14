package com.spdb.fdev.freport.spdb.dto.sonar;

import lombok.Data;

@Data
public class ProjectsMeasures {

    String component;// "a-a-a"

    String metric;//"bugs"

    Boolean bestValue;// false

    String value;// "43"
}
