package events.campfire;

import deck.DiscardPile;
import deck.BuyPile;
import entities.Hero;
import java.util.Scanner;

/**
 * Interface que representa uma ação disponível na fogueira.
 * Cada implementação define um comportamento distinto que modifica
 * o estado do herói ou do seu baralho.
 *
 * @see events.campfire.Rest
 * @see events.campfire.UpgradeCard
 */

public interface CampFireAction {

     /** @return descrição textual da ação exibida ao jogador */

    String getDescription();

    /**
     * Executa a ação da fogueira sobre o herói e seu baralho.
     *
     * @param hero        herói que está descansando na fogueira
     * @param buyPile     pilha de compra do herói
     * @param discardPile pilha de descarte do herói
     * @param scanner     leitor de entrada do terminal
     */
    void execute(Hero hero, BuyPile buyPile, DiscardPile discardPile, Scanner scanner);

    /** @return emoji representativo da ação para exibição no terminal */
    String getEmoji();
}
