package gofd.gFMenu.menu.actions;

public enum ActionType {
    COMMAND("command:"),
    MESSAGE("tell:"),
    CLOSE("close"),
    SOUND("sound:"),
    OP_COMMAND("op:"),
    CHAT("chat:"),
    CATCHER("catcher:"),
    CONDITION("condition:");

    private final String prefix;

    ActionType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public static ActionType fromString(String action) {
        for (ActionType type : values()) {
            if (action.startsWith(type.prefix) || action.equals(type.prefix.trim())) {
                return type;
            }
        }
        return null;
    }
}

