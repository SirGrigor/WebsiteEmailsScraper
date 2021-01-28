package com.ilgrig.backend.service;

import com.ilgrig.backend.dto.QueryDTO;
import com.ilgrig.backend.repository.ProspectRepository;
import org.apache.commons.net.whois.WhoisClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QueryService {
    private final ProspectRepository prospectRepository;
    private static Pattern pattern;
    private Matcher matcher;

    private static final String WHOIS_SERVER_PATTERN = "Whois Server:\\s(.*)";

    static {
        pattern = Pattern.compile(WHOIS_SERVER_PATTERN);
    }

    public QueryService(ProspectRepository prospectRepository) {
        this.prospectRepository = prospectRepository;
    }

    public void getSingularQuery(QueryDTO queryDTO) throws IOException {
        WhoisClient whois = new WhoisClient();
            whois.connect("whois.iana.org", 43);
            System.out.println(whois.query(queryDTO.getUrls().get(0).getUrl()));
            whois.disconnect();

        System.out.println(getEmailsByUrl("https://" + queryDTO.getUrls().get(0).getUrl()));
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

