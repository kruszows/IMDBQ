package org.kruszows.imdbq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SpringBootApplication
@Controller
public class ImdbqApplication {

    private static final WebCrawler webCrawler = new WebCrawler();

    public static void main(String[] args) {
        webCrawler.parseSources();
        SpringApplication.run(ImdbqApplication.class, args);
    }

    public static WebCrawler getWebCrawler() {
        return webCrawler;
    }

    @GetMapping(params = "query")
    public String getQueryResult(@RequestParam String query, ModelMap modelMap) {
        if (modelMap.containsKey("results")) {
            modelMap.replace("results", ImdbqApplication.getWebCrawler().search(query));
            modelMap.replace("query", query);
        }
        else {
            modelMap.put("results", ImdbqApplication.getWebCrawler().search(query));
            modelMap.put("query", query);
        }
        return "index";
    }

    @GetMapping
    public String getUserInput() {
        return "index";
    }

}
