package br.ufjf.planejamento.validacao;

import br.ufjf.planejamento.modelo.Aluno;
import br.ufjf.planejamento.servico.CatalogoDisciplinas;

public class ValidadorSimples implements ValidadorPreRequisito {
    private final String codigoDisciplina;
    
    public ValidadorSimples(String codigoDisciplina) {
        this.codigoDisciplina = codigoDisciplina;
    }
    
    @Override
    public boolean validar(Aluno aluno) {
        return aluno.cursouDisciplina(CatalogoDisciplinas.getDisciplina(codigoDisciplina));
    }
    
    @Override
    public String getMensagemErro() {
        return "Pré-requisito não cumprido: " + codigoDisciplina;
    }
}