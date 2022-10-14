package com.spdb.fdev.freport.spdb.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class DmmDto {

    @DateTimeFormat(pattern = "yyyy-MM")
    private Date startDate;

    @DateTimeFormat(pattern = "yyyy-MM")
    private Date endDate;
}
