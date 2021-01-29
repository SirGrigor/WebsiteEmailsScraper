package com.ilgrig.backend.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class UrlCsvDTO {

    @CsvBindByName
    private String url;

}
