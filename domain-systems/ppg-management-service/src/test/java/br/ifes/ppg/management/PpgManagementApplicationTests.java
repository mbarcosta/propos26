package br.ifes.ppg.management;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "ppg.data-file=target/test-data/ppg-management-test.json",
        "ppg.documents-dir=target/test-data/documents"
})
class PpgManagementApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void searchesStudentsAndReportsStatus() throws Exception {
        long studentId = createStudent("2026001", "Maria Silva", "maria@example.edu");

        mockMvc.perform(get("/api/students/by-registration/{registration}", "2026001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentId));

        mockMvc.perform(get("/api/students/by-email").param("email", "MARIA@example.edu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registration").value("2026001"));

        mockMvc.perform(get("/api/students/{id}/status", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void searchesProfessorsByEmail() throws Exception {
        long professorId = createProfessor("Joao Pereira", "joao@example.edu");

        mockMvc.perform(get("/api/professors/by-email").param("email", "JOAO@example.edu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(professorId));
    }

    @Test
    void searchesAndCancelsAdvisorships() throws Exception {
        long studentId = createStudent("2026002", "Ana Lima", "ana@example.edu");
        long professorId = createProfessor("Paula Costa", "paula@example.edu");
        long advisorshipId = createAdvisorship(studentId, professorId);

        mockMvc.perform(get("/api/advisorships/by-student/{studentId}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(get("/api/advisorships/by-advisor/{advisorId}", professorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(post("/api/advisorships/{id}/cancel", advisorshipId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/advisorships/{id}", advisorshipId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void createsUpdatesAndChangesDefenseStatus() throws Exception {
        long studentId = createStudent("2026003", "Bruno Reis", "bruno@example.edu");
        long professorId = createProfessor("Carla Nunes", "carla@example.edu");
        long defenseId = createDefense(studentId, professorId);

        mockMvc.perform(get("/api/defenses/by-student/{studentId}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(put("/api/defenses/{id}", defenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": %d,
                                  "advisorId": %d,
                                  "title": "Dissertacao revisada",
                                  "date": "2026-10-15",
                                  "location": "Sala 2",
                                  "status": "HOMOLOGATED",
                                  "committeeMembers": [
                                    {"name":"Avaliador Um","email":"um@example.edu","institution":"Ifes","role":"MEMBER"}
                                  ]
                                }
                                """.formatted(studentId, professorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dissertacao revisada"))
                .andExpect(jsonPath("$.committeeMembers[0].name").value("Avaliador Um"));

        mockMvc.perform(patch("/api/defenses/{id}/status", defenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DEFENDED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEFENDED"));

        mockMvc.perform(post("/api/defenses/{id}/cancel", defenseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void uploadsDownloadsAndLinksDissertation() throws Exception {
        long studentId = createStudent("2026004", "Luiza Ramos", "luiza@example.edu");
        long professorId = createProfessor("Diego Mota", "diego@example.edu");
        long defenseId = createDefense(studentId, professorId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "dissertacao.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "conteudo da dissertacao".getBytes());

        mockMvc.perform(multipart("/api/defenses/{defenseId}/dissertation", defenseId).file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defenseId").value(defenseId))
                .andExpect(jsonPath("$.fileName").value("dissertacao.txt"))
                .andExpect(jsonPath("$.version").value(1));

        mockMvc.perform(get("/api/defenses/{defenseId}/dissertation", defenseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("dissertacao.txt"));

        mockMvc.perform(get("/api/defenses/{defenseId}/dissertation/download", defenseId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("dissertacao.txt")))
                .andExpect(content().string("conteudo da dissertacao"));

        mockMvc.perform(post("/api/defenses/{defenseId}/dissertation/download-link", defenseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defenseId").value(defenseId))
                .andExpect(jsonPath("$.downloadUrl").value(containsString("/api/defenses/" + defenseId + "/dissertation/download")));
    }

    private long createStudent(String registration, String name, String email) throws Exception {
        String response = mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"registration":"%s","name":"%s","email":"%s","status":"ACTIVE"}
                                """.formatted(registration, name, email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return id(response);
    }

    private long createProfessor(String name, String email) throws Exception {
        String response = mockMvc.perform(post("/api/professors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","email":"%s","status":"ACTIVE"}
                                """.formatted(name, email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return id(response);
    }

    private long createAdvisorship(long studentId, long professorId) throws Exception {
        String response = mockMvc.perform(post("/api/advisorships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": %d,
                                  "advisorId": %d,
                                  "title": "Pesquisa Aplicada",
                                  "researchArea": "Sistemas de Informacao"
                                }
                                """.formatted(studentId, professorId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return id(response);
    }

    private long createDefense(long studentId, long professorId) throws Exception {
        String response = mockMvc.perform(post("/api/defenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": %d,
                                  "advisorId": %d,
                                  "title": "Dissertacao",
                                  "date": "2026-10-01",
                                  "location": "Sala 1",
                                  "committeeMembers": [
                                    {"name":"Avaliador","email":"avaliador@example.edu","institution":"Ifes","role":"MEMBER"}
                                  ]
                                }
                                """.formatted(studentId, professorId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return id(response);
    }

    private long id(String json) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        return node.get("id").asLong();
    }
}
