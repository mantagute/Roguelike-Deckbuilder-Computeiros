package events.choice;

import entities.Hero;

import java.util.Scanner;

import deck.BuyPile;
import deck.DiscardPile;

/**
 * Opção de escolha que recupera vida do herói ao ser selecionada.
 * Restaura {@code 10%} da vida máxima do herói.
 */

public class HealOption implements ChoiceOption{

    private String action;
    private String feedback;

    /**
     * Constrói uma opção de cura com o texto de ação e feedback especificados.
     *
     * @param action   texto descritivo exibido ao jogador antes da escolha
     * @param feedback texto de consequência exibido após a escolha
     */

    public HealOption(String action, String feedback) {
        this.action = action;
        this.feedback = feedback;
    }

    public String getAction() {
        return action;
    }

    public String getFeedback() {
        return feedback;
    }
    
    public String getEmoji(){
        return "❤️";
    }

    public void execute(Hero hero, BuyPile buyPile, DiscardPile discardPile, Scanner scanner) {
        hero.heal(hero.getMaxHealth() * 0.1);
    }
}
