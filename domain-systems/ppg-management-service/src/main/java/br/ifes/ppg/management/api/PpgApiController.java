package br.ifes.ppg.management.api;

import br.ifes.ppg.management.domain.Advisorship;
import br.ifes.ppg.management.domain.Defense;
import br.ifes.ppg.management.domain.DissertationDocument;
import br.ifes.ppg.management.domain.Professor;
import br.ifes.ppg.management.domain.Program;
import br.ifes.ppg.management.domain.Student;
import br.ifes.ppg.management.store.DissertationStorageService;
import br.ifes.ppg.management.store.PpgDataStore;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PpgApiController {

    private final PpgDataStore store;
    private final DissertationStorageService dissertationStorageService;

    public PpgApiController(PpgDataStore store, DissertationStorageService dissertationStorageService) {
        this.store = store;
        this.dissertationStorageService = dissertationStorageService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "service", "ppg-management-service");
    }

    @GetMapping("/students")
    public List<Student> students() {
        return store.students();
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<Student> student(@PathVariable Long id) {
        return store.student(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/students/by-registration/{registration}")
    public ResponseEntity<Student> studentByRegistration(@PathVariable String registration) {
        return store.studentByRegistration(registration)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/students/by-email")
    public ResponseEntity<Student> studentByEmail(@RequestParam String email) {
        return store.studentByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/students")
    public Student addStudent(@RequestBody Student student) {
        return store.addStudent(student);
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        return store.updateStudent(id, student)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        if (!store.deleteStudent(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/students/{id}/status")
    public ResponseEntity<Map<String, Object>> studentStatus(@PathVariable Long id) {
        return store.student(id)
                .map(student -> ResponseEntity.ok(Map.<String, Object>of(
                        "studentId", student.getId(),
                        "status", student.getStatus(),
                        "exists", true,
                        "active", "ACTIVE".equalsIgnoreCase(student.getStatus()))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/professors")
    public List<Professor> professors() {
        return store.professors();
    }

    @GetMapping("/professors/{id}")
    public ResponseEntity<Professor> professor(@PathVariable Long id) {
        return store.professor(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/professors/by-email")
    public ResponseEntity<Professor> professorByEmail(@RequestParam String email) {
        return store.professorByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/professors")
    public Professor addProfessor(@RequestBody Professor professor) {
        return store.addProfessor(professor);
    }

    @PutMapping("/professors/{id}")
    public ResponseEntity<Professor> updateProfessor(@PathVariable Long id, @RequestBody Professor professor) {
        return store.updateProfessor(id, professor)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/professors/{id}")
    public ResponseEntity<Void> deleteProfessor(@PathVariable Long id) {
        if (!store.deleteProfessor(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/programs")
    public List<Program> programs() {
        return store.programs();
    }

    @GetMapping("/programs/default")
    public ResponseEntity<Program> defaultProgram() {
        return store.defaultProgram().map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/programs/{id}")
    public ResponseEntity<Program> program(@PathVariable Long id) {
        return store.program(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/programs")
    public Program addProgram(@RequestBody Program program) {
        return store.addProgram(program);
    }

    @GetMapping("/advisorships")
    public List<Advisorship> advisorships() {
        return store.advisorships();
    }

    @GetMapping("/advisorships/{id}")
    public ResponseEntity<Advisorship> advisorship(@PathVariable Long id) {
        return store.advisorship(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/advisorships/by-student/{studentId}")
    public List<Advisorship> advisorshipsByStudent(@PathVariable Long studentId) {
        return store.advisorshipsByStudent(studentId);
    }

    @GetMapping("/advisorships/by-advisor/{advisorId}")
    public List<Advisorship> advisorshipsByAdvisor(@PathVariable Long advisorId) {
        return store.advisorshipsByAdvisor(advisorId);
    }

    @PostMapping("/advisorships")
    public Advisorship addAdvisorship(@RequestBody Advisorship advisorship) {
        return store.addAdvisorship(advisorship);
    }

    @PostMapping("/advisorships/{id}/cancel")
    public ResponseEntity<Advisorship> cancelAdvisorship(@PathVariable Long id) {
        return store.cancelAdvisorship(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/defenses")
    public List<Defense> defenses() {
        return store.defenses();
    }

    @GetMapping("/defenses/{id}")
    public ResponseEntity<Defense> defense(@PathVariable Long id) {
        return store.defense(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/defenses/by-student/{studentId}")
    public List<Defense> defensesByStudent(@PathVariable Long studentId) {
        return store.defensesByStudent(studentId);
    }

    @PostMapping("/defenses")
    public Defense addDefense(@RequestBody Defense defense) {
        return store.addDefense(defense);
    }

    @PutMapping("/defenses/{id}")
    public ResponseEntity<Defense> updateDefense(@PathVariable Long id, @RequestBody Defense defense) {
        return store.updateDefense(id, defense)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/defenses/{id}/status")
    public ResponseEntity<Defense> updateDefenseStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return store.updateDefenseStatus(id, status)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/defenses/{id}/cancel")
    public ResponseEntity<Defense> cancelDefense(@PathVariable Long id) {
        return store.cancelDefense(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/defenses/{defenseId}/dissertation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DissertationDocument> uploadDissertation(@PathVariable Long defenseId,
                                                                   @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (store.defense(defenseId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String documentId = "DOC-" + UUID.randomUUID();
        var stored = dissertationStorageService.store(defenseId, documentId, file);
        DissertationDocument document = store.addDissertationDocument(
                documentId,
                defenseId,
                stored.originalName(),
                file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType(),
                file.getSize(),
                stored.storagePath()
        );
        return ResponseEntity.ok(document);
    }

    @GetMapping("/defenses/{defenseId}/dissertation")
    public ResponseEntity<DissertationDocument> dissertation(@PathVariable Long defenseId) {
        if (store.defense(defenseId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return store.latestDissertationDocument(defenseId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/defenses/{defenseId}/dissertation/versions")
    public ResponseEntity<List<DissertationDocument>> dissertationVersions(@PathVariable Long defenseId) {
        if (store.defense(defenseId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(store.dissertationDocumentVersions(defenseId));
    }

    @GetMapping("/defenses/{defenseId}/dissertation/download")
    public ResponseEntity<Resource> downloadDissertation(@PathVariable Long defenseId) {
        if (store.defense(defenseId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return store.latestDissertationDocument(defenseId)
                .map(document -> {
                    Resource resource = dissertationStorageService.load(document);
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(document.getContentType()))
                            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                                    .filename(document.getFileName())
                                    .build()
                                    .toString())
                            .body(resource);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/defenses/{defenseId}/dissertation/download-link")
    public ResponseEntity<Map<String, Object>> dissertationDownloadLink(@PathVariable Long defenseId,
                                                                       HttpServletRequest request) {
        if (store.defense(defenseId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return store.latestDissertationDocument(defenseId)
                .map(document -> {
                    String downloadUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                            .replacePath("/api/defenses/" + defenseId + "/dissertation/download")
                            .replaceQuery(null)
                            .build()
                            .toUriString();
                    return ResponseEntity.ok(Map.<String, Object>of(
                            "documentId", document.getDocumentId(),
                            "defenseId", defenseId,
                            "downloadUrl", downloadUrl,
                            "temporary", false,
                            "revocable", false));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
