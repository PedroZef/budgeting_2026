package br.com.budgeting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/login")
    public String forwardLogin() {
        return "forward:/index.html";
    }
}
