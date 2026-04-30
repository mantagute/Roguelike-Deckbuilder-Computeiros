package gameOrchestrator;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

import gamePath.Node;
import gamePath.TreePath;
import deck.BuyPile;
import deck.DiscardPile;
import entities.Hero;
import events.Battle;
import observer.Publisher;
import events.Event;
import events.Event.EventResult;
import gameOrchestrator.Data.CardDefinition;

/**
 * Classe principal do jogo — orquestra o loop de batalha entre o herói e os
 * inimigos.
 *
 * <p>
 * Responsabilidades desta classe:
 * <ul>
 * <li>Inicializar as entidades e baralhos do jogo via {@link Data};</li>
 * <li>Gerenciar os turnos do herói e dos inimigos;</li>
 * <li>Disparar notificações de fim de turno para o sistema Observer;</li>
 * <li>Delegar toda a renderização visual a {@link UserInterface}.</li>
 * </ul>
 *
 * <p>
 * Princípios de POO aplicados:
 * <ul>
 * <li><b>Encapsulamento:</b> todos os campos de instância são {@code private},
 * sendo acessados apenas pelos métodos desta classe ou por getters explícitos,
 * evitando acesso direto via {@code app.campo} de fora da classe.</li>
 * <li><b>Responsabilidade única (SRP):</b> {@code App} orquestra o fluxo do
 * jogo,
 * enquanto {@link UserInterface} cuida exclusivamente da apresentação e
 * {@link Data} da criação de entidades.</li>
 * <li><b>Separação de interesses:</b> a lógica de negócio (turno, combate,
 * Observer)
 * está separada da lógica de renderização (UserInterface) e de dados
 * (Data).</li>
 * </ul>
 *
 * <p>
 * O ponto de entrada da aplicação é o método {@link #main(String[])}.
 *
 * @see Data
 * @see UserInterface
 * @see observer.Publisher
 */
public class App {

    /**
     * Constrói uma nova instância de {@code App}.
     * Inicializa a lista de inimigos e o Publisher central do sistema Observer.
     */
    public App() {}

    // =========================================================================
    // Campos de instância (privados — encapsulamento)
    // =========================================================================

    /** Herói controlado pelo jogador. */
    private Hero hero;

    /** Árvore que representa o mapa de progressão do jogo. */
    private TreePath treePath;

    /** Nó atual na árvore de progressão — define os inimigos da fase em curso. */
    private Node currentNode;

    /**
     * Pilha de compra do herói. Reabastecida automaticamente pelo descarte quando
     * vazia. Persiste entre batalhas para manter o estado do baralho ao longo do jogo.
     */
    private BuyPile heroBuyPile;

    /**
     * Pilha de descarte do herói. Recebe as cartas jogadas durante o turno.
     * Persiste entre batalhas junto com {@link #heroBuyPile}.
     */
    private DiscardPile heroDiscardPile;

    private int currentEventIndex = 0;

    /**
     * Publisher central do padrão Observer.
     *
     * <p>
     * Compartilhado por todos os efeitos do jogo para garantir que um único
     * barramento de eventos gerencie as assinaturas e notificações.
     */
    private Publisher publisher = new Publisher();

    /**
     * Histórico de direções percorridas na árvore de progressão durante a sessão atual.
     * Cada entrada é {@code "left"} ou {@code "right"}, na ordem em que foram escolhidas.
     * Usado por {@link #buildSaveState()} para serializar o caminho percorrido
     * e por {@link TreePath#getNodeBeforeSave(List)} para reposicionar o jogador ao carregar.
     */
    private List<String> pathTaken = new ArrayList<>();

    // =========================================================================
    // Utilitários de terminal
    // =========================================================================

    /**
     * Exibe a tela de introdução do jogo com arte ASCII colorida via ANSI.
     *
     * <p>
     * Imprime o título "DIDI MARCO" versus "SR. DR. CABO ARRUDA" em arte
     * ASCII, usando as constantes de cor definidas em {@link UserInterface}.
     */
    public static void gameIntro() {
        System.out.println(UserInterface.BOLD + UserInterface.BCYAN);
        System.out.println("  _____  _____ _____ _____   __  __          _____   _____ ____  ");
        System.out.println(" |  __ \\|_   _|  __ \\_   _| |  \\/  |   /\\   |  __ \\ / ____/ __ \\ ");
        System.out.println(" | |  | | | | | |  | || |   | \\  / |  /  \\  | |__) | |   | |  | |");
        System.out.println(" | |  | | | | | |  | || |   | |\\/| | / /\\ \\ |  _  /| |   | |  | |");
        System.out.println(" | |__| |_| |_| |__| || |_  | |  | |/ ____ \\| | \\ \\| |___| |__| |");
        System.out.println(" |_____/|_____|_____/_____| |_|  |_/_/    \\_\\_|  \\_\\\\_____\\____/ ");
        System.out.println(UserInterface.RESET);

        System.out.println(
                UserInterface.BOLD + UserInterface.WHITE + "                            V S" + UserInterface.RESET);
        System.out.println();

        System.out.println(UserInterface.BOLD + UserInterface.BRED);
        System.out.println("  _____ _____     _____  _____      _____          ____   ____  ");
        System.out.println(" / ____|  __ \\   |  __ \\|  __ \\    / ____|   /\\   |  _ \\ / __ \\ ");
        System.out.println("| (___ | |__) |  | |  | | |__) |  | |       /  \\  | |_) | |  | |");
        System.out.println(" \\___ \\|  _  /   | |  | |  _  /   | |      / /\\ \\ |  _ <| |  | |");
        System.out.println(" ____) | | \\ \\   | |__| | | \\ \\   | |____ / ____ \\| |_) | |__| |");
        System.out.println("|_____/|_|  \\_\\  |_____/|_|  \\_\\   \\_____/_/    \\_\\____/ \\____/ ");

        System.out.println("          /\\    |  __ \\|  __ \\| |  | |  __ \\   /\\     ");
        System.out.println("         /  \\   | |__) | |__) | |  | | |  | | /  \\    ");
        System.out.println("        / /\\ \\  |  _  /|  _  /| |  | | |  | |/ /\\ \\   ");
        System.out.println("       / ____ \\ | | \\ \\| | \\ \\| |__| | |__| / ____ \\  ");
        System.out.println("      /_/    \\_\\_|  \\_\\_|  \\_\\\\____/|_____/_/    \\_\\ ");
        System.out.println(UserInterface.RESET);
    }


    // =========================================================================
    // Inicialização
    // =========================================================================

    /**
     * Inicializa o estado do jogo: instancia o herói, constrói a árvore
     * de progressão, monta e embaralha o baralho do herói, e carrega
     * os inimigos do nó raiz.
     *
     * <p>Este método deve ser chamado exatamente uma vez antes do início
     * do loop principal em {@link #main(String[])}.
     */
    public void start() {
        hero = GameFactory.createHero();

        treePath = GameFactory.createTreePath(publisher);
        currentNode = treePath.getRoot();

        List<CardDefinition> heroCards = new ArrayList<>();
        heroCards.addAll(Data.heroDamageCardsDefinitions);
        heroCards.addAll(Data.heroShieldCardsDefinitions);
        heroCards.addAll(Data.heroEffectCardsDefinitions);

        heroBuyPile = GameFactory.createBuyPile(publisher, heroCards);
        
        heroDiscardPile = new DiscardPile();
    }

    /**
     * Avança o jogo para o próximo nó da árvore de progressão,
     * limpando os inimigos e efeitos da fase anterior e carregando
     * os inimigos do nó escolhido.
     *
     * @param currentNode  nó atual, cujos filhos representam as opções de avanço
     * @param isGoingLeft  {@code true} para avançar pelo filho esquerdo;
     *                     {@code false} para o filho direito
     */
    private void startNewFase(Node currentNode, boolean isGoingLeft) {
        publisher.resetPublisher();
        hero.clearEffects();
        if (isGoingLeft) {
            this.currentNode = currentNode.getLeftNode();
            pathTaken.add("left");
        } else {
            this.currentNode = currentNode.getRightNode();
            pathTaken.add("right");
        }
    }

    /**
     * Constrói um {@link SaveState} representando o estado atual do jogo,
     * capturando a vida do herói, todas as cartas do baralho (buy pile + discard pile),
     * o caminho percorrido na árvore de progressão e o índice do próximo evento a executar.
     *
     * @return {@link SaveState} pronto para ser persistido por {@link SaveManager}
 */
    public SaveState buildSaveState() {
        List<String> allCardNames = new ArrayList<>();
        allCardNames.addAll(heroBuyPile.getCardNames());
        allCardNames.addAll(heroDiscardPile.getCardNames());
        return new SaveState(hero.getHealth(), allCardNames, pathTaken, currentEventIndex);
    }

    /**
     * Carrega um jogo salvo anteriormente, restaurando a vida do herói,
     * reconstruindo o baralho a partir dos nomes salvos e reposicionando
     * o jogador no nó correto da árvore de progressão.
     *
     * <p>As pilhas de compra e descarte são limpas antes de serem repovoadas
     * para evitar duplicação de cartas em relação ao baralho criado por {@link #start()}.
     *
     * <p>O nó de destino é determinado por {@link TreePath#getNodeBeforeSave(List)},
     * que navega a árvore seguindo o caminho salvo e retorna o nó da próxima batalha.
     */
    public void loadGame() {
        SaveState saveState = SaveManager.loadGame();
        currentEventIndex = saveState.getEventIndex();
        hero.setHealth(saveState.getHeroHealth());
        while (heroBuyPile.getSize() > 0) heroBuyPile.extractCard(0);
        while (heroDiscardPile.getSize() > 0) heroDiscardPile.extractCard(0);
        for (String cardName : saveState.getDeckCardNames()) {
            heroBuyPile.push(GameFactory.createCardbyName(cardName, publisher));
        }
        Node nodeBeforeSave = treePath.getNodeBeforeSave(saveState.getPathTaken());
        pathTaken.clear();
        saveState.getPathTaken().forEach(direction -> pathTaken.add(direction));
        currentNode = nodeBeforeSave;
    }

    // =========================================================================
    // Getters (encapsulamento — acesso controlado ao estado interno)
    // =========================================================================

    /**
     * Retorna o herói controlado pelo jogador.
     *
     * @return o {@link Hero} atual; {@code null} se {@link #start()} ainda não foi
     *         chamado
     */
    public Hero getHero() {
        return hero;
    }

    /**
     * Ponto de entrada da aplicação.
     *
     * <p>Sequência de execução:
     * <ol>
     *   <li>Exibe a tela de introdução e aguarda {@code 3000 ms};</li>
     *   <li>Inicializa o estado do jogo via {@link #start()};</li>
     *   <li>Se houver save em disco, exibe aviso e carrega via {@link #loadGame()};</li>
     *   <li>Executa o loop de progressão enquanto o herói estiver vivo
     *       e houver nós no mapa:
     *     <ol>
     *       <li>Cria uma nova {@link Battle} com o estado atual;</li>
     *       <li>Executa o combate via {@link Battle#runBattle()};</li>
     *       <li>Em vitória, verifica se o nó atual é folha (fim do jogo);
     *           caso contrário, navega para o próximo nó e salva;</li>
     *       <li>Em derrota, apaga o save e encerra o loop;</li>
     *       <li>Em {@code QUIT}, salva o estado atual incluindo o índice do evento interrompido e encerra o loop, permitindo retomar a partir do mesmo ponto na próxima execução.</li>
     *     </ol>
     *   </li>
     *   <li>Exibe a tela de fim de jogo e aguarda {@code 10000 ms}.</li>
     * </ol>
     *
     * @param args argumentos de linha de comando (não utilizados)
     * @throws Exception se ocorrer erro inesperado durante a execução
     */
    public static void main(String[] args) throws Exception {
        App.gameIntro();
        GameUtils.Wait(3000);
        Scanner scanner = new Scanner(System.in);
        App app = new App();
        app.start();
        if (SaveManager.isThereAnySave()) {
            UserInterface.printSaveFound();
            GameUtils.Wait(2000);
            app.loadGame();
        }
        boolean nodeAborted = false;
        while (app.hero.isAlive() && app.currentNode != null) {
            nodeAborted = false;
            List<Event> events = app.currentNode.getEvents();
            for (int i = app.currentEventIndex; i < events.size(); i++) {
                Event event = events.get(i);
                app.currentEventIndex = i + 1;
                if (event instanceof Battle) {
                    ((Battle) event).setPublisher(app.publisher);
                }
                EventResult eventResult = event.initializeEvent(app.hero, app.heroBuyPile, app.heroDiscardPile, scanner);
                if (eventResult.equals(EventResult.CONTINUE)) {
                    continue;
                } else if (eventResult.equals(EventResult.DEFEAT)) {
                    SaveManager.resetSave();
                    nodeAborted = true;
                    break;
                } else {
                    SaveState saveState = app.buildSaveState();
                    SaveManager.saveGame(saveState);
                    UserInterface.printAction("Progresso salvo! Até a próxima...");
                    GameUtils.Wait(1500);
                    nodeAborted = true;
                    break;
                }
            }
            if (!nodeAborted) {
                app.currentEventIndex = 0;
                if (app.currentNode.getLeftNode() == null && app.currentNode.getRightNode() == null) {
                    app.currentNode = null;
                } else {
                    UserInterface.printFaseClear(app.currentNode);
                    GameUtils.Wait(2000);
                    if (app.currentNode.getLeftNode() == null) {
                        app.startNewFase(app.currentNode, false);
                    } else if (app.currentNode.getRightNode() == null) {
                        app.startNewFase(app.currentNode, true);
                    } else {
                        int choice = scanner.nextInt();
                        app.startNewFase(app.currentNode, choice == 1);
                    }
                    SaveState saveState = app.buildSaveState();
                    SaveManager.saveGame(saveState);
                }
            } else {
                break;
            }
        }
        boolean gameWon = app.currentNode == null && app.hero.isAlive();
        boolean showEndScreen = !app.hero.isAlive() || app.currentNode == null;
        if (showEndScreen) {
            UserInterface.clearScreen();
            UserInterface.printGameOver(gameWon, app.hero.getName(), app.currentNode);
            GameUtils.Wait(10000);
        }
        scanner.close();
    }
}