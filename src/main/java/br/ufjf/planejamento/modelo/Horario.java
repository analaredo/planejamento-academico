package br.ufjf.planejamento.modelo;

import java.util.Objects;

/**
 * Representa um horário de aula, com dia da semana, hora de início e hora de fim.
 */
public class Horario {

    public enum DiaDaSemana {
        SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO
    }

    private final DiaDaSemana dia;
    private final int horaInicio;
    private final int horaFim;

    public Horario(DiaDaSemana dia, int horaInicio, int horaFim) {
        if (horaInicio >= horaFim) {
            throw new IllegalArgumentException("A hora de início deve ser anterior à hora de fim.");
        }
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    /**
     * Verifica se este horário tem sobreposição com outro horário.
     * @param outro O outro horário a ser verificado.
     * @return true se os horários conflitarem, false caso contrário.
     */
    public boolean conflitaCom(Horario outro) {
        if (this.dia != outro.dia) {
            return false;
        }

        return this.horaInicio < outro.horaFim && outro.horaInicio < this.horaFim;
    }

    // Getters

    public DiaDaSemana getDia() {
        return dia;
    }

    public int getHoraInicio() {
        return horaInicio;
    }

    public int getHoraFim() {
        return horaFim;
    }

    @Override
    public String toString() {
        return dia + " " + horaInicio + "h-" + horaFim + "h";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Horario horario = (Horario) o;
        return horaInicio == horario.horaInicio && horaFim == horario.horaFim && dia == horario.dia;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dia, horaInicio, horaFim);
    }
}
