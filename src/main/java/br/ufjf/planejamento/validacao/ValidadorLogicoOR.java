package br.ufjf.planejamento.validacao;

import br.ufjf.planejamento.modelo.Disciplina;

import java.util.List;

import br.ufjf.planejamento.modelo.Aluno;

public class ValidadorLogicoOR implements ValidadorPreRequisito{
    private List<Disciplina> disciplinasRequisito;

    public ValidadorLogicoOR (List<Disciplina> disciplinas){
        this.disciplinasRequisito = disciplinas;
    }

    @Override
    public boolean validar(Aluno aluno){

        for (Disciplina d : disciplinasRequisito){
            if (aluno.foiAprovado(d)){
                return true;
            }
        }
        
        return false;
    }
}