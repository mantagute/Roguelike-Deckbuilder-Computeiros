package events;

import java.util.Scanner;

import deck.BuyPile;
import deck.DiscardPile;
import entities.Hero;

/**
 * Classe abstrata base para todos os eventos do mapa de progressão do jogo.
 * Eventos representam qualquer interação que o jogador pode ter ao visitar um nó,
 * como batalhas, lojas, fogueiras ou escolhas narrativas.
 *
 * <p>Cada evento recebe o estado do jogador via {@link #initializeEvent} e retorna
 * um {@link EventResult} indicando se o jogo deve continuar, o herói foi derrotado
 * ou o jogador escolheu sair e salvar.
 */

public abstract class Event {

    /**
     * Possíveis resultados da execução de um evento.
     */
    public enum EventResult {
        CONTINUE,
        DEFEAT,
        QUIT
    }
    
    /**
     * Executa o evento, recebendo o estado atual do jogador e retornando
     * o resultado ao término.
     *
     * @param hero        herói controlado pelo jogador
     * @param buyPile     pilha de compra do herói; persiste entre eventos
     * @param discardPile pilha de descarte do herói; persiste entre eventos
     * @param scanner     leitor de entrada do terminal
     * @return {@link EventResult} indicando o desfecho do evento
     */
    public abstract EventResult initializeEvent(Hero hero, BuyPile buyPile, DiscardPile discardPile, Scanner scanner);

    /**
     * Retorna uma string de prévia do evento para exibição no mapa de progressão,
     * descrevendo brevemente o tipo de evento com emoji e texto.
     *
     * @return string de prévia do evento
     */
    public abstract String getPreview();
}
