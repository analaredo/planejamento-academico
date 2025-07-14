package br.ufjf.planejamento;

import br.ufjf.planejamento.modelo.Aluno;
import br.ufjf.planejamento.modelo.CatalogoAlunos;
import br.ufjf.planejamento.modelo.CatalogoDisciplinas;
import br.ufjf.planejamento.modelo.CatalogoTurmas;
import br.ufjf.planejamento.servico.SistemaAcademico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suíte de testes de integração para a classe SistemaAcademico.
 */
class SistemaAcademicoTest {

    private SistemaAcademico sistema;

    @BeforeEach
    void setUp() {
        sistema = new SistemaAcademico.Configurador().construir();
    }

    @Test
    @DisplayName("Deve criar um sistema com configuração padrão corretamente")
    void testCriarConfiguracaoPadrao() {
        sistema = SistemaAcademico.Configurador.criarConfiguracaoPadrao().construir();

        assertFalse(CatalogoAlunos.getTodosAlunos().isEmpty(), "O catálogo de alunos não deveria estar vazio.");
        assertFalse(CatalogoTurmas.getTodasTurmas().isEmpty(), "O catálogo de turmas não deveria estar vazio.");
        assertEquals(1, sistema.getPeriodoAtual());
    }

    @Test
    @DisplayName("Deve executar a simulação, avançar o período e atualizar o histórico do aluno")
    void testExecutarSimulacaoEAvancarPeriodo() {
        // Cria um cenário simples com o co-requisito atendido
        sistema = new SistemaAcademico.Configurador()
                .adicionarAluno("Aluno Simples", 240)
                .adicionarTurma("T01", "DCC199", new br.ufjf.planejamento.modelo.Horario(br.ufjf.planejamento.modelo.Horario.DiaDaSemana.SEGUNDA, 8, 10), 10)
                .adicionarTurma("T02", "DC5199", new br.ufjf.planejamento.modelo.Horario(br.ufjf.planejamento.modelo.Horario.DiaDaSemana.TERCA, 8, 10), 10)
                .construir();

        Aluno aluno = sistema.listarAlunos().iterator().next();
        assertNotNull(aluno);

        // Planejamento
        aluno.adicionarTurmaPlanejamento(sistema.buscarTurma("T01"));
        aluno.adicionarTurmaPlanejamento(sistema.buscarTurma("T02")); // Adiciona o co-requisito

        // Execução
        assertEquals(1, sistema.getPeriodoAtual());
        sistema.executarSimulacaoPeriodo();

        // Verificações
        assertEquals(2, sistema.getPeriodoAtual(), "O período deveria ter avançado para 2.");
        assertTrue(aluno.foiAprovado(CatalogoDisciplinas.getDisciplina("DCC199")), "O histórico do aluno deveria conter a disciplina cursada.");
        assertTrue(aluno.getPlanejamento().isEmpty(), "O planejamento do aluno deveria ter sido limpo para o próximo período.");
    }

    @Test
    @DisplayName("Não deve atualizar histórico se matrícula falhar, mas deve avançar o período")
    void testExecutarSimulacaoComFalha() {
        // falta co-requisito
        sistema = new SistemaAcademico.Configurador()
                .adicionarAluno("Aluno Azarado", 240)
                .adicionarTurma("T01", "DCC199", new br.ufjf.planejamento.modelo.Horario(br.ufjf.planejamento.modelo.Horario.DiaDaSemana.SEGUNDA, 8, 10), 10)
                .construir();

        Aluno aluno = sistema.buscarAluno(sistema.listarAlunos().iterator().next().getMatricula());
        aluno.adicionarTurmaPlanejamento(sistema.buscarTurma("T01")); // Falta o co-requisito DC5199

        // Execução
        assertEquals(1, sistema.getPeriodoAtual());
        sistema.executarSimulacaoPeriodo();

        // Verificações
        assertEquals(2, sistema.getPeriodoAtual(), "O período deveria ter avançado mesmo com a falha na matrícula.");
        assertFalse(aluno.foiAprovado(CatalogoDisciplinas.getDisciplina("DCC199")), "O histórico do aluno não deveria ter sido atualizado.");
        assertTrue(aluno.getPlanejamento().isEmpty(), "O planejamento deveria ser limpo mesmo com a falha.");
    }
}