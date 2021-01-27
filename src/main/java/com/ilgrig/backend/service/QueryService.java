package com.ilgrig.backend.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ilgrig.backend.entity.Prospect;
import com.ilgrig.backend.dto.QueryDTO;
import com.ilgrig.backend.repository.ProspectRepository;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

@Service
public class QueryService {
    private final ProspectRepository prospectRepository;

    public QueryService(ProspectRepository prospectRepository) {
        this.prospectRepository = prospectRepository;
    }

    public void getSingularQuery(QueryDTO queryDTO) throws IOException {
        queryDTO.getUrls().forEach(url -> {
            Document doc = null;
            try {
                doc = Jsoup.connect(url.getUrl()).get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Pattern p = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
            Matcher matcher = p.matcher(doc.text());
            Set<String> emails = new HashSet<String>();
            while (matcher.find()) {
                emails.add(matcher.group());
                Prospect prospect = new Prospect();
                prospect.setProspectEmails(matcher.group());
                prospectRepository.save(prospect);
            }
        });

        }

    }
