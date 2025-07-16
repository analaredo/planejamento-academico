package br.ufjf.planejamento.excecoes;

public class TurmaCheiaException extends GerenciamentoVagasException{
    public TurmaCheiaException(String mensagem){
        super("Turma cheia: " + mensagem);
    }
}