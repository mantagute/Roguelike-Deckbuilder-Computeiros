package events;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

import cards.Card;
import deck.BuyPile;
import deck.DiscardPile;
import entities.Hero;
import gameOrchestrator.App;
import gameOrchestrator.GameFactory;
import gameOrchestrator.UserInterface;
import gameOrchestrator.Data.EnemyDefinition;
import observer.Publisher;
import entities.Enemy;
import entities.Entity;
import gameOrchestrator.GameUtils;

/**
 * Encapsula a lógica de um combate individual entre o herói e os inimigos.
 * Responsável por gerenciar os turnos do herói e dos inimigos, executar
 * os ataques e disparar notificações do sistema Observer ao fim de cada turno.
 *
 * <p>Ao final do combate, retorna um {@link BattleResult} indicando se o herói
 * venceu ({@code VICTORY}), foi derrotado ({@code DEFEAT}) ou escolheu sair
 * e salvar ({@code     }), permitindo que {@link App} tome a decisão adequada.
 *
 * <p>Princípios de POO aplicados:
 * <ul>
 *   <li><b>Responsabilidade única (SRP):</b> {@code Battle} cuida exclusivamente
 *       da lógica de combate, delegando apresentação a {@link UserInterface}
 *       e progressão a {@link App}.</li>
 *   <li><b>Encapsulamento:</b> todos os campos são {@code private};
 *       a lógica interna de turno não é exposta ao chamador.</li>
 * </ul>
 *
 * @see App
 * @see UserInterface
 * @see observer.Publisher
 */
public class Battle extends Event {
    private Hero hero;
    private List<EnemyDefinition> enemyDefinitions;
    private List<Enemy> enemies = new ArrayList<>();
    private Publisher publisher;
    private Scanner scanner;
    private BuyPile heroBuyPile;
    private DiscardPile heroDiscardPile;

    /**
     * Flag que indica se o jogador solicitou sair e salvar durante o turno.
     * Quando {@code true}, {@link #heroTurn(Scanner)} retorna imediatamente
     * e {@link #runBattle()} encerra o combate com {@link BattleResult#QUIT}.
     */
    private boolean quitRequested = false;

    /**
     * Constrói uma nova instância de batalha com as definições de inimigos fornecidas.
     * Os demais recursos (herói, publisher, scanner, pilhas) são injetados via
     * {@link #initializeEvent} no momento em que o evento é iniciado pelo mapa.
     *
     * @param enemyDefinitions lista de definições dos inimigos presentes neste combate
     */
    public Battle(List<EnemyDefinition> enemyDefinitions) {
        this.enemyDefinitions = enemyDefinitions;

    }

    /**
     * Possíveis resultados de um combate.
     */
    private enum BattleResult {
        /** O herói derrotou todos os inimigos. */
        VICTORY,
        /** O herói foi derrotado. */
        DEFEAT,
        /** O jogador escolheu sair e salvar durante seu turno. */
        QUIT
    }

    public EventResult initializeEvent(Hero hero, BuyPile buyPile, DiscardPile discardPile, Scanner scanner) {
        this.hero = hero;
        this.scanner = scanner;
        this.heroBuyPile = buyPile;
        this.heroDiscardPile = discardPile;

        for (EnemyDefinition enemyDefinition : enemyDefinitions) {  
            Enemy enemy = GameFactory.createEnemyFromDefinition(enemyDefinition, publisher);
            enemies.add(enemy);
        }

        BattleResult battleResult = runBattle();

        switch (battleResult) {
            case VICTORY:
                    /**
                     * Concede uma recompensa aleatória de ouro ao herói após a vitória,
                     * no intervalo de 30 a 45 moedas.
                     *
                     * @param hero herói que receberá o ouro
                     */
                    rewardGold(hero);
                    return EventResult.CONTINUE;
            case DEFEAT:
                    return EventResult.DEFEAT;
            case QUIT:
                    return EventResult.QUIT;   
            default:
                    throw new IllegalStateException("Unexpected BattleResult: " + battleResult);
        }
    }

    /**
     * Executa o loop completo de combate até que o herói, todos os inimigos
     * sejam derrotados, ou o jogador escolha sair.
     *
     * <p>A cada iteração do loop:
     * <ol>
     *   <li>Os inimigos definem e anunciam sua estratégia ({@link #enemyTurn()});</li>
     *   <li>O herói realiza suas ações ({@link #heroTurn(Scanner)});
     *       se {@link #quitRequested} for marcado, retorna {@link BattleResult#QUIT}
     *       imediatamente, sem que os inimigos ataquem;</li>
     *   <li>Os inimigos vivos executam seus ataques contra o herói;</li>
     *   <li>O evento {@code "FIM_TURNO"} é disparado para todas as entidades,
     *       aplicando efeitos de status pendentes.</li>
     * </ol>
     *
     * @return {@link BattleResult#VICTORY} se todos os inimigos forem derrotados,
     *         {@link BattleResult#DEFEAT} se o herói morrer,
     *         {@link BattleResult#QUIT} se o jogador escolher sair e salvar
     */
    private BattleResult runBattle() {
        while (hero.isAlive() && enemies.stream().anyMatch(Enemy::isAlive)) {
            enemyTurn();
            heroTurn(scanner);

            if (quitRequested) {
                return BattleResult.QUIT;
            }

            for (Enemy enemy : enemies) {
                if (enemy.isAlive()) {
                    enemy.resetShield();
                    UserInterface.clearScreen();
                    UserInterface.printEnemyAttackHeader(enemy.getName());
                    enemy.executeEnemyStrategy(hero);
                    GameUtils.Wait(2500);
                }
            }

            notifyAndClean("FIM_TURNO", hero, enemies.get(0));
            for (Enemy enemy : enemies) {
                notifyAndClean("FIM_TURNO", enemy, hero);
            }
        }
        return hero.isAlive() ? BattleResult.VICTORY : BattleResult.DEFEAT;
    }

        private void rewardGold(Hero hero) {
        int gold = 30 + new Random().nextInt(16);
        hero.addGold(gold);
        UserInterface.printReward(hero.getName(), gold);
        GameUtils.Wait(2500);
    }

    // =========================================================================
    // Turnos
    // =========================================================================

    /**
     * Executa o turno completo do herói.
     *
     * <p>
     * O turno encerra-se quando ocorre uma das seguintes condições:
     * <ul>
     * <li>O jogador escolhe passar a vez;</li>
     * <li>O herói não possui energia suficiente para nenhuma carta;</li>
     * <li>Todos os inimigos são derrotados durante o turno;</li>
     * <li>O jogador escolhe sair e salvar (marca {@link #quitRequested} e retorna).</li>
     * </ul>
     *
     * <p>
     * Durante o turno, o herói pode jogar cartas de dano (escolhendo um alvo
     * específico ou atacando todos com cartas multi-alvo), cartas de escudo
     * (auto-alvo) ou cartas de efeito.
     *
     * @param scanner leitor de entrada do teclado; não deve ser {@code null}
     */
    public void heroTurn(Scanner scanner) {
        hero.resetShield();
        hero.newTurn(heroBuyPile, heroDiscardPile);
        boolean isTurnOver = false;

        while (!isTurnOver && hero.isAlive() && enemies.stream().anyMatch(Enemy::isAlive)) {

            UserInterface.clearScreen();

            if (!hero.hasEnoughEnergyForAnyCard()) {
                UserInterface.printHeroTurnHeader(hero.getName());
                UserInterface.printCombatState(hero, enemies);
                UserInterface.printNoEnergy(hero.getName());
                GameUtils.Wait(2000);
                isTurnOver = true;
                break;
            }

            UserInterface.printHeroTurnHeader(hero.getName());
            UserInterface.printCombatState(hero, enemies);
            UserInterface.printHand(hero, hero.getHandSize());
            UserInterface.printPassOption(hero.getHandSize() + 1);
            UserInterface.printQuitOption(hero.getHandSize() + 2);
            UserInterface.printChoicePrompt();

            int choice = scanner.nextInt();

            if (choice == hero.getHandSize() + 1) {
                UserInterface.printHeroPass(hero.getName());
                GameUtils.Wait(1500);
                isTurnOver = true;
                continue;
            }

            if (choice == hero.getHandSize() + 2) {
                quitRequested = true;
                return;
            }

            if (choice < 1 || choice > hero.getHandSize()) {
                UserInterface.printError("Opção inválida. Tente novamente.");
                GameUtils.Wait(1500);
                continue;
            }

            Card chosenCard = hero.getCardFromHand(choice - 1);

            if (chosenCard.getEnergyCost() > hero.getEnergy()) {
                UserInterface.printWarning("Energia insuficiente para esta carta.");
                GameUtils.Wait(1500);
                continue;
            }

            if (chosenCard.isMultiTarget()) {
                applyCardToAllEnemies(choice - 1, chosenCard);
            } else if (chosenCard.isSelfTarget()) {
                hero.useCard(choice - 1, hero, heroDiscardPile);
            } else {
                applyCardToSingleEnemy(choice - 1, scanner);
            }

            GameUtils.Wait(1800);

            if (enemies.stream().allMatch(e -> !e.isAlive())) {
                isTurnOver = true;
            }
        }
    }

    /**
     * Aplica uma carta de multi-alvo a todos os inimigos vivos.
     *
     * <p>
     * O uso formal da carta (desconto de energia e descarte) é realizado apenas
     * na primeira aplicação via {@link Hero#useCard}; nas aplicações seguintes,
     * o efeito é executado diretamente sobre cada inimigo sem descontar energia
     * nem descartar a carta novamente.
     *
     * @param cardIndex índice da carta na mão do herói (base 0)
     * @param card      carta a ser aplicada; deve ter {@code isMultiTarget() == true}
     */
    private void applyCardToAllEnemies(int cardIndex, Card card) {
        boolean first = true;
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                if (first) {
                    hero.useCard(cardIndex, enemy, heroDiscardPile);
                    first = false;
                } else {
                    card.useCard(hero, enemy);
                }
            }
        }
    }

    /**
     * Solicita ao jogador que escolha um inimigo vivo e aplica a carta selecionada.
     *
     * <p>
     * Se houver apenas um inimigo vivo, ele é atacado automaticamente, sem
     * necessidade de input do jogador.
     *
     * @param cardIndex índice da carta na mão do herói (base 0)
     * @param scanner   leitor de entrada do teclado; não deve ser {@code null}
     */
    private void applyCardToSingleEnemy(int cardIndex, Scanner scanner) {
        List<Enemy> aliveEnemies = enemies.stream()
                .filter(Enemy::isAlive)
                .collect(Collectors.toList());

        if (aliveEnemies.size() == 1) {
            hero.useCard(cardIndex, aliveEnemies.get(0), heroDiscardPile);
            return;
        }

        UserInterface.printEnemyTargetList(aliveEnemies);
        UserInterface.printEnemyChoicePrompt();
        int chosenEnemy = scanner.nextInt();

        if (chosenEnemy < 1 || chosenEnemy > aliveEnemies.size()) {
            UserInterface.printError("Opção inválida. Tente novamente.");
            GameUtils.Wait(1500);
            return;
        }

        hero.useCard(cardIndex, aliveEnemies.get(chosenEnemy - 1), heroDiscardPile);
    }

    /**
     * Executa o turno dos inimigos: cada inimigo vivo compra cartas e anuncia
     * sua estratégia para o próximo ataque.
     *
     * <p>
     * O anúncio de cada inimigo é exibido com uma pausa de {@code 2500 ms}
     * para que o jogador possa ler a intenção antes do combate.
     */
    public void enemyTurn() {
        UserInterface.clearScreen();
        UserInterface.printEnemyPlanHeader();
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.newTurn();
                UserInterface.printEnemyAnnouncement(enemy.prepareForBattle());
                GameUtils.Wait(2500);
            }
        }
    }

    // =========================================================================
    // Observer
    // =========================================================================

    /**
     * Dispara o evento especificado no {@link Publisher} e remove os efeitos
     * expirados da entidade originadora.
     *
     * <p>
     * Este método centraliza a integração entre o loop de combate e o padrão
     * Observer: primeiro notifica todos os subscribers (que podem reduzir seus
     * próprios acúmulos e se desinscrever), depois chama {@link Entity#manageEffects()}
     * para remover da lista interna os efeitos com acúmulos zerados.
     *
     * @param event  identificador do evento a ser publicado (ex.: {@code "FIM_TURNO"})
     * @param user   entidade que originou o evento; seus efeitos serão gerenciados
     *               após a notificação
     * @param target entidade alvo do evento
     */
    private void notifyAndClean(String event, Entity user, Entity target) {
        publisher.notify(event, user, target);
        user.manageEffects();
    }

    /**
     * Define o Publisher do sistema Observer a ser utilizado neste combate.
     * Deve ser chamado antes de {@link #initializeEvent} para que os efeitos
     * de status possam se inscrever corretamente no barramento de eventos.
     *
     * @param publisher Publisher central do jogo
     */
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Retorna a lista de definições dos inimigos presentes neste combate.
     * Utilizado por {@link gameOrchestrator.UserInterface#printGameOver} para
     * exibir os nomes dos inimigos na tela de derrota.
     *
     * @return lista imutável de {@link EnemyDefinition}
     */
    public List<EnemyDefinition> getEnemyDefinitions() {
        return enemyDefinitions;
    }

    public String getPreview() {
        List<String> enemiesNames = new ArrayList<>();
        for (EnemyDefinition def : enemyDefinitions) {
            enemiesNames.add(def.name());
        }
        return "⚔️  Batalha: " + String.join(", ", enemiesNames);
    }
} 