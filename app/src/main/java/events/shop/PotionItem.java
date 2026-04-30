package events.shop;

import deck.BuyPile;
import entities.Hero;

/**
 * Item de loja que cura imediatamente {@code 30%} da vida máxima do herói
 * quando comprado. Consumida no ato da compra, sem necessidade de uso manual.
 */

public class PotionItem implements ShopItem{

    private boolean sold = false;

    public String getName(){
        return "Poção de Cura";
    }

    public String getEmoji(){
        return "🧪";
    }

    public int getPrice() {
        return 20;
    }

    public String getDetails(){
        return "Cura 30% da vida máxima";
    }

    public void purchase(Hero hero, BuyPile buyPile) {
        hero.spendGold(getPrice());
        hero.heal(hero.getMaxHealth() * 0.3);
        sold = true;
    }

    public boolean isSold() {
        return sold;
    }
    
}

