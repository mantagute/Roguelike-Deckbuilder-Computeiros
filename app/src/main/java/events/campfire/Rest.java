package events.campfire;

import java.util.Scanner;
import deck.BuyPile;
import deck.DiscardPile;
import entities.Hero;

/**
 * Ação de fogueira que permite ao herói descansar, recuperando
 * {@code 35%} da sua vida máxima.
 */

public class Rest implements CampFireAction  {

    public String getDescription() {
        return "Descansar: Recupera 35% da vida máxima do herói.";
    }

    public void execute(Hero hero, BuyPile buypile, DiscardPile discardPile, Scanner scanner) {
        double healAmount = hero.getMaxHealth() * 0.35;
        hero.heal(healAmount);
    }

    public String getEmoji(){
        return "🛌";
    }
}
