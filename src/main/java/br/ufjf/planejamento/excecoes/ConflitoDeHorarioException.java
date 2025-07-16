package br.ufjf.planejamento.excecoes;

public class ConflitoDeHorarioException extends ValidacaoMatriculaException{
    public ConflitoDeHorarioException(String mensagem){
        super("Conflito de horário: " + mensagem);
    }
}