package br.ufjf.planejamento.validacao;

import br.ufjf.planejamento.modelo.Disciplina;

import java.util.List;

import br.ufjf.planejamento.modelo.Aluno;

public class ValidadorCreditosMinimos implements ValidadorPreRequisito{
    private int creditosMinimos;

    public ValidadorCreditosMinimos (int creditos){
        this.creditosMinimos = creditos;
    }

    @Override
    public boolean validar(Aluno aluno){
        //Falta a implementação de créditos!
    }
}