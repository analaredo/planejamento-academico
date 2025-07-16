package br.ufjf.planejamento.modelo;

public class DisciplinaObrigatoria extends Disciplina {

    public DisciplinaObrigatoria(String codigo, String nome, int cargaHoraria) {
        super(codigo, nome, cargaHoraria);
    }

    @Override
    public int getPrioridade() {
        return 3;
    }

    @Override
    public String getTipo() {
        return "Obrigatória";
    }
}
