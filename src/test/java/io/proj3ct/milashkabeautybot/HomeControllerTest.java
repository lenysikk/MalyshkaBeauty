package io.proj3ct.milashkabeautybot;
import io.proj3ct.milashkabeautybot.controller.HomeController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHomePage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/home")
                        .with(user("admin").password("admin@123").roles("ADMIN")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home"));
    }

    @Test
    void testUsersPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/home/users")
                        .with(user("admin").password("admin@123").roles("ADMIN")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("users"));
    }

    @Test
    void testRecordsPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/home/records")
                        .with(user("admin").password("admin@123").roles("ADMIN")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("records"));
    }

    @Test
    void testMastersPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/home/masters")
                        .with(user("admin").password("admin@123").roles("ADMIN")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("masters"));
    }

    @Test
    void testServicesPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/home/services")
                        .with(user("admin").password("admin@123").roles("ADMIN")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("services"));
    }

    @Test
    void testResponsesPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/home/responses")
                        .with(user("admin").password("admin@123").roles("ADMIN")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("responses"));
    }
}
