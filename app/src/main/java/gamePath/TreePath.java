package gamePath;

import java.util.List;
import events.Event;

/**
 * Representa o mapa de progressão do jogo como uma árvore binária.
 * A árvore é construída a partir de uma lista de grupos de inimigos,
 * onde o elemento central da lista vira a raiz, e as metades
 * esquerda e direita formam as subárvores recursivamente.
 *
 * <p>Essa estrutura garante que o jogador sempre avance para nós
 * mais profundos, nunca retrocedendo no mapa.
 */
public class TreePath {
    Node root;

    /**
     * Constrói a árvore de progressão a partir da lista de eventos por nó.
     * O elemento central da lista torna-se a raiz; as metades esquerda
     * e direita formam as subárvores de forma recursiva.
     *
     * @param events lista de listas de eventos, onde cada lista representa
     *               os eventos de um nó do mapa
     */
    public TreePath(List<List<Event>> events) {
        this.root = listToTree(events, 0, events.size() - 1);
    }

    /**
     * Constrói recursivamente a subárvore a partir do intervalo
     * {@code [start, end]} da lista de eventos.
     * O elemento central do intervalo torna-se a raiz da subárvore.
     *
     * @param events lista completa de listas de eventos por nó
     * @param start  índice inicial do intervalo atual
     * @param end    índice final do intervalo atual
     * @return raiz da subárvore construída, ou {@code null} se {@code start > end}
     */
    private Node listToTree(List<List<Event>> events, int start, int end) {
        if (start > end) {
            return null;
        }

        int mid = (start + end) / 2;
        Node node = new Node(events.get(mid));

        node.setLeftNode(listToTree(events, start, mid - 1));
        node.setRightNode(listToTree(events, mid + 1, end));

        return node;
    }

    /**
     * Retorna o nó raiz da árvore de progressão, que representa
     * a batalha inicial do jogo.
     *
     * @return nó raiz da árvore
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Navega a árvore seguindo a sequência de direções fornecida e retorna
     * o nó correspondente ao final do caminho — ou seja, o nó da próxima
     * batalha a ser enfrentada após carregar o save.
     *
     * <p>Se uma direção apontar para um filho {@code null} (beco sem saída),
     * a navegação para naquele ponto e o nó atual é retornado, evitando
     * {@code NullPointerException}.
     *
     * @param path lista ordenada de direções ({@code "left"} ou {@code "right"})
     *             que representa o caminho percorrido até o save
     * @return nó ao final do caminho; nunca {@code null}
     */
    public Node getNodeBeforeSave(List<String> path) {
        Node currentNode = root;
        for (String direction : path) {
            if (direction.equals("left")) {
                if (currentNode.getLeftNode() != null) {
                    currentNode = currentNode.getLeftNode();
                } else {
                    return currentNode;
                }
            } else if (direction.equals("right")) {
                if (currentNode.getRightNode() != null) {
                    currentNode = currentNode.getRightNode();
                } else {
                    return currentNode;
                }
            }
        }
        return currentNode;
    }
}

