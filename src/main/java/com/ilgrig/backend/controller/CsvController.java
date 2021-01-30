package com.ilgrig.backend.controller;

import com.ilgrig.backend.service.CsvService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class CsvController {

    private final CsvService csvService;

    public CsvController(CsvService csvService) {
        this.csvService = csvService;
    }

    @PostMapping("/upload")
    public ModelAndView uploadCSV(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        csvService.saveCSV(file, model);
        return new ModelAndView("redirect:/success.html");
    }

    @GetMapping(value = "/report")
    public void getCsvReport(HttpServletResponse response) throws IOException {
        csvService.createCsvReport(response);
    }
}
