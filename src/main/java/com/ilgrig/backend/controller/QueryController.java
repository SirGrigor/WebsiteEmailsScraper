package com.ilgrig.backend.controller;

import com.ilgrig.backend.dto.QueryDTO;
import com.ilgrig.backend.entity.Prospect;
import com.ilgrig.backend.service.ProspectService;
import com.ilgrig.backend.service.QueryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
public class QueryController {
    private final ProspectService prospectService;
    private final QueryService queryService;

    public QueryController(ProspectService prospectService, QueryService queryService) {
        this.prospectService = prospectService;
        this.queryService = queryService;
    }

    @PostMapping("/query")
    public List<Prospect> addUrlsToScrape(@RequestBody QueryDTO queryDTO) throws IOException, InterruptedException {
        return queryService.getReport(queryDTO);
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
