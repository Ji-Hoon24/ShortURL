package com.jh.shorturl.shorter.controller;

import com.jh.shorturl.shorter.service.ShorterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequiredArgsConstructor
public class RedirectController {

    private final ShorterService shorterService;

    @GetMapping("/{domain}")
    public RedirectView longUrl(
            @PathVariable String domain
    ) {
        String longUrl = shorterService.getLongUrl(domain);
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(longUrl);
        return redirectView;
    }
}
