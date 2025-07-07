package br.ufjf.planejamento.modelo;

import br.ufjf.planejamento.validacao.ValidadorPreRequisito;

import java.util.ArrayList;
import java.util.List;

public abstract class Disciplina {
    protected String codigo;
    protected String nome;
    protected int cargaHoraria;

    // Lista de validadores de pré-requisito (polimórficos)
    protected List<ValidadorPreRequisito> validadoresPreRequisito = new ArrayList<>();

    // Lista de co-requisitos (disciplinas que devem ser cursadas juntas)
    protected List<Disciplina> coRequisitos = new ArrayList<>();

    public Disciplina(String codigo, String nome, int cargaHoraria) {
        this.codigo = codigo;
        this.nome = nome;
        this.cargaHoraria = cargaHoraria;
    }

    // Método abstrato que será implementado pelas subclasses
    public abstract String getTipo(); // "Obrigatória", "Eletiva", "Optativa"

    // ----------------------
    // Getters e Setters
    // ----------------------

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

    // ----------------------
    // Métodos auxiliares
    // ----------------------

    public void adicionarValidador(ValidadorPreRequisito validador) {
        this.validadoresPreRequisito.add(validador);
    }

    public void adicionarCoRequisito(Disciplina disciplina) {
        this.coRequisitos.add(disciplina);
    }

    @Override
    public String toString() {
        return getTipo() + ": " + codigo + " - " + nome + " (" + cargaHoraria + "h/semana)";
    }
}
