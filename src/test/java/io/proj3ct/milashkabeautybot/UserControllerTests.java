package io.proj3ct.milashkabeautybot;

import io.proj3ct.milashkabeautybot.controller.UserController;
import io.proj3ct.milashkabeautybot.model.User;
import io.proj3ct.milashkabeautybot.repositories.RecordsRepository;
import io.proj3ct.milashkabeautybot.repositories.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RecordsRepository recordsRepository;

    @Test
    void testGetUsersWithoutSearchQuery() throws Exception {
        // Создание списка пользователей
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User 1");
        User user2 = new User();
        user2.setId(2L);
        user2.setName("User 2");
        List<User> users = Arrays.asList(user1, user2);

        // Задание поведения макета userRepository
        when(userRepository.findAll()).thenReturn(users);

        // Выполнение GET-запроса на /users без searchQuery
        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("users"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("users"))
                .andExpect(MockMvcResultMatchers.model().attribute("users", users));
    }

    @Test
    void testGetUsersWithSearchQuery() throws Exception {
        // Создание списка пользователей
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User 1");
        User user2 = new User();
        user2.setId(2L);
        user2.setName("User 2");
        List<User> users = Arrays.asList(user1, user2);

        // Задание поведения макета userRepository
        when(userRepository.findByNameContainingIgnoreCase("User")).thenReturn(users);

        // Выполнение GET-запроса на /users с searchQuery
        mockMvc.perform(MockMvcRequestBuilders.get("/users").param("search", "User"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("users"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("users"))
                .andExpect(MockMvcResultMatchers.model().attribute("users", users));
    }

    @Test
    void testGetUser() throws Exception {
        // Создание пользователя
        User user = new User();
        user.setId(1L);
        user.setName("User 1");

        // Задание поведения макета userRepository
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Выполнение GET-запроса на /users/{id}
        mockMvc.perform(MockMvcRequestBuilders.get("/users/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("user"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("user"))
                .andExpect(MockMvcResultMatchers.model().attribute("user", user));
    }

    @Test
    void testDeleteUser() throws Exception {
        // Создание пользователя
        User user = new User();
        user.setId(1L);
        user.setName("User 1");

        // Задание поведения макета userRepository
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Выполнение GET-запроса на /users/{id}/delete
        mockMvc.perform(MockMvcRequestBuilders.get("/users/1/delete"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/users"));

        // Проверка, что методы deleteById и deleteAll были вызваны
        verify(userRepository).deleteById(1L);
    }
}
