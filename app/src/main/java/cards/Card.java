package cards;
import entities.Entity;

/**
 * Classe abstrata que representa uma carta do jogo.
 * Toda carta possui um nome, custo em energia, descrição e informação
 * sobre se atinge múltiplos alvos. Subclasses devem implementar o
 * comportamento específico do uso da carta.
 */
public abstract class Card {

    private String name;
    private int energyCost;
    private String description;
    private boolean multiTarget;

    /**
     * Constrói uma nova carta com os atributos fornecidos.
     *
     * @param name        nome da carta exibido ao jogador
     * @param energyCost  custo em energia para jogar esta carta
     * @param description texto descritivo do efeito ou lore da carta
     * @param multiTarget {@code true} se a carta atinge todos os inimigos vivos;
     *                    {@code false} se atinge um alvo único
     */
    public Card(String name, int energyCost, String description, boolean multiTarget) {
        this.name = name;
        this.energyCost = energyCost;
        this.description = description;
        this.multiTarget = multiTarget;
    }

    /**
     * Retorna o custo em energia necessário para jogar esta carta.
     *
     * @return custo em energia da carta
     */
    public int getEnergyCost() {
        return energyCost;
    }

    /**
     * Retorna o nome da carta.
     *
     * @return nome da carta
     */
    public String getName() {
        return name;
    }

    /**
     * Retorna a descrição textual da carta.
     *
     * @return descrição da carta
     */
    public String getDescription() {
        return description;
    }

    /**
     * Indica se esta carta atinge múltiplos alvos simultaneamente.
     *
     * @return {@code true} se a carta é multi-alvo; {@code false} caso contrário
     */
    public boolean isMultiTarget() {
        return multiTarget;
    }

    /**
     * Indica se esta carta tem como alvo o próprio usuário.
     * Por padrão retorna {@code false}; subclasses podem sobrescrever.
     *
     * @return {@code true} se a carta atinge o próprio usuário
     */
    public boolean isSelfTarget() {
        return false;
    }

    /**
     * Retorna uma string com os detalhes numéricos da carta
     * (ex.: dano, escudo ou tipo de efeito).
     *
     * @return string formatada com os detalhes da carta
     */
    public abstract String getDetails();

    /**
     * Executa o efeito desta carta, aplicando-o sobre o alvo.
     *
     * @param user   entidade que está jogando a carta
     * @param target entidade que receberá o efeito da carta
     */
    public abstract void useCard(Entity user, Entity target);

    /**
     * Retorna o valor numérico principal do efeito desta carta
     * (dano, escudo ou quantidade de acúmulos de efeito).
     *
     * @return valor base do efeito da carta
     */
    public abstract double getEffectValue();

    /**
     * Melhora os atributos numéricos desta carta (dano, escudo ou acúmulos de efeito),
     * aplicando um bônus percentual sobre o valor atual.
     * Chamado pela {@link events.campfire.UpgradeCard} durante o evento de fogueira.
     */
    public abstract void upgrade();
}
