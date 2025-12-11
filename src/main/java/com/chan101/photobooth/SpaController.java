package com.chan101.photobooth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping(value = { "/"})
    public String forwardSPA() {
        return "forward:/index.html";
    }
}
