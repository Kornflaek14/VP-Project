import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.cardgame.CardBattlerGame;
import com.cardgame.data.CharacterData;
import com.cardgame.logic.GameState;
import com.cardgame.logic.RunManager;
import com.cardgame.logic.monsters.FleshAmalgam;
import com.cardgame.logic.monsters.AbstractMonster;
import com.cardgame.logic.monsters.Boss;
import com.cardgame.logic.monsters.CrawlingEye;
import com.cardgame.logic.monsters.FrenziedPatient;
import com.cardgame.logic.monsters.MonsterGroup;
import com.cardgame.logic.potions.AdrenalineSyringe;
import com.cardgame.screens.BattleScreen;
import com.cardgame.ui.CombatMapOverlay;
import com.cardgame.ui.CombatUiAssets;
import com.cardgame.ui.DamageLabel;
import com.cardgame.ui.EnemyAnimation;
import com.cardgame.ui.HUD;
import com.cardgame.ui.PileViewerOverlay;

import java.lang.reflect.Field;
import java.util.Map;

/** Hidden desktop render harness; writes combat, map and deck screenshots, then exits. */
public class PreviewCombatUi extends CardBattlerGame {
    private final String output;
    private BattleScreen battle;
    private GameState state;
    private int frames;

    public PreviewCombatUi(String output) { this.output = output; }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Combat UI visual verification");
        config.setWindowedMode(1440, 900);
        config.setInitialVisible(false);
        config.disableAudio(true);
        config.setForegroundFPS(60);
        config.setIdleFPS(60);
        new Lwjgl3Application(new PreviewCombatUi(args[0]), config);
    }

    @Override public void create() {
        RunManager run = RunManager.getInstance();
        run.startNewRun(new CharacterData("The Patient", "Character sprite/Protag/idle/ChatGPT Image Sep 12, 2026, 09_47_47 PM_06.png", "", "", 3, 80, 99, "", ""),
                getAllCards().subList(0, 10));
        run.addPotion(new AdrenalineSyringe());
        run.setLastVisitedNodeId(run.getReachableNodeIds().get(0));
        run.getPathTaken().add(run.getLastVisitedNodeId());
        FrenziedPatient first = new FrenziedPatient(950f, 280f);
        FleshAmalgam second = new FleshAmalgam(1250f, 280f);
        battle = new BattleScreen(this, new MonsterGroup(first, second));
        setScreen(battle);
        state = (GameState) field(battle, "gameState");
        state.playerHp = 52;
        state.playerBlock = 8;
        first.currentHp = 36;
        first.block = 16;
        first.intentType = "BUFF";
        second.currentHp = 32;
        second.intentType = "ATTACK_DEFEND";
        second.intentValue = 7;
    }

    @Override public void render() {
        super.render();
        frames++;
        if (frames == 40) capture("combat.png");
        if (frames == 45) navigation().onMapClicked();
        if (frames == 60) capture("map.png");
        if (frames == 65) ((CombatMapOverlay) field(battle, "mapPreview")).hide();
        if (frames == 70) navigation().onDeckClicked();
        if (frames == 85) capture("deck.png");
        if (frames == 90) {
            ((PileViewerOverlay) field(battle, "pileViewer")).hide();
            if (state != field(battle, "gameState") || state.playerHp != 52 || state.playerBlock != 8) {
                throw new IllegalStateException("Inspection changed combat state");
            }
            Gdx.app.log("Preview", "Combat state preserved across map/deck inspection.");
        }
        if (frames == 92) {
            CombatUiAssets art = (CombatUiAssets) field(battle, "combatUi");
            Group effects = (Group) field(battle, "effectsLayer");
            DamageLabel damage = new DamageLabel("-18", Color.ORANGE, art.damageBadge, true);
            damage.setPosition(930f, 540f);
            effects.addActor(damage);
            DamageLabel block = new DamageLabel("+12", Color.CYAN, art.blockBadge, false);
            block.setPosition(280f, 520f);
            effects.addActor(block);
        }
        if (frames == 105) capture("effects.png");
        if (frames == 115) enemyPose("enemy-idle.png", "IDLE", 0f);
        if (frames == 116) enemyPose("enemy-windup.png", "ATTACK", 0f);
        if (frames == 117) enemyPose("enemy-attack.png", "ATTACK", 0.24f);
        if (frames == 118) enemyPose("enemy-hurt.png", "HURT", 0f);
        if (frames == 125) {
            battle = new BattleScreen(this, new MonsterGroup(new CrawlingEye(850f, 280f), new CrawlingEye(1150f, 280f)));
            setScreen(battle);
            state = (GameState) field(battle, "gameState");
            enemyPose("enemy-pair.png", "IDLE", 0f);
        }
        if (frames == 126) enemyPose("enemy-pair-attack.png", "ATTACK", 0.12f);
        if (frames == 130) {
            new com.cardgame.logic.rooms.BossRoom().onPlayerEntry(this);
            battle = (BattleScreen) getScreen();
            state = (GameState) field(battle, "gameState");
            if (!(state.monsterGroup.monsters.get(0) instanceof Boss)) {
                throw new IllegalStateException("Boss room did not spawn the boss");
            }
            enemyPose("enemy-boss.png", "IDLE", 0f);
        }
        if (frames == 131) enemyPose("enemy-boss-attack.png", "ATTACK", 0.12f);
        if (frames == 132) enemyPose("enemy-boss-buff.png", "BUFF", 0.45f);
        if (frames == 133) enemyPose("enemy-boss-buff-final.png", "BUFF", 0.65f);
        if (frames == 140) {
            try {
                java.lang.reflect.Method init = CardBattlerGame.class.getDeclaredMethod("initDevMode");
                init.setAccessible(true);
                init.invoke(this);
                toggleDevMode();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
        if (frames == 145) capture("developer-console.png");
        if (frames == 150) {
            toggleDevMode();
            setScreen(new com.cardgame.screens.MapScreen(this));
        }
        if (frames == 165) capture("asylum-map.png");
        if (frames == 170) {
            RunManager run = RunManager.getInstance();
            int next = run.getReachableNodeIds().get(0);
            run.setLastVisitedNodeId(next);
            run.getPathTaken().add(next);
            setScreen(new com.cardgame.screens.MapScreen(this));
        }
        if (frames == 185) capture("asylum-map-traveled.png");
        if (frames == 190) setScreen(new com.cardgame.screens.ShopScreen(this));
        if (frames == 205) capture("pharmacy-shop.png");
        if (frames == 206) {
            com.badlogic.gdx.scenes.scene2d.Stage shopStage =
                    (com.badlogic.gdx.scenes.scene2d.Stage) field(getScreen(), "stage");
            shopStage.mouseMoved(520, 290);
        }
        if (frames == 209) capture("pharmacy-shop-inspect.png");
        if (frames == 210) {
            RunManager run = RunManager.getInstance();
            int goldBefore = run.getGold();
            int cardsBefore = run.getDeck().size();
            com.badlogic.gdx.scenes.scene2d.Stage shopStage =
                    (com.badlogic.gdx.scenes.scene2d.Stage) field(getScreen(), "stage");
            com.badlogic.gdx.scenes.scene2d.ui.TextButton buy = shopStage.getRoot().findActor("card-offer-0");
            buy.fire(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent());
            if (run.getDeck().size() != cardsBefore + 1 || run.getGold() >= goldBefore) {
                throw new IllegalStateException("Shop purchase failed");
            }
            int goldAfter = run.getGold();
            buy.fire(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent());
            if (run.getDeck().size() != cardsBefore + 1 || run.getGold() != goldAfter) {
                throw new IllegalStateException("Sold offer charged twice");
            }
        }
        if (frames == 220) capture("pharmacy-shop-purchased.png");
        if (frames == 225) {
            RunManager run = RunManager.getInstance();
            run.addGold(300);
            run.getPotions().clear();
            for (int i = 0; i < 3; i++) run.addPotion(new com.cardgame.logic.potions.ManaPotion());
            setScreen(new com.cardgame.screens.ShopScreen(this));
        }
        if (frames == 240) capture("pharmacy-shop-full.png");
        if (frames == 245) setScreen(new com.cardgame.screens.RewardScreen(this));
        if (frames == 260) capture("rewards.png");
        if (frames == 262) {
            com.badlogic.gdx.scenes.scene2d.Stage rewardStage =
                    (com.badlogic.gdx.scenes.scene2d.Stage) field(getScreen(), "stage");
            com.badlogic.gdx.scenes.scene2d.ui.TextButton claim = rewardStage.getRoot().findActor("claim-gold");
            int before = RunManager.getInstance().getGold();
            claim.fire(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent());
            int after = RunManager.getInstance().getGold();
            claim.fire(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent());
            if (after <= before || RunManager.getInstance().getGold() != after) {
                throw new IllegalStateException("Gold reward did not claim exactly once");
            }
        }
        if (frames == 265) setScreen(new com.cardgame.screens.RestScreen(this));
        if (frames == 280) capture("rest-site.png");
        if (frames == 285) Gdx.app.exit();
    }

    private void enemyPose(String filename, String stateName, float time) {
        @SuppressWarnings("unchecked")
        Map<AbstractMonster, EnemyAnimation> animations =
                (Map<AbstractMonster, EnemyAnimation>) field(battle, "enemyAnimations");
        if (animations.size() != state.monsterGroup.monsters.size()) {
            throw new IllegalStateException("Not all enemies have animations");
        }
        for (EnemyAnimation animation : animations.values()) {
            animation.play(EnemyAnimation.State.valueOf(stateName));
            animation.update(time);
            if (animation.frame() == null) throw new IllegalStateException("Enemy pose failed to load");
        }
        battle.render(0f);
        capture(filename);
    }

    private HUD.NavigationCallback navigation() {
        return (HUD.NavigationCallback) field(field(battle, "hud"), "navigationCallback");
    }

    private void capture(String name) {
        Pixmap screenshot = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        PixmapIO.writePNG(new FileHandle(output + "/" + name), screenshot, -1, true);
        screenshot.dispose();
    }

    private static Object field(Object target, String name) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
