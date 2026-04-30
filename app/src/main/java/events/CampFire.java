package events;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import deck.BuyPile;
import deck.DiscardPile;
import entities.Hero;
import events.campfire.CampFireAction;
import events.campfire.Rest;
import events.campfire.UpgradeCard;
import gameOrchestrator.UserInterface;


/**
 * Evento de fogueira — permite ao herói descansar ou melhorar uma carta entre batalhas.
 *
 * <p>Ao ser iniciado, apresenta ao jogador duas opções via {@link events.campfire.CampFireAction}:
 * descansar ({@link events.campfire.Rest}) ou forjar ({@link events.campfire.UpgradeCard}).
 *
 * <p>Implementa o padrão <b>Strategy</b>: cada ação da fogueira é encapsulada
 * em uma implementação distinta de {@link events.campfire.CampFireAction},
 * permitindo adicionar novas ações sem modificar esta classe.
 */

public class CampFire extends Event{

        /**
         * Inicializa o evento de fogueira, apresentando ao jogador as ações
         * disponíveis e executando a escolhida.
         *
         * @param hero        herói controlado pelo jogador
         * @param buyPile     pilha de compra do herói
         * @param discardPile pilha de descarte do herói
         * @param scanner     leitor de entrada do terminal
         * @return {@link EventResult#CONTINUE} sempre, pois a fogueira nunca
         *         encerra o jogo
         */
        public EventResult initializeEvent(Hero hero, BuyPile buyPile, DiscardPile discardPile, Scanner scanner) {
            List<CampFireAction> actions = new ArrayList<>();
            actions.add(new Rest());
            actions.add(new UpgradeCard());

            UserInterface.printCampFireOptions(actions);
            UserInterface.printChoicePrompt();

            int choice = scanner.nextInt();

            if (choice >=1 && choice <= actions.size()) {
                CampFireAction campFireActionChosen = actions.get(choice - 1);
                campFireActionChosen.execute(hero, buyPile, discardPile, scanner);
            }
            
            return EventResult.CONTINUE;
        }

        /**
         * Retorna a prévia textual deste evento para exibição no mapa de progressão.
         *
         * @return string {@code "🔥 Fogueira"}
         */
        public String getPreview() {
            return "🔥 Fogueira";
        }
}
