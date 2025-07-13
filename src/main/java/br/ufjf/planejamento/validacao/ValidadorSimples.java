package br.ufjf.planejamento.validacao;

import br.ufjf.planejamento.modelo.Disciplina;
import br.ufjf.planejamento.modelo.Aluno;

public class ValidadorSimples implements ValidadorPreRequisito{
    private Disciplina disciplinaRequisito;

    public ValidadorSimples(Disciplina disciplina){
        this.disciplinaRequisito = disciplina;
    }

    @Override
    public boolean validar(Aluno aluno){
        return aluno.foiAprovado(this.disciplinaRequisito);
    }
}