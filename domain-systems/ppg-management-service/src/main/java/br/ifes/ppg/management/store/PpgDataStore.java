package br.ifes.ppg.management.store;

import br.ifes.ppg.management.domain.Advisorship;
import br.ifes.ppg.management.domain.Professor;
import br.ifes.ppg.management.domain.Program;
import br.ifes.ppg.management.domain.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PpgDataStore {

    private final Path dataFile;
    private final ObjectMapper objectMapper;
    private PpgState state = new PpgState();

    public PpgDataStore(@Value("${ppg.data-file:data/ppg-management.json}") String dataFile) {
        this.dataFile = Path.of(dataFile);
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public synchronized void load() {
        try {
            if (Files.exists(dataFile)) {
                state = objectMapper.readValue(dataFile.toFile(), PpgState.class);
                ensureDefaultProgram();
            } else {
                Files.createDirectories(dataFile.toAbsolutePath().getParent());
                ensureDefaultProgram();
                save();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not load PPG data file: " + dataFile, e);
        }
    }

    public synchronized List<Student> students() {
        return new ArrayList<>(state.getStudents());
    }

    public synchronized Optional<Student> student(Long id) {
        return state.getStudents().stream().filter(item -> id.equals(item.getId())).findFirst();
    }

    public synchronized Student addStudent(Student student) {
        student.setId(state.nextStudentId());
        if (student.getStatus() == null || student.getStatus().isBlank()) {
            student.setStatus("ACTIVE");
        }
        state.getStudents().add(student);
        save();
        return student;
    }

    public synchronized Optional<Student> updateStudent(Long id, Student updated) {
        return student(id).map(existing -> {
            existing.setRegistration(updated.getRegistration());
            existing.setName(updated.getName());
            existing.setEmail(updated.getEmail());
            existing.setStatus(defaultStatus(updated.getStatus(), "ACTIVE"));
            save();
            return existing;
        });
    }

    public synchronized boolean deleteStudent(Long id) {
        boolean removed = state.getStudents().removeIf(item -> id.equals(item.getId()));
        if (removed) {
            save();
        }
        return removed;
    }

    public synchronized List<Professor> professors() {
        return new ArrayList<>(state.getProfessors());
    }

    public synchronized Optional<Professor> professor(Long id) {
        return state.getProfessors().stream().filter(item -> id.equals(item.getId())).findFirst();
    }

    public synchronized Professor addProfessor(Professor professor) {
        professor.setId(state.nextProfessorId());
        if (professor.getStatus() == null || professor.getStatus().isBlank()) {
            professor.setStatus("ACTIVE");
        }
        state.getProfessors().add(professor);
        save();
        return professor;
    }

    public synchronized Optional<Professor> updateProfessor(Long id, Professor updated) {
        return professor(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setEmail(updated.getEmail());
            existing.setStatus(defaultStatus(updated.getStatus(), "ACTIVE"));
            save();
            return existing;
        });
    }

    public synchronized boolean deleteProfessor(Long id) {
        boolean removed = state.getProfessors().removeIf(item -> id.equals(item.getId()));
        if (removed) {
            save();
        }
        return removed;
    }

    public synchronized List<Program> programs() {
        return new ArrayList<>(state.getPrograms());
    }

    public synchronized Optional<Program> program(Long id) {
        return state.getPrograms().stream().filter(item -> id.equals(item.getId())).findFirst();
    }

    public synchronized Optional<Program> defaultProgram() {
        return state.getPrograms().stream().findFirst();
    }

    public synchronized Program addProgram(Program program) {
        program.setId(state.nextProgramId());
        state.getPrograms().add(program);
        save();
        return program;
    }

    public synchronized List<Advisorship> advisorships() {
        return new ArrayList<>(state.getAdvisorships());
    }

    public synchronized Optional<Advisorship> advisorship(Long id) {
        return state.getAdvisorships().stream().filter(item -> id.equals(item.getId())).findFirst();
    }

    public synchronized Advisorship addAdvisorship(Advisorship advisorship) {
        advisorship.setId(state.nextAdvisorshipId());
        if (advisorship.getStatus() == null || advisorship.getStatus().isBlank()) {
            advisorship.setStatus("IN_PROGRESS");
        }
        state.getAdvisorships().add(advisorship);
        save();
        return advisorship;
    }

    private void save() {
        try {
            Files.createDirectories(dataFile.toAbsolutePath().getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile.toFile(), state);
        } catch (Exception e) {
            throw new IllegalStateException("Could not save PPG data file: " + dataFile, e);
        }
    }

    private void ensureDefaultProgram() {
        if (!state.getPrograms().isEmpty()) {
            return;
        }
        Program program = new Program();
        program.setId(state.nextProgramId());
        program.setName("Programa de Pos Graduacao em Computacao Aplicada");
        program.setInstitution("Ifes");
        program.setCampus("Serra");
        program.setCoordinatorName("Leandro Colombi Resendo");
        program.setCoordinatorEmail("mcostaifes@gmail.com");
        state.getPrograms().add(program);
    }

    private String defaultStatus(String status, String fallback) {
        return status == null || status.isBlank() ? fallback : status;
    }
}
