package br.ufjf.planejamento.modelo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class CatalogoAlunos {
    private static final Map<String, Aluno> alunos = new HashMap<>();

    public static void adicionarAluno(Aluno aluno) {
        alunos.put(aluno.getMatricula(), aluno);
    }

    public static Aluno getAluno(String matricula) {
        return alunos.get(matricula);
    }

    public static Collection<Aluno> getTodosAlunos() {
        return alunos.values();
    }

    public static boolean removerAluno(String matricula) {
        return alunos.remove(matricula) != null;
    }
}