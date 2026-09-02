package br.ifes.ppg.management;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(properties = "ppg.data-file=target/test-data/ppg-management-test.json")
class PpgManagementApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void managesStudentsWithCrudEndpoints() throws Exception {
        String location = mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "registration": "2026001",
                                  "name": "Maria Silva",
                                  "email": "maria@example.edu"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"id\"\\s*:\\s*(\\d+).*", "$1");

        mockMvc.perform(put("/api/students/{id}", location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "registration": "2026001",
                                  "name": "Maria Souza",
                                  "email": "maria.souza@example.edu",
                                  "status": "GRADUATED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maria Souza"))
                .andExpect(jsonPath("$.status").value("GRADUATED"));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(1))));

        mockMvc.perform(delete("/api/students/{id}", location))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/students/{id}", location))
                .andExpect(status().isNotFound());
    }

    @Test
    void managesProfessorsWithCrudEndpoints() throws Exception {
        String location = mockMvc.perform(post("/api/professors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Joao Pereira",
                                  "email": "joao@example.edu"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"id\"\\s*:\\s*(\\d+).*", "$1");

        mockMvc.perform(put("/api/professors/{id}", location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Joao Pereira",
                                  "email": "jpereira@example.edu",
                                  "status": "INACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jpereira@example.edu"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(delete("/api/professors/{id}", location))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/professors/{id}", location))
                .andExpect(status().isNotFound());
    }
}
