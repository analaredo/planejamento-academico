package br.ufjf.planejamento.validacao;

import br.ufjf.planejamento.modelo.Aluno;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        String erros = validadores.stream()
                .filter(v -> !v.validar(null))
                .map(ValidadorPreRequisito::getMensagemErro)
                .collect(Collectors.joining(" E "));
        return "Todos os seguintes pré-requisitos devem ser atendidos: " + erros;
    }
}