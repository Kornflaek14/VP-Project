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
import com.cardgame.logic.monsters.FrenziedPatient;
import com.cardgame.logic.monsters.MonsterGroup;
import com.cardgame.logic.potions.AdrenalineSyringe;
import com.cardgame.screens.BattleScreen;
import com.cardgame.ui.CombatMapOverlay;
import com.cardgame.ui.CombatUiAssets;
import com.cardgame.ui.DamageLabel;
import com.cardgame.ui.HUD;
import com.cardgame.ui.PileViewerOverlay;

import java.lang.reflect.Field;

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
        run.startNewRun(new CharacterData("Ironclad", "IMAGES/play/character.png", "", "", 3, 80, 99, "", ""),
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
        if (frames == 150) Gdx.app.exit();
    }

    private void enemyPose(String filename, String stateName, float time) {
        try {
            Field animationState = BattleScreen.class.getDeclaredField("enemyState");
            animationState.setAccessible(true);
            for (Object value : animationState.getType().getEnumConstants()) {
                if (value.toString().equals(stateName)) animationState.set(battle, value);
            }
            Field timer = BattleScreen.class.getDeclaredField("enemyStateTime");
            timer.setAccessible(true);
            timer.setFloat(battle, time);
            if (field(battle, "enemyIdleAnim") == null || field(battle, "enemyAttackAnim") == null
                    || field(battle, "enemyHurtAnim") == null) {
                throw new IllegalStateException("Enemy poses failed to load");
            }
            battle.render(0f);
            capture(filename);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
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
