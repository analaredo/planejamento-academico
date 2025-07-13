package br.ufjf.planejamento.excecoes;

public abstract class MatriculaException extends Exception{
    public MatriculaException(String mensagem){
        super(mensagem);
    }
}