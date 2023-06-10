package io.proj3ct.milashkabeautybot.controller;

import io.proj3ct.milashkabeautybot.model.StandardTextResponse;
import io.proj3ct.milashkabeautybot.repositories.StandardTextResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StandardTextResponseController {
    private final StandardTextResponseRepository textResponseRepository;

    @GetMapping("/responses") // выввести всю таблицу
    public String getTextResponses(Model model) {
        List<StandardTextResponse> textResponses = textResponseRepository.findAll();
        model.addAttribute("textResponses", textResponses);
        return "responses";
    }

    @GetMapping("/responses/edit/{id}") // вывод страницы с возможностью изменения
    public String editResponse(@PathVariable("id") Long id, Model model) {
        StandardTextResponse textResponse = textResponseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Text Response ID: " + id));
        model.addAttribute("textResponse", textResponse);
        return "edit_responses";
    }

    @PostMapping("/responses/update/{id}") // отправка обновленных данных на сервер
    public String updateResponse(@PathVariable("id") Long id, @ModelAttribute StandardTextResponse updatedResponse) {
        StandardTextResponse textResponse = textResponseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Text Response ID: " + id));
        textResponse.setName(updatedResponse.getName());
        textResponse.setText(updatedResponse.getText());
        textResponseRepository.save(textResponse);
        return "redirect:/responses";
    }
}
