package br.ufjf.planejamento.modelo;

public class DisciplinaOptativa extends Disciplina {

    public DisciplinaOptativa(String codigo, String nome, int cargaHoraria) {
        super(codigo, nome, cargaHoraria);
    }

    @Override
    public int getPrioridade() {
        return 1;
    }
    
    @Override
    public String getTipo() {
        return "Optativa";
    }
}
