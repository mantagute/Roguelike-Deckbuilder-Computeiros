package gameOrchestrator;

import java.util.List;

/**
 * Representa o estado mínimo necessário para retomar uma partida salva.
 *
 * <p>Um {@code SaveState} é criado por {@link App#buildSaveState()} ao salvar
 * e sair durante o combate, ou após avançar de nó, sendo serializado em disco
 * por {@link SaveManager}. Ao reabrir o jogo, ele é desserializado e aplicado
 * ao estado atual via {@link App#loadGame()}.
 *
 * <p>O estado capturado inclui a vida do herói, o baralho completo, o caminho
 * percorrido na árvore de progressão e o índice do próximo evento a executar
 * no nó atual, garantindo que o jogador retome exatamente de onde parou.
 *
 * @see SaveManager
 * @see App#buildSaveState()
 * @see App#loadGame()
 */
public class SaveState {

    /** Pontos de vida do herói no momento em que o save foi criado. */
    private double heroHealth;

    /**
     * Nomes de todas as cartas do baralho do herói (buy pile + discard pile)
     * no momento do save. Usados para reconstruir o baralho via {@link Data#getCardbyName}.
     */
    private List<String> deckCardNames;

    /**
     * Sequência de direções percorridas na árvore de progressão até o momento do save
     * (ex.: {@code ["left", "right", "left"]}). Usada para reposicionar o jogador
     * no nó correto ao carregar o jogo.
     */
    private List<String> pathTaken;

    /** Índice do próximo evento a executar no nó atual no momento do save. */
    private int eventIndex;

    /**
     * Constrói um novo {@code SaveState} com os dados do estado atual do jogo.
     *
     * @param heroHealth    pontos de vida atuais do herói
     * @param deckCardNames nomes de todas as cartas do baralho (buy pile + discard pile)
     * @param pathTaken     lista ordenada de direções percorridas na árvore de progressão
     * @param eventIndex    índice do próximo evento a executar no nó atual
     */
    public SaveState(double heroHealth, List<String> deckCardNames, List<String> pathTaken, int eventIndex) {
        this.heroHealth = heroHealth;
        this.deckCardNames = deckCardNames;
        this.pathTaken = pathTaken;
        this.eventIndex = eventIndex;
    }

    /**
     * Retorna os pontos de vida do herói salvos.
     *
     * @return vida do herói no momento do save
     */
    public double getHeroHealth() {
        return heroHealth;
    }

    /**
     * Retorna os nomes de todas as cartas do baralho salvo.
     *
     * @return lista de nomes de cartas (buy pile + discard pile)
     */
    public List<String> getDeckCardNames() {
        return deckCardNames;
    }

    /**
     * Retorna a sequência de direções percorridas na árvore de progressão.
     *
     * @return lista ordenada de direções ({@code "left"} ou {@code "right"})
     */
    public List<String> getPathTaken() {
        return pathTaken;
    }

    /**
     * Retorna o índice do próximo evento a executar no nó atual.
     *
     * @return índice do evento a retomar ao carregar o jogo
     */
    public int getEventIndex() { 
        return eventIndex; 
    }
}
