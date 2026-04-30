package entities;

/**
 * Representa o herói controlado pelo jogador.
 * Herda todos os comportamentos de combate de {@link Entity},
 * sendo controlado diretamente pelas escolhas do usuário durante o turno.
 */
public class Hero extends Entity {

    private int gold = 0;

    /**
     * Constrói um herói com os atributos base especificados.
     *
     * @param name   nome do herói
     * @param health pontos de vida iniciais
     * @param energy energia máxima por turno
     */
    public Hero(String name, int health, int energy) {
        super(name, health, energy);
    }

    /**
     * Adiciona a quantidade especificada de ouro ao total do herói.
     *
     * @param amount quantidade de ouro a adicionar; deve ser não-negativo
     */
    public void addGold(int amount) {
        gold = gold + amount;
    }

    /**
     * Retorna o total de ouro atual do herói.
     *
     * @return quantidade de ouro disponível
     */
    public int getGold() {
        return gold;
    }

    /**
     * Desconta a quantidade especificada do ouro do herói.
     * Não verifica saldo — o chamador é responsável por garantir
     * que o herói possui ouro suficiente antes de chamar este método.
     *
     * @param amount quantidade de ouro a descontar; deve ser não-negativo
     */
    public void spendGold(int amount) {
        gold = gold - amount;
    }
}
