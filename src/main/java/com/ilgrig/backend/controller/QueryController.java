package com.ilgrig.backend.controller;

import com.ilgrig.backend.dto.QueryDTO;
import com.ilgrig.backend.service.QueryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class QueryController {

    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping("/query")
    public void addUrlsToScrape(@RequestBody QueryDTO queryDTO) throws IOException {
        queryService.getSingularQuery(queryDTO);
    }
}
