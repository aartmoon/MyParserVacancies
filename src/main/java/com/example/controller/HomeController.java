package com.example.controller;

import com.example.config.Constants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("languages", Constants.LANGUAGES);
        model.addAttribute("cities", Constants.CITIES);
        return "index";
    }
}