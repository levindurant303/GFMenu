package gofd.gFMenu.menu.actions.actions;

import gofd.gFMenu.menu.actions.Action;
import gofd.gFMenu.menu.actions.ActionEngine;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import java.util.Map;

public class SoundAction implements Action {

    @Override
    public void execute(Player player, String action, ActionEngine engine, Map<String, String> variables) {
        String soundData = action.substring(6).trim(); // 去掉 "sound:"

        try {
            // 解析音效数据 格式: SOUND_NAME-VOLUME-PITCH
            String[] parts = soundData.split("-");
            if (parts.length >= 1) {
                String soundName = parts[0].trim();
                float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
                float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;

                // 尝试获取音效
                Sound sound = null;
                try {
                    sound = Sound.valueOf(soundName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // 尝试使用 Bukkit 的音效枚举
                    for (Sound s : Sound.values()) {
                        if (s.name().equalsIgnoreCase(soundName)) {
                            sound = s;
                            break;
                        }
                    }
                }

                if (sound != null) {
                    player.playSound(player.getLocation(), sound, volume, pitch);
                }
            }
        } catch (Exception e) {
            // 忽略音效解析错误
        }
    }

    @Override
    public boolean canExecute(Player player, String action) {
        return action.startsWith("sound:");
    }
}
