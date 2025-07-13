package br.ufjf.planejamento.servico;

import br.ufjf.planejamento.excecoes.PreRequisitoNaoCumpridoException;
import br.ufjf.planejamento.modelo.Aluno;
import br.ufjf.planejamento.modelo.Disciplina;
import br.ufjf.planejamento.modelo.Turma;
import br.ufjf.planejamento.validacao.ValidadorPreRequisito;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Orquestra o processo de simulação de matrícula. Ajustado para:
 * - Contabilizar carga horária de todas as turmas processadas (mesmo rejeitadas por conflito).
 * - Resolver conflitos de mesma prioridade rejeitando ambas.
 */
public class ServicoMatricula {

    public RelatorioMatricula processarPlanejamento(Aluno aluno) {
        List<Turma> planejamentoOrdenado = ordenarPlanejamentoPorPrioridade(aluno.getPlanejamento());

        Map<Turma, List<String>> logs = new LinkedHashMap<>();
        Map<Turma, String> rejeicoes = new LinkedHashMap<>();
        List<Turma> aceitas = new ArrayList<>();

        for (int i = 0; i < planejamentoOrdenado.size(); i++) {
            Turma turma = planejamentoOrdenado.get(i);
            List<String> logEventos = new ArrayList<>();
            logs.put(turma, logEventos);

            // 1. Pré-requisitos
            logEventos.add("Verificando pré-requisitos...");
            try{
                validarPreRequisitos(aluno, turma.getDisciplina());
                logEventos.add("SUCESSO: Pré-requisitos atendidos.");
            }catch(PreRequisitoNaoCumpridoException e) {
                logEventos.add("FALHA: " + e.getMessage());
                rejeicoes.put(turma, e.getMessage());
                continue;
            }

            // 2. Co-requisitos
            logEventos.add("Verificando co-requisitos...");
            motivo = validarCoRequisitos(turma, aluno.getPlanejamento());
            if (motivo != null) {
                logEventos.add("FALHA: " + motivo);
                rejeicoes.put(turma, motivo);
                continue;
            }
            logEventos.add("SUCESSO: Co-requisitos atendidos.");

            // 3. Vagas
            logEventos.add("Verificando vagas...");
            if (turma.estaCheia()) {
                motivo = "Turma cheia.";
                logEventos.add("FALHA: " + motivo);
                rejeicoes.put(turma, motivo);
                continue;
            }
            logEventos.add("SUCESSO: Vagas disponíveis.");

            // 4. Conflitos de horário
            logEventos.add("Verificando conflitos de horário...");
            boolean rejeitadaPorConflito = false;
            Iterator<Turma> it = aceitas.iterator();
            while (it.hasNext()) {
                Turma outra = it.next();
                if (turma.temConflitoDeHorario(outra)) {
                    logEventos.add(String.format("CONFLITO DETECTADO com %s.", outra.getDisciplina().getCodigo()));
                    int prioNova = turma.getDisciplina().getPrioridade();
                    int prioAceita = outra.getDisciplina().getPrioridade();

                    if (prioNova > prioAceita) {
                        logEventos.add(String.format(
                                "RESOLUÇÃO: %s (prio %d) vence %s (prio %d). Removendo a segunda.",
                                turma.getDisciplina().getCodigo(), prioNova,
                                outra.getDisciplina().getCodigo(), prioAceita));
                        it.remove();
                        String motivoRemocao = "Conflito com " + turma.getDisciplina().getCodigo() + " de maior prioridade.";
                        rejeicoes.put(outra, motivoRemocao);
                        logs.get(outra).add("STATUS ALTERADO: Rejeitada por conflito com turma de maior prioridade.");
                    } else if (prioNova < prioAceita) {
                        motivo = "Conflito com " + outra.getDisciplina().getCodigo() + " de prioridade maior.";
                        logEventos.add("FALHA: " + motivo);
                        rejeicoes.put(turma, motivo);
                        rejeitadaPorConflito = true;
                        break;
                    } else {
                        // mesma prioridade: rejeita ambas
                        it.remove();
                        String motivoRemocao = "Conflito de mesma prioridade com " + outra.getDisciplina().getCodigo() + ".";
                        rejeicoes.put(outra, motivoRemocao);
                        logs.get(outra).add("STATUS ALTERADO: Rejeitada por conflito de mesma prioridade.");

                        motivo = "Conflito de horário com " + outra.getDisciplina().getCodigo() + ", que possui prioridade igual.";
                        logEventos.add("FALHA: " + motivo);
                        rejeicoes.put(turma, motivo);
                        rejeitadaPorConflito = true;
                        break;
                    }
                }
            }
            if (rejeitadaPorConflito) continue;
            logEventos.add("SUCESSO: Nenhum conflito de horário impeditivo.");

            // 5. Carga horária (somando todas as turmas já processadas)
            int cargaAtual = 0;
            for (int j = 0; j < i; j++) {
                cargaAtual += planejamentoOrdenado.get(j).getDisciplina().getCargaHoraria();
            }
            int chNova = turma.getDisciplina().getCargaHoraria();
            logEventos.add(String.format(
                    "Verificando carga horária (Total processadas: %dh + Nova: %dh <= Max: %dh)...",
                    cargaAtual, chNova, aluno.getCargaHorariaMaxima()));
            if (cargaAtual + chNova > aluno.getCargaHorariaMaxima()) {
                motivo = "Carga horária máxima do semestre seria excedida.";
                logEventos.add("FALHA: " + motivo);
                rejeicoes.put(turma, motivo);
                continue;
            }
            logEventos.add("SUCESSO: Carga horária dentro do limite.");

            // 6. Turma aceita
            aceitas.add(turma);
        }

        // --- Construção do relatório ---
        List<RelatorioMatricula.EntradaRelatorio> resultadosFinais = new ArrayList<>();
        for (Turma t : ordenarPlanejamentoPorPrioridade(aluno.getPlanejamento())) {
            List<String> log = logs.get(t);
            if (rejeicoes.containsKey(t)) {
                log.add("==> Resultado Final: REJEITADA");
                resultadosFinais.add(new RelatorioMatricula.EntradaRelatorio(
                        t, RelatorioMatricula.Status.REJEITADA, rejeicoes.get(t), log));
            } else if (aceitas.contains(t)) {
                log.add("==> Resultado Final: ACEITA");
                resultadosFinais.add(new RelatorioMatricula.EntradaRelatorio(
                        t, RelatorioMatricula.Status.ACEITA, "Matrícula efetivada na simulação.", log));
            }
        }
        return new RelatorioMatricula(aluno, resultadosFinais);
    }

    private List<Turma> ordenarPlanejamentoPorPrioridade(List<Turma> planejamento) {
        return planejamento.stream()
                .sorted(Comparator.comparingInt((Turma t) -> t.getDisciplina().getPrioridade()).reversed())
                .collect(Collectors.toList());
    }

    private String validarPreRequisitos(Aluno aluno, Disciplina disciplina) throws PreRequisitoNaoCumpridoException {
        for (ValidadorPreRequisito val : disciplina.getValidadoresPreRequisito()) {
            if (!val.validar(aluno)) {
                throw new PreRequisitoNaoCumpridoException(disciplina.getCodigo());
            }
        }
        return null;
    }

    private String validarCoRequisitos(Turma turmaAtual, List<Turma> planejamento) {
        Disciplina d = turmaAtual.getDisciplina();
        for (Disciplina c : d.getCoRequisitos()) {
            boolean found = planejamento.stream()
                    .anyMatch(t -> t.getDisciplina().equals(c));
            if (!found) {
                return "Co-requisito não atendido: A disciplina " + c.getNome() + " deve ser cursada no mesmo semestre.";
            }
        }
        return null;
    }
}
