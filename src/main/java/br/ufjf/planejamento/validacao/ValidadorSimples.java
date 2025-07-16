package br.ufjf.planejamento.validacao;

import br.ufjf.planejamento.modelo.Aluno;
import br.ufjf.planejamento.modelo.Disciplina;
import br.ufjf.planejamento.modelo.CatalogoDisciplinas;

public class ValidadorSimples implements ValidadorPreRequisito {
    private final String codigoDisciplinaRequisito;

    public ValidadorSimples(String codigoDisciplinaRequisito) {
        this.codigoDisciplinaRequisito = codigoDisciplinaRequisito;
    }

    @Override
    public boolean validar(Aluno aluno) {
        Disciplina disciplinaRequisito = CatalogoDisciplinas.getDisciplina(codigoDisciplinaRequisito);
        if (disciplinaRequisito == null) {
            return false;
        }
        return aluno.foiAprovado(disciplinaRequisito);
    }

    @Override
    public String getMensagemErro() {
        Disciplina disciplina = CatalogoDisciplinas.getDisciplina(codigoDisciplinaRequisito);
        String nomeDisciplina = (disciplina != null) ? disciplina.getNome() : codigoDisciplinaRequisito;
        return "É necessário ter sido aprovado em '" + nomeDisciplina + "'";
    }
}
