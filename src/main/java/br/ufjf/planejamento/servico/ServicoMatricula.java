package br.ufjf.planejamento.servico;

import br.ufjf.planejamento.excecoes.*;
import br.ufjf.planejamento.modelo.Aluno;
import br.ufjf.planejamento.modelo.Disciplina;
import br.ufjf.planejamento.modelo.Turma;
import br.ufjf.planejamento.validacao.ValidadorPreRequisito;

import java.util.*;
import java.util.stream.Collectors;


public class ServicoMatricula {

    public RelatorioMatricula processarPlanejamento(Aluno aluno) {
        List<Turma> planejamentoOrdenado = ordenarPlanejamentoPorPrioridade(aluno.getPlanejamento());

        Map<Turma, List<String>> logs = new LinkedHashMap<>();
        Map<Turma, String> rejeicoes = new LinkedHashMap<>();
        List<Turma> aceitas = new ArrayList<>();

        // Pré-verificação para conflitos de mesma prioridade
        Set<Turma> rejeitadasPorConflitoDePares = new HashSet<>();
        for (int i = 0; i < planejamentoOrdenado.size(); i++) {
            for (int j = i + 1; j < planejamentoOrdenado.size(); j++) {
                Turma t1 = planejamentoOrdenado.get(i);
                Turma t2 = planejamentoOrdenado.get(j);
                if (t1.getDisciplina().getPrioridade() == t2.getDisciplina().getPrioridade() && t1.temConflitoDeHorario(t2)) {
                    rejeitadasPorConflitoDePares.add(t1);
                    rejeitadasPorConflitoDePares.add(t2);
                }
            }
        }

        // Ciclo de processamento principal
        for (Turma turma : planejamentoOrdenado) {
            List<String> logEventos = new ArrayList<>();
            logs.put(turma, logEventos);

            try {
                // 1. Verificação inicial de conflito de pares
                if (rejeitadasPorConflitoDePares.contains(turma)) {
                    throw new ConflitoDeHorarioException("Conflito com outra turma de mesma prioridade no planejamento.");
                }

                // 2. Validações que lançam exceções
                logEventos.add("Verificando pré-requisitos...");
                validarPreRequisitos(aluno, turma.getDisciplina());
                logEventos.add("SUCESSO: Pré-requisitos atendidos.");

                logEventos.add("Verificando co-requisitos...");
                validarCoRequisitos(turma, aluno.getPlanejamento());
                logEventos.add("SUCESSO: Co-requisitos atendidos.");

                logEventos.add("Verificando vagas...");
                validarVagas(turma);
                logEventos.add("SUCESSO: Vagas disponíveis.");

                // 3. Resolução de conflitos com turmas já aceites (de maior prioridade)
                logEventos.add("Verificando conflitos de horário...");
                for (Turma turmaAceita : aceitas) {
                    if (turma.temConflitoDeHorario(turmaAceita)) {
                        throw new ConflitoDeHorarioException("Conflito com " + turmaAceita.getDisciplina().getCodigo() + " de maior prioridade.");
                    }
                }
                logEventos.add("SUCESSO: Nenhum conflito de horário impeditivo.");

                // 4. Validação de carga horária
                int cargaAtual = aceitas.stream().mapToInt(t -> t.getDisciplina().getCargaHoraria()).sum();
                logEventos.add(String.format("Verificando carga horária (Atual: %dh + Nova: %dh <= Max: %dh)...",
                        cargaAtual, turma.getDisciplina().getCargaHoraria(), aluno.getCargaHorariaMaxima()));
                validarCargaHoraria(turma, cargaAtual, aluno.getCargaHorariaMaxima());
                logEventos.add("SUCESSO: Carga horária dentro do limite.");

                // 5. Se passou por tudo, é aceite
                aceitas.add(turma);
                logEventos.add("==> Resultado Final: ACEITA");

            } catch (MatriculaException e) {
                logEventos.add("FALHA: " + e.getMessage());
                rejeicoes.put(turma, e.getMessage());
                logEventos.add("==> Resultado Final: REJEITADA");
            }
        }

        // Constrói o relatório final a partir dos resultados consolidados
        return construirRelatorioFinal(aluno, planejamentoOrdenado, aceitas, rejeicoes, logs);
    }

    private List<Turma> ordenarPlanejamentoPorPrioridade(List<Turma> planejamento) {
        if (planejamento == null) {
            return new ArrayList<>();
        }
        return planejamento.stream()
                .sorted(Comparator.comparingInt((Turma t) -> t.getDisciplina().getPrioridade()).reversed())
                .collect(Collectors.toList());
    }

    private void validarPreRequisitos(Aluno aluno, Disciplina disciplina) throws PreRequisitoNaoCumpridoException {
        for (ValidadorPreRequisito validador : disciplina.getValidadoresPreRequisito()) {
            if (!validador.validar(aluno)) {
                throw new PreRequisitoNaoCumpridoException(validador.getMensagemErro());
            }
        }
    }

    private void validarCoRequisitos(Turma turmaAtual, List<Turma> planejamento) throws CoRequisitoNaoAtendidoException {
        for (Disciplina coRequisito : turmaAtual.getDisciplina().getCoRequisitos()) {
            boolean coRequisitoEncontrado = planejamento.stream()
                    .anyMatch(t -> t.getDisciplina().equals(coRequisito));
            if (!coRequisitoEncontrado) {
                throw new CoRequisitoNaoAtendidoException("A disciplina " + coRequisito.getNome() + " deve ser cursada no mesmo semestre.");
            }
        }
    }

    private void validarVagas(Turma turma) throws TurmaCheiaException {
        if (turma.estaCheia()) {
            throw new TurmaCheiaException("Não há vagas disponíveis na turma " + turma.getId() + ".");
        }
    }

    private void validarCargaHoraria(Turma nova, int cargaAtual, int cargaMaxima) throws CargaHorariaExcedidaException {
        if (cargaAtual + nova.getDisciplina().getCargaHoraria() > cargaMaxima) {
            throw new CargaHorariaExcedidaException("A inclusão da disciplina excederia a carga horária máxima do semestre.");
        }
    }

    private RelatorioMatricula construirRelatorioFinal(Aluno aluno, List<Turma> planejamento, List<Turma> aceitas, Map<Turma, String> rejeicoes, Map<Turma, List<String>> logs) {
        List<RelatorioMatricula.EntradaRelatorio> resultadosFinais = new ArrayList<>();
        for (Turma t : planejamento) {
            List<String> log = logs.getOrDefault(t, new ArrayList<>());
            if (rejeicoes.containsKey(t)) {
                resultadosFinais.add(new RelatorioMatricula.EntradaRelatorio(
                        t, RelatorioMatricula.Status.REJEITADA, rejeicoes.get(t), log));
            } else if (aceitas.contains(t)) {
                resultadosFinais.add(new RelatorioMatricula.EntradaRelatorio(
                        t, RelatorioMatricula.Status.ACEITA, "Matrícula efetivada na simulação.", log));
            }
        }
        return new RelatorioMatricula(aluno, resultadosFinais);
    }
}
