package br.ufjf.planejamento.validacao;

import br.ufjf.planejamento.modelo.Disciplina;
import br.ufjf.planejamento.modelo.Aluno;

public interface ValidadorPreRequisito {
    public boolean validar(Aluno aluno);
}