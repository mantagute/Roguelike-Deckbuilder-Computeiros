package events.campfire;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

import deck.BuyPile;
import deck.DiscardPile;
import entities.Hero;
import gameOrchestrator.UserInterface;
import cards.Card;

/**
 * Ação de fogueira que permite ao herói melhorar uma carta do seu baralho.
 * Apresenta 3 cartas aleatórias do baralho (buy pile + discard pile) e
 * aplica {@link cards.Card#upgrade()} na carta escolhida pelo jogador.
 */

public class UpgradeCard implements CampFireAction {

    public String getDescription() {
        return "Forjar: (Melhora os atributos de uma carta do seu baralho)";
    }

    public String getEmoji() {
        return "⚒️";
    }

    public void execute(Hero hero, BuyPile buyPile, DiscardPile discardPile, Scanner scanner) {
        
        List<Card> allCards = new ArrayList<>();

        for (int i = 0 ; i < buyPile.getSize() ; i ++) {
            allCards.add(buyPile.getCard(i));
        }

        for (int i = 0 ; i < discardPile.getSize() ; i ++) {
            allCards.add(discardPile.getCard(i));
        }

        Random random = new Random();

        List<Card> cardsAvailable = new ArrayList<>();

        List<Integer> indexAlreadySorted = new ArrayList<>();

        int counter = 0;

        while(counter < 3) {
            int index = random.nextInt(allCards.size());
            if (indexAlreadySorted.contains(index)) {
                continue;
            }
            else {
                indexAlreadySorted.add(index);
                cardsAvailable.add(allCards.get(index));
                counter++;
            }
        }

        UserInterface.printCardsToUpdate(cardsAvailable);
        UserInterface.printChoicePrompt();

        int choice = scanner.nextInt();
        
        if (choice >=1 && choice <= cardsAvailable.size()) {
            Card CardChosen = cardsAvailable.get(choice - 1);
            CardChosen.upgrade(); 
        }
    }
}
