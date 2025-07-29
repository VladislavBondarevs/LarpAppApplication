package de.larphelden.larp_app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccessDeniedController {

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("message", "Eingang nur für Admin");
        return "access_denied";
    }
}