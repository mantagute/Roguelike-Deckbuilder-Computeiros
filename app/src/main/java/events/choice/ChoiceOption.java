package events.choice;

import java.util.Scanner;

import entities.Hero;
import deck.BuyPile;
import deck.DiscardPile;

/**
 * Interface que representa uma opção de escolha narrativa no jogo.
 * Cada implementação define uma ação executada sobre o estado do herói
 * ao ser selecionada pelo jogador.
 *
 * @see events.choice.HealOption
 * @see events.choice.DamageOption
 */

public interface ChoiceOption {

    /**
     * Retorna o texto descritivo da ação exibido ao jogador.
     *
     * @return descrição da opção
     */
    public String getAction();
    
    /**
     * Retorna o texto de feedback exibido após a escolha ser feita.
     *
     * @return texto de consequência da opção
     */
    public String getFeedback();

    /**
     * Retorna o emoji associado a esta opção para exibição no terminal.
     *
     * @return emoji representativo da opção
     */
    public String getEmoji();

    /**
     * Executa o efeito desta opção sobre o herói.
     *
     * @param hero        herói que sofrerá o efeito
     * @param buyPile     pilha de compra do herói
     * @param discardPile pilha de descarte do herói
     * @param scanner     leitor de entrada do terminal
     */
    public void execute(Hero hero, BuyPile buyPile, DiscardPile discardPile, Scanner scanner);
}
