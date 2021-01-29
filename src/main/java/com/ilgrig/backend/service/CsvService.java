package com.ilgrig.backend.service;

import com.ilgrig.backend.dto.UrlCsvDTO;
import com.ilgrig.backend.entity.Prospect;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
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

@Service
public class CsvService {
    private final QueryService queryService;
    private final ProspectService prospectService;

    public CsvService(QueryService queryService, ProspectService prospectService) {
        this.queryService = queryService;
        this.prospectService = prospectService;
    }

    public void saveCSV(MultipartFile file, Model model) throws IOException {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a CSV file to upload.");
            model.addAttribute("status", false);

        } else {
            Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            CsvToBean csvToBean = new CsvToBeanBuilder(reader)
                    .withType(UrlCsvDTO.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<UrlCsvDTO> urls = csvToBean.parse();
            queryService.getReport(urls);
        }
    }


    public void createCsvReport(HttpServletResponse response) throws IOException {
        String currentDateTime = setTimeOfReport(response);
        setHeader(response, currentDateTime);
        List<Prospect> prospects = prospectService.getAllProspects();
        writeCSV(response, prospects);
    }

    private void writeCSV(HttpServletResponse response, List<Prospect> prospects) throws IOException {
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
        String[] csvHeader = {"prospectId", "companyName", "prospectEmail", "alternativeEmail", "active", "platform", "companyId"};
        String[] nameMapping = {"prospectId", "companyName", "prospectEmail", "alternativeEmail", "active", "platform", "companyId"};

        csvWriter.writeHeader(csvHeader);

        for (Prospect prospect : prospects) {
            csvWriter.write(prospect, nameMapping);
        }

        csvWriter.close();
    }

    private void setHeader(HttpServletResponse response, String currentDateTime) {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=prospects_" + currentDateTime + ".csv";
        response.setHeader(headerKey, headerValue);
    }

    private String setTimeOfReport(HttpServletResponse response) {
        response.setContentType("text/csv");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return dateFormatter.format(new Date());
    }
}
