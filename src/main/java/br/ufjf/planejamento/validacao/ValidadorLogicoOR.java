package br.ufjf.planejamento.validacao;

import br.ufjf.planejamento.modelo.Aluno;
import java.util.Arrays;
import java.util.List;

public class ValidadorLogicoOR implements ValidadorPreRequisito {
    private final List<ValidadorPreRequisito> validadores;
    
    public ValidadorLogicoOR(ValidadorPreRequisito... validadores) {
        this.validadores = Arrays.asList(validadores);
    }
    
    @Override
    public boolean validar(Aluno aluno) {
        return validadores.stream().anyMatch(v -> v.validar(aluno));
    }
    
    @Override
    public String getMensagemErro() {
        StringBuilder sb = new StringBuilder("Pelo menos um destes pré-requisitos deve ser atendido:");
        validadores.forEach(v -> sb.append("\n- ").append(v.getMensagemErro()));
        return sb.toString();
    }
}