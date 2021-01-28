package com.ilgrig.backend.service;

import com.ilgrig.backend.dto.QueryDTO;
import com.ilgrig.backend.entity.Prospect;
import com.ilgrig.backend.repository.ProspectRepository;
import org.apache.commons.net.whois.WhoisClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QueryService {
    private final ProspectRepository prospectRepository;

    public QueryService(ProspectRepository prospectRepository) {
        this.prospectRepository = prospectRepository;
    }

    public List<Prospect> getReport(QueryDTO queryDTO) throws IOException {
        List<String> urls = new ArrayList<>();
        queryDTO.getUrls().forEach(url -> {
            urls.add(url.getUrl());
        });

        return getReportDetails(urls);
    }

    private List<Prospect> getReportDetails(List<String> urls) throws IOException {
        List<Prospect> prospects = new ArrayList<>();

        for (String url : urls) {
            prospects.add(getWhoIS(url));
        }

        prospectRepository.saveAll(prospects);
        return prospects;
    }

    private Prospect getWhoIS(String url) throws IOException {
        Prospect prospect = new Prospect();

        String query = getActiveStatus(url);
        String referAddressExtracted = referAddressExtract(query);
        String fingerQuery = getRemoteWhoIsConfiguration(url, referAddressExtracted);
        String firmDescriptionData = getFirmDescription(fingerQuery);
        String firmName = firmDescriptionData.substring(firmDescriptionData.indexOf("name") + 4, firmDescriptionData.indexOf("org") - 2);
        String firmID = firmDescriptionData.substring(firmDescriptionData.indexOf("id") + 3, firmDescriptionData.indexOf("country") - 2);
        String alternativeEmail = firmDescriptionData.substring(firmDescriptionData.indexOf("email") + 6, firmDescriptionData.indexOf("changed") - 2);

        prospect.setCompanyId(firmID.trim());
        prospect.setAlternativeEmail(alternativeEmail.trim());
        prospect.setCompanyName(firmName.trim());
        prospect.setActive(query.contains("ACTIVE"));
        prospect.setProspectEmail(getEmailsByUrl(url).get(0));
        prospect.setPlatform(getPlatform(url).trim());


        return prospect;
    }

    private String getFirmDescription(String fingerQuery) {
        return fingerQuery.substring(fingerQuery.indexOf("delete:"), fingerQuery.indexOf("Administrative contact:") - 2);
    }

    private String getRemoteWhoIsConfiguration(String url, String referAddressExtracted) throws IOException {
        WhoisClient fingerRequest = new WhoisClient();
        fingerRequest.connect(referAddressExtracted, 43);
        return fingerRequest.query(url);
    }

    private String getActiveStatus(String url) throws IOException {
        WhoisClient firstRequest = new WhoisClient();
        firstRequest.connect("whois.iana.org", 43);
        return firstRequest.query(url);
    }

    private String referAddressExtract(String query) {
        return query.substring(query.indexOf("whois"), query.indexOf("domain") - 2);
    }

    private String getPlatform(String url) throws IOException {
        String password = "6679d84065caa4e47bb6aa4820355b18b069854604720aa50ae8b2dc1c857d67faddf8";

        String[] command = {"curl", "-G", "https://whatcms.org/API/CMS",
                "--data-urlencode", "key=" + password,
                "--data-urlencode", "url=" + url};

        ProcessBuilder process = new ProcessBuilder(command);
        Process p;
        try {
            p = process.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();
            return builder.substring(result.indexOf("name") + 7, result.indexOf("confidence") - 3);

        } catch (IOException e) {
            System.out.print("error");
            e.printStackTrace();
            return "not found";
        }
    }

    public List<String> getEmailsByUrl(String url) {
        Document doc;
        List<String> emailSet = new ArrayList<>();
        System.out.println(url);
        try {
            doc = Jsoup.connect("https://" + url)
                    .userAgent("Mozilla")
                    .get();

            Pattern p = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
            Matcher matcher = p.matcher(doc.body().html());
            while (matcher.find()) {
                emailSet.add(matcher.group());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (emailSet.size() == 0) {
            emailSet.add("not found");
        }
        return emailSet;
    }
}

