package br.ufjf.planejamento.validacao;

import br.ufjf.planejamento.modelo.Aluno;
import java.util.Arrays;
import java.util.List;

public class ValidadorLogicoAND implements ValidadorPreRequisito {
    private final List<ValidadorPreRequisito> validadores;
    
    public ValidadorLogicoAND(ValidadorPreRequisito... validadores) {
        this.validadores = Arrays.asList(validadores);
    }
    
    @Override
    public boolean validar(Aluno aluno) {
        return validadores.stream().allMatch(v -> v.validar(aluno));
    }
    
    @Override
    public String getMensagemErro() {
        StringBuilder sb = new StringBuilder("Os seguintes pré-requisitos não foram atendidos:");
        validadores.forEach(v -> sb.append("\n- ").append(v.getMensagemErro()));
        return sb.toString();
    }
}