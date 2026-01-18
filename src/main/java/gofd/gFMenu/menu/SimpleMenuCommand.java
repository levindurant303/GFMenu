/*     */ package gofd.gFMenu.menu;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import org.bukkit.command.Command;
/*     */ import org.bukkit.command.CommandSender;
/*     */ import org.bukkit.entity.Player;

/*     */ public class SimpleMenuCommand
/*     */   extends Command
/*     */ {
/*     */   private final MenuManager menuManager;
/*     */   private final String menuName;
/*     */   
/*     */   public SimpleMenuCommand(String name, MenuManager menuManager, String menuName) {
/* 217 */     super(name);
/* 218 */     this.menuManager = menuManager;
/* 219 */     this.menuName = menuName;
/*     */   }
/*     */ 
/*     */   
/*     */   public boolean execute(CommandSender sender, String label, String[] args) {
/* 224 */     if (!(sender instanceof Player)) {
/* 225 */       sender.sendMessage("§c只有玩家可以使用此命令。");
/* 226 */       return true;
/*     */     } 
/*     */     
/* 229 */     Player player = (Player)sender;
/*     */ 
/*     */     
/* 232 */     if (getPermission() != null && !player.hasPermission(getPermission())) {
/* 233 */       player.sendMessage("§c你没有权限使用此命令。");
/* 234 */       return true;
/*     */     } 
/*     */ 
/*     */     
/* 238 */     this.menuManager.openMenu(player, this.menuName);
/* 239 */     return true;
/*     */   }
/*     */ 
/*     */   
/*     */   public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
/* 244 */     return new ArrayList<>();
/*     */   }
/*     */ }


