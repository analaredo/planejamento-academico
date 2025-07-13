package br.ufjf.planejamento.modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representa um aluno no sistema acadêmico.
 * Armazena dados pessoais, histórico acadêmico e o planejamento para o próximo semestre.
 */
public class Aluno {

    private String nome;
    private String matricula; // AAAACCMMM
    private int cargaHorariaMaxima;

    private Map<Disciplina, Float> disciplinasCursadas;

    private List<Turma> planejamento;

    public Aluno(String nome, String matricula, int cargaHorariaMaxima) {
        this.nome = nome;
        this.matricula = matricula;
        this.cargaHorariaMaxima = cargaHorariaMaxima;
        this.disciplinasCursadas = new HashMap<>();
        this.planejamento = new ArrayList<>();
    }

    /**
     * Adiciona uma disciplina ao histórico do aluno com a nota obtida.
     * @param disciplina A disciplina concluída.
     * @param nota A nota final (0 a 100).
     */
    public void adicionarDisciplinaCursada(Disciplina disciplina, float nota) {
        this.disciplinasCursadas.put(disciplina, nota);
    }

    /**
     * Adiciona uma turma ao planejamento de matrícula do aluno para o próximo semestre.
     * @param turma A turma desejada.
     */
    public void adicionarTurmaPlanejamento(Turma turma) {
        this.planejamento.add(turma);
    }

    /**
     * Verifica se o aluno já cursou e foi aprovado em uma determinada disciplina.
     * A aprovação requer nota >= 60.
     * @param disciplina A disciplina a ser verificada.
     * @return true se o aluno foi aprovado na disciplina, false caso contrário.
     */
    public boolean foiAprovado(Disciplina disciplina) {
        Float nota = disciplinasCursadas.get(disciplina);
        return nota != null && nota >= 60.0f;
    }

    // Getters e Setters

    public String getNome() {
        return nome;
    }

    public String getMatricula() {
        return matricula;
    }

    public int getCargaHorariaMaxima() {
        return cargaHorariaMaxima;
    }

    public Map<Disciplina, Float> getDisciplinasCursadas() {
        return disciplinasCursadas;
    }

    public List<Turma> getPlanejamento() {
        return planejamento;
    }

    public void setPlanejamento(List<Turma> planejamento) {
        this.planejamento = planejamento;
    }
}
