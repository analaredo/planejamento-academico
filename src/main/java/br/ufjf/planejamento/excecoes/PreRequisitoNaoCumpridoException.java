package br.ufjf.planejamento.excecoes;

public abstract class PreRequisitoNaoCumpridoException extends ValidacaoMatriculaException{
    public PreRequisitoNaoCumpridoException(String mensagem){
        super("Pré-requisito não cumprido: " + mensagem);
    }
}