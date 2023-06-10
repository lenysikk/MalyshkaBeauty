package io.proj3ct.milashkabeautybot;

import io.proj3ct.milashkabeautybot.controller.RecordsController;
import io.proj3ct.milashkabeautybot.model.Master;
import io.proj3ct.milashkabeautybot.model.Records;
import io.proj3ct.milashkabeautybot.model.Service;
import io.proj3ct.milashkabeautybot.model.User;
import io.proj3ct.milashkabeautybot.repositories.MasterRepository;
import io.proj3ct.milashkabeautybot.repositories.RecordsRepository;
import io.proj3ct.milashkabeautybot.repositories.ServiceRepository;
import io.proj3ct.milashkabeautybot.repositories.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.dao.EmptyResultDataAccessException;

@WebMvcTest(RecordsController.class)
class RecordsControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecordsRepository recordsRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ServiceRepository serviceRepository;

    @MockBean
    private MasterRepository masterRepository;

    @Test
    void testGetRecords() throws Exception {
        List<Records> recordsList = new ArrayList<>();
        Records record1 = new Records();
        record1.setId(1L);
        record1.setComment("Comment 1");
        Records record2 = new Records();
        record2.setId(2L);
        record2.setComment("Comment 2");
        recordsList.add(record1);
        recordsList.add(record2);

        when(recordsRepository.findAll()).thenReturn(recordsList);

        mockMvc.perform(get("/records")
                        .with(user("admin").password("admin@123").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("records"))
                .andExpect(model().attributeExists("records"))
                .andExpect(model().attribute("records", recordsList));

        verify(recordsRepository, times(1)).findAll();
    }

    @Test
    void testShowAddRecordForm() throws Exception {
        List<User> userList = new ArrayList<>();
        User user1 = new User();
        user1.setId(1L);
        user1.setName("John");
        User user2 = new User();
        user2.setId(2L);
        user2.setName("Jane");
        userList.add(user1);
        userList.add(user2);

        List<Service> serviceList = new ArrayList<>();
        Service service1 = new Service();
        service1.setId(1L);
        service1.setName("Haircut");
        Service service2 = new Service();
        service2.setId(2L);
        service2.setName("Coloring");
        serviceList.add(service1);
        serviceList.add(service2);

        List<Master> masterList = new ArrayList<>();
        Master master1 = new Master();
        master1.setId(1L);
        master1.setName("Alex");
        Master master2 = new Master();
        master2.setId(2L);
        master2.setName("Emily");
        masterList.add(master1);
        masterList.add(master2);

        when(userRepository.findAll()).thenReturn(userList);
        when(serviceRepository.findAll()).thenReturn(serviceList);
        when(masterRepository.findAll()).thenReturn(masterList);

        mockMvc.perform(get("/records/add")
                .with(user("admin").password("admin@123").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("add-record"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("services"))
                .andExpect(model().attributeExists("masters"))
                .andExpect(model().attributeExists("record"))
                .andExpect(model().attribute("users", userList))
                .andExpect(model().attribute("services", serviceList))
                .andExpect(model().attribute("masters", masterList))
                .andExpect(model().attribute("record", Matchers.instanceOf(Records.class)));

        verify(userRepository, times(1)).findAll();
        verify(serviceRepository, times(1)).findAll();
        verify(masterRepository, times(1)).findAll();
    }

    @Test
    void testAddRecord() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("John");

        Service service = new Service();
        service.setId(1L);
        service.setName("Haircut");

        Master master = new Master();
        master.setId(1L);
        master.setName("Alex");

        Records record = new Records();
        record.setUser(user);
        record.setService(service);
        record.setMaster(master);
        record.setComment("Comment");

        when(recordsRepository.save(any(Records.class))).thenReturn(record);

        mockMvc.perform(post("/records/add")
                        .flashAttr("record", record)
                        .with(user("admin").password("admin@123").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/records"));

        verify(recordsRepository, times(1)).save(record);
    }

    @Test
    void testShowEditRecordForm_RecordExists() throws Exception {
        Long recordId = 1L;
        Records record = new Records();
        record.setId(recordId);
        record.setComment("Comment");

        User user1 = new User();
        user1.setId(1L);
        user1.setName("John");
        User user2 = new User();
        user2.setId(2L);
        user2.setName("Jane");
        List<User> userList =new ArrayList<>();
        userList.add(user1);
        userList.add(user2);

        record.setUser(user1);

        List<Service> serviceList = new ArrayList<>();
        Service service1 = new Service();
        service1.setId(1L);
        service1.setName("Haircut");
        Service service2 = new Service();
        service2.setId(2L);
        service2.setName("Coloring");
        serviceList.add(service1);
        serviceList.add(service2);

        List<Master> masterList = new ArrayList<>();
        Master master1 = new Master();
        master1.setId(1L);
        master1.setName("Alex");
        Master master2 = new Master();
        master2.setId(2L);
        master2.setName("Emily");
        masterList.add(master1);
        masterList.add(master2);
        record.setService(service1);
        record.setMaster(master1);

        when(recordsRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(userRepository.findAll()).thenReturn(userList);
        when(serviceRepository.findAll()).thenReturn(serviceList);
        when(masterRepository.findAll()).thenReturn(masterList);

        mockMvc.perform(get("/records/{id}/edit", recordId)
                        .with(user("admin").password("admin@123").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-record"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("services"))
                .andExpect(model().attributeExists("masters"))
                .andExpect(model().attributeExists("record"))
                .andExpect(model().attribute("users", userList))
                .andExpect(model().attribute("services", serviceList))
                .andExpect(model().attribute("masters", masterList))
                .andExpect(model().attribute("record", record));

        verify(recordsRepository, times(1)).findById(recordId);
        verify(userRepository, times(1)).findAll();
        verify(serviceRepository, times(1)).findAll();
        verify(masterRepository, times(1)).findAll();
    }


    @Test
    void testEditRecord_RecordExists() throws Exception {
        Long recordId = 1L;
        Records existingRecord = new Records();
        existingRecord.setId(recordId);
        existingRecord.setComment("Comment");

        User user = new User();
        user.setId(1L);
        user.setName("John");

        Service service = new Service();
        service.setId(1L);
        service.setName("Haircut");

        Master master = new Master();
        master.setId(1L);
        master.setName("Alex");

        Records updatedRecord = new Records();
        updatedRecord.setUser(user);
        updatedRecord.setService(service);
        updatedRecord.setMaster(master);
        updatedRecord.setComment("Updated comment");

        when(recordsRepository.findById(recordId)).thenReturn(Optional.of(existingRecord));
        when(recordsRepository.save(any(Records.class))).thenReturn(existingRecord);

        mockMvc.perform(post("/records/{id}/edit", recordId)
                        .flashAttr("record", updatedRecord)
                        .with(user("admin").password("admin@123").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/records"));

        verify(recordsRepository, times(1)).findById(recordId);
        verify(recordsRepository, times(1)).save(existingRecord);
    }

    @Test
    void testEditRecord_RecordNotExists() throws Exception {
        Long recordId = 1L;

        when(recordsRepository.findById(recordId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/records/{id}/edit", recordId)
                        .with(user("admin").password("admin@123").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/records"));

        verify(recordsRepository, times(1)).findById(recordId);
    }

    @Test
    void testDeleteRecord_RecordExists() throws Exception {
        Long recordId = 1L;

        mockMvc.perform(get("/records/{id}/delete", recordId)
                        .with(user("admin").password("admin@123").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/records"));

        verify(recordsRepository, times(1)).deleteById(recordId);
    }

    @Test
    void testDeleteRecord_RecordNotExists() throws Exception {
        Long recordId = 1L;

        doThrow(EmptyResultDataAccessException.class).when(recordsRepository).deleteById(recordId);

        mockMvc.perform(get("/records/{id}/delete", recordId)
                        .with(user("admin").password("admin@123").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/error"));

        verify(recordsRepository, times(1)).deleteById(recordId);
    }
}
