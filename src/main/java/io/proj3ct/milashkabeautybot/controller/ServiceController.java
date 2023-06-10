package io.proj3ct.milashkabeautybot.controller;

import io.proj3ct.milashkabeautybot.model.Service;
import io.proj3ct.milashkabeautybot.repositories.MasterRepository;
import io.proj3ct.milashkabeautybot.repositories.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceRepository serviceRepository;
    private final MasterRepository masterRepository;

    @GetMapping("/services")
    public String getServices(Model model) {
        List<Service> services = serviceRepository.findAll();
        log.info("Retrieved {} services from the database", services.size());
        model.addAttribute("services", services);
        return "services";
    }

    @GetMapping("/services/add")
    public String addServiceForm(Model model) {
        model.addAttribute("service", new Service());
        log.info("Added a new service");
        return "add-service";
    }

    @PostMapping("/services/add")
    public String addService(@ModelAttribute("service") Service service) {
        serviceRepository.save(service);
        log.info("Added a new service with id {}", service.getId());
        return "redirect:/services";
    }

    @GetMapping("/services/{id}") // просмотр конкретной записи
    public String getService(@PathVariable("id") Long id, Model model) {
        Service service = serviceRepository.findById(id).orElse(null);
        model.addAttribute("service", service);
        log.info("Retrieved service with id {} from the database", id);
        return "service";
    }

    @GetMapping("/services/{id}/delete")
    public String deleteService(@PathVariable("id") Long id) {
        Optional<Service> optionalService = serviceRepository.findById(id);
        if (optionalService.isEmpty()) {
            // Запись не существует, обработка случая отсутствующей записи
            return "redirect:/error"; // URL для обработки ошибки
        }

        serviceRepository.deleteById(id);
        log.info("Deleted service with id {}", id);
        return "redirect:/services";
    }
}
