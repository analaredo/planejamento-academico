package br.ufjf.planejamento.modelo;

import br.ufjf.planejamento.validacao.ValidadorLogicoAND;
import br.ufjf.planejamento.validacao.ValidadorLogicoOR;
import br.ufjf.planejamento.validacao.ValidadorSimples;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe utilitária para centralizar a criação e o acesso a todas as
 * disciplinas do sistema. Funciona como um banco de dados em memória para a simulação.
 */
public class CatalogoDisciplinas {
    private static final Map<String, Disciplina> disciplinas = new HashMap<>();

    static {
        //------------------OBRIGATÓRIAS--------------------
        // Algoritmos
        Disciplina alg1 = criarObrigatoria("DCC199", "Algoritmos 1", 60);
        Disciplina alg1Lab = criarObrigatoria("DC5199", "Algoritmos 1 - Prática", 30);
        configurarCodependencia(alg1, alg1Lab);

        Disciplina alg2 = criarObrigatoria("DCC200", "Algoritmos 2", 60);
        Disciplina alg2Lab = criarObrigatoria("DC5200", "Algoritmos 2 - Prática", 30);

        alg2Lab.adicionarCoRequisito(alg2);
        alg2.adicionarValidador(new ValidadorSimples("DCC199"));


        // Matemática
        Disciplina calc1 = criarObrigatoria("MAT154", "Cálculo 1", 60);
        Disciplina gasl = criarObrigatoria("MAT155", "Geometria Analítica e Álgebra Linear", 60);
        configurarCodependencia(calc1, gasl);

        //------------------ELETIVAS--------------------
        Disciplina tctrl = criarEletiva("CEL038", "Teoria de Controle 1", 60);
        Disciplina mindados = criarEletiva("DCC127", "Mineração de Dados", 30);

        Disciplina viscient = criarEletiva("DCC191", "Visualização Científica", 30);
        // Pré-requisito: Para cursar Visualização Científica, é preciso ter sido aprovado em Algoritmos 1 E Cálculo 1.
        viscient.adicionarValidador(new ValidadorLogicoAND(
                new ValidadorSimples("DCC199"),
                new ValidadorSimples("MAT154")
        ));

        Disciplina teoriacompiladores = criarEletiva("DCC045", "Teoria dos Compiladores", 60);
        Disciplina redes = criarEletiva("DCC046", "Redes de Computadores", 60);
        Disciplina sistemasoperacionais = criarEletiva("DCC047", "Sistemas Operacionais", 60);

        //------------------OPTATIVAS--------------------
        Disciplina inginst = criarOptativa("LIN005", "Inglês Instrumental", 30);
        Disciplina piano = criarOptativa("MUS001", "Piano 1", 30);
        Disciplina design = criarOptativa("DES001", "Design Gráfico", 30);
        Disciplina partitura = criarOptativa("MUS003", "Partitura 1", 30);
        Disciplina cifra = criarOptativa("MUS004", "Cifra 1", 30);

        Disciplina guitarra = criarOptativa("MUS002", "Guitarra 1", 30);
        // Pré-requisito: Para cursar Guitarra 1, é preciso ter sido aprovado em Partitura 1 OU Cifra 1.
        guitarra.adicionarValidador(new ValidadorLogicoOR(
                new ValidadorSimples("MUS003"),
                new ValidadorSimples("MUS004")
        ));
    }

    private static Disciplina criarObrigatoria(String codigo, String nome, int cargaHoraria) {
        Disciplina disciplina = new DisciplinaObrigatoria(codigo, nome, cargaHoraria);
        disciplinas.put(codigo, disciplina);
        return disciplina;
    }

    private static Disciplina criarEletiva(String codigo, String nome, int cargaHoraria) {
        Disciplina disciplina = new DisciplinaEletiva(codigo, nome, cargaHoraria);
        disciplinas.put(codigo, disciplina);
        return disciplina;
    }

    private static Disciplina criarOptativa(String codigo, String nome, int cargaHoraria) {
        Disciplina disciplina = new DisciplinaOptativa(codigo, nome, cargaHoraria);
        disciplinas.put(codigo, disciplina);
        return disciplina;
    }

    /**
     * Configura duas disciplinas para serem co-requisitos uma da outra.
     * @param d1 Primeira disciplina.
     * @param d2 Segunda disciplina.
     */
    private static void configurarCodependencia(Disciplina d1, Disciplina d2) {
        d1.adicionarCoRequisito(d2);
        d2.adicionarCoRequisito(d1);
    }

    /**
     * Retorna uma disciplina do catálogo a partir de seu código.
     * @param codigo O código da disciplina.
     * @return A instância da Disciplina ou null se não for encontrada.
     */
    public static Disciplina getDisciplina(String codigo) {
        return disciplinas.get(codigo);
    }

    public static Collection<Disciplina> getTodasDisciplinas() {
        return disciplinas.values();
    }

    public static boolean removerDisciplina(String codigo) {
        return disciplinas.remove(codigo) != null;
    }
}
