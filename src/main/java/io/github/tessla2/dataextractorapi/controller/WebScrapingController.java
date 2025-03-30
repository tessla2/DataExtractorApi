package io.github.tessla2.dataextractorapi.controller;

import io.github.tessla2.dataextractorapi.service.ExtractorService;
import io.github.tessla2.dataextractorapi.service.WebScraping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/scraping")
public class WebScrapingController {

    private final WebScraping webScraping;

    public WebScrapingController(WebScraping webScraping, ExtractorService extractorService) {
        this.webScraping = webScraping;
    }

    @GetMapping("/pdfs")
    public String pdfList(){
         webScraping.linksSearch();
         return "Arquivos baixados e compactados com sucesso!";
    }

}
