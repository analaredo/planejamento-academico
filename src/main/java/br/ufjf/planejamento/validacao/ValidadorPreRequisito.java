package br.ufjf.planejamento.validacao;

import br.ufjf.planejamento.modelo.Aluno;

public interface ValidadorPreRequisito {
    boolean validar(Aluno aluno);
    String getMensagemErro();
}