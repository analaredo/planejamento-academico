package br.ufjf.planejamento.modelo;

import br.ufjf.planejamento.excecoes.TurmaCheiaException;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma oferta de uma disciplina em um semestre específico,
 * com horário, professor e vagas limitadas.
 */
public class Turma {

    private String id;
    private Disciplina disciplina;
    private Horario horario;
    private int capacidadeMaxima;
    private List<Aluno> alunosMatriculados;

    public Turma(String id, Disciplina disciplina, Horario horario, int capacidadeMaxima) {
        this.id = id;
        this.disciplina = disciplina;
        this.horario = horario;
        this.capacidadeMaxima = capacidadeMaxima;
        this.alunosMatriculados = new ArrayList<>();
    }

    /**
     * Verifica se a turma atingiu sua capacidade máxima de alunos.
     * @return true se a turma está cheia, false caso contrário.
     */
    public boolean estaCheia() {
        return alunosMatriculados.size() >= capacidadeMaxima;
    }

    /**
     * Adiciona um aluno à lista de matriculados na turma.
     * @param aluno O aluno a ser matriculado.
     * @throws TurmaCheiaException se a turma já estiver cheia.
     */
    public void matricular(Aluno aluno) throws TurmaCheiaException {
        if (estaCheia()) {
            throw new TurmaCheiaException("A turma " + id + " para a disciplina " + disciplina.getNome() + " está cheia.");
        }
        this.alunosMatriculados.add(aluno);
    }

    /**
     * Verifica se o horário desta turma conflita com o horário de outra turma.
     * @param outra A outra turma a ser comparada.
     * @return true se houver conflito de horário, false caso contrário.
     */
    public boolean temConflitoDeHorario(Turma outra) {
        return this.horario.conflitaCom(outra.getHorario());
    }

    // Getters

    public String getId() {
        return id;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public Horario getHorario() {
        return horario;
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public List<Aluno> getAlunosMatriculados() {
        return alunosMatriculados;
    }

    public int getNumeroDeVagas() {
        return capacidadeMaxima - alunosMatriculados.size();
    }
}
