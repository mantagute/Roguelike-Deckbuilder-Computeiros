package gamePath;

import java.util.ArrayList;
import java.util.List;

import events.Event;

import gameOrchestrator.Data.EnemyDefinition;

/**
 * Representa um nó da árvore de progressão do jogo.
 * Cada nó contém uma lista de inimigos que o jogador enfrentará
 * ao chegar nesse ponto do mapa, além de referências para os
 * próximos nós disponíveis (esquerda e direita).
 *
 * <p>A navegação na árvore é sempre unidirecional — o jogador
 * só pode avançar para nós filhos, nunca retornar.
 */

public class Node {
    private List<Event> events;
    private Node left, right;

    /**
     * Constrói um nó com a lista de eventos especificada.
     * Os ponteiros para os nós filhos são inicializados como {@code null}.
     *
     * @param events lista de eventos presentes neste nó do mapa
     */

    public Node(List<Event> events) {
        this.events = new ArrayList<Event>(events);
        left = right = null;
    }

    /**
     * Retorna a lista de definições de inimigos deste nó.
     *
     * @return lista imutável de {@link EnemyDefinition}
     */

    public List<Event> getEvents() {
        return events;
    }

    /**
     * Retorna o nó filho à esquerda, representando um dos
     * caminhos disponíveis após vencer este nó.
     *
     * @return nó à esquerda, ou {@code null} se não existir
     */

    public Node getLeftNode() {
        return left;
    }

    /**
     * Retorna o nó filho à direita, representando um dos
     * caminhos disponíveis após vencer este nó.
     *
     * @return nó à direita, ou {@code null} se não existir
     */

    public Node getRightNode() {
        return right;
    }

    /**
     * Define o nó filho à esquerda desta posição no mapa.
     * Visível apenas dentro do pacote {@code gamePath}.
     *
     * @param left nó a ser conectado como filho esquerdo
     * @return o nó recém-conectado à esquerda (retorna o próprio parâmetro {@code left})
     */
    protected Node setLeftNode(Node left) {
    this.left = left;
    return left;
    }

    /**
     * Define o nó filho à direita desta posição no mapa.
     * Visível apenas dentro do pacote {@code gamePath}.
     *
     * @param right nó a ser conectado como filho direito
     * @return o nó recém-conectado à direita (retorna o próprio parâmetro {@code right})
     */
    protected Node setRightNode(Node right) {
        this.right = right;
        return right;
    }
}
