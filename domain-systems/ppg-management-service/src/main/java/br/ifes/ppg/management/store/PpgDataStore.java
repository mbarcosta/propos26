package br.ifes.ppg.management.store;

import br.ifes.ppg.management.domain.Advisorship;
import br.ifes.ppg.management.domain.Defense;
import br.ifes.ppg.management.domain.DissertationDocument;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
                ensureCollections();
                ensureDefaultProgram();
            } else {
                Files.createDirectories(dataFile.toAbsolutePath().getParent());
                ensureCollections();
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

    public synchronized Optional<Student> studentByRegistration(String registration) {
        return state.getStudents().stream()
                .filter(item -> equalsIgnoreCase(item.getRegistration(), registration))
                .findFirst();
    }

    public synchronized Optional<Student> studentByEmail(String email) {
        return state.getStudents().stream()
                .filter(item -> equalsIgnoreCase(item.getEmail(), email))
                .findFirst();
    }

    public synchronized Student addStudent(Student student) {
        student.setId(state.nextStudentId());
        student.setStatus(defaultStatus(student.getStatus(), "ACTIVE"));
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

    public synchronized Optional<Professor> professorByEmail(String email) {
        return state.getProfessors().stream()
                .filter(item -> equalsIgnoreCase(item.getEmail(), email))
                .findFirst();
    }

    public synchronized Professor addProfessor(Professor professor) {
        professor.setId(state.nextProfessorId());
        professor.setStatus(defaultStatus(professor.getStatus(), "ACTIVE"));
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

    public synchronized List<Advisorship> advisorshipsByStudent(Long studentId) {
        return state.getAdvisorships().stream()
                .filter(item -> studentId.equals(item.getStudentId()))
                .toList();
    }

    public synchronized List<Advisorship> advisorshipsByAdvisor(Long advisorId) {
        return state.getAdvisorships().stream()
                .filter(item -> advisorId.equals(item.getAdvisorId()))
                .toList();
    }

    public synchronized Advisorship addAdvisorship(Advisorship advisorship) {
        advisorship.setId(state.nextAdvisorshipId());
        advisorship.setStatus(defaultStatus(advisorship.getStatus(), "ACTIVE"));
        if (advisorship.getStartDate() == null) {
            advisorship.setStartDate(LocalDate.now());
        }
        state.getAdvisorships().add(advisorship);
        save();
        return advisorship;
    }

    public synchronized Optional<Advisorship> cancelAdvisorship(Long id) {
        return advisorship(id).map(existing -> {
            existing.setStatus("CANCELLED");
            save();
            return existing;
        });
    }

    public synchronized List<Defense> defenses() {
        return new ArrayList<>(state.getDefenses());
    }

    public synchronized Optional<Defense> defense(Long id) {
        return state.getDefenses().stream().filter(item -> id.equals(item.getId())).findFirst();
    }

    public synchronized List<Defense> defensesByStudent(Long studentId) {
        return state.getDefenses().stream()
                .filter(item -> studentId.equals(item.getStudentId()))
                .toList();
    }

    public synchronized Defense addDefense(Defense defense) {
        OffsetDateTime now = OffsetDateTime.now();
        defense.setId(state.nextDefenseId());
        defense.setStatus(defaultStatus(defense.getStatus(), "HOMOLOGATED"));
        defense.setCreatedAt(now);
        defense.setUpdatedAt(now);
        state.getDefenses().add(defense);
        save();
        return defense;
    }

    public synchronized Optional<Defense> updateDefense(Long id, Defense updated) {
        return defense(id).map(existing -> {
            existing.setStudentId(updated.getStudentId());
            existing.setAdvisorId(updated.getAdvisorId());
            existing.setTitle(updated.getTitle());
            existing.setDate(updated.getDate());
            existing.setLocation(updated.getLocation());
            existing.setStatus(defaultStatus(updated.getStatus(), existing.getStatus()));
            existing.setCommitteeMembers(updated.getCommitteeMembers());
            existing.setUpdatedAt(OffsetDateTime.now());
            save();
            return existing;
        });
    }

    public synchronized Optional<Defense> updateDefenseStatus(Long id, String status) {
        return defense(id).map(existing -> {
            existing.setStatus(status);
            existing.setUpdatedAt(OffsetDateTime.now());
            save();
            return existing;
        });
    }

    public synchronized Optional<Defense> cancelDefense(Long id) {
        return updateDefenseStatus(id, "CANCELLED");
    }

    public synchronized DissertationDocument addDissertationDocument(String documentId, Long defenseId, String fileName,
                                                                     String contentType, long size,
                                                                     String storagePath) {
        DissertationDocument document = new DissertationDocument();
        state.nextDocumentId();
        document.setDocumentId(documentId);
        document.setDefenseId(defenseId);
        document.setFileName(fileName);
        document.setContentType(contentType);
        document.setSize(size);
        document.setUploadedAt(OffsetDateTime.now());
        document.setVersion(nextDissertationVersion(defenseId));
        document.setStoragePath(storagePath);
        state.getDissertationDocuments().add(document);
        save();
        return document;
    }

    public synchronized Optional<DissertationDocument> latestDissertationDocument(Long defenseId) {
        return state.getDissertationDocuments().stream()
                .filter(item -> defenseId.equals(item.getDefenseId()))
                .max(Comparator.comparingInt(DissertationDocument::getVersion));
    }

    public synchronized List<DissertationDocument> dissertationDocumentVersions(Long defenseId) {
        return state.getDissertationDocuments().stream()
                .filter(item -> defenseId.equals(item.getDefenseId()))
                .sorted(Comparator.comparingInt(DissertationDocument::getVersion))
                .toList();
    }

    private int nextDissertationVersion(Long defenseId) {
        return state.getDissertationDocuments().stream()
                .filter(item -> defenseId.equals(item.getDefenseId()))
                .mapToInt(DissertationDocument::getVersion)
                .max()
                .orElse(0) + 1;
    }

    private void save() {
        try {
            Files.createDirectories(dataFile.toAbsolutePath().getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile.toFile(), state);
        } catch (Exception e) {
            throw new IllegalStateException("Could not save PPG data file: " + dataFile, e);
        }
    }

    private void ensureCollections() {
        if (state.getStudents() == null) {
            state.setStudents(new ArrayList<>());
        }
        if (state.getProfessors() == null) {
            state.setProfessors(new ArrayList<>());
        }
        if (state.getPrograms() == null) {
            state.setPrograms(new ArrayList<>());
        }
        if (state.getAdvisorships() == null) {
            state.setAdvisorships(new ArrayList<>());
        }
        if (state.getDefenses() == null) {
            state.setDefenses(new ArrayList<>());
        }
        if (state.getDissertationDocuments() == null) {
            state.setDissertationDocuments(new ArrayList<>());
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

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null
                && left.toLowerCase(Locale.ROOT).equals(right.toLowerCase(Locale.ROOT));
    }
}
