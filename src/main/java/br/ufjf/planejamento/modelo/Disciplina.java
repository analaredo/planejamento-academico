package br.ufjf.planejamento.modelo;

import br.ufjf.planejamento.validacao.ValidadorPreRequisito;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Disciplina {
    protected String codigo;           
    protected String nome;             
    protected int cargaHoraria;       

    protected List<ValidadorPreRequisito> validadoresPreRequisito = new ArrayList<>();
    protected List<Disciplina> coRequisitos = new ArrayList<>();

    public Disciplina(String codigo, String nome, int cargaHoraria) {
        this.codigo = codigo;
        this.nome = nome;
        this.cargaHoraria = cargaHoraria;
    }

    // Métodos abstratos: 
    public abstract String getTipo();
    public abstract int getPrioridade(); // maior número = maior prioridade

    // Getters
    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public int getCargaHoraria() {
        return cargaHoraria;
    }

    public List<ValidadorPreRequisito> getValidadoresPreRequisito() {
        return validadoresPreRequisito;
    }

    public List<Disciplina> getCoRequisitos() {
        return coRequisitos;
    }

    // Métodos auxiliares
    public void adicionarValidador(ValidadorPreRequisito validador) {
        validadoresPreRequisito.add(validador);
    }

    public void adicionarCoRequisito(Disciplina disciplina) {
        coRequisitos.add(disciplina);
    }

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Disciplina)) return false;
        Disciplina that = (Disciplina) o;
        return codigo.equals(that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return getTipo() + ": " + codigo + " - " + nome + " (" + cargaHoraria + "h/semana)";
    }
}
