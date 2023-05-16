package com.jh.shorturl.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import static com.jh.shorturl.config.ApiResultUtil.success;

@Controller
public class SwaggerRedirector {

    @RequestMapping("/swagger")
    public RedirectView swagger() {
        return new RedirectView("/swagger-ui/index.html");
    }

    @ResponseBody
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ApiResultUtil.ApiResult<Boolean> home() {
        return success(true);
    }
}
