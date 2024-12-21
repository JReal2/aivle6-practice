package com.example.mvn.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {
    @GetMapping("/hello")
    public String helloUser(Model model) {
        model.addAttribute("userName", "Jeoungryeol");

        return "helloUser";
    }
}
