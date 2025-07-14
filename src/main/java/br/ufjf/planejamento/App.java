package br.ufjf.planejamento;

import br.ufjf.planejamento.modelo.*;
import br.ufjf.planejamento.servico.RelatorioMatricula;
import br.ufjf.planejamento.servico.SistemaAcademico;

import java.util.*;
import java.util.stream.Collectors;


public class App {

    private SistemaAcademico sistema;
    private final Scanner scanner;
    private final Map<String, Map<Integer, RelatorioMatricula>> historicoDeRelatorios;
    private int periodoCorrente;

    public App() {
        this.sistema = null;
        this.scanner = new Scanner(System.in);
        this.historicoDeRelatorios = new HashMap<>();
        this.periodoCorrente = 1;
    }

    public void executar() {
        System.out.println("Bem-vindo ao Sistema de Planejamento Acadêmico!");
        System.out.println("Por favor, crie um cenário para começar.");

        while (true) {
            exibirMenuPrincipal();
            int opcao = lerInteiro("Escolha uma opção");
            if (!processarOpcaoMenu(opcao)) {
                break; // Sai do loop se o usuário escolher sair
            }
        }
        System.out.println("Encerrando o sistema.");
    }

    private void exibirMenuPrincipal() {
        if (sistema != null) {
            System.out.printf("     SISTEMA DE PLANEJAMENTO ACADÊMICO (Período: %d)\n", this.periodoCorrente);
        } else {
            System.out.println("     SISTEMA DE PLANEJAMENTO ACADÊMICO (Nenhum cenário carregado)");
        }
        System.out.println("1. Gerenciar cenário");
        System.out.println("2. Consultas e listagens");
        System.out.println("3. Adicionar turmas nos planejamento");
        System.out.println("4. Remover turmas dos planejamentos");
        System.out.println("5. Avançar o período");
        System.out.println("6. Ver histórico de relatórios");
        System.out.println("7. Sair");
    }

    private boolean processarOpcaoMenu(int opcao) {
        if (sistema == null && opcao > 1 && opcao < 7) {
            System.out.println("ERRO: Você precisa criar um cenário primeiro (Opção 1).");
            return true;
        }

        switch (opcao) {
            case 1:
                menuGerenciarCenario();
                break;
            case 2:
                menuConsultas();
                break;
            case 3:
                montarPlanejamento();
                break;
            case 4:
                removerTurmaDoPlanejamento();
                break;
            case 5:
                executarSimulacao();
                break;
            case 6:
                verHistorico();
                break;
            case 7:
                return false; // Sinaliza para sair
            default:
                System.out.println("Opção inválida. Tente novamente.");
        }
        return true;
    }

    private void menuGerenciarCenario() {
        System.out.println("\n--- Gerenciar cenário atual ---");
        System.out.println("1. Utilizar dados prontos");
        System.out.println("2. Definir como cenário vazio (para montar do zero)");
        System.out.println("3. Criar cenário por etapas");
        System.out.println("4. Voltar ao menu principal");
        int opcao = lerInteiro("Escolha uma opção");

        switch (opcao) {
            case 1:
                this.sistema = SistemaAcademico.Configurador.criarConfiguracaoPadrao().construir();
                this.historicoDeRelatorios.clear();
                this.periodoCorrente = 1;
                System.out.println("Cenário padrão criado com sucesso!");
                break;
            case 2:
                this.sistema = new SistemaAcademico.Configurador().construir();
                this.historicoDeRelatorios.clear();
                this.periodoCorrente = 1;
                System.out.println("Cenário vazio criado. Adicione alunos e turmas.");
                break;
            case 3:
                criarCenarioPersonalizado();
                break;
            case 4:
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }

    private void criarCenarioPersonalizado() {
        SistemaAcademico.Configurador configurador = new SistemaAcademico.Configurador();
        System.out.println("\n--- Criação de cenário ---");

        // Adicionar Alunos
        System.out.println("\n** Adicionar alunos **");
        while (true) {
            String nome = lerString("Nome do novo aluno (ou 'fim' para parar)");
            if (nome.equalsIgnoreCase("fim")) break;
            int chMax = lerInteiro("Carga horária máxima para " + nome);
            configurador.adicionarAluno(nome, chMax);
            System.out.println("Aluno " + nome + " adicionado.");
        }

        // Adicionar Turmas
        System.out.println("\n** Adicionar turmas ofertadas **");
        CatalogoDisciplinas.getTodasDisciplinas().forEach(d -> System.out.printf("Código: %s - %s\n", d.getCodigo(), d.getNome()));
        while (true) {
            String cod = lerString("Código da disciplina para criar turma (ou 'fim' para parar)");
            if (cod.equalsIgnoreCase("fim")) break;
            if (CatalogoDisciplinas.getDisciplina(cod) == null) {
                System.out.println("Código de disciplina inválido.");
                continue;
            }
            String idTurma = lerString("ID para a nova turma (ex: T99)");
            int capacidade = lerInteiro("Capacidade da turma");
            // Simplificação de horário para o assistente
            Horario.DiaDaSemana dia = Horario.DiaDaSemana.values()[new Random().nextInt(6)];
            int horaInicio = 8 + new Random().nextInt(10);
            configurador.adicionarTurma(idTurma, cod, new Horario(dia, horaInicio, horaInicio + 2), capacidade);
            System.out.println("Turma " + idTurma + " para " + cod + " adicionada.");
        }

        this.sistema = configurador.construir();
        this.historicoDeRelatorios.clear();
        this.periodoCorrente = 1;
        System.out.println("\nCenário personalizado criado com sucesso!");
    }

    private void menuConsultas() {
        System.out.println("\n--- Consultas e listagens ---");
        System.out.println("1. Listar alunos e turmas ofertadas");
        System.out.println("2. Ver grade planejada do aluno");
        System.out.println("3. Voltar ao menu principal");
        int opcao = lerInteiro("Escolha uma opção");

        switch (opcao) {
            case 1:
                listarAlunosETurmas();
                break;
            case 2:
                verGradePlanejada();
                break;
            case 3:
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }

    private void listarAlunosETurmas() {
        System.out.println("\n--- Alunos Cadastrados ---");
        if (sistema.listarAlunos().isEmpty()) {
            System.out.println("Nenhum aluno cadastrado neste cenário.");
        } else {
            sistema.listarAlunos().forEach(aluno -> {
                System.out.printf("Matrícula: %s | Nome: %s | CH Máx: %dh\n",
                        aluno.getMatricula(), aluno.getNome(), aluno.getCargaHorariaMaxima());
            });
        }

        System.out.println("\n--- Turmas Ofertadas ---");
        if (sistema.listarTurmas().isEmpty()) {
            System.out.println("Nenhuma turma ofertada neste cenário.");
        } else {
            sistema.listarTurmas().forEach(turma -> {
                System.out.printf("ID: %-5s | Disciplina: %s (%s) | Vagas: %d/%d | Horário: %s\n",
                        turma.getId(), turma.getDisciplina().getNome(), turma.getDisciplina().getCodigo(),
                        turma.getNumeroDeVagas(), turma.getCapacidadeMaxima(), turma.getHorario());
            });
        }
    }

    private void verGradePlanejada() {
        System.out.println("\n--- Ver grade planejada do aluno ---");
        String matricula = lerString("Digite a matrícula do aluno");
        Aluno aluno = sistema.buscarAluno(matricula);

        if (aluno == null) {
            System.out.println("ERRO: Aluno não encontrado.");
            return;
        }

        exibirGradeHoraria("Grade planejada para " + aluno.getNome(), aluno.getPlanejamento());
    }

    private void montarPlanejamento() {
        System.out.println("\n--- Planejamento de matrícula ---");
        listarAlunosETurmas();
        String matricula = lerString("\nDigite a matrícula do aluno");
        Aluno aluno = sistema.buscarAluno(matricula);

        if (aluno == null) {
            System.out.println("ERRO: Aluno não encontrado.");
            return;
        }

        List<Turma> plano = new ArrayList<>();
        System.out.println("\nDigite os IDs das turmas desejadas (digite 'fim' para terminar):");
        while (true) {
            String idTurma = lerString("ID da Turma");
            if (idTurma.equalsIgnoreCase("fim")) break;
            Turma turma = sistema.buscarTurma(idTurma);
            if (turma != null) {
                plano.add(turma);
                System.out.printf("Turma '%s' adicionada ao planejamento.\n", turma.getDisciplina().getNome());
            } else {
                System.out.println("ERRO: Turma não encontrada.");
            }
        }
        aluno.setPlanejamento(plano);
        System.out.printf("Planejamento do aluno %s atualizado com %d turmas.\n", aluno.getNome(), plano.size());
    }

    private void removerTurmaDoPlanejamento() {
        System.out.println("\n--- Remover Turma do planejamento ---");
        String matricula = lerString("Digite a matrícula do aluno");
        Aluno aluno = sistema.buscarAluno(matricula);

        if (aluno == null) {
            System.out.println("ERRO: Aluno não encontrado.");
            return;
        }

        if (aluno.getPlanejamento() == null || aluno.getPlanejamento().isEmpty()) {
            System.out.println("O aluno não possui turmas em seu planejamento atual.");
            return;
        }

        System.out.println("Turmas no planejamento de " + aluno.getNome() + ":");
        aluno.getPlanejamento().forEach(t -> System.out.println("ID: " + t.getId() + " - " + t.getDisciplina().getNome()));

        String idTurma = lerString("Digite o ID da turma a ser removida");
        boolean removido = aluno.getPlanejamento().removeIf(t -> t.getId().equalsIgnoreCase(idTurma));

        if (removido) {
            System.out.println("Turma removida com sucesso.");
        } else {
            System.out.println("ERRO: Turma com ID '" + idTurma + "' não encontrada no planejamento.");
        }
    }

    private void executarSimulacao() {
        int periodoDaSimulacao = this.periodoCorrente;
        System.out.println("\n>>> INICIANDO SIMULAÇÃO DE MATRÍCULA PARA O PERÍODO " + periodoDaSimulacao + " <<<");

        Map<Aluno, RelatorioMatricula> relatorios = sistema.executarSimulacaoPeriodo();

        if (relatorios.isEmpty()) {
            System.out.println("Nenhum aluno tinha planejamento para ser processado.");
        } else {
            System.out.println("\n--- Relatórios gerados ---");
            relatorios.forEach((aluno, relatorio) -> {
                historicoDeRelatorios.computeIfAbsent(aluno.getMatricula(), k -> new HashMap<>())
                        .put(periodoDaSimulacao, relatorio);
                System.out.println(relatorio.toString());
            });
        }

        this.periodoCorrente++;
        System.out.println("\n>>> SIMULAÇÃO CONCLUÍDA. O sistema avançou para o período " + this.periodoCorrente + ". <<<");
        System.out.println(">>> O histórico dos alunos foi ATUALIZADO com as turmas aceitas. <<<");
        System.out.println(">>> Os planejamentos foram LIMPOS automaticamente. Os alunos podem criar novos planejamentos. <<<");
    }

    private void verHistorico() {
        System.out.println("\n--- Histórico de Relatórios ---");
        if (historicoDeRelatorios.isEmpty()) {
            System.out.println("Nenhum relatório no histórico. Execute uma simulação primeiro.");
            return;
        }

        System.out.println("Alunos com histórico:");
        historicoDeRelatorios.keySet().forEach(System.out::println);
        String matricula = lerString("Digite a matrícula do aluno para ver o histórico");

        Map<Integer, RelatorioMatricula> relatoriosDoAluno = historicoDeRelatorios.get(matricula);
        if (relatoriosDoAluno == null || relatoriosDoAluno.isEmpty()) {
            System.out.println("Nenhum histórico encontrado para este aluno.");
            return;
        }

        System.out.println("Períodos disponíveis para " + matricula + ": " + relatoriosDoAluno.keySet());
        int periodo = lerInteiro("Digite o período para ver o relatório");

        RelatorioMatricula relatorio = relatoriosDoAluno.get(periodo);
        if (relatorio == null) {
            System.out.println("Nenhum relatório para este período.");
        } else {
            System.out.println("\n--- Exibindo Relatório do Período " + periodo + " para " + matricula + " ---");
            System.out.println(relatorio.toString());
        }
    }

    private String lerString(String mensagem) {
        System.out.print(mensagem + ": ");
        return scanner.nextLine();
    }

    private int lerInteiro(String mensagem) {
        System.out.print(mensagem + ": ");
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida. Por favor, digite um número.");
            return -1;
        }
    }

    private void exibirGradeHoraria(String titulo, List<Turma> turmas) {
        System.out.println("\n--- " + titulo + " ---");
        if (turmas == null || turmas.isEmpty()) {
            System.out.println("   Nenhuma turma para exibir.");
            return;
        }

        Map<Horario.DiaDaSemana, List<Turma>> grade = new TreeMap<>();
        for (Turma t : turmas) {
            grade.computeIfAbsent(t.getHorario().getDia(), k -> new ArrayList<>()).add(t);
        }

        if (grade.isEmpty()) {
            System.out.println("   Nenhuma disciplina alocada na grade.");
            return;
        }

        String format = "   %-10s | %s\n";
        for (Map.Entry<Horario.DiaDaSemana, List<Turma>> entry : grade.entrySet()) {
            String disciplinasDoDia = entry.getValue().stream()
                    .map(t -> String.format("%s (%s)", t.getDisciplina().getCodigo(), t.getHorario().toString()))
                    .collect(Collectors.joining(" | "));
            System.out.printf(format, entry.getKey(), disciplinasDoDia);
        }
    }

    public static void main(String[] args) {
        new App().executar();
    }
}
