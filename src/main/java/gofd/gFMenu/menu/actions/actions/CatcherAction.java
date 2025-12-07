package gofd.gFMenu.menu.actions.actions;

import gofd.gFMenu.GFMenu;
import gofd.gFMenu.menu.actions.Action;
import gofd.gFMenu.menu.actions.ActionEngine;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CatcherAction implements Action, Listener {

    private final GFMenu plugin;
    private final ActionEngine actionEngine;

    public CatcherAction() {
        this.plugin = GFMenu.getInstance();
        this.actionEngine = plugin.getMenuManager().getActionEngine();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void execute(Player player, String action, ActionEngine engine, Map<String, String> variables) {
        // 解析 catcher 配置
        // 格式: catcher:{name}:{type}:{start}:{cancel}:{end}
        String catcherConfig = action.substring(8).trim(); // 去掉 "catcher:"

        try {
            // 使用正则表达式解析复杂结构
            Pattern pattern = Pattern.compile("(\\w+):\\s*\\{([^}]*)\\}");
            Matcher matcher = pattern.matcher(catcherConfig);

            String catcherName = null;
            String type = "CHAT";
            String startMessage = "请输入内容:";
            String cancelMessage = "已取消";
            List<String> endActions = new ArrayList<>();
            List<String> denyActions = new ArrayList<>();

            while (matcher.find()) {
                String key = matcher.group(1);
                String value = matcher.group(2);

                switch (key.toLowerCase()) {
                    case "type":
                        type = value.trim();
                        break;
                    case "start":
                        startMessage = value.replace("tell:", "").trim();
                        break;
                    case "cancel":
                        cancelMessage = value.replace("tell:", "").trim();
                        break;
                    case "end":
                        // 解析结束动作，可能包含condition
                        parseEndActions(value, endActions, denyActions);
                        break;
                    default:
                        catcherName = key;
                }
            }

            if (catcherName == null) {
                // 尝试简单解析
                String[] parts = catcherConfig.split(":");
                if (parts.length > 0) {
                    catcherName = parts[0].trim();
                }
            }

            if (catcherName == null) {
                player.sendMessage("§c捕获配置错误");
                return;
            }

            // 关闭菜单
            player.closeInventory();

            // 发送开始消息
            player.sendMessage("§e" + startMessage.replace("&", "§"));

            // 创建捕获会话
            ActionEngine.CatcherSession session =
                    new ActionEngine.CatcherSession(player, catcherName, endActions,
                            Arrays.asList("tell: " + cancelMessage), engine);

            actionEngine.startCatcherSession(player, session);

        } catch (Exception e) {
            player.sendMessage("§c捕获配置解析错误");
            e.printStackTrace();
        }
    }

    private void parseEndActions(String endConfig, List<String> actions, List<String> denyActions) {
        // 查找 condition 块
        if (endConfig.contains("condition:")) {
            Pattern condPattern = Pattern.compile("condition:\\s*\\{([^}]*)\\}");
            Matcher condMatcher = condPattern.matcher(endConfig);

            if (condMatcher.find()) {
                String conditionBlock = condMatcher.group(1);

                // 解析 actions 和 deny
                Pattern actionPattern = Pattern.compile("actions:\\s*\\[([^\\]]*)\\]");
                Pattern denyPattern = Pattern.compile("deny:\\s*\\[([^\\]]*)\\]");

                Matcher actionMatcher = actionPattern.matcher(conditionBlock);
                if (actionMatcher.find()) {
                    String actionStr = actionMatcher.group(1);
                    parseActionArray(actionStr, actions);
                }

                Matcher denyMatcher = denyPattern.matcher(conditionBlock);
                if (denyMatcher.find()) {
                    String denyStr = denyMatcher.group(1);
                    parseActionArray(denyStr, denyActions);
                }
            }
        } else {
            // 没有 condition，直接解析动作
            parseActionArray(endConfig, actions);
        }
    }

    private void parseActionArray(String actionStr, List<String> actions) {
        // 简单解析动作数组，格式: [action1, action2, ...]
        String cleanStr = actionStr.trim();
        if (cleanStr.startsWith("[") && cleanStr.endsWith("]")) {
            cleanStr = cleanStr.substring(1, cleanStr.length() - 1);
        }

        String[] actionArray = cleanStr.split(",");
        for (String action : actionArray) {
            action = action.trim();
            if (!action.isEmpty() && !action.equals("'") && !action.equals("\"")) {
                actions.add(action);
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (actionEngine.hasActiveCatcher(player)) {
            event.setCancelled(true);

            String message = event.getMessage();

            // 在主线程处理
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (message.equalsIgnoreCase("cancel")) {
                        actionEngine.cancelCatcherSession(player);
                    } else {
                        actionEngine.endCatcherSession(player, message);
                    }
                }
            }.runTask(plugin);
        }
    }

    @Override
    public boolean canExecute(Player player, String action) {
        return action.startsWith("catcher:");
    }
}
