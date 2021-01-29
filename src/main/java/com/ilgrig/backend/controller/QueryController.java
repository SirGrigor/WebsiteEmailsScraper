package com.ilgrig.backend.controller;

import com.ilgrig.backend.dto.UrlCsvDTO;
import com.ilgrig.backend.entity.Prospect;
import com.ilgrig.backend.service.ProspectService;
import com.ilgrig.backend.service.QueryService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class QueryController {


    private final ProspectService prospectService;

    private final QueryService queryService;
    public QueryController(ProspectService prospectService, QueryService queryService) {
        this.prospectService = prospectService;
        this.queryService = queryService;
    }

    @PostMapping("/upload")
    public void handleFileUpload(@RequestParam("file") MultipartFile file,  Model model) throws IOException, InterruptedException {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a CSV file to upload.");
            model.addAttribute("status", false);
        } else {

            // parse CSV file to create a list of `User` objects
            Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                // create csv bean reader
                CsvToBean csvToBean = new CsvToBeanBuilder(reader)
                        .withType(UrlCsvDTO.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();

                List<UrlCsvDTO> urls = csvToBean.parse();
                queryService.getReport(urls);
        }
    }

    @RequestMapping(value = "/report")
    public void getCsvReport(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=prospects_" + currentDateTime + ".csv";
        response.setHeader(headerKey, headerValue);

        List<Prospect> prospects = prospectService.getAllProspects();

        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
        String[] csvHeader = {"prospectId", "companyName", "prospectEmail", "alternativeEmail", "active", "platform", "companyId"};
        String[] nameMapping = {"prospectId", "companyName", "prospectEmail", "alternativeEmail", "active", "platform", "companyId"};

        csvWriter.writeHeader(csvHeader);

        for (Prospect prospect : prospects) {
            csvWriter.write(prospect, nameMapping);
        }

        csvWriter.close();
    }
}
