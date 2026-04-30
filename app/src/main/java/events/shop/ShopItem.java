package events.shop;

import deck.BuyPile;
import entities.Hero;

/**
 * Interface que representa um item disponível para compra na loja.
 * Cada implementação define um item específico com nome, preço e efeito
 * de compra sobre o estado do herói.
 *
 * @see events.shop.DamageCardItem
 * @see events.shop.ShieldCardItem
 * @see events.shop.PotionItem
 */

public interface ShopItem {
    
     /** @return nome do item exibido na loja */
    public String getName();

     /** @return emoji representativo do item */
    public String getEmoji();

    /** @return preço do item em ouro */
    public int  getPrice();

    /** @return descrição do efeito ou atributos do item */
    public String getDetails();

    /** @return {@code true} se o item já foi vendido nesta visita à loja */
    public boolean isSold();

    /**
     * Executa a compra do item: desconta o ouro do herói e aplica o efeito.
     *
     * @param hero    herói que está comprando o item
     * @param buyPile pilha de compra do herói; usada para adicionar cartas compradas
     */
    public void purchase(Hero hero, BuyPile buyPile);

}


