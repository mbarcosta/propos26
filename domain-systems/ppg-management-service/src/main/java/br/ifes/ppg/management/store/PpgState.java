package br.ifes.ppg.management.store;

import br.ifes.ppg.management.domain.Advisorship;
import br.ifes.ppg.management.domain.Defense;
import br.ifes.ppg.management.domain.DissertationDocument;
import br.ifes.ppg.management.domain.Professor;
import br.ifes.ppg.management.domain.Program;
import br.ifes.ppg.management.domain.Student;

import java.util.ArrayList;
import java.util.List;

public class PpgState {

    private long studentSequence = 0;
    private long professorSequence = 0;
    private long programSequence = 0;
    private long advisorshipSequence = 0;
    private long defenseSequence = 0;
    private long documentSequence = 0;
    private List<Student> students = new ArrayList<>();
    private List<Professor> professors = new ArrayList<>();
    private List<Program> programs = new ArrayList<>();
    private List<Advisorship> advisorships = new ArrayList<>();
    private List<Defense> defenses = new ArrayList<>();
    private List<DissertationDocument> dissertationDocuments = new ArrayList<>();

    public long nextStudentId() {
        return ++studentSequence;
    }

    public long nextProfessorId() {
        return ++professorSequence;
    }

    public long nextProgramId() {
        return ++programSequence;
    }

    public long nextAdvisorshipId() {
        return ++advisorshipSequence;
    }

    public long nextDefenseId() {
        return ++defenseSequence;
    }

    public long nextDocumentId() {
        return ++documentSequence;
    }

    public long getStudentSequence() {
        return studentSequence;
    }

    public void setStudentSequence(long studentSequence) {
        this.studentSequence = studentSequence;
    }

    public long getProfessorSequence() {
        return professorSequence;
    }

    public void setProfessorSequence(long professorSequence) {
        this.professorSequence = professorSequence;
    }

    public long getProgramSequence() {
        return programSequence;
    }

    public void setProgramSequence(long programSequence) {
        this.programSequence = programSequence;
    }

    public long getAdvisorshipSequence() {
        return advisorshipSequence;
    }

    public void setAdvisorshipSequence(long advisorshipSequence) {
        this.advisorshipSequence = advisorshipSequence;
    }

    public long getDefenseSequence() {
        return defenseSequence;
    }

    public void setDefenseSequence(long defenseSequence) {
        this.defenseSequence = defenseSequence;
    }

    public long getDocumentSequence() {
        return documentSequence;
    }

    public void setDocumentSequence(long documentSequence) {
        this.documentSequence = documentSequence;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public List<Professor> getProfessors() {
        return professors;
    }

    public void setProfessors(List<Professor> professors) {
        this.professors = professors;
    }

    public List<Program> getPrograms() {
        return programs;
    }

    public void setPrograms(List<Program> programs) {
        this.programs = programs;
    }

    public List<Advisorship> getAdvisorships() {
        return advisorships;
    }

    public void setAdvisorships(List<Advisorship> advisorships) {
        this.advisorships = advisorships;
    }

    public List<Defense> getDefenses() {
        return defenses;
    }

    public void setDefenses(List<Defense> defenses) {
        this.defenses = defenses;
    }

    public List<DissertationDocument> getDissertationDocuments() {
        return dissertationDocuments;
    }

    public void setDissertationDocuments(List<DissertationDocument> dissertationDocuments) {
        this.dissertationDocuments = dissertationDocuments;
    }
}
