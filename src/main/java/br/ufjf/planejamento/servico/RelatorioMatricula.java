package br.ufjf.planejamento.servico;

import br.ufjf.planejamento.modelo.Aluno;
import br.ufjf.planejamento.modelo.Horario;
import br.ufjf.planejamento.modelo.Turma;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Encapsula o resultado completo e detalhado de uma simulação de matrícula.
 */
public class RelatorioMatricula {

    public enum Status {
        ACEITA, REJEITADA
    }

    /**
     * Representa o resultado do processamento para uma única turma.
     */
    public static class EntradaRelatorio {
        private final Turma turma;
        private final Status status;
        private final String motivoFinal;
        private final List<String> logEventos;

        public EntradaRelatorio(Turma turma, Status status, String motivoFinal, List<String> logEventos) {
            this.turma = turma;
            this.status = status;
            this.motivoFinal = motivoFinal;
            this.logEventos = logEventos;
        }

        public Turma getTurma() { return turma; }
        public Status getStatus() { return status; }
    }

    private final Aluno aluno;
    private final List<EntradaRelatorio> resultados;
    private final LocalDateTime dataProcessamento;

    public RelatorioMatricula(Aluno aluno, List<EntradaRelatorio> resultados) {
        this.aluno = aluno;
        this.resultados = resultados;
        this.dataProcessamento = LocalDateTime.now();
    }

    public List<Turma> getTurmasAceitas() {
        return resultados.stream()
                .filter(r -> r.status == Status.ACEITA)
                .map(EntradaRelatorio::getTurma)
                .collect(Collectors.toList());
    }

    /**
     * Gera uma representação em String completa e formatada do relatório.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        List<Turma> turmasAceitas = getTurmasAceitas();
        List<EntradaRelatorio> turmasRejeitadas = resultados.stream()
                .filter(r -> r.status == Status.REJEITADA)
                .collect(Collectors.toList());

        int cargaHorariaTotal = turmasAceitas.stream()
                .mapToInt(t -> t.getDisciplina().getCargaHoraria()).sum();

        sb.append("======================================================================\n");
        sb.append("              RELATÓRIO DE SIMULAÇÃO DE MATRÍCULA\n");
        sb.append("======================================================================\n");
        sb.append(String.format("Aluno: %s | Matrícula: %s\n", aluno.getNome(), aluno.getMatricula()));
        sb.append(String.format("Processado em: %s\n", dataProcessamento.format(dtf)));
        sb.append("----------------------------------------------------------------------\n\n");


        sb.append("[TURMAS ACEITAS]\n");
        if (turmasAceitas.isEmpty()) {
            sb.append("   Nenhuma turma aceita.\n");
        } else {
            for (Turma turma : turmasAceitas) {
                sb.append(String.format("   - [%s] %s (%dh) | %s\n",
                        turma.getDisciplina().getCodigo(),
                        turma.getDisciplina().getNome(),
                        turma.getDisciplina().getCargaHoraria(),
                        turma.getHorario().toString()));
            }
        }
        sb.append("\n");

        sb.append("[TURMAS REJEITADAS]\n");
        if (turmasRejeitadas.isEmpty()) {
            sb.append("   Nenhuma turma rejeitada.\n");
        } else {
            for (EntradaRelatorio res : turmasRejeitadas) {
                sb.append(String.format("   - [%s] %s\n", res.turma.getDisciplina().getCodigo(), res.turma.getDisciplina().getNome()));
                sb.append(String.format("     Motivo: %s\n", res.motivoFinal));
            }
        }
        sb.append("\n");

        // Resumo e Estatísticas
        sb.append("RESUMO\n");
        sb.append(String.format("   - Carga Horária Total Aceita: %dh (de um máximo de %dh)\n", cargaHorariaTotal, aluno.getCargaHorariaMaxima()));
        sb.append(String.format("   - Turmas Processadas: %d | Aceitas: %d | Rejeitadas: %d\n\n", resultados.size(), turmasAceitas.size(), turmasRejeitadas.size()));

        sb.append("GRADE HORÁRIA\n");
        sb.append(gerarGradeHoraria(turmasAceitas));
        sb.append("\n");

        // Log de Processamento Detalhado
        sb.append("LOG DE PROCESSAMENTO\n");
        for (EntradaRelatorio res : resultados) {
            sb.append(String.format("   - Processando [%s] %s:\n", res.turma.getDisciplina().getCodigo(), res.turma.getDisciplina().getNome()));
            for (String log : res.logEventos) {
                sb.append(String.format("     > %s\n", log));
            }
            sb.append(String.format("     ==> Resultado Final: %s (%s)\n\n", res.status, res.motivoFinal));
        }
        sb.append("======================================================================\n");
        sb.append("                          FIM DO RELATÓRIO\n");
        sb.append("======================================================================\n");

        return sb.toString();
    }

    private String gerarGradeHoraria(List<Turma> turmasAceitas) {
        // Mapeia turmas por dia da semana
        Map<Horario.DiaDaSemana, List<Turma>> grade = new TreeMap<>();
        for (Turma t : turmasAceitas) {
            grade.computeIfAbsent(t.getHorario().getDia(), k -> new ArrayList<>()).add(t);
        }

        if (grade.isEmpty()) {
            return "   Nenhuma disciplina alocada na grade.\n";
        }

        StringBuilder sb = new StringBuilder();
        String format = "   %-10s | %s\n";
        for (Map.Entry<Horario.DiaDaSemana, List<Turma>> entry : grade.entrySet()) {
            String disciplinasDoDia = entry.getValue().stream()
                    .map(t -> String.format("%s (%s)", t.getDisciplina().getCodigo(), t.getHorario().toString()))
                    .collect(Collectors.joining(", "));
            sb.append(String.format(format, entry.getKey(), disciplinasDoDia));
        }
        return sb.toString();
    }
}
