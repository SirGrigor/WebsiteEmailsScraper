package com.ilgrig.backend.service;

import com.ilgrig.backend.dto.UrlCsvDTO;
import com.ilgrig.backend.entity.Prospect;
import com.ilgrig.backend.repository.ProspectRepository;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class QueryService {
    private final ProspectRepository prospectRepository;

    public QueryService(ProspectRepository prospectRepository) {
        this.prospectRepository = prospectRepository;
    }


    public void getReport(List<UrlCsvDTO> queryDTO) throws IOException {
        List<String> urls = new ArrayList<>();
        queryDTO.forEach(url -> urls.add(url.getUrl()));
        getReportDetails(urls);
    }

    private void getReportDetails(List<String> urls) throws IOException {
        List<Prospect> prospects = new ArrayList<>();

        for (String url : urls) {
            log.debug("Current URL: " + url);
            System.out.println("Current URL: " + url);
            prospects.add(getWhoIS(url));
        }

        prospectRepository.saveAll(prospects);
    }

    private Prospect getWhoIS(String url) throws IOException {
        Prospect prospect = new Prospect();

        String query = getActiveStatus(url);
        try {
            if (!query.contains("The queried object does not exist")) {
                String referAddressExtracted = referAddressExtract(query);
                String fingerQuery = getRemoteWhoIsConfiguration(url, referAddressExtracted);
                String firmDescriptionData = getFirmDescription(fingerQuery);
                String firmName = firmDescriptionData.substring(firmDescriptionData.indexOf("name") + 5, firmDescriptionData.indexOf("org") - 1).trim();
                String firmID = firmDescriptionData.substring(firmDescriptionData.indexOf("id") + 3, firmDescriptionData.indexOf("country") - 1).trim();
                String alternativeEmail = firmDescriptionData.substring(firmDescriptionData.indexOf("email") + 6, firmDescriptionData.indexOf("changed") - 1).trim();

                prospect.setCompanyId(firmID);
                prospect.setAlternativeEmail(alternativeEmail);
                prospect.setCompanyName(firmName);
                prospect.setActive(query.contains("ACTIVE"));
            } else {
                prospect.setCompanyId("Not found");
                prospect.setAlternativeEmail("none");
                prospect.setCompanyName("not found");
                prospect.setActive(false);
            }
            prospect.setProspectEmail(getEmailsByUrl(url).get(0));
            prospect.setPlatform(getPlatform(url));

        } catch (IOException e) {
            e.printStackTrace();

            Prospect failedToFindProspect = new Prospect();
            failedToFindProspect.setProspectEmail("none");
            failedToFindProspect.setActive(false);
            failedToFindProspect.setAlternativeEmail("none");
            failedToFindProspect.setCompanyName("not found");
            log.error("prospect for URL: " + url + " cannot be procceed");
            return prospect;
        }
        return prospect;
    }

    private String getFirmDescription(String fingerQuery) {
        log.error(fingerQuery);
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
        System.out.println(query);
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
            String line;
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

