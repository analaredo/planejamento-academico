package br.ufjf.planejamento.excecoes;

public abstract class CargaHorariaExcedidaException extends ValidacaoMatriculaException{
    public CargaHorariaExcedidaException(String mensagem){
        super("Carga horária excedida: " + mensagem);
    }
}