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


public class GameFactory {
    
    public static Hero createHero() {
        HeroDefinition def = Data.heroDefinition;
        return new Hero(def.name(), def.health(), def.energy());
    }

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

    public static BuyPile createBuyPile(Publisher publisher, List<CardDefinition> cardDefinitions) {
        BuyPile buyPile = new BuyPile();
        for (CardDefinition def : cardDefinitions) {
            buyPile.push(createCardFromDefinition(def, publisher));
        }
        buyPile.shuffle();
        return buyPile;
    }

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

    public static TreePath createTreePath(Publisher publisher) {
        List<List<Event>> gamePath = new ArrayList<>();
        for (NodeDefinition nodeDefinition : Data.nodeDefinitions ) {
            gamePath.add(createEvents(nodeDefinition, publisher));
        }
        return new TreePath(gamePath);
    }
}
