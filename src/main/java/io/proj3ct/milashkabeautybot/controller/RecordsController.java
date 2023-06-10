package io.proj3ct.milashkabeautybot.controller;

import io.proj3ct.milashkabeautybot.repositories.MasterRepository;
import io.proj3ct.milashkabeautybot.repositories.RecordsRepository;
import io.proj3ct.milashkabeautybot.repositories.ServiceRepository;
import io.proj3ct.milashkabeautybot.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ui.Model;
import io.proj3ct.milashkabeautybot.model.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RecordsController {

    @Autowired
    private final RecordsRepository recordsRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final ServiceRepository serviceRepository;
    @Autowired
    private final MasterRepository masterRepository;

    @GetMapping("/records")
    public String getRecords(Model model) {
        List<Records> recordsList = recordsRepository.findAll();
        model.addAttribute("records", recordsList);
        return "records";
    }

    @GetMapping("/records/add")
    public String showAddRecordForm(Model model) {
        List<User> userList = userRepository.findAll();
        List<Service> serviceList = serviceRepository.findAll();
        List<Master> masterList = masterRepository.findAll();
        model.addAttribute("users", userList);
        model.addAttribute("services", serviceList);
        model.addAttribute("masters", masterList);
        model.addAttribute("record", new Records());
        return "add-record";
    }

    @PostMapping("/records/add")
    public String addRecord(@ModelAttribute("record") Records recordToSave) {
        recordsRepository.save(recordToSave);
        return "redirect:/records";
    }


    @GetMapping("/records/{id}/edit")
    public String showEditRecordForm(@PathVariable("id") Long id, Model model) {
        Optional<Records> optionalRecord = recordsRepository.findById(id);
        if (optionalRecord.isPresent()) {
            Records newRecord = optionalRecord.get();
            List<User> userList = userRepository.findAll();
            List<Service> serviceList = serviceRepository.findAll();
            List<Master> masterList = masterRepository.findAll();
            model.addAttribute("users", userList);
            model.addAttribute("services", serviceList);
            model.addAttribute("masters", masterList);
            model.addAttribute("record", newRecord);
            return "edit-record";
        }
        return "redirect:/records";
    }

    @PostMapping("/records/{id}/edit")
    public String editRecord(@PathVariable("id") Long id, @ModelAttribute("record") Records updatedRecord) {
        Optional<Records> optionalRecord = recordsRepository.findById(id);
        if (optionalRecord.isPresent()) {
            Records newRecord = optionalRecord.get();
            newRecord.setUser(updatedRecord.getUser());
            newRecord.setService(updatedRecord.getService());
            newRecord.setMaster(updatedRecord.getMaster());
            newRecord.setComment(updatedRecord.getComment());
            newRecord.setAccess(updatedRecord.isAccess());
            recordsRepository.save(newRecord);
        }
        return "redirect:/records";
    }

    @GetMapping("/records/{id}/delete")
    public String deleteRecord(@PathVariable("id") Long id) {
        try {
            recordsRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            // Запись не существует, обработка случая отсутствующей записи
            return "redirect:/error";
        }
        return "redirect:/records";
    }
}
