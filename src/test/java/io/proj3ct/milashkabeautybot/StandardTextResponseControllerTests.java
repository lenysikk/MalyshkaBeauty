package io.proj3ct.milashkabeautybot;

import io.proj3ct.milashkabeautybot.controller.StandardTextResponseController;
import io.proj3ct.milashkabeautybot.model.StandardTextResponse;
import io.proj3ct.milashkabeautybot.repositories.StandardTextResponseRepository;
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
@WebMvcTest(StandardTextResponseController.class)
public class StandardTextResponseControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StandardTextResponseRepository textResponseRepository;

    @Test
    public void testGetTextResponses() throws Exception {
        // Создание списка StandardTextResponse
        StandardTextResponse response1 = new StandardTextResponse();
        response1.setId(1L);
        response1.setName("Response 1");
        StandardTextResponse response2 = new StandardTextResponse();
        response2.setId(2L);
        response2.setName("Response 2");
        List<StandardTextResponse> textResponses = Arrays.asList(response1, response2);

        // Задание поведения макета репозитория
        when(textResponseRepository.findAll()).thenReturn(textResponses);

        // Выполнение GET-запроса на /responses
        mockMvc.perform(MockMvcRequestBuilders.get("/responses"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("responses"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("textResponses"))
                .andExpect(MockMvcResultMatchers.model().attribute("textResponses", textResponses));
    }

    @Test
    public void testEditResponse() throws Exception {
        // Создание объекта StandardTextResponse
        StandardTextResponse response = new StandardTextResponse();
        response.setId(1L);
        response.setName("Response 1");

        // Задание поведения макета репозитория
        when(textResponseRepository.findById(1L)).thenReturn(Optional.of(response));

        // Выполнение GET-запроса на /responses/edit/{id}
        mockMvc.perform(MockMvcRequestBuilders.get("/responses/edit/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("edit_responses"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("textResponse"))
                .andExpect(MockMvcResultMatchers.model().attribute("textResponse", response));
    }

    @Test
    public void testUpdateResponse() throws Exception {
        // Создание объекта StandardTextResponse
        StandardTextResponse response = new StandardTextResponse();
        response.setId(1L);
        response.setName("Response 1");
        response.setText("Updated text");

        // Задание поведения макета репозитория
        when(textResponseRepository.findById(1L)).thenReturn(Optional.of(response));

        // Выполнение POST-запроса на /responses/update/{id} с передачей параметров
        mockMvc.perform(MockMvcRequestBuilders.post("/responses/update/1")
                        .param("name", "Response 1")
                        .param("text", "Updated text"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/responses"));

        // Проверка, что метод save в textResponseRepository был вызван
        verify(textResponseRepository).save(response);
    }
}
