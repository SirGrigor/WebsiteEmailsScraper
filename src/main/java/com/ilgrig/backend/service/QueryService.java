package com.ilgrig.backend.service;

import com.ilgrig.backend.dto.QueryDTO;
import com.ilgrig.backend.repository.ProspectRepository;
import org.apache.commons.net.whois.WhoisClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QueryService {
    private final ProspectRepository prospectRepository;

    public QueryService(ProspectRepository prospectRepository) {
        this.prospectRepository = prospectRepository;
    }

    public void getSingularQuery(QueryDTO queryDTO) throws IOException {
        WhoisClient whois = new WhoisClient();
        whois.connect("whois.iana.org", 43);
        System.out.println(whois.query(queryDTO.getUrls().get(0).getUrl()));
        whois.disconnect();

        System.out.println("emails: " + getEmailsByUrl("https://" + queryDTO.getUrls().get(0).getUrl()));
        getPlatform("https://" + queryDTO.getUrls().get(0).getUrl());
    }

    private void getPlatform(String url) throws IOException {
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
            System.out.print(result);

        } catch (IOException e) {
            System.out.print("error");
            e.printStackTrace();
        }
    }

    public Set<String> getEmailsByUrl(String url) {
        Document doc;
        Set<String> emailSet = new HashSet<>();

        try {
            doc = Jsoup.connect(url)
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

        return emailSet;
    }


}

