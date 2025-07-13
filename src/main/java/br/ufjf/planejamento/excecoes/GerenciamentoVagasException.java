package br.ufjf.planejamento.excecoes;

public abstract class GerenciamentoVagasException extends MatriculaException{
    public GerenciamentoVagasException(String mensagem){
        super(mensagem);
    }
}