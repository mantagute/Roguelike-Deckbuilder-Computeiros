package gameOrchestrator;

import cards.Card;
import cards.DamageCard;
import cards.ShieldCard;
import entities.Enemy;
import entities.Entity;
import gameOrchestrator.Data.EnemyDefinition;
import gamePath.Node;
import entities.Hero;
import events.Battle;
import events.Event;
import events.campfire.CampFireAction;
import events.choice.ChoiceOption;
import events.shop.ShopItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe utilitária estática responsável por toda a renderização visual do jogo
 * no terminal, incluindo formatação com cores ANSI, exibição de status de
 * entidades, cartas e mensagens de sistema.
 *
 * <p>Princípios de POO aplicados:
 * <ul>
 *   <li><b>Responsabilidade única (SRP):</b> a classe cuida exclusivamente da
 *       camada de apresentação, sem nenhuma lógica de negócio.</li>
 *   <li><b>Coesão:</b> todos os métodos giram em torno de uma única responsabilidade
 *       — renderizar o estado do jogo no terminal.</li>
 * </ul>
 *
 * @see App
 */
public final class UserInterface {

    /**
     * Construtor privado — impede instanciação desta classe utilitária.
     */
    private UserInterface() {}

    // =========================================================================
    // Constantes de escape ANSI
    // =========================================================================

    /** Código ANSI para resetar todas as formatações. */
    public static final String RESET   = "\033[0m";

    /** Código ANSI para texto em negrito. */
    public static final String BOLD    = "\033[1m";

    /** Código ANSI para texto em modo fraco (dim). */
    public static final String DIM     = "\033[2m";

    /** Código ANSI para texto com efeito tachado (strikethrough). */
    public static final String STRIKE  = "\033[9m";

    // --- Cores de texto normais ---

    /** Texto na cor vermelha. */
    public static final String RED     = "\033[31m";

    /** Texto na cor verde. */
    public static final String GREEN   = "\033[32m";

    /** Texto na cor amarela. */
    public static final String YELLOW  = "\033[33m";

    /** Texto na cor azul. */
    public static final String BLUE    = "\033[34m";

    /** Texto na cor magenta. */
    public static final String MAGENTA = "\033[35m";

    /** Texto na cor ciano. */
    public static final String CYAN    = "\033[36m";

    /** Texto na cor branca. */
    public static final String WHITE   = "\033[37m";

    // --- Cores de texto brilhantes (bright) ---

    /** Texto em vermelho brilhante. */
    public static final String BRED    = "\033[91m";

    /** Texto em verde brilhante. */
    public static final String BGREEN  = "\033[92m";

    /** Texto em amarelo brilhante. */
    public static final String BYELLOW = "\033[93m";

    /** Texto em azul brilhante. */
    public static final String BBLUE   = "\033[94m";

    /** Texto em magenta brilhante. */
    public static final String BMAGENTA = "\033[95m";

    /** Texto em ciano brilhante. */
    public static final String BCYAN   = "\033[96m";

    /** Texto em branco brilhante. */
    public static final String BWHITE  = "\033[97m";

    // =========================================================================
    // Utilitários de terminal
    // =========================================================================

    /**
     * Limpa o terminal usando sequências de escape ANSI.
     *
     * <p>Aguarda {@code 600 ms} antes de limpar a tela, suavizando a transição
     * visual entre estados do jogo.
     */
    public static void clearScreen() {
        GameUtils.Wait(600);
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Imprime uma linha divisória horizontal com a largura e cor especificadas.
     *
     * @param width largura da linha (número de caracteres {@code ─})
     * @param color sequência de escape ANSI que define a cor da linha
     */
    public static void printDivider(int width, String color) {
        System.out.println(color + "─".repeat(width) + RESET);
    }

    /**
     * Imprime uma linha divisória padrão com largura {@code 60} e cor branca fraca.
     *
     * <p>Atalho para {@link #printDivider(int, String)} com valores padrão do jogo.
     */
    public static void printDivider() {
        printDivider(60, DIM + WHITE);
    }

    // =========================================================================
    // Status de entidades
    // =========================================================================

    /**
     * Monta e retorna a {@link String} formatada com o status completo de uma entidade.
     *
     * <p>O status inclui sempre o nome, pontos de vida e escudo da entidade.
     * Opcionalmente, exibe a energia atual. Efeitos ativos são listados ao final,
     * caso existam.
     *
     * @param entity     entidade cujo status será formatado; não deve ser {@code null}
     * @param showEnergy {@code true} para incluir a energia atual na saída
     * @return string formatada com cores ANSI pronta para impressão no terminal
     */
    public static String getEntityStatusString(Entity entity, boolean showEnergy) {
        StringBuilder sb = new StringBuilder();

        sb.append(BOLD).append(BWHITE).append(entity.getName()).append(RESET).append("  ");
        sb.append(RED).append("❤️  ").append(BOLD).append(String.format("%.0f", entity.getHealth())).append(RESET).append("  ");
        sb.append(BLUE).append("🛡️  ").append(BOLD).append(String.format("%.0f", entity.getShield())).append(RESET);

        if (showEnergy) {
            sb.append("  ").append(YELLOW).append("⚡ ").append(BOLD).append(entity.getEnergy()).append(RESET);
        }

        String effects = entity.getEffectString();
        if (!effects.equals("Sem efeitos ativos")) {
            sb.append("  | ").append(MAGENTA).append(effects).append(RESET);
        }

        return sb.toString();
    }

    /**
     * Imprime no terminal o status formatado de uma única entidade.
     *
     * <p>Delegado de {@link #getEntityStatusString(Entity, boolean)}.
     *
     * @param entity     entidade a ser exibida; não deve ser {@code null}
     * @param showEnergy {@code true} para incluir a energia atual na saída
     */
    public static void printEntityStatus(Entity entity, boolean showEnergy) {
        System.out.println(getEntityStatusString(entity, showEnergy));
    }

    /**
     * Renderiza todos os inimigos da lista em uma única linha, separados por {@code |}.
     *
     * <p>Cada inimigo é formatado por {@link #getEntityStatusString(Entity, boolean)}
     * sem exibição de energia.
     *
     * @param enemies lista de inimigos a exibir; não deve ser {@code null} nem vazia
     */
    public static void printEnemiesInline(List<Enemy> enemies) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < enemies.size(); i++) {
            sb.append(getEntityStatusString(enemies.get(i), false));
            if (i < enemies.size() - 1) {
                sb.append(DIM).append("   |   ").append(RESET);
            }
        }
        System.out.println(sb.toString());
    }

    // =========================================================================
    // Cabeçalhos de turno
    // =========================================================================

    /**
     * Imprime o cabeçalho do turno do herói, com nome em destaque e separadores ciano.
     *
     * @param heroName nome do herói; exibido em maiúsculas
     */
    public static void printHeroTurnHeader(String heroName) {
        System.out.println();
        printDivider(60, CYAN);
        System.out.println(BOLD + BCYAN + "  ⚔️  TURNO DE " + heroName.toUpperCase() + RESET);
        printDivider(60, CYAN);
        System.out.println();
    }

    /**
     * Imprime o cabeçalho de ataque de um inimigo, com separadores vermelhos.
     *
     * @param enemyName nome do inimigo; exibido em maiúsculas
     */
    public static void printEnemyAttackHeader(String enemyName) {
        System.out.println();
        printDivider(60, RED);
        System.out.println(BOLD + BRED + "  💥 ATAQUE DE " + enemyName.toUpperCase() + RESET);
        printDivider(60, RED);
        System.out.println();
    }

    /**
     * Imprime o cabeçalho da fase em que os inimigos definem seus próximos movimentos,
     * com separadores amarelos.
     */
    public static void printEnemyPlanHeader() {
        System.out.println();
        printDivider(60, YELLOW);
        System.out.println(BOLD + BYELLOW + "  🎯 INIMIGOS DEFININDO SEUS PRÓXIMOS MOVIMENTOS..." + RESET);
        printDivider(60, YELLOW);
        System.out.println();
    }

    // =========================================================================
    // Renderização de cartas
    // =========================================================================

    /**
     * Retorna o emoji e a cor ANSI associados ao tipo de uma carta.
     *
     * <p>O tipo é determinado por polimorfismo via {@code instanceof}:
     * <ul>
     *   <li>{@link DamageCard} → espada vermelha;</li>
     *   <li>{@link ShieldCard} → escudo azul;</li>
     *   <li>qualquer outro tipo → estrela magenta.</li>
     * </ul>
     *
     * @param card carta cujo estilo visual será determinado; não deve ser {@code null}
     * @return array de dois elementos: {@code [emoji, codigoAnsiCor]}
     */
        public static String[] getCardStyle(Card card) {
            if (card instanceof DamageCard) {
                return new String[]{"⚔️ ", BRED};
            } else if (card instanceof ShieldCard) {
                return new String[]{"🛡️ ", BBLUE};
            } else {
                return new String[]{"✨ ", BMAGENTA};
            }
        }

    // Mantém o privado delegando pro público — retrocompatibilidade
    private static String[] cardStyle(Card card) {
        return getCardStyle(card);
    }

    /**
     * Renderiza a mão do jogador no terminal, listando cada carta com índice,
     * ícone, nome, custo de energia e detalhes.
     *
     * <p>Cartas cujo custo excede a energia atual do jogador são exibidas com
     * efeito tachado ({@link #STRIKE}), sinalizando que não podem ser jogadas.
     * Cartas com alvo múltiplo recebem uma tag visual adicional.
     *
     * @param entity   entidade dona da mão (normalmente o herói); não deve ser {@code null}
     * @param handSize número de cartas na mão a serem exibidas
     */
    public static void printHand(Entity entity, int handSize) {
        System.out.println(BOLD + WHITE + "  Suas cartas:" + RESET);
        System.out.println();
        for (int i = 0; i < handSize; i++) {
            Card card = entity.getCardFromHand(i);
            String[] style = cardStyle(card);

            String emoji = style[0].replace(" ", "");
            String color = style[1];

            boolean canAfford = card.getEnergyCost() <= entity.getEnergy();
            String strike = canAfford ? "" : STRIKE;

            String name    = card.getName().trim();
            String details = card.getDetails().strip();
            String multi   = card.isMultiTarget()
                ? (BYELLOW + "🎯 Ataca todos os inimigos!" + RESET)
                : "";

            String indexStr = String.format("%2d", i + 1);
            String costStr  = String.valueOf(card.getEnergyCost());

            System.out.printf("  %s%s%s. %s%s%s\033[11G%s%s%s\033[34G%s⚡%s%s\033[42G%s  %s%n",
                BOLD + WHITE, indexStr, RESET,
                color, emoji, RESET,
                strike, name, RESET,
                BYELLOW, costStr, RESET,
                details, multi
            );
        }
        System.out.println();
    }

    /**
     * Imprime a opção de passar a vez, numerada pelo índice fornecido.
     *
     * @param index número que o jogador deve digitar para passar a vez
     */
    public static void printPassOption(int index) {
        System.out.printf("  %s%d%s. %s⏭️  Passar a vez%s%n%n",
            BOLD + WHITE, index, RESET,
            BOLD + BWHITE, RESET);
    }

    /**
     * Imprime a opção de sair e salvar o jogo, numerada pelo índice fornecido.
     * Ao escolher esta opção, o combate é interrompido, o progresso é salvo
     * e a aplicação encerra sem perda de dados.
     *
     * @param index número que o jogador deve digitar para sair e salvar
     */
    public static void printQuitOption(int index) {
        System.out.printf("  %s%d%s. %s💾  Sair e salvar%s%n%n",
            BOLD + WHITE, index, RESET,
            BOLD + BWHITE, RESET);
    }

    /**
     * Imprime o prompt solicitando ao jogador que escolha uma ação.
     */
    public static void printChoicePrompt() {
        System.out.print(BOLD + BCYAN + "  Escolha uma ação: " + RESET);
    }

    /**
     * Imprime o prompt solicitando ao jogador que escolha qual inimigo atacar.
     */
    public static void printEnemyChoicePrompt() {
        System.out.print(BOLD + BYELLOW + "  Escolha qual inimigo atacar: " + RESET);
    }

    // =========================================================================
    // Mensagens de jogo e menus secundários
    // =========================================================================

    /**
     * Imprime uma mensagem de ação padrão com ícone de seta.
     *
     * @param message texto da ação a ser exibido
     */
    public static void printAction(String message) {
        System.out.println("  " + BWHITE + "▶ " + RESET + message);
    }

    /**
     * Informa ao jogador que um save anterior foi encontrado e está sendo
     * carregado automaticamente ao iniciar o jogo.
     */
    public static void printSaveFound() {
        System.out.println("  " + BWHITE + "▶ " + RESET + "Save encontrado! Carregando progresso anterior...");
    }

    public static void printReward(String heroName, int gold) {
        System.out.println("  " + BYELLOW + "★ " + RESET + BWHITE + heroName + " recebeu " + BYELLOW + gold + " de ouro!" + RESET);
    }

    /**
     * Imprime uma mensagem de aviso com ícone de alerta amarelo.
     *
     * @param message texto do aviso a ser exibido
     */
    public static void printWarning(String message) {
        System.out.println("  " + BYELLOW + "⚠️  " + message + RESET);
    }

    /**
     * Imprime uma mensagem de erro com ícone vermelho.
     *
     * @param message texto do erro a ser exibido
     */
    public static void printError(String message) {
        System.out.println("  " + BRED + "✖ " + message + RESET);
    }

    /**
     * Exibe a tela de fim de jogo, indicando vitória ou derrota.
     *
     * <p>Em caso de vitória, exibe o nome do herói e uma mensagem de encerramento.
     * Em caso de derrota, percorre os eventos do último nó para exibir os nomes
     * dos inimigos presentes na batalha final.
     *
     * @param heroWon  {@code true} se o herói venceu todos os combates; {@code false} caso contrário
     * @param heroName nome do herói
     * @param lastNode último nó visitado na árvore de progressão; usado para extrair
     *                 os nomes dos inimigos em caso de derrota
     */
    public static void printGameOver(boolean heroWon, String heroName, Node lastNode) {
        System.out.println();
        if (heroWon) {
            printDivider(60, BGREEN);
            System.out.println(BOLD + BGREEN + "  🏆 VITÓRIA!" + RESET);
            System.out.println(BWHITE + "  " + heroName + " venceu o jogo!" + RESET);
            System.out.println(DIM + "  Didi Marco provou é o tal do 01." + RESET);
            printDivider(60, BGREEN);
        } else {
            List<String> enemyNames = new ArrayList<>();
            for (Event event : lastNode.getEvents()) {
                if (event instanceof Battle) {
                    for (EnemyDefinition def : ((Battle) event).getEnemyDefinitions()) {
                        enemyNames.add(def.name());
                    }
                }
            }
            printDivider(60, BRED);
            System.out.println(BOLD + BRED + "  💀 DERROTA!" + RESET);
            System.out.println(BWHITE + "  " + String.join(" e ", enemyNames) + " venceram!" + RESET);
            System.out.println(DIM + "  Não sobrou nada..." + RESET);
            printDivider(60, BRED);
        }
        System.out.println();
    }

    /**
     * Imprime um anúncio de inimigo com ícone de megafone.
     *
     * @param message mensagem a ser anunciada
     */
    public static void printEnemyAnnouncement(String message) {
        System.out.println("  " + BYELLOW + "📢 " + RESET + BWHITE + message + RESET);
        System.out.println();
    }

    /**
     * Informa que o herói passou a vez para os inimigos.
     *
     * @param heroName nome do herói que está passando a vez
     */
    public static void printHeroPass(String heroName) {
        System.out.println();
        System.out.println("  " + BWHITE + heroName + " passa a vez para os inimigos..." + RESET);
    }

    /**
     * Informa que o herói não tem energia suficiente para jogar qualquer carta
     * e passará a vez automaticamente.
     *
     * @param heroName nome do herói sem energia
     */
    public static void printNoEnergy(String heroName) {
        System.out.println();
        System.out.println("  " + BYELLOW + "⚡ " + RESET + BWHITE + heroName
            + " sem energia — passando a vez..." + RESET);
    }

    /**
     * Renderiza a lista de inimigos numerada para que o jogador escolha um alvo.
     *
     * <p>Cada inimigo é exibido com seu índice (base 1) e status atual, sem
     * informações de energia.
     *
     * @param enemies lista de inimigos vivos disponíveis como alvo;
     *                não deve ser {@code null} nem vazia
     */
    public static void printEnemyTargetList(List<Enemy> enemies) {
        System.out.println();
        System.out.println(BOLD + WHITE + "  Escolha o alvo:" + RESET);
        System.out.println();
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            System.out.printf("  %s%d%s. ", BOLD + WHITE, i + 1, RESET);
            printEntityStatus(e, false);
        }
        System.out.println();
    }

    public static void printFaseClear(Node currentNode) {
        System.out.println();
        printDivider(60, BGREEN);
        System.out.println(BOLD + BGREEN + "  ✨ NÍVEL CONCLUÍDO! O CAMINHO DIVIDE-SE... ✨" + RESET);
        printDivider(60, BGREEN);
        System.out.println();
    
        if (currentNode.getLeftNode() != null) {
            System.out.println("  " + BCYAN + "1.  O Caminho da Esquerda" + RESET);
            for (Event event : currentNode.getLeftNode().getEvents()) {
                System.out.println("     " + DIM + event.getPreview() + RESET);
            }
        }

        System.out.println("\n");
        
        if (currentNode.getRightNode() != null) {
            System.out.println("  " + BRED + "2.  O Caminho da Direita" + RESET);
            for (Event event : currentNode.getRightNode().getEvents()) {
                System.out.println("     " + DIM + event.getPreview() + RESET);
            }
        }
    
        System.out.println("\n");
        printChoicePrompt();
    }

    // =========================================================================
    // Renderização do estado de combate
    // =========================================================================

    /**
     * Renderiza o estado atual do combate no terminal.
     *
     * <p>Exibe o status do herói (com energia) e, abaixo, os status de todos os
     * inimigos vivos em uma única linha separada por {@code |}.
     *
     * @param hero    herói controlado pelo jogador; não deve ser {@code null}
     * @param enemies lista de inimigos presentes no combate; não deve ser {@code null}
     */
    public static void printCombatState(Hero hero, List<Enemy> enemies) {
        UserInterface.printEntityStatus(hero, true);
        System.out.println();
        System.out.println(UserInterface.DIM + "  vs" + UserInterface.RESET);
        System.out.println();

        List<Enemy> aliveEnemies = enemies.stream()
                .filter(Enemy::isAlive)
                .collect(Collectors.toList());

        if (!aliveEnemies.isEmpty()) {
            UserInterface.printEnemiesInline(aliveEnemies);
        }

        System.out.println();
        UserInterface.printDivider();
        System.out.println();
    }

    public static void printCampFireOptions(List<CampFireAction> actions) {
        System.out.println();
        printDivider(60, BYELLOW);
        System.out.println(BOLD + BYELLOW + "  🔥 FOGUEIRA — O QUE DESEJA FAZER?" + RESET);
        printDivider(60, BYELLOW);
        System.out.println();
        for (int i = 0; i < actions.size(); i++) {
            String emoji = actions.get(i).getEmoji();
            System.out.printf("  %s%d%s. %s%s %s%s%n",
                BOLD + BWHITE, i + 1, RESET,
                BYELLOW, emoji,
                actions.get(i).getDescription(),
                RESET);
        }
        System.out.println();
    }
    
    public static void printCardsToUpdate(List<Card> cardsAvailable) {
        System.out.println();
        printDivider(60, BMAGENTA);
        System.out.println(BOLD + BMAGENTA + "  ✨ ESCOLHA UMA CARTA PARA MELHORAR" + RESET);
        printDivider(60, BMAGENTA);
        System.out.println();
        for (int i = 0; i < cardsAvailable.size(); i++) {
            Card card = cardsAvailable.get(i);
            String[] style = getCardStyle(card);
            String emoji = style[0].replace(" ", "");
            String color = style[1];
            String indexStr = String.format("%2d", i + 1);
            System.out.printf("  %s%s%s. %s%s%s  %s%s%n",
                BOLD + BWHITE, indexStr, RESET,
                color, emoji, RESET,
                card.getName() + card.getDetails(),
                RESET);
        }
        System.out.println();
    }

    public static void printChoiceLore(String lore) {
        System.out.println();
        printDivider(60, BCYAN);
        System.out.println();
        System.out.println("  " + DIM + BWHITE + lore + RESET);
        System.out.println();
    }

    public static void printChoiceOptions(List<ChoiceOption> options) {
        System.out.println(BOLD + BCYAN + "  ❓ UMA ESCOLHA..." + RESET);
        System.out.println();
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("  %s%d%s. %s%s%s  %s%n",
                BOLD + BWHITE, i + 1, RESET,
                BCYAN, options.get(i).getEmoji(), RESET,
                options.get(i).getAction());
        }
        System.out.println();
    }

    public static void printChoiceFeedback(ChoiceOption option) {
        System.out.println();
        printDivider(60, BCYAN);
        System.out.println("  " + option.getEmoji() + "  " + BOLD + BWHITE + option.getFeedback() + RESET);
        printDivider(60, BCYAN);
        System.out.println();
    }

    public static void printShop(Hero hero, List<ShopItem> items) {
        System.out.println();
        printDivider(60, BYELLOW);
        System.out.println(BOLD + BYELLOW + "  🛒 LOJA" + RESET);
        printDivider(60, BYELLOW);
        System.out.println();
        System.out.println("  " + BYELLOW + "💰 Ouro: " + BOLD + hero.getGold() + RESET);
        System.out.println();

        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            String strike = item.isSold() ? STRIKE : "";
            System.out.printf("  %s%d%s. %s%s %s — %s%s  %s%d de ouro%s%n",
                BOLD + BWHITE, i + 1, RESET,
                strike, item.getEmoji(), item.getName(),
                item.getDetails(), RESET,
                BYELLOW, item.getPrice(), RESET);
        }

        System.out.println();
        System.out.printf("  %s%d%s. %s🚪 Sair da loja%s%n%n",
            BOLD + BWHITE, items.size() + 1, RESET,
            BOLD + BWHITE, RESET);
    }


}