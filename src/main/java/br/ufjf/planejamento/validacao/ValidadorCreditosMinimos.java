package br.ufjf.planejamento.validacao;

import br.ufjf.planejamento.modelo.Aluno;

public class ValidadorCreditosMinimos implements ValidadorPreRequisito {
    private final int creditosMinimos;

    public ValidadorCreditosMinimos(int creditos) {
        this.creditosMinimos = creditos;
    }

    @Override
    public boolean validar(Aluno aluno) {
        return aluno.getCreditosConcluidos() >= this.creditosMinimos;
    }

    @Override
    public String getMensagemErro() {
        return "É necessário ter no mínimo " + creditosMinimos + " créditos concluídos.";
    }
}
