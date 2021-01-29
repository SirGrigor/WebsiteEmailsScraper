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
            System.out.println("Current URL: " + url);
            prospects.add(getWhoIS(url));
        }

        prospectRepository.saveAll(prospects);
    }

    private Prospect getWhoIS(String url) throws IOException {
        Prospect prospect = new Prospect();
        String query = getActiveStatus(url);
        try {

            WhoisClient whoisClient = new WhoisClient();
            whoisClient.connect("whois.iana.org", 43);
            String queryRecord = whoisClient.query(url);
            prospect.setActive(queryRecord.contains("ACTIVE"));

            if (query.contains("refer") && query.contains("domain")) {

                String remoteAddress = queryRecord.substring(query.indexOf("refer") + 7, query.indexOf("domain")).trim();

                WhoisClient remoteClient = new WhoisClient();
                remoteClient.connect(remoteAddress, 43);
                String remoteQuery = remoteClient.query(url);

                if (remoteQuery.contains("Registrant")) {
                    System.out.println(remoteQuery);
                    String firmDescription = remoteQuery.substring(remoteQuery.indexOf("Registrant"), remoteQuery.indexOf("Administrative contact:") - 1);
                    String firmName = firmDescription.substring(firmDescription.indexOf("name") + 5, firmDescription.indexOf("org")).trim();
                    String firmId = firmDescription.substring(firmDescription.indexOf("id:") + 7, firmDescription.indexOf("country")).trim();
                    String firmAlternativeEmail = firmDescription.substring(firmDescription.indexOf("email:") + 7, firmDescription.indexOf("changed")).trim();

                    prospect.setCompanyName(firmName);
                    prospect.setCompanyId(firmId);
                    prospect.setProspectEmail(firmAlternativeEmail);

                } else {
                    return prospect;
                }

            } else {
                return prospect;
            }


        } catch (Exception e) {
            log.error(String.valueOf(e));
            return prospect;
        }
        return prospect;
    }


    private String getActiveStatus(String url) throws IOException {
        WhoisClient firstRequest = new WhoisClient();
        firstRequest.connect("whois.iana.org", 43);
        firstRequest.setConnectTimeout(150);

        return firstRequest.query(url);
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

