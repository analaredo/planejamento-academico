package br.ufjf.planejamento.modelo;

import java.util.HashMap;
import java.util.Map;

public class Aluno {
    private String nome;
    private String matricula;

    // Histórico: disciplina, nota
    private Map<Disciplina, Integer> disciplinasCursadas = new HashMap<>();

    public Aluno(String nome, String matricula) {
        this.nome = nome;
        this.matricula = matricula;
    }

    public void registrarDisciplinaConcluida(Disciplina disciplina, int nota) {
        disciplinasCursadas.put(disciplina, nota);
    }

    public boolean temDisciplinaComNota(String codigoDisciplina, int notaMinima) {
        for (Disciplina d : disciplinasCursadas.keySet()) {
            if (d.getCodigo().equals(codigoDisciplina)) {
                Integer nota = disciplinasCursadas.get(d);
                return nota != null && nota >= notaMinima;
            }
        }
        return false;
    }

    public void listarHistorico() {
        System.out.println("Histórico de " + nome + ":");
        for (Map.Entry<Disciplina, Integer> entry : disciplinasCursadas.entrySet()) {
            System.out.println("- " + entry.getKey() + " → Nota: " + entry.getValue());
        }
    }
}
