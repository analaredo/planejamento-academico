package br.ufjf.planejamento.validacao;

import br.ufjf.planejamento.modelo.Aluno;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        String opcoes = validadores.stream()
                .map(ValidadorPreRequisito::getMensagemErro)
                .collect(Collectors.joining(" OU "));
        return "Pelo menos um dos seguintes pré-requisitos deve ser atendido: " + opcoes;
    }
}