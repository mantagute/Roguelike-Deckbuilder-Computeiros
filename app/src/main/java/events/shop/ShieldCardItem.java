package events.shop;

import cards.ShieldCard;
import deck.BuyPile;
import entities.Hero;

/**
 * Item de loja que adiciona uma {@link cards.ShieldCard} ao baralho do herói
 * quando comprado.
 */

public class ShieldCardItem implements ShopItem{
    
    private ShieldCard shieldCard;
    private boolean sold = false;

    /**
     * Constrói um item de carta de escudo para venda na loja.
     *
     * @param shieldCard carta de escudo a ser vendida
     */
    public ShieldCardItem(ShieldCard shieldCard) {
        this.shieldCard = shieldCard;
    }

    public String getName(){
        return shieldCard.getName();
    }

    public String getEmoji(){
        return "🛡️";
    }

    public int getPrice() {
        return 25;
    }

    public String getDetails(){
        return shieldCard.getDetails();
    }

    public void purchase(Hero hero, BuyPile buyPile) {
        hero.spendGold(getPrice());
        buyPile.push(shieldCard);
        sold = true;
    }

    public boolean isSold() {
        return sold;
    }

}
