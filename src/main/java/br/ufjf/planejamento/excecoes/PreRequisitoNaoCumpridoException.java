package br.ufjf.planejamento.excecoes;

public class PreRequisitoNaoCumpridoException extends ValidacaoMatriculaException{
    public PreRequisitoNaoCumpridoException(String mensagem){
        super("Pré-requisito não cumprido: " + mensagem);
    }
}