package br.ufjf.planejamento.modelo;

public class DisciplinaEletiva extends Disciplina {

    public DisciplinaEletiva(String codigo, String nome, int cargaHoraria) {
        super(codigo, nome, cargaHoraria);
    }

    @Override
    public int getPrioridade() {
        return 2;
    }

    @Override
    public String getTipo() {
        return "Eletiva";
    }
}
