package br.ufjf.planejamento.excecoes;

public abstract class ValidacaoMatriculaException extends MatriculaException{
    public ValidacaoMatriculaException(String mensagem){
        super(mensagem);
    }
}