/*     */ package gofd.gFMenu.menu;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ public class TrMenuValidation
/*     */ {
/* 311 */   private final List<String> errors = new ArrayList<>();
/* 312 */   private final List<String> warnings = new ArrayList<>();
/*     */   
/*     */   public void addError(String error) {
/* 315 */     this.errors.add(error);
/*     */   }
/*     */   
/*     */   public void addWarning(String warning) {
/* 319 */     this.warnings.add(warning);
/*     */   }
/*     */   
/*     */   public boolean isValid() {
/* 323 */     return this.errors.isEmpty();
/*     */   }
/*     */   
/*     */   public List<String> getErrors() {
/* 327 */     return this.errors;
/*     */   }
/*     */   
/*     */   public List<String> getWarnings() {
/* 331 */     return this.warnings;
/*     */   }
/*     */   
/*     */   public void setValid(boolean valid) {
/* 335 */     this.errors.clear();
/* 336 */     this.warnings.clear();
/*     */   }
/*     */ }
