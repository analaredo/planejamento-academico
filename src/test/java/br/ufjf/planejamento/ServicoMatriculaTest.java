package br.ufjf.planejamento;

import br.ufjf.planejamento.excecoes.*;
import br.ufjf.planejamento.modelo.*;
import br.ufjf.planejamento.servico.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suíte de testes robusta para a classe ServicoMatricula.
 * Valida todos os cenários de matrícula, incluindo sucesso, falhas de pré-requisitos,
 * co-requisitos, vagas, carga horária e resolução de conflitos de horário.
 */
class ServicoMatriculaTest {

    private ServicoMatricula servicoMatricula;
    private Aluno aluno;

    // Disciplinas
    private Disciplina alg1, alg1Lab, alg2, alg2Lab, calc1, gasl, redes, piano, guitarra, cifra;

    // Turmas
    private Turma turmaAlg1, turmaAlg1Lab, turmaAlg2, turmaAlg2Lab, turmaRedes, turmaPiano, turmaGuitarra, turmaCalc1, turmaGasl;

    @BeforeEach
    void setUp() {
        // Força a reinicialização dos catálogos estáticos para garantir isolamento dos testes
        new SistemaAcademico.Configurador().construir();

        servicoMatricula = new ServicoMatricula();
        aluno = new Aluno("João da Silva", "202501001", 240);

        // Pega as disciplinas do catálogo para usar nos testes
        alg1 = CatalogoDisciplinas.getDisciplina("DCC199");
        alg1Lab = CatalogoDisciplinas.getDisciplina("DC5199");
        alg2 = CatalogoDisciplinas.getDisciplina("DCC200");
        alg2Lab = CatalogoDisciplinas.getDisciplina("DC5200");
        calc1 = CatalogoDisciplinas.getDisciplina("MAT154");
        gasl = CatalogoDisciplinas.getDisciplina("MAT155");
        redes = CatalogoDisciplinas.getDisciplina("DCC046");
        piano = CatalogoDisciplinas.getDisciplina("MUS001");
        guitarra = CatalogoDisciplinas.getDisciplina("MUS002");
        cifra = CatalogoDisciplinas.getDisciplina("MUS004");

        // Cria turmas para as disciplinas
        Horario h1 = new Horario(Horario.DiaDaSemana.SEGUNDA, 8, 10);
        Horario h2 = new Horario(Horario.DiaDaSemana.SEGUNDA, 10, 12);
        Horario h3 = new Horario(Horario.DiaDaSemana.TERCA, 8, 10);
        Horario h4 = new Horario(Horario.DiaDaSemana.TERCA, 10, 12);

        turmaAlg1 = new Turma("T01", alg1, h1, 30);
        turmaAlg1Lab = new Turma("T02", alg1Lab, h2, 30);
        turmaAlg2 = new Turma("T03", alg2, h1, 30); // Conflita com T01
        turmaRedes = new Turma("T04", redes, h1, 20); // Conflita com T01 e T03
        turmaPiano = new Turma("T05", piano, h4, 10);
        turmaGuitarra = new Turma("T06", guitarra, h4, 5); // Conflita com T05
        turmaCalc1 = new Turma("T07", calc1, h3, 30);
        turmaGasl = new Turma("T08", gasl, new Horario(Horario.DiaDaSemana.QUARTA, 8, 10), 30);
    }

    @Test
    @DisplayName("Deve aceitar matrícula quando todos os requisitos são atendidos")
    void testMatriculaComSucesso() {
        aluno.adicionarTurmaPlanejamento(turmaAlg1);
        aluno.adicionarTurmaPlanejamento(turmaAlg1Lab); // Co-requisito

        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);

        assertEquals(2, relatorio.getTurmasAceitas().size());
        assertTrue(relatorio.getTurmasAceitas().containsAll(java.util.List.of(turmaAlg1, turmaAlg1Lab)));
    }

    @Test
    @DisplayName("Deve rejeitar matrícula por pré-requisito com nota insuficiente")
    void testRejeitaPorPreRequisitoComNotaBaixa() {
        aluno.adicionarDisciplinaCursada(alg1, 59.9f); // Não aprovado
        aluno.adicionarTurmaPlanejamento(turmaAlg2);

        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);

        assertEquals(0, relatorio.getTurmasAceitas().size());
        assertTrue(relatorio.toString().contains("Pré-requisito não cumprido"));
    }

    @Test
    @DisplayName("Deve rejeitar matrícula por co-requisito não selecionado")
    void testRejeitaPorCoRequisitoFaltando() {
        aluno.adicionarTurmaPlanejamento(turmaAlg1); // Falta o Lab

        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);

        assertEquals(0, relatorio.getTurmasAceitas().size());
        assertTrue(relatorio.toString().contains("Co-requisito não atendido"));
    }

    @Test
    @DisplayName("Deve rejeitar matrícula em turma cheia")
    void testRejeitaPorTurmaCheia() throws TurmaCheiaException {
        for (int i = 0; i < 10; i++) turmaPiano.matricular(new Aluno("Al" + i, "M" + i, 100));

        aluno.adicionarTurmaPlanejamento(turmaPiano);
        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);

        assertTrue(turmaPiano.estaCheia());
        assertEquals(0, relatorio.getTurmasAceitas().size());
        assertTrue(relatorio.toString().contains("Turma cheia"));
    }

    @Test
    @DisplayName("Deve rejeitar matrícula por exceder carga horária máxima")
    void testRejeitaPorCargaHorariaExcedida() {
        aluno = new Aluno("João da Silva", "202501001", 90);
        aluno.adicionarTurmaPlanejamento(turmaAlg1); // 60h
        aluno.adicionarTurmaPlanejamento(turmaAlg1Lab); // 30h
        aluno.adicionarTurmaPlanejamento(turmaCalc1); // 60h -> deve ser rejeitada

        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);

        assertEquals(2, relatorio.getTurmasAceitas().size());
    }

    @Test
    @DisplayName("Deve resolver conflito: Obrigatória (3) prevalece sobre Eletiva (2)")
    void testConflitoObrigatoriaVsEletiva() {
        aluno.adicionarDisciplinaCursada(alg1, 100);
        aluno.adicionarTurmaPlanejamento(turmaAlg2); // Obrigatória (Prio 3)
        aluno.adicionarTurmaPlanejamento(turmaRedes); // Eletiva (Prio 2) - Mesmo horário

        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);

        assertEquals(1, relatorio.getTurmasAceitas().size());
        assertTrue(relatorio.getTurmasAceitas().contains(turmaAlg2));
        assertFalse(relatorio.getTurmasAceitas().contains(turmaRedes));
    }

    @Test
    @DisplayName("Deve resolver conflito: Eletiva (2) prevalece sobre Optativa (1)")
    void testConflitoEletivaVsOptativa() {
        Turma turmaRedesConflitante = new Turma("T09", redes, turmaPiano.getHorario(), 20);
        aluno.adicionarTurmaPlanejamento(turmaRedesConflitante); // Eletiva (Prio 2)
        aluno.adicionarTurmaPlanejamento(turmaPiano); // Optativa (Prio 1) - Mesmo horário

        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);

        assertEquals(1, relatorio.getTurmasAceitas().size());
        assertTrue(relatorio.getTurmasAceitas().contains(turmaRedesConflitante));
        assertFalse(relatorio.getTurmasAceitas().contains(turmaPiano));
    }

    @Test
    @DisplayName("Deve rejeitar AMBAS as turmas em conflito de mesma prioridade")
    void testConflitoMesmaPrioridade() {
        aluno.adicionarDisciplinaCursada(cifra, 100);
        aluno.adicionarTurmaPlanejamento(turmaGuitarra); // Optativa (Prio 1)
        aluno.adicionarTurmaPlanejamento(turmaPiano);    // Optativa (Prio 1) - Mesmo horário

        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);

        assertEquals(0, relatorio.getTurmasAceitas().size(), "Nenhuma turma deveria ser aceita em conflito de mesma prioridade.");
    }
}
