package br.ufjf.planejamento.servico;

import br.ufjf.planejamento.modelo.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Responsável por inicializar o ambiente (disciplinas, turmas, alunos)
 */
public class SistemaAcademico {

    private final ServicoMatricula servicoMatricula;
    private int periodoAtual;

    private SistemaAcademico(Configurador config) {
        this.servicoMatricula = new ServicoMatricula();
        this.periodoAtual = 1;
        inicializarSistema(config);
    }

    /**
     * Configura o estado inicial do sistema a partir de uma configuração.
     */
    private void inicializarSistema(Configurador config) {
        limparCatalogos();
        config.turmasParaCriar.forEach(CatalogoTurmas::adicionarTurma);
        config.alunosParaCriar.forEach(CatalogoAlunos::adicionarAluno);
    }

    private void limparCatalogos() {
        Set<String> idsTurmas = new HashSet<>(CatalogoTurmas.getTodasTurmas().stream().map(Turma::getId).toList());
        idsTurmas.forEach(CatalogoTurmas::removerTurma);

        Set<String> matriculasAlunos = new HashSet<>(CatalogoAlunos.getTodosAlunos().stream().map(Aluno::getMatricula).toList());
        matriculasAlunos.forEach(CatalogoAlunos::removerAluno);
    }

    /**
     * Executa a simulação de matrícula para o período atual para todos os alunos.
     * Após a execução, atualiza o histórico dos alunos com as disciplinas aceitas e avança o período.
     * @return Um mapa contendo o relatório de cada aluno processado.
     */
    public Map<Aluno, RelatorioMatricula> executarSimulacaoPeriodo() {
        Map<Aluno, RelatorioMatricula> relatorios = new LinkedHashMap<>();
        for (Aluno aluno : CatalogoAlunos.getTodosAlunos()) {
            if (aluno.getPlanejamento() == null || aluno.getPlanejamento().isEmpty()) {
                continue; // Pula alunos que não planejaram nada
            }
            RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);
            relatorios.put(aluno, relatorio);

            atualizarHistoricoAluno(aluno, relatorio);
        }

        this.periodoAtual++;

        return relatorios;
    }

    /**
     * Adiciona as disciplinas aceitas ao histórico do aluno e limpa o planejamento.
     */
    private void atualizarHistoricoAluno(Aluno aluno, RelatorioMatricula relatorio) {
        final float NOTA_APROVACAO_SIMULADA = 70.0f;

        for (Turma turmaAceita : relatorio.getTurmasAceitas()) {
            aluno.adicionarDisciplinaCursada(turmaAceita.getDisciplina(), NOTA_APROVACAO_SIMULADA);
        }
        if (aluno.getPlanejamento() != null) {
            aluno.getPlanejamento().clear();
        }
    }

    // MÉTODOS DE FAÇADE (para interagir com os catálogos)
    public Aluno buscarAluno(String matricula) { return CatalogoAlunos.getAluno(matricula); }
    public Collection<Aluno> listarAlunos() { return CatalogoAlunos.getTodosAlunos(); }
    public Turma buscarTurma(String id) { return CatalogoTurmas.getTurma(id); }
    public Collection<Turma> listarTurmas() { return CatalogoTurmas.getTodasTurmas(); }
    public Collection<Disciplina> listarDisciplinas() { return CatalogoDisciplinas.getTodasDisciplinas(); }
    public int getPeriodoAtual() { return periodoAtual; }

    /**
     * Configurarma instância de SistemaAcademico de forma flexível e personalizável.
     */
    public static class Configurador {
        private final List<Turma> turmasParaCriar = new ArrayList<>();
        private final List<Aluno> alunosParaCriar = new ArrayList<>();
        private final AtomicInteger sequencialMatricula = new AtomicInteger(1);

        public Configurador adicionarTurma(String id, String codDisciplina, Horario horario, int capacidade) {
            Disciplina d = CatalogoDisciplinas.getDisciplina(codDisciplina);
            if (d != null) {
                turmasParaCriar.add(new Turma(id, d, horario, capacidade));
            } else {
                System.err.println("AVISO: Disciplina com código " + codDisciplina + " não encontrada no catálogo. Turma não será criada.");
            }
            return this;
        }

        public Configurador adicionarAluno(String nome, int cargaHorariaMaxima, Consumer<Aluno> historicoBuilder) {
            String matricula = gerarMatricula();
            Aluno novoAluno = new Aluno(nome, matricula, cargaHorariaMaxima);
            if (historicoBuilder != null) {
                historicoBuilder.accept(novoAluno);
            }
            alunosParaCriar.add(novoAluno);
            return this;
        }

        public Configurador adicionarAluno(String nome, int cargaHorariaMaxima) {
            return adicionarAluno(nome, cargaHorariaMaxima, null);
        }

        private String gerarMatricula() {
            int ano = Calendar.getInstance().get(Calendar.YEAR);
            return String.format("%d65%03d", ano, sequencialMatricula.getAndIncrement());
        }

        public SistemaAcademico construir() {
            return new SistemaAcademico(this);
        }

        public static Configurador criarConfiguracaoPadrao() {
            return new Configurador()
                    .adicionarTurma("T01", "DCC199", new Horario(Horario.DiaDaSemana.SEGUNDA, 8, 10), 50)
                    .adicionarTurma("T02", "DC5199", new Horario(Horario.DiaDaSemana.SEGUNDA, 10, 12), 50)
                    .adicionarTurma("T03", "MAT154", new Horario(Horario.DiaDaSemana.TERCA, 8, 10), 50)
                    .adicionarTurma("T04", "MAT155", new Horario(Horario.DiaDaSemana.QUARTA, 10, 12), 50)
                    .adicionarTurma("T05", "DCC200", new Horario(Horario.DiaDaSemana.QUINTA, 8, 10), 40)
                    .adicionarTurma("T06", "DC5200", new Horario(Horario.DiaDaSemana.QUINTA, 10, 12), 40)
                    .adicionarTurma("T07", "DCC046", new Horario(Horario.DiaDaSemana.SEXTA, 8, 10), 25)
                    .adicionarTurma("T08", "MUS001", new Horario(Horario.DiaDaSemana.SEXTA, 14, 16), 15)
                    .adicionarAluno("Ana (Caloura)", 240)
                    .adicionarAluno("Bruno (Veterano)", 300, aluno -> {
                        aluno.adicionarDisciplinaCursada(CatalogoDisciplinas.getDisciplina("DCC199"), 80f);
                        aluno.adicionarDisciplinaCursada(CatalogoDisciplinas.getDisciplina("DC5199"), 75f);
                        aluno.adicionarDisciplinaCursada(CatalogoDisciplinas.getDisciplina("MAT154"), 65f);
                        aluno.adicionarDisciplinaCursada(CatalogoDisciplinas.getDisciplina("MAT155"), 85f);
                    });
        }
    }
}
