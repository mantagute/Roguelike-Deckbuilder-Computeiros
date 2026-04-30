package entities;

import cards.Card;
import deck.BuyPile;
import deck.DiscardPile;
import deck.Hand;
import effects.Effect;
import effects.Effect.EffectType;
import effects.Poison;
import effects.Strength;
import observer.Publisher;
import java.util.StringJoiner;
import java.util.ArrayList;

/**
 * Classe abstrata base para todos os personagens do jogo (heróis e inimigos).
 * Gerencia atributos fundamentais de combate: vida, escudo, energia, mão de cartas
 * e efeitos de status ativos. Subclasses implementam comportamentos específicos
 * de heróis ({@link Hero}) e inimigos ({@link Enemy}).
 */
public abstract class Entity {

    private String name;
    private double health;
    private double maxHealth;
    private double currentShield;
    private int currentEnergy;
    private int maxEnergy;
    private Hand hand;
    private ArrayList<Effect> effects = new ArrayList<>();

    /**
     * Constrói uma entidade com os atributos base especificados.
     *
     * @param name   nome do personagem exibido no jogo
     * @param health pontos de vida iniciais
     * @param energy quantidade máxima de energia por turno
     */
    public Entity(String name, double health, int energy) {
        this.name = name;
        this.health = health;
        this.maxHealth = health;
        this.currentEnergy = energy;
        this.maxEnergy = energy;
        this.currentShield = 0;
        this.hand = new Hand();
    }

    /**
     * Aplica um efeito de status à entidade. Se a entidade já possui o efeito,
     * adiciona os acúmulos ao efeito existente em vez de criar uma nova instância.
     *
     * @param type      tipo do efeito a aplicar ({@link EffectType})
     * @param balance   quantidade de acúmulos a adicionar
     * @param publisher Publisher para inscrição do efeito no sistema Observer
     */
    public void applyEffect(EffectType type, double balance, Publisher publisher) {
        for (Effect effect : effects) {
            if (effect.getType() == type) {
                effect.addBalance(balance);
                return;
            }
        }
        Effect newEffect;
        switch (type) {
            case POISON:
                newEffect = new Poison(this, balance, publisher);
                break;
            case STRENGTH:
                newEffect = new Strength(this, balance, publisher);
                break;
            default:
                return;
        }
        effects.add(newEffect);
    }

    /**
     * Remove todos os efeitos de status ativos da entidade sem desinscrever
     * os respectivos {@link effects.Effect} do {@link Publisher}.
     * Chamado ao iniciar uma nova fase, após o Publisher já ter sido resetado,
     * para garantir que a lista interna fique consistente com o novo barramento.
     */
    public void clearEffects() {
        effects.clear();
    }

    /**
     * Aplica o multiplicador do efeito Strength sobre um valor base.
     * Se a entidade não possui Strength ativo, retorna o valor original.
     *
     * @param baseValue valor base de dano ou escudo a ser multiplicado
     * @return valor base multiplicado pelos acúmulos de Strength, ou o próprio valor base
     *         se não houver Strength ativo
     */
    public double applyEffectMultiplier(double baseValue) {
        for (Effect effect : effects) {
            if (effect.getType() == Effect.EffectType.STRENGTH) {
                return baseValue * effect.getBalance();
            }
        }
        return baseValue;
    }

    /**
     * Remove da lista interna os efeitos cujos acúmulos chegaram a zero.
     * Deve ser chamado após notificações de fim de turno, já que é durante
     * a notificação que os efeitos reduzem seus próprios acúmulos e se
     * desinscrevem do Publisher — mas não se removem desta lista.
     */
    public void manageEffects() {
        effects.removeIf(effect -> effect.getBalance() <= 0);
    }

    /**
     * Retorna uma string legível com todos os efeitos ativos da entidade.
     *
     * @return string descrevendo os efeitos ativos, ou {@code "Sem efeitos ativos"} se não houver nenhum
     */
    public String getEffectString() {
        if (effects.isEmpty()) {
            return "Sem efeitos ativos";
        }
        StringJoiner joiner = new StringJoiner(" | ");
        for (Effect effect : effects) {
            joiner.add(effect.getString());
        }
        return joiner.toString();
    }

    /**
     * Usa a carta no índice especificado da mão, aplicando seu efeito sobre o alvo.
     * Desconta o custo em energia, executa o efeito da carta e a move para a pilha
     * de descarte. Não faz nada se a energia atual for insuficiente para a carta.
     *
     * @param index       índice da carta na mão (base 0)
     * @param target      entidade alvo da carta
     * @param discardPile pilha de descarte para onde a carta irá após ser usada
     */
    public void useCard(int index, Entity target, DiscardPile discardPile) {
        Card cardToUse = hand.getCard(index);
        if (currentEnergy >= cardToUse.getEnergyCost()) {
            currentEnergy = currentEnergy - cardToUse.getEnergyCost();
            cardToUse.useCard(this, target);
            System.out.println(getName() + " usou " + cardToUse.getName() + "! " + cardToUse.getDescription());
            hand.extractCard(index);
            discardPile.push(cardToUse);
        }
    }

    /**
     * Aplica dano à entidade. O dano é absorvido pelo escudo primeiro;
     * o restante é descontado diretamente da vida.
     *
     * @param damage quantidade de dano recebido
     */
    public void receiveDamage(double damage) {
        if (currentShield > damage) {
            currentShield = currentShield - damage;
        } else {
            health = health - (damage - currentShield);
            currentShield = 0;
        }
    }

    /**
     * Adiciona a quantidade especificada ao escudo atual da entidade.
     *
     * @param shield quantidade de escudo a adicionar
     */
    public void receiveShield(double shield) {
        currentShield = currentShield + shield;
    }

    /**
     * Verifica se a entidade tem energia suficiente para jogar ao menos uma carta da mão.
     *
     * @param energy quantidade de energia a verificar
     * @return {@code true} se houver pelo menos uma carta jogável com esta energia
     */
    public boolean hasEnoughEnergyForAnyCard(int energy) {
        return energy >= hand.getMinimumEnergyCost();
    }

    /**
     * Verifica se a entidade tem energia suficiente para jogar ao menos uma carta,
     * usando a energia atual como referência.
     *
     * @return {@code true} se houver pelo menos uma carta jogável com a energia atual
     */
    public boolean hasEnoughEnergyForAnyCard() {
        return hasEnoughEnergyForAnyCard(getEnergy());
    }

    /**
     * Zera o escudo atual da entidade. Chamado no início de cada turno,
     * pois o escudo não é acumulado entre turnos.
     */
    public void resetShield() {
        currentShield = 0;
    }

    /**
     * Inicia um novo turno para esta entidade: remove efeitos expirados,
     * restaura a energia máxima, descarta a mão atual e compra
     * {@link Hand#MAX_HAND_SIZE} novas cartas da pilha de compra.
     *
     * @param buyPile     pilha de compra da entidade
     * @param discardPile pilha de descarte da entidade
     */
    public void newTurn(BuyPile buyPile, DiscardPile discardPile) {
        currentEnergy = maxEnergy;
        manageEffects();
        hand.moveAllCardsTo(discardPile);
        while (getHandSize() < Hand.MAX_HAND_SIZE) {
            Card drawnCard = buyPile.drawCard(discardPile);
            if (drawnCard != null) {
                hand.push(drawnCard);
            }
        }
    }

    /**
     * Verifica se a entidade ainda está viva (vida acima de zero).
     *
     * @return {@code true} se a vida for maior que zero
     */
    public boolean isAlive() {
        return health > 0;
    }

    /**
     * Retorna o número de cartas atualmente na mão da entidade.
     *
     * @return tamanho da mão
     */
    public int getHandSize() {
        return hand.getSize();
    }

    /**
     * Retorna a carta no índice especificado da mão sem removê-la.
     *
     * @param index índice da carta na mão (base 0)
     * @return carta no índice especificado
     */
    public Card getCardFromHand(int index) {
        return hand.getCard(index);
    }

    /**
     * Retorna os pontos de vida atuais da entidade.
     *
     * @return vida atual
     */
    public double getHealth() {
        return health;
    }

    /**
     * Retorna o índice de uma carta específica na mão da entidade.
     *
     * @param carta carta a ser localizada na mão
     * @return índice da carta na mão (base 0), ou {@code -1} se não for encontrada
     */
    protected int getCardIndex(Card carta) {
        for (int i = 0; i < getHandSize(); i++) {
            if (getCardFromHand(i) == carta) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Retorna o escudo atual da entidade.
     *
     * @return escudo atual
     */
    public double getShield() {
        return currentShield;
    }

    /**
     * Retorna o nome da entidade.
     *
     * @return nome do personagem
     */
    public String getName() {
        return name;
    }

    /**
     * Retorna a energia atual da entidade.
     *
     * @return energia disponível no turno atual
     */
    public int getEnergy() {
        return currentEnergy;
    }

    /**
     * Retorna o total máximo de pontos de vida desta entidade.
     *
     * @return vida máxima da entidade
     */
    public double getMaxHealth() {
        return maxHealth;
    }

    /**
     * Define diretamente os pontos de vida da entidade, sobrescrevendo o valor atual.
     * Usado exclusivamente para restaurar o estado do herói ao carregar um save
     * via {@link gameOrchestrator.App#loadGame()}.
     *
     * @param health novo valor de vida a ser atribuído
     */
    public void setHealth(double health) {
        this.health = health;
    }

    /**
     * Restaura pontos de vida da entidade pela quantidade especificada,
     * sem ultrapassar a vida máxima.
     *
     * @param amount quantidade de vida a restaurar; deve ser não-negativo
     */
    public void heal(double amount) {
        health = health + amount;
        if (health > maxHealth) {
            health = maxHealth;
        }
    }
}
