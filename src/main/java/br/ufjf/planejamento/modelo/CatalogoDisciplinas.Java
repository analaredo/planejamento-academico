// br.ufjf.planejamento.servico.CatalogoDisciplinas
package br.ufjf.planejamento.servico;

public class CatalogoDisciplinas {
    private static Map<String, Disciplina> disciplinas = new HashMap<>();

    static {

        //------------------OBRIGATÓRIAS--------------------
        // Algoritmos
        Disciplina alg1 = criarObrigatoria("DCC199", "Algoritmos 1", 60);
        Disciplina alg1Lab = criarObrigatoria("DC5199", "Algoritmos 1 - Prática", 30);
        configurarCodependencia(alg1, alg1Lab);

        Disciplina alg2 = criarObrigatoria("DCC200", "Algoritmos 2", 60);
        Disciplina alg2Lab = criarObrigatoria("DC5200", "Algoritmos 2 - Prática", 30);
        configurarCodependencia(alg2, alg2Lab);
        alg2.adicionarValidador(new ValidadorPreRequisito("DCC199"));

        // Matemática
        Disciplina calc1 = criarObrigatoria("MAT154", "Cálculo 1", 60);
        Disciplina gasl = criarObrigatoria("MAT155", "Geometria Analítica e Álgebra Linear", 60);
        configurarCodependencia(calc1, gasl);

        //------------------ELETIVAS--------------------
        Disciplina tctrl = criarEletiva("CEL038", "Teoria de Controle 1", 60);

        Disciplina mindados = criarEletiva("DCC127", "Mineração de Dados", 30);

        Disciplina viscient = criarEletiva("DCC191", "Visualização Científica", 30);
        viscient.adicionarValidador(new ValidadorLogicoAND("DCC199","MAT154"));

        Disciplina teoriacompiladores = criarEletiva("DCC045", "Teoria dos Compiladores", 60);

        Disciplina redes = criarEletiva("DCC046", "Redes de Computadores", 60);

        Disciplina sistemasoperacionais = criarEletiva("DCC047", "Sistemas Operacionais", 60);

        //------------------OPTATIVAS--------------------
        Disciplina inginst = criarOptativa("LIN005", "Inglês Instrumental", 30);

        Disciplina piano = criarOptativa("MUS001", "Piano 1", 30);

        Disciplina guitarra = criarOptativa("MUS002", "Guitarra 1", 30);
        guitarra.adicionarValidador(new ValidadorLogicoOR("MUS003", "MUS004"));

        Disciplina design = criarOptativa("DES001", "Design Gráfico", 30);

        Disciplina partitura = criarOptativa("MUS003", "Partitura 1", 30);

        Disciplina cifra = criarOptativa("MUS004", "Cifra 1", 30);
    }

    private static Disciplina criarObrigatoria(String codigo, String nome, int cargaHoraria) {
        Disciplina disciplina = new DisciplinaObrigatoria(codigo, nome, cargaHoraria);
        disciplinas.put(codigo, disciplina);
        return disciplina;
    }

    private static Disciplina criarEletiva(String codigo, String nome, int cargaHoraria) {
        Disciplina disciplina = new DisciplinaObrigatoria(codigo, nome, cargaHoraria);
        disciplinas.put(codigo, disciplina);
        return disciplina;
    }

    private static Disciplina criarOptativa(String codigo, String nome, int cargaHoraria) {
        Disciplina disciplina = new DisciplinaOptativa(codigo, nome, cargaHoraria);
        disciplinas.put(codigo, disciplina);
        return disciplina;
    }

    private static void configurarCodependencia (Disciplina d1, Disciplina d2) {
        d1.adicionarCoRequisito(d2.getCodigo());
        d2.adicionarCoRequisito(d1.getCodigo());
    }

    public static Disciplina getDisciplina(String codigo) {
        return disciplinas.get(codigo);
    }
}

