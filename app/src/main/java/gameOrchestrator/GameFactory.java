package gameOrchestrator;

import java.util.ArrayList;
import java.util.List;

import gameOrchestrator.Data.CardDefinition;
import gameOrchestrator.Data.EnemyDefinition;
import gameOrchestrator.Data.HeroDefinition;
import gameOrchestrator.Data.NodeDefinition;
import gameOrchestrator.Data.NodeDefinition.NodeEventDefinition;
import gamePath.TreePath;
import gameOrchestrator.Data.ChoiceDefinition.OptionDefinition;
import gameOrchestrator.Data.ChoiceDefinition;
import cards.DamageCard;
import cards.EffectCard;
import cards.ShieldCard;
import deck.BuyPile;
import cards.Card;
import entities.Enemy;
import entities.Hero;
import entities.enemies.Azoide;
import entities.enemies.Bzoide;
import events.Event;
import events.Shop;
import events.Battle;
import events.CampFire;
import events.Choice;
import events.choice.ChoiceOption;
import events.choice.DamageOption;
import events.choice.HealOption;
import observer.Publisher;


/**
 * Fábrica central responsável por instanciar todas as entidades e estruturas
 * do jogo a partir das definições declarativas em {@link Data}.
 *
 * <p>Segue o padrão de projeto <b>Factory Method</b>, centralizando a lógica
 * de criação e desacoplando os consumidores (como {@link App} e {@link events.Battle})
 * das classes concretas que instanciam.
 *
 * <p>Não deve ser instanciada — todos os métodos são estáticos.
 *
 * @see Data
 */

public class GameFactory {
    
    /**
     * Cria e retorna o herói do jogo a partir de {@link Data#heroDefinition}.
     *
     * @return instância de {@link Hero} configurada com nome, vida e energia padrão
     */
    public static Hero createHero() {
        HeroDefinition def = Data.heroDefinition;
        return new Hero(def.name(), def.health(), def.energy());
    }

    /**
     * Cria uma carta a partir de uma {@link Data.CardDefinition}.
     * O tipo concreto ({@link cards.DamageCard}, {@link cards.ShieldCard} ou
     * {@link cards.EffectCard}) é determinado pelo campo {@code type} da definição.
     *
     * @param def       definição declarativa da carta
     * @param publisher Publisher necessário para inscrição de {@link cards.EffectCard}
     *                  no sistema Observer; ignorado para outros tipos
     * @return carta instanciada conforme a definição
     * @throws IllegalArgumentException se o tipo da carta for desconhecido
     */
    public static Card createCardFromDefinition(CardDefinition def, Publisher publisher) {
        switch (def.type()) {
            case DAMAGE:
                return new DamageCard(def.name(), def.energyCost(), def.effectValue(), def.description(), def.multiTarget());
            case SHIELD:
                return new ShieldCard(def.name(), def.energyCost(), def.effectValue(), def.description(), def.multiTarget());
            case EFFECT:
                return new EffectCard(def.name(), def.energyCost(), def.description(), def.effectType(), def.effectValue(), def.selfTarget(), publisher, def.multiTarget());
            default:
                throw new IllegalArgumentException("Unknown card type: " + def.type());
        }
    }

    /**
     * Cria e retorna a lista de cartas de dano disponíveis na loja,
     * a partir de {@link Data#shopDamageCardsDefinitions}.
     *
     * @return lista de {@link cards.DamageCard} para uso na loja
     */
    public static List<DamageCard> createShopDamageCards() {
        List<DamageCard> damageCards = new ArrayList<>();
        for (CardDefinition shopDamageCardDefinition : Data.shopDamageCardsDefinitions) {
            damageCards.add(new DamageCard(shopDamageCardDefinition.name(), shopDamageCardDefinition.energyCost(), shopDamageCardDefinition.effectValue(), shopDamageCardDefinition.description(), shopDamageCardDefinition.multiTarget()));
        }
        return damageCards;
    }
    
    /**
     * Cria e retorna a lista de cartas de escudo disponíveis na loja,
     * a partir de {@link Data#shopShieldCardsDefinitions}.
     *
     * @return lista de {@link cards.ShieldCard} para uso na loja
     */
    public static List<ShieldCard> createShopShieldCards() {
        List<ShieldCard> shieldCards = new ArrayList<>();
        for (CardDefinition shopShieldCardDefinition : Data.shopShieldCardsDefinitions) {
            shieldCards.add(new ShieldCard(shopShieldCardDefinition.name(), shopShieldCardDefinition.energyCost(), shopShieldCardDefinition.effectValue(), shopShieldCardDefinition.description(), shopShieldCardDefinition.multiTarget()));
        }
        return shieldCards;
    }

    /**
     * Cria uma {@link deck.BuyPile} populada e embaralhada a partir de uma lista
     * de definições de cartas. Cada definição é convertida via
     * {@link #createCardFromDefinition}.
     *
     * @param publisher       Publisher para inscrição de cartas de efeito no Observer
     * @param cardDefinitions lista de definições das cartas a inserir na pilha
     * @return pilha de compra pronta para uso
     */
    public static BuyPile createBuyPile(Publisher publisher, List<CardDefinition> cardDefinitions) {
        BuyPile buyPile = new BuyPile();
        for (CardDefinition def : cardDefinitions) {
            buyPile.push(createCardFromDefinition(def, publisher));
        }
        buyPile.shuffle();
        return buyPile;
    }

    /**
     * Busca uma carta do baralho do herói pelo nome e a instancia a partir
     * das definições em {@link Data}. Utilizado ao reconstruir o baralho
     * durante o carregamento de um save.
     *
     * @param name      nome exato da carta a ser buscada
     * @param publisher Publisher para inscrição de cartas de efeito no Observer
     * @return carta correspondente ao nome, ou {@code null} se não encontrada
     */
    public static Card createCardbyName(String name, Publisher publisher) {
        for (CardDefinition card : Data.heroDamageCardsDefinitions) {
            if (card.name().equals(name)) {
                return createCardFromDefinition(card, publisher);
            }
        }
        for (CardDefinition card : Data.heroShieldCardsDefinitions) {
            if (card.name().equals(name)) {
                return createCardFromDefinition(card, publisher);
            }
        }
        for (CardDefinition card : Data.heroEffectCardsDefinitions) {
            if (card.name().equals(name)) {
                return createCardFromDefinition(card, publisher);
            }
        }
        return null;
    }

    /**
     * Instancia um inimigo a partir de uma {@link Data.EnemyDefinition},
     * inicializando seu Publisher e baralho via {@link entities.Enemy#initializePublisher}.
     *
     * @param enemyDefinition definição declarativa do inimigo
     * @param publisher       Publisher central do jogo para inscrição de efeitos
     * @return inimigo instanciado e pronto para combate
     * @throws IllegalArgumentException se o tipo do inimigo for desconhecido
     */
    public static Enemy createEnemyFromDefinition(EnemyDefinition enemyDefinition, Publisher publisher){
        switch (enemyDefinition.type()) {
            case AZOIDE:
                Azoide azoide = new Azoide(enemyDefinition.name(), enemyDefinition.health(), enemyDefinition.energy());
                azoide.initializePublisher(publisher);
                return azoide;
            case BZOIDE:
                Bzoide bzoide = new Bzoide(enemyDefinition.name(), enemyDefinition.health(), enemyDefinition.energy());
                bzoide.initializePublisher(publisher);
                return bzoide;
            default:
                throw new IllegalArgumentException("Unknown enemy type: " + enemyDefinition.type());
        }
    }   

    /**
     * Constrói um evento {@link events.Choice} a partir de uma {@link Data.ChoiceDefinition},
     * instanciando as opções concretas ({@link events.choice.HealOption} ou
     * {@link events.choice.DamageOption}) conforme o tipo de cada opção.
     *
     * @param choiceDefinition definição declarativa da escolha
     * @return evento {@link events.Choice} pronto para ser adicionado ao mapa
     * @throws IllegalArgumentException se o tipo de opção for desconhecido
     */
    public static Choice createChoiceFromDefinition(ChoiceDefinition choiceDefinition) {
        List<ChoiceOption> choiceOptions = new ArrayList<>();
        for(OptionDefinition optionDefinition : choiceDefinition.options()) {
            switch (optionDefinition.type()) {
                case HEAL:
                    choiceOptions.add(new HealOption(optionDefinition.action(), optionDefinition.feedback()));
                    break;
                case DAMAGE:
                    choiceOptions.add(new DamageOption(optionDefinition.action(), optionDefinition.feedback()));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown option type: " + optionDefinition.type());
            }
        }
        return new Choice(choiceDefinition.lore(), choiceOptions);

    }

    /**
     * Constrói a lista de eventos de um nó do mapa a partir de uma
     * {@link Data.NodeDefinition}, delegando a criação de cada evento
     * ao método correspondente desta fábrica.
     *
     * @param nodeDefinition definição declarativa do nó
     * @param publisher      Publisher central do jogo
     * @return lista de {@link events.Event} prontos para execução no nó
     * @throws IllegalArgumentException se o tipo de evento for desconhecido
     */
    public static List<Event> createEvents(NodeDefinition nodeDefinition, Publisher publisher) {
        List<Event> events = new ArrayList<>();
        for (NodeEventDefinition nodeEventDefinition : nodeDefinition.events()) {
            switch (nodeEventDefinition.type()) {
                case CHOICE:
                    int index = (Integer) nodeEventDefinition.payload();
                    events.add(createChoiceFromDefinition(Data.choiceDefinitions.get(index)));
                    break;
                case CAMPFIRE:
                    events.add(new CampFire());
                    break;
                case SHOP:
                    events.add(new Shop());
                    break;
                case BATTLE:
                    events.add(new Battle( ((List<EnemyDefinition>) nodeEventDefinition.payload())));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown event type: " + nodeEventDefinition.type());
            }
        }
        return events;
    }

    /**
     * Constrói a {@link gamePath.TreePath} completa do jogo a partir de
     * {@link Data#nodeDefinitions}, convertendo cada {@link Data.NodeDefinition}
     * em uma lista de eventos via {@link #createEvents}.
     *
     * @param publisher Publisher central do jogo
     * @return árvore de progressão pronta para navegação
     */
    public static TreePath createTreePath(Publisher publisher) {
        List<List<Event>> gamePath = new ArrayList<>();
        for (NodeDefinition nodeDefinition : Data.nodeDefinitions ) {
            gamePath.add(createEvents(nodeDefinition, publisher));
        }
        return new TreePath(gamePath);
    }
}
