package gofd.gFMenu.menu.actions;

import gofd.gFMenu.GFMenu;
import gofd.gFMenu.menu.LayoutMenuData;
import gofd.gFMenu.menu.MenuManager;
import gofd.gFMenu.menu.actions.actions.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ActionEngine {

    private final GFMenu plugin;
    private final MenuManager menuManager;
    private final Map<String, Object> variables;
    private final Map<Player, CatcherSession> catcherSessions;

    public ActionEngine(GFMenu plugin, MenuManager menuManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.variables = new HashMap<>();
        this.catcherSessions = new HashMap<>();
    }

    public void executeActions(Player player, List<String> rawActions, LayoutMenuData menuData) {
        executeActions(player, rawActions, menuData, new HashMap<>());
    }

    public void executeActions(Player player, List<String> rawActions,
                               LayoutMenuData menuData, Map<String, String> localVars) {
        if (rawActions == null || rawActions.isEmpty()) return;

        for (String rawAction : rawActions) {
            ActionType type = ActionType.fromString(rawAction);

            if (type == null) {
                plugin.getLogger().warning("未知的动作类型: " + rawAction);
                continue;
            }

            // 替换变量
            String processedAction = replaceVariables(rawAction, player, localVars);

            switch (type) {
                case COMMAND:
                    new CommandAction().execute(player, processedAction, this, localVars);
                    break;
                case MESSAGE:
                    new MessageAction().execute(player, processedAction, this, localVars);
                    break;
                case CLOSE:
                    new CloseAction().execute(player, processedAction, this, localVars);
                    break;
                case SOUND:
                    new SoundAction().execute(player, processedAction, this, localVars);
                    break;
                case OP_COMMAND:
                    executeOpCommand(player, processedAction);
                    break;
                case CHAT:
                    executeChat(player, processedAction);
                    break;
                case CATCHER:
                    new CatcherAction().execute(player, processedAction, this, localVars);
                    break;
                case CONDITION:
                    executeCondition(player, processedAction, menuData, localVars);
                    break;
            }
        }
    }

    private String replaceVariables(String action, Player player, Map<String, String> localVars) {
        String result = action;

        // 替换玩家相关变量
        result = result.replace("%player%", player.getName());
        result = result.replace("%player_displayname%", player.getDisplayName());
        result = result.replace("%player_world%", player.getWorld().getName());

        // 替换本地变量
        for (Map.Entry<String, String> entry : localVars.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        // 替换全局变量
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%",
                    entry.getValue() != null ? entry.getValue().toString() : "");
        }

        return result;
    }

    private void executeOpCommand(Player player, String action) {
        String command = action.substring(3).trim();
        boolean wasOp = player.isOp();
        try {
            player.setOp(true);
            player.performCommand(command);
        } finally {
            player.setOp(wasOp);
        }
    }

    private void executeChat(Player player, String action) {
        String message = action.substring(5).trim();
        player.chat(message);
    }

    private void executeCondition(Player player, String action,
                                  LayoutMenuData menuData, Map<String, String> localVars) {
        // 简单的条件判断实现
        // 格式: condition:expression?actions:denyActions
        String conditionStr = action.substring(10).trim();

        // 解析条件表达式和动作
        String[] parts = conditionStr.split("\\?");
        if (parts.length < 2) return;

        String expression = parts[0].trim();
        String actionsPart = parts[1].trim();

        // 分离通过和拒绝的动作
        String[] actionGroups = actionsPart.split(":");
        String successActions = actionGroups[0];
        String denyActions = actionGroups.length > 1 ? actionGroups[1] : "";

        // 评估条件 (简化版本)
        boolean conditionMet = evaluateCondition(expression, player);

        if (conditionMet) {
            // 执行通过的动作
            List<String> actions = parseActionList(successActions);
            executeActions(player, actions, menuData, localVars);
        } else if (!denyActions.isEmpty()) {
            // 执行拒绝的动作
            List<String> actions = parseActionList(denyActions);
            executeActions(player, actions, menuData, localVars);
        }
    }

    private boolean evaluateCondition(String expression, Player player) {
        // 简单的条件评估
        if (expression.contains("==")) {
            String[] parts = expression.split("==");
            if (parts.length == 2) {
                String left = replaceVariables(parts[0].trim(), player, new HashMap<>());
                String right = replaceVariables(parts[1].trim(), player, new HashMap<>());
                return left.equals(right);
            }
        } else if (expression.contains("!=")) {
            String[] parts = expression.split("!=");
            if (parts.length == 2) {
                String left = replaceVariables(parts[0].trim(), player, new HashMap<>());
                String right = replaceVariables(parts[1].trim(), player, new HashMap<>());
                return !left.equals(right);
            }
        } else if (expression.contains(" contains ")) {
            String[] parts = expression.split(" contains ");
            if (parts.length == 2) {
                String left = replaceVariables(parts[0].trim(), player, new HashMap<>());
                String right = replaceVariables(parts[1].trim(), player, new HashMap<>());
                return left.contains(right);
            }
        }

        // 默认为 true
        return true;
    }

    private List<String> parseActionList(String actionsStr) {
        List<String> actions = new ArrayList<>();
        if (actionsStr == null || actionsStr.isEmpty()) return actions;

        // 简单的动作解析，按逗号分割
        String[] actionArray = actionsStr.split(",");
        for (String action : actionArray) {
            action = action.trim();
            if (!action.isEmpty()) {
                actions.add(action);
            }
        }
        return actions;
    }

    // 捕获会话管理
    public void startCatcherSession(Player player, CatcherSession session) {
        catcherSessions.put(player, session);

        // 设置超时（30秒后自动取消）
        new BukkitRunnable() {
            @Override
            public void run() {
                CatcherSession activeSession = catcherSessions.get(player);
                if (activeSession == session) {
                    catcherSessions.remove(player);
                    player.sendMessage("§c输入超时，已取消。");
                }
            }
        }.runTaskLater(plugin, 20 * 30); // 30秒
    }

    public void endCatcherSession(Player player, String input) {
        CatcherSession session = catcherSessions.remove(player);
        if (session != null) {
            session.complete(input);
        }
    }

    public void cancelCatcherSession(Player player) {
        CatcherSession session = catcherSessions.remove(player);
        if (session != null) {
            session.cancel();
        }
    }

    public boolean hasActiveCatcher(Player player) {
        return catcherSessions.containsKey(player);
    }

    // 变量管理
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }

    public Object getVariable(String key) {
        return variables.get(key);
    }

    public void removeVariable(String key) {
        variables.remove(key);
    }

    // 捕获会话内部类
    public static class CatcherSession {
        private final String catcherName;
        private final List<String> onCompleteActions;
        private final List<String> onCancelActions;
        private final Player player;
        private final ActionEngine engine;

        public CatcherSession(Player player, String catcherName,
                              List<String> onCompleteActions,
                              List<String> onCancelActions,
                              ActionEngine engine) {
            this.player = player;
            this.catcherName = catcherName;
            this.onCompleteActions = onCompleteActions;
            this.onCancelActions = onCancelActions;
            this.engine = engine;
        }

        public void complete(String input) {
            // 设置输入变量
            Map<String, String> localVars = new HashMap<>();
            localVars.put("trmenu_meta_input-" + catcherName, input);
            localVars.put("input", input);

            // 执行完成动作
            engine.executeActions(player, onCompleteActions, null, localVars);
        }

        public void cancel() {
            // 执行取消动作
            engine.executeActions(player, onCancelActions, null, new HashMap<>());
        }
    }
}
