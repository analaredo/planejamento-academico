package br.ufjf.planejamento;

import br.ufjf.planejamento.modelo.Aluno;
import br.ufjf.planejamento.modelo.CatalogoDisciplinas;
import br.ufjf.planejamento.modelo.Disciplina;
import br.ufjf.planejamento.servico.SistemaAcademico;
import br.ufjf.planejamento.validacao.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suíte de testes de unidade para todas as implementações de ValidadorPreRequisito.
 * Garante que cada regra de negócio de validação funcione corretamente de forma isolada.
 */
class ValidadorTest {

    private static Aluno alunoComHistorico;
    private static Aluno alunoSemHistorico;

    @BeforeAll
    static void setUpGlobal() {
        // Inicializa o catálogo de disciplinas uma vez para todos os testes
        new SistemaAcademico.Configurador().criarConfiguracaoPadrao().construir();

        Disciplina disciplinaAprovada = CatalogoDisciplinas.getDisciplina("DCC199"); // Algoritmos 1 (60h)
        Disciplina disciplinaReprovada = CatalogoDisciplinas.getDisciplina("MAT154"); // Cálculo 1 (60h)

        // Cria um aluno com histórico definido
        alunoComHistorico = new Aluno("Aluno Teste", "T001", 240);
        alunoComHistorico.adicionarDisciplinaCursada(disciplinaAprovada, 80.0f); // Aprovado
        alunoComHistorico.adicionarDisciplinaCursada(disciplinaReprovada, 45.0f); // Reprovado

        // Cria um aluno sem histórico
        alunoSemHistorico = new Aluno("Aluno Novo", "T002", 240);
    }

    @Nested
    @DisplayName("Testes para ValidadorSimples")
    class ValidadorSimplesTest {
        @Test
        @DisplayName("Deve retornar true se o aluno foi aprovado na disciplina")
        void testValidarAprovado() {
            assertTrue(new ValidadorSimples("DCC199").validar(alunoComHistorico));
        }

        @Test
        @DisplayName("Deve retornar false se o aluno foi reprovado na disciplina")
        void testValidarReprovado() {
            assertFalse(new ValidadorSimples("MAT154").validar(alunoComHistorico));
        }

        @Test
        @DisplayName("Deve retornar false se o aluno não cursou a disciplina")
        void testValidarNaoCursado() {
            assertFalse(new ValidadorSimples("DCC046").validar(alunoComHistorico));
        }
    }

    @Nested
    @DisplayName("Testes para ValidadorCreditosMinimos")
    class ValidadorCreditosMinimosTest {
        @Test
        @DisplayName("Deve retornar true se o aluno tem créditos suficientes")
        void testCreditosSuficientes() {
            // alunoComHistorico tem 60 créditos (apenas de DCC199)
            assertTrue(new ValidadorCreditosMinimos(60).validar(alunoComHistorico));
        }

        @Test
        @DisplayName("Deve retornar false se o aluno não tem créditos suficientes")
        void testCreditosInsuficientes() {
            assertFalse(new ValidadorCreditosMinimos(70).validar(alunoComHistorico));
        }
    }

    @Nested
    @DisplayName("Testes para ValidadorLogicoAND")
    class ValidadorLogicoANDTest {
        @Test
        @DisplayName("Deve retornar true se todas as condições forem verdadeiras")
        void testTodasCondicoesVerdadeiras() {
            ValidadorLogicoAND validador = new ValidadorLogicoAND(
                    new ValidadorSimples("DCC199"),
                    new ValidadorCreditosMinimos(50)
            );
            assertTrue(validador.validar(alunoComHistorico));
        }

        @Test
        @DisplayName("Deve retornar false se uma das condições for falsa")
        void testUmaCondicaoFalsa() {
            ValidadorLogicoAND validador = new ValidadorLogicoAND(
                    new ValidadorSimples("DCC199"),
                    new ValidadorSimples("DCC046")
            );
            assertFalse(validador.validar(alunoComHistorico));
        }
    }

    @Nested
    @DisplayName("Testes para ValidadorLogicoOR")
    class ValidadorLogicoORTest {
        @Test
        @DisplayName("Deve retornar true se pelo menos uma condição for verdadeira")
        void testUmaCondicaoVerdadeira() {
            ValidadorLogicoOR validador = new ValidadorLogicoOR(
                    new ValidadorSimples("DCC199"),
                    new ValidadorSimples("DCC046")
            );
            assertTrue(validador.validar(alunoComHistorico));
        }

        @Test
        @DisplayName("Deve retornar false se todas as condições forem falsas")
        void testTodasCondicoesFalsas() {
            ValidadorLogicoOR validador = new ValidadorLogicoOR(
                    new ValidadorSimples("MAT154"),
                    new ValidadorSimples("DCC046")
            );
            assertFalse(validador.validar(alunoComHistorico));
        }
    }
}