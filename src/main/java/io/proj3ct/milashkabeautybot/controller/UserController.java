package io.proj3ct.milashkabeautybot.controller;

import io.proj3ct.milashkabeautybot.model.Records;
import io.proj3ct.milashkabeautybot.repositories.RecordsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import io.proj3ct.milashkabeautybot.model.User;
import io.proj3ct.milashkabeautybot.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecordsRepository recordsRepository;

    @GetMapping("/users") // Get запрос при переходе на users
    public String getUsers(@RequestParam(value = "search", required = false) String searchQuery, Model model) {
        List<User> users;
        if (searchQuery != null && !searchQuery.isEmpty()) {
            users = userRepository.findByNameContainingIgnoreCase(searchQuery);
            log.info("Retrieved {} users matching search query '{}'", users.size(), searchQuery);
        } else {
            users = userRepository.findAll();
            log.info("Retrieved {} users from the database", users.size());
        }
        model.addAttribute("users", users);
        return "users";
    }

    @GetMapping("/users/{id}") // просмотр пользователя с id из таблицы users
    public String getUser(@PathVariable("id") Long id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        model.addAttribute("user", user);
        log.info("Retrieved user with id {} from the database", id);
        return "user";
    }

    @GetMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            // Пользователь не существует, обработка случая отсутствующего пользователя
            return "redirect:/error"; // URL для обработки ошибки
        }
        // Удаление записей в таблице Records, связанных с удаляемым пользователем
        List<Records> userRecords = recordsRepository.findByUserId(id);
        recordsRepository.deleteAll(userRecords);

        // Удаление пользователя
        userRepository.deleteById(id);
        log.info("Deleted user with id {}", id);
        return "redirect:/users";
    }
}