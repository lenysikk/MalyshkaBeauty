package io.proj3ct.milashkabeautybot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.proj3ct.milashkabeautybot.controller.MasterController;
import io.proj3ct.milashkabeautybot.model.Master;
import io.proj3ct.milashkabeautybot.model.Service;
import io.proj3ct.milashkabeautybot.repositories.MasterRepository;
import io.proj3ct.milashkabeautybot.repositories.ServiceRepository;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(MasterController.class)
class MasterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MasterRepository masterRepository;

    @MockBean
    private ServiceRepository serviceRepository;

    @Test
    void testGetMasters() throws Exception {
        List<Master> masterList = new ArrayList<>();
        Long masterId = 1L;
        Master master1 = new Master();
        master1.setId(masterId);
        master1.setName("Кирилл");

        Master master2 = new Master();
        master2.setId(masterId);
        master2.setName("Алиса");
        masterList.add(master1);
        masterList.add(master2);

        when(masterRepository.findAll()).thenReturn(masterList);

        mockMvc.perform(get("/masters"))
                .andExpect(status().isOk())
                .andExpect(view().name("masters"))
                .andExpect(model().attributeExists("masters"))
                .andExpect(model().attribute("masters", masterList));

        verify(masterRepository, times(1)).findAll();
    }

    @Test
    void testAddMaster() throws Exception {
        Master newmaster = new Master();
        newmaster.setId(1L);
        newmaster.setName("Кирилл");

        mockMvc.perform(post("/masters/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "John"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/masters"));

        verify(masterRepository, times(1)).save(any(Master.class));
    }

    @Test
    void testGetMaster() throws Exception {
        Long masterId = 1L;
        Master master = new Master();
        master.setId(masterId);
        master.setName("Кирилл");

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));

        mockMvc.perform(get("/masters/{id}", masterId))
                .andExpect(status().isOk())
                .andExpect(view().name("master-details"))
                .andExpect(model().attributeExists("master"))
                .andExpect(model().attribute("master", master));

        verify(masterRepository, times(1)).findById(masterId);
    }

    @Test
    void testGetMaster_NotFound() throws Exception {
        Long masterId = 1L;

        when(masterRepository.findById(masterId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/masters/{id}", masterId))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/masters"));

        verify(masterRepository, times(1)).findById(masterId);
    }

    @Test
    void testShowEditMasterForm() throws Exception {
        Long masterId = 1L;
        Master master = new Master();
        master.setId(masterId);
        master.setName("Кирилл");

        List<Service> serviceList = new ArrayList<>();
        Service service1 = new Service();
        service1.setId(1L);
        service1.setName("Стрижка");
        Service service2 = new Service();
        service2.setId(2L);
        service2.setName("Окрашивание");
        serviceList.add(service1);
        serviceList.add(service2);

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(serviceRepository.findAll()).thenReturn(serviceList);

        mockMvc.perform(get("/masters/{id}/edit", masterId))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-master"))
                .andExpect(model().attributeExists("services"))
                .andExpect(model().attributeExists("master"))
                .andExpect(model().attribute("services", serviceList))
                .andExpect(model().attribute("master", master));

        verify(masterRepository, times(1)).findById(masterId);
        verify(serviceRepository, times(1)).findAll();
    }

    @Test
    void testShowEditMasterForm_NotFound() throws Exception {
        Long masterId = 1L;

        when(masterRepository.findById(masterId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/masters/{id}/edit", masterId))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/masters"));

        verify(masterRepository, times(1)).findById(masterId);
        verify(serviceRepository, never()).findAll();
    }

    @Test
    void testEditMaster() throws Exception {
        Long masterId = 1L;
        Master existingMaster = new Master();
        existingMaster.setId(masterId);
        existingMaster.setName("Кирилл");

        Master updatedMaster = new Master();
        updatedMaster.setId(masterId);
        updatedMaster.setName("Алиса");

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(existingMaster));

        mockMvc.perform(post("/masters/{id}/edit", masterId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", updatedMaster.getName()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/masters/" + masterId));
    }

    @Test
    void testEditMaster_NotFound() throws Exception {
        Long masterId = 1L;

        when(masterRepository.findById(masterId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/masters/{id}/edit", masterId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Alice"))

                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/masters"));

        verify(masterRepository, times(1)).findById(masterId);
        verify(masterRepository, never()).save(any(Master.class));
    }

    @Test
    void testDeleteMaster() throws Exception {
        Long masterId = 1L;
        Optional<Master> optionalMaster = Optional.of(new Master());

        when(masterRepository.findById(masterId)).thenReturn(optionalMaster);
        doNothing().when(masterRepository).deleteById(anyLong());

        mockMvc.perform(MockMvcRequestBuilders.get("/masters/{id}/delete", masterId))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/masters"));

        // Проверяем, что метод findById был вызван с нужным идентификатором
        verify(masterRepository, times(1)).findById(masterId);

        // Проверяем, что метод deleteById был вызван с нужным идентификатором
        verify(masterRepository, times(1)).deleteById(masterId);
    }
}
