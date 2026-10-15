package com.resume.resume_api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaWebController {

    @GetMapping(value = {
            "/dashboard",
            "/resumes",
            "/jobs",
            "/analysis",
            "/skills",
            "/architecture"
    })
    public String forwardToSpa() {
        return "forward:/index.html";
    }
}
