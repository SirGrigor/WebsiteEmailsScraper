package com.ilgrig.backend.controller;

import com.ilgrig.backend.service.CsvService;
import com.ilgrig.backend.service.ProspectService;
import com.ilgrig.backend.service.QueryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class CsvController {


    private final ProspectService prospectService;
    private final CsvService csvService;

    public CsvController(ProspectService prospectService, CsvService csvService, QueryService queryService) {
        this.prospectService = prospectService;
        this.csvService = csvService;
    }

    @PostMapping("/upload")
    public void handleFileUpload(@RequestParam("file") MultipartFile file, Model model) throws IOException, InterruptedException {
        csvService.saveCSV(file, model);
    }

    @RequestMapping(value = "/report")
    public void getCsvReport(HttpServletResponse response) throws IOException {
        csvService.createCsvReport(response);
    }
}
