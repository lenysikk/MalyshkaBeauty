package io.proj3ct.milashkabeautybot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    @GetMapping("/home/users")
    public String usersPage() {
        return "users";
    }

    @GetMapping("/home/records")
    public String recordsPage() {
        return "records";
    }

    @GetMapping("/home/masters")
    public String mastersPage() {
        return "masters";
    }

    @GetMapping("/home/services")
    public String servicesPage() {
        return "services";
    }

    @GetMapping("/home/responses")
    public String responsesPage() {
        return "responses";
    }
}
