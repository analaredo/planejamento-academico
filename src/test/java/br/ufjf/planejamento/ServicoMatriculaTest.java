package br.ufjf.planejamento;

import br.ufjf.planejamento.excecoes.TurmaCheiaException;
import br.ufjf.planejamento.modelo.*;
import br.ufjf.planejamento.servico.RelatorioMatricula;
import br.ufjf.planejamento.servico.ServicoMatricula;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Conjunto de testes unitários para a classe ServicoMatricula.
 * Valida todos os cenários de matrícula, incluindo sucesso, falhas de pré-requisitos,
 * co-requisitos, vagas, carga horária e resolução de conflitos de horário.
 */
class ServicoMatriculaTest {

    private ServicoMatricula servicoMatricula;
    private Aluno aluno;

    // Disciplinas
    private Disciplina alg1, alg2, calc1, redes, piano, guitarra, partitura, cifra;

    // Turmas
    private Turma turmaAlg2, turmaRedes, turmaPiano, turmaGuitarra;

    @BeforeEach
    void setUp() {
        // Inicializa o serviço a ser testado
        servicoMatricula = new ServicoMatricula();

        // Cria um aluno padrão para os testes com carga horária máxima de 120h
        aluno = new Aluno("João da Silva", "202501001", 120);

        // Pega as disciplinas do catálogo para usar nos testes
        alg1 = CatalogoDisciplinas.getDisciplina("DCC199");
        alg2 = CatalogoDisciplinas.getDisciplina("DCC200");
        calc1 = CatalogoDisciplinas.getDisciplina("MAT154");
        redes = CatalogoDisciplinas.getDisciplina("DCC046"); // Eletiva
        piano = CatalogoDisciplinas.getDisciplina("MUS001"); // Optativa
        guitarra = CatalogoDisciplinas.getDisciplina("MUS002"); // Optativa com pré-req OR
        partitura = CatalogoDisciplinas.getDisciplina("MUS003");
        cifra = CatalogoDisciplinas.getDisciplina("MUS004");


        // Cria turmas para as disciplinas
        Horario horarioSegundaManha = new Horario(Horario.DiaDaSemana.SEGUNDA, 8, 10);
        Horario horarioTercaManha = new Horario(Horario.DiaDaSemana.TERCA, 10, 12);

        turmaAlg2 = new Turma("T01", alg2, horarioSegundaManha, 30);
        turmaRedes = new Turma("T02", redes, horarioSegundaManha, 20); // Conflita com Alg2
        turmaPiano = new Turma("T03", piano, horarioTercaManha, 10);
        turmaGuitarra = new Turma("T04", guitarra, horarioTercaManha, 5); // Conflita com Piano
    }

    @Test
    @DisplayName("Deve aceitar matrícula quando todos os requisitos são atendidos")
    void testMatriculaComSucesso() {
        aluno.adicionarDisciplinaCursada(alg1, 75.0f); // Cumpre o pré-requisito de Alg2
        aluno.adicionarTurmaPlanejamento(turmaAlg2);

        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);

        assertEquals(1, relatorio.getTurmasAceitas().size());
        assertTrue(relatorio.getTurmasAceitas().contains(turmaAlg2));
    }

    @Test
    @DisplayName("Deve rejeitar matrícula por pré-requisito simples não cumprido")
    void testRejeitaPorPreRequisitoNaoCumprido() {
        // Aluno não cursou Alg1, que é pré-requisito de Alg2
        aluno.adicionarTurmaPlanejamento(turmaAlg2);

        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);
        String relatorioTexto = relatorio.toString();

        assertEquals(0, relatorio.getTurmasAceitas().size());
        assertTrue(relatorioTexto.contains("REJEITADA"));
        assertTrue(relatorioTexto.contains("Pré-requisito não cumprido"));
    }

    @Test
    @DisplayName("Deve rejeitar matrícula por pré-requisito com nota insuficiente")
    void testRejeitaPorPreRequisitoComNotaBaixa() {
        aluno.adicionarDisciplinaCursada(alg1, 59.0f); // Cursou, mas não foi aprovado
        aluno.adicionarTurmaPlanejamento(turmaAlg2);

        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);
        String relatorioTexto = relatorio.toString();

        assertEquals(0, relatorio.getTurmasAceitas().size());
        assertTrue(relatorioTexto.contains("REJEITADA"));
        assertTrue(relatorioTexto.contains("É necessário ter sido aprovado em 'Algoritmos 1'"));
    }

    @Test
    @DisplayName("Deve aceitar matrícula com pré-requisito lógico OR atendido")
    void testAceitaComPreRequisitoLogicoOR() {
        aluno.adicionarDisciplinaCursada(cifra, 80.0f); // Atende a UMA das condições para Guitarra
        aluno.adicionarTurmaPlanejamento(turmaGuitarra);

        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);

        assertEquals(1, relatorio.getTurmasAceitas().size());
        assertTrue(relatorio.getTurmasAceitas().contains(turmaGuitarra));
    }

    @Test
    @DisplayName("Deve rejeitar matrícula por co-requisito não selecionado")
    void testRejeitaPorCoRequisitoFaltando() {
        Disciplina alg1Lab = CatalogoDisciplinas.getDisciplina("DC5199");
        Turma turmaAlg1 = new Turma("T05", alg1, new Horario(Horario.DiaDaSemana.QUARTA, 8, 10), 30);
        // Não adiciona a turma do laboratório (co-requisito)
        aluno.adicionarTurmaPlanejamento(turmaAlg1);

        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);
        String relatorioTexto = relatorio.toString();

        assertEquals(0, relatorio.getTurmasAceitas().size());
        assertTrue(relatorioTexto.contains("Co-requisito não atendido"));
    }

    @Test
    @DisplayName("Deve rejeitar matrícula em turma cheia")
    void testRejeitaPorTurmaCheia() throws TurmaCheiaException {
        // Lota a turma de piano
        for (int i = 0; i < 10; i++) {
            turmaPiano.matricular(new Aluno("Aluno " + i, "202501" + (100 + i), 120));
        }

        aluno.adicionarTurmaPlanejamento(turmaPiano);
        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);
        String relatorioTexto = relatorio.toString();

        assertTrue(turmaPiano.estaCheia());
        assertEquals(0, relatorio.getTurmasAceitas().size());
        assertTrue(relatorioTexto.contains("Turma cheia"));
    }

    @Test
    @DisplayName("Deve rejeitar matrícula por exceder carga horária máxima")
    void testRejeitaPorCargaHorariaExcedida() {
        aluno.adicionarDisciplinaCursada(alg1, 100);

        // Carga horária total = 60 (Alg2) + 60 (Redes) + 30 (Piano) = 150h > 120h (limite)
        aluno.adicionarTurmaPlanejamento(turmaAlg2); // 60h (Obrigatória)
        aluno.adicionarTurmaPlanejamento(turmaRedes); // 60h (Eletiva)
        aluno.adicionarTurmaPlanejamento(turmaPiano); // 30h (Optativa) - Deve ser rejeitada

        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);
        String relatorioTexto = relatorio.toString();

        // Como redes e alg2 conflitam, e alg2 tem maior prioridade, redes é rejeitada.
        // Piano é então avaliada, mas excede a carga horária.
        assertEquals(1, relatorio.getTurmasAceitas().size()); // Apenas Alg2
        assertTrue(relatorio.getTurmasAceitas().contains(turmaAlg2));
        assertTrue(relatorioTexto.contains("Carga horária máxima do semestre seria excedida."));
    }

    @Test
    @DisplayName("Deve resolver conflito: Obrigatória prevalece sobre Eletiva")
    void testConflitoHorarioObrigatoriaVsEletiva() {
        aluno.adicionarDisciplinaCursada(alg1, 100);

        // Ambas no mesmo horário, mas Alg2 (Obrigatória) tem prioridade sobre Redes (Eletiva)
        aluno.adicionarTurmaPlanejamento(turmaAlg2);
        aluno.adicionarTurmaPlanejamento(turmaRedes);

        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);

        assertEquals(1, relatorio.getTurmasAceitas().size());
        assertTrue(relatorio.getTurmasAceitas().contains(turmaAlg2));
        assertFalse(relatorio.getTurmasAceitas().contains(turmaRedes));
    }

    @Test
    @DisplayName("Deve resolver conflito: Eletiva prevalece sobre Optativa")
    void testConflitoHorarioEletivaVsOptativa() {
        Turma turmaRedesConflitante = new Turma("T06", redes, turmaPiano.getHorario(), 20);

        // Ambas no mesmo horário, mas Redes (Eletiva) tem prioridade sobre Piano (Optativa)
        aluno.adicionarTurmaPlanejamento(turmaRedesConflitante);
        aluno.adicionarTurmaPlanejamento(turmaPiano);

        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);

        assertEquals(1, relatorio.getTurmasAceitas().size());
        assertTrue(relatorio.getTurmasAceitas().contains(turmaRedesConflitante));
        assertFalse(relatorio.getTurmasAceitas().contains(turmaPiano));
    }

    @Test
    @DisplayName("Deve rejeitar ambas as turmas em conflito de mesma prioridade")
    void testConflitoHorarioMesmaPrioridade() {
        // Guitarra e Piano são ambas Optativas e conflitam no horário
        aluno.adicionarDisciplinaCursada(cifra, 100); // pré-req de guitarra
        aluno.adicionarTurmaPlanejamento(turmaGuitarra);
        aluno.adicionarTurmaPlanejamento(turmaPiano);

        RelatorioMatricula relatorio = servicoMatricula.processarPlanejamento(aluno);
        String relatorioTexto = relatorio.toString();

        // Nenhuma deve ser aceita
        assertEquals(0, relatorio.getTurmasAceitas().size());
        assertTrue(relatorioTexto.contains("Conflito de horário com MUS001, que possui prioridade maior ou igual."));
    }
}
