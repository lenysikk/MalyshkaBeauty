package io.proj3ct.milashkabeautybot.controller;

import io.proj3ct.milashkabeautybot.model.Master;
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
public class MasterController {
    private final MasterRepository masterRepository;
    private final ServiceRepository serviceRepository;

    @GetMapping("/masters")
    public String getMasters(Model model) {
        List<Master> masterList = masterRepository.findAll();
        model.addAttribute("masters", masterList);
        return "masters";
    }

    @GetMapping("/masters/add")
    public String showAddMasterForm(Model model) {
        List<Service> serviceList = serviceRepository.findAll();
        model.addAttribute("services", serviceList);
        model.addAttribute("master", new Master());
        return "add-master";
    }

    @PostMapping("/masters/add")
    public String addMaster(@ModelAttribute("master") Master master) {
        masterRepository.save(master);
        return "redirect:/masters";
    }

    @GetMapping("/masters/{id}")
    public String getMaster(@PathVariable("id") Long id, Model model) {
        Optional<Master> optionalMaster = masterRepository.findById(id);
        if (optionalMaster.isPresent()) {
            Master master = optionalMaster.get();
            model.addAttribute("master", master);
            return "master-details";
        }
        return "redirect:/masters";
    }

    @GetMapping("/masters/{id}/edit")
    public String showEditMasterForm(@PathVariable("id") Long id, Model model) {
        Optional<Master> optionalMaster = masterRepository.findById(id);
        if (optionalMaster.isPresent()) {
            Master master = optionalMaster.get();
            List<Service> serviceList = serviceRepository.findAll();
            model.addAttribute("services", serviceList);
            model.addAttribute("master", master);
            return "edit-master";
        }
        return "redirect:/masters";
    }

    @PostMapping("/masters/{id}/edit")
    public String editMaster(@PathVariable("id") Long id, @ModelAttribute("master") Master updatedMaster) {
        Optional<Master> optionalMaster = masterRepository.findById(id);
        if (optionalMaster.isPresent()) {
            Master master = optionalMaster.get();
            master.setName(updatedMaster.getName());
            master.setServices(updatedMaster.getServices());
            masterRepository.save(master);
            return "redirect:/masters/" + id;
        }
        return "redirect:/masters";
    }

    @GetMapping("/masters/{id}/delete")
    public String deleteMaster(@PathVariable("id") Long id) {
        Optional<Master> optionalMaster = masterRepository.findById(id);
        if (optionalMaster.isEmpty()) {
            // Мастер не существует, обработка случая отсутствующего мастера
            return "redirect:/masters";
        }

        masterRepository.deleteById(id);
        log.info("Deleted master with id {}", id);
        return "redirect:/masters";
    }
}
