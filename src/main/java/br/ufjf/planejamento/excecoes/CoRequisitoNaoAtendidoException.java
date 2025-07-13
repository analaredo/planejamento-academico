package br.ufjf.planejamento.excecoes;

public class CoRequisitoNaoAtendidoException extends ValidacaoMatriculaException{
    public CoRequisitoNaoAtendidoException(String mensagem){
        super("Co-requisito não atendido: " + mensagem);
    }
}