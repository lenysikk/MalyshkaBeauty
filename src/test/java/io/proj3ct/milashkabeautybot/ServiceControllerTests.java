package io.proj3ct.milashkabeautybot;

import io.proj3ct.milashkabeautybot.controller.ServiceController;
import io.proj3ct.milashkabeautybot.model.Service;
import io.proj3ct.milashkabeautybot.repositories.MasterRepository;
import io.proj3ct.milashkabeautybot.repositories.ServiceRepository;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(ServiceController.class)
class ServiceControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MasterRepository masterRepository;
    @MockBean
    private ServiceRepository serviceRepository;

    @Test
    void testGetServices() throws Exception {
        List<Service> services = new ArrayList<>();
        Service service1 = new Service();
        service1.setName("Service 1");
        Service service2 = new Service();
        service2.setName("Service 2");
        services.add(service1);
        services.add(service2);

        given(serviceRepository.findAll()).willReturn(services);

        // Выполнение GET-запроса на /services
        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Service 1")))
                .andExpect(content().string(containsString("Service 2")));
    }

    @Test
    void testAddService() throws Exception {
        // Выполнение POST-запроса на /services/add с передачей параметров
        mockMvc.perform(MockMvcRequestBuilders.post("/services/add")
                        .param("name", "Test Service"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/services"));

        // Проверка, что сервис был успешно добавлен
        verify(serviceRepository).save(Mockito.any(Service.class));
    }

    @Test
    void testGetService() throws Exception {
        // Создание сервиса для добавления и получения его идентификатора
        Service service = new Service();
        service.setName("Test Service");
        service.setId(1L);

        given(serviceRepository.findById(1L)).willReturn(Optional.of(service));

        // Выполнение GET-запроса на /services/{id} для получения добавленного сервиса
        mockMvc.perform(get("/services/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Test Service")));
    }

    @Test
    void testDeleteService() throws Exception {
        // Создание сервиса для удаления и получения его идентификатора
        Service service = new Service();
        service.setName("Test Service");
        service.setId(1L);

        given(serviceRepository.findById(1L)).willReturn(Optional.of(service));

        // Выполнение GET-запроса на /services/{id}/delete для удаления сервиса
        mockMvc.perform(get("/services/{id}/delete", 1))
                .andExpect(status().isFound());

        // Проверка, что сервис был успешно удален
        verify(serviceRepository, times(1)).deleteById(1L);
    }
}
