package events;


import java.util.Scanner;
import java.util.Collections;
import java.util.List;

import deck.BuyPile;
import deck.DiscardPile;
import entities.Hero;
import events.choice.ChoiceOption;
import gameOrchestrator.GameUtils;
import gameOrchestrator.UserInterface;

/**
 * Evento de escolha narrativa — apresenta ao jogador uma situação com múltiplas opções,
 * cada uma com consequências distintas sobre o estado do herói.
 *
 * <p>As opções são embaralhadas a cada execução para variar a apresentação.
 */

public class Choice extends Event{

    private String Lore;
    private List<ChoiceOption> choiceOptions;

    /**
     * Constrói um evento de escolha com o lore e as opções especificadas.
     *
     * @param Lore          texto narrativo que descreve a situação ao jogador
     * @param choiceOptions lista de opções disponíveis para o jogador escolher
     */
    public Choice(String Lore, List<ChoiceOption> choiceOptions) {
        this.Lore = Lore;
        this.choiceOptions = choiceOptions;
    }

    /**
     * Embaralha as opções, exibe o lore e aguarda a escolha do jogador,
     * executando o efeito da opção selecionada sobre o herói.
     *
     * @param hero        herói que sofrerá o efeito da opção escolhida
     * @param buyPile     pilha de compra do herói
     * @param discardPile pilha de descarte do herói
     * @param scanner     leitor de entrada do terminal
     * @return {@link EventResult#CONTINUE} sempre
     */

    public EventResult initializeEvent(Hero hero, BuyPile buyPile, DiscardPile discardPile, Scanner scanner ) {
        
        Collections.shuffle(choiceOptions);
        UserInterface.printChoiceLore(Lore);
        UserInterface.printChoiceOptions(choiceOptions);
        UserInterface.printChoicePrompt();

        int choice = scanner.nextInt();

        if (choice >= 1 && choice <= choiceOptions.size()) {
            choiceOptions.get(choice - 1).execute(hero, buyPile, discardPile, scanner);
            UserInterface.printChoiceFeedback(choiceOptions.get(choice - 1));
            GameUtils.Wait(2500);
        }
        return EventResult.CONTINUE;
    }
    

    /**
     * Retorna a prévia textual deste evento para exibição no mapa de progressão.
     *
     * @return string {@code "❓ ESCOLHA ❓"}
     */
    public String getPreview() {
        return "❓ ESCOLHA ❓";
    }
}
