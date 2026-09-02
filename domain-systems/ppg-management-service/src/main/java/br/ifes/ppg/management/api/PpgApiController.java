package br.ifes.ppg.management.api;

import br.ifes.ppg.management.domain.Advisorship;
import br.ifes.ppg.management.domain.Professor;
import br.ifes.ppg.management.domain.Program;
import br.ifes.ppg.management.domain.Student;
import br.ifes.ppg.management.store.PpgDataStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PpgApiController {

    private final PpgDataStore store;

    public PpgApiController(PpgDataStore store) {
        this.store = store;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "service", "ppg-management-service");
    }

    @GetMapping("/students")
    public Object students() {
        return store.students();
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<Student> student(@PathVariable Long id) {
        return store.student(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
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

    @GetMapping("/professors")
    public Object professors() {
        return store.professors();
    }

    @GetMapping("/professors/{id}")
    public ResponseEntity<Professor> professor(@PathVariable Long id) {
        return store.professor(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
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
    public Object programs() {
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
    public Object advisorships() {
        return store.advisorships();
    }

    @GetMapping("/advisorships/{id}")
    public ResponseEntity<Advisorship> advisorship(@PathVariable Long id) {
        return store.advisorship(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/advisorships")
    public Advisorship addAdvisorship(@RequestBody Advisorship advisorship) {
        return store.addAdvisorship(advisorship);
    }
}
