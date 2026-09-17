package com.cardgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cardgame.CardBattlerGame;
import com.cardgame.logic.cards.AbstractCard;
import com.cardgame.logic.CombatResolver;
import com.cardgame.logic.GameState;
import com.cardgame.logic.RunManager;
import com.cardgame.logic.TurnManager;
import com.cardgame.logic.events.GameEvent;
import com.cardgame.logic.events.BlockGainedEvent;
import com.cardgame.logic.events.CardPlayedEvent;
import com.cardgame.logic.events.DamageDealtEvent;
import com.cardgame.logic.events.PlayerDamagedEvent;
import com.cardgame.logic.monsters.AbstractMonster;
import com.cardgame.logic.monsters.FrenziedPatient;
import com.cardgame.logic.monsters.HeadNurse;
import com.cardgame.logic.monsters.MaskedPatient;
import com.cardgame.ui.CardActor;
import com.cardgame.ui.CombatHealthBar;
import com.cardgame.ui.CombatMapOverlay;
import com.cardgame.ui.CombatUiAssets;
import com.cardgame.ui.DamageLabel;
import com.cardgame.ui.EnemyAnimation;
import com.cardgame.ui.HUD;
import com.cardgame.ui.HandArea;
import com.cardgame.ui.PauseOverlay;
import com.cardgame.ui.PileViewerOverlay;
import com.cardgame.ui.SlashAnimationActor;
import com.cardgame.ui.TargetingArrow;
import com.cardgame.ui.UiTheme;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BattleScreen implements Screen {

    private final CardBattlerGame game;
    private Stage stage;
    private Texture bgTexture;
    private Texture slashSheetTex;
    private CombatUiAssets combatUi;
    private TextureRegion combatTex;
    private TextureRegion blockTex;
    private TextureRegion heartTex;
    private CombatMapOverlay mapPreview;
    private Animation<TextureRegion> slashAnimation;
    private Sound slashSound;
    private Sound uiSound;
    private Group effectsLayer;
    private float effectsRemaining;
    private boolean battleEnding;
    private int pendingWinner;
    private final Set<AbstractMonster> impactTargets = new HashSet<>();
    
    private Texture playerTexture;
    private static final String[] PLAYER_IDLE_FRAME_FILES = {
        "Character sprite/Protag/idle/ChatGPT Image Sep 12, 2026, 09_47_47 PM_06.png",
        "Character sprite/Protag/idle/ChatGPT Image Sep 12, 2026, 09_47_47 PM_08.png",
        "Character sprite/Protag/idle/ChatGPT Image Sep 12, 2026, 09_47_47 PM_10.png",
        "Character sprite/Protag/idle/ChatGPT Image Sep 12, 2026, 09_47_47 PM_12.png"
    };

    private static final String[] BOSS_IDLE_FRAME_FILES = {
        "Character sprite/Enemies/Boss/idle/idle1.png",
        "Character sprite/Enemies/Boss/idle/idle2.png",
        "Character sprite/Enemies/Boss/idle/idle3.png",
        "Character sprite/Enemies/Boss/idle/idle4.png"
    };
    private static final String[] BOSS_ATTACK_FRAME_FILES = {
        "Character sprite/Enemies/Boss/attack/attack1.png",
        "Character sprite/Enemies/Boss/attack/attack2.png",
        "Character sprite/Enemies/Boss/attack/attack3.png",
        "Character sprite/Enemies/Boss/attack/attack4.png"
    };
    private static final String[] COMMON_CHAINED_IDLE_FILES = {
        "Character sprite/Enemies/common enemy/chained/idle/ChatGPT Image Sep 16, 2026, 11_13_41 PM.png"
    };
    private static final String[] COMMON_CHAINED_ATTACK_FILES = {
        "Character sprite/Enemies/common enemy/chained/attack/attack1.png",
        "Character sprite/Enemies/common enemy/chained/attack/attack2.png",
        "Character sprite/Enemies/common enemy/chained/attack/attack3.png",
        "Character sprite/Enemies/common enemy/chained/attack/attack4.png"
    };

    private static final String[] MASKED_IDLE_FRAME_FILES = {
        "Character sprite/Enemies/Elite enemy/masked/idle/Idle.png"
    };
    private static final String[] MASKED_ATTACK_FRAME_FILES = {
        "Character sprite/Enemies/Elite enemy/masked/attack/attack1.png",
        "Character sprite/Enemies/Elite enemy/masked/attack/attack2.png",
        "Character sprite/Enemies/Elite enemy/masked/attack/attack3.png",
        "Character sprite/Enemies/Elite enemy/masked/attack/attack4.png"
    };

    private static final String[] NURSE_IDLE_FRAME_FILES = {
        "Character sprite/Enemies/Elite enemy/nurse/idle/Idle.png"
    };
    private static final String[] NURSE_ATTACK_FRAME_FILES = {
        "Character sprite/Enemies/Elite enemy/nurse/attack/ChatGPT_Image_Sep_17_2026_04_44_31_AM_1.png",
        "Character sprite/Enemies/Elite enemy/nurse/attack/attack2.png",
        "Character sprite/Enemies/Elite enemy/nurse/attack/attack3.png",
        "Character sprite/Enemies/Elite enemy/nurse/attack/ChatGPT_Image_Sep_17_2026_04_44_32_AM_4.png"
    };

    private final List<Texture> playerAnimationTextures = new ArrayList<>();
    private final List<Texture> enemyAnimationTextures = new ArrayList<>();
    private Animation<TextureRegion> playerIdleAnim;
    private Animation<TextureRegion> playerAttackAnim;
    private Animation<TextureRegion> playerHurtAnim;
    private Animation<TextureRegion> playerDefendAnim;
    private final Map<AbstractMonster, EnemyAnimation> enemyAnimations = new HashMap<>();

    private enum PlayerAnimState { IDLE, ATTACK, HURT, DEFEND }

    private PlayerAnimState playerState = PlayerAnimState.IDLE;
    private float playerStateTime = 0f;
    // Unified scale: computed once across ALL animation states so every pose
    // displays at the same size regardless of individual frame dimensions.
    private float playerUnifiedScale = 1f;

    private GameState gameState;
    private TurnManager turnManager;
    private CombatResolver combatResolver;

    private HandArea handArea;
    private TargetingArrow targetingArrow;
    private HUD hud;

    private PauseOverlay pauseOverlay;
    private boolean paused = false;

    // Screen shake
    private float shakeTimer = 0f;
    private static final float SHAKE_DURATION = 0.35f;
    private static final float SHAKE_INTENSITY = 8f;
    private float shakeOffsetX = 0f;
    private float shakeOffsetY = 0f;

    private CombatHealthBar monsterHealthBar;
    private com.badlogic.gdx.graphics.g2d.BitmapFont monsterFont;
    private com.badlogic.gdx.graphics.g2d.BitmapFont monsterSmallFont;

    // Pile viewer
    private PileViewerOverlay pileViewer;

    private com.cardgame.logic.monsters.MonsterGroup monsters;
    private boolean isBossFight = false;

    public BattleScreen(CardBattlerGame game, com.cardgame.logic.monsters.MonsterGroup monsters) {
        this(game, monsters, false);
    }

    public BattleScreen(CardBattlerGame game, com.cardgame.logic.monsters.MonsterGroup monsters, boolean isBossFight) {
        this.game = game;
        this.monsters = monsters;
        this.isBossFight = isBossFight;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        battleEnding = false;
        effectsRemaining = 0f;
        impactTargets.clear();
        loadSlashEffect();
        loadCombatIcons();

        try {
            bgTexture = new Texture(Gdx.files.internal("IMAGES/Backgrounds/battle1.png"));
        } catch (Exception e) {}

        gameState = new GameState();
        turnManager = new TurnManager();
        combatResolver = new CombatResolver();

        // Init player from RunManager
        RunManager rm = RunManager.getInstance();
        gameState.initPlayer(rm.getCurrentHp(), rm.getMaxHp(),
                rm.getSelectedCharacter().energy(), rm.getDeck());

        // Load player character texture
        try {
            String charImage = rm.getSelectedCharacter().image();
            if (charImage != null && !charImage.isEmpty()) {
                playerTexture = new Texture(Gdx.files.internal(charImage));
            }
        } catch (Exception e) {
            try { playerTexture = new Texture(Gdx.files.internal("IMAGES/play/character.png")); } catch (Exception e2) {}
        }

        loadPlayerAnimation();

        // Init random monster for current floor level
        
        gameState.initMonsters(this.monsters);
        loadEnemyAnimation();

        monsterHealthBar = new CombatHealthBar();

        monsterFont = UiTheme.font(14f);
        monsterSmallFont = UiTheme.font(13f);

        buildUI();
        buildPauseOverlay();

        // Set up input
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    if (mapPreview != null && mapPreview.isVisible()) mapPreview.hide();
                    else if (pileViewer != null && pileViewer.isVisible()) pileViewer.hide();
                    else togglePause();
                    return true;
                }
                return false;
            }
        });
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

        // Start combat
        turnManager.startCombat(gameState);
        updateUI();
    }

    private void loadCombatIcons() {
        combatUi = new CombatUiAssets();
        combatTex = combatUi.damageBadge;
        blockTex = combatUi.blockBadge;
        heartTex = combatUi.heart;
    }

    private void loadPlayerAnimation() {
        disposePlayerAnimation();
        setPlayerState(PlayerAnimState.IDLE);
        playerIdleAnim = loadAnimation(PLAYER_IDLE_FRAME_FILES, 0.4f, true, playerAnimationTextures);
        playerAttackAnim = loadAnimation(playerFramePaths("attack", new String[] {
                "frame 1.png", "frame 2.png", "frame 3.png", "frame 4.png"
        }), 0.12f, false, playerAnimationTextures);
        playerHurtAnim = loadAnimation(playerFramePaths("hurt", new String[] {
                "ChatGPT Image Sep 12, 2026, 11_12_03 PM_02.png",
                "ChatGPT Image Sep 12, 2026, 11_12_03 PM_03.png",
                "ChatGPT Image Sep 12, 2026, 11_12_03 PM_04.png",
                "ChatGPT Image Sep 12, 2026, 11_12_03 PM_06.png"
        }), 0.15f, false, playerAnimationTextures);
        playerDefendAnim = loadAnimation(playerFramePaths("defend", new String[] {
                "1.png", "2.png", "3.png", "4.png"
        }), 0.35f, false, playerAnimationTextures);
        // One scale for ALL states so no pose shrinks or grows relative to the others.
        playerUnifiedScale = unifiedScale(400f, 480f,
                playerIdleAnim, playerAttackAnim, playerHurtAnim, playerDefendAnim);
    }

    private void loadSlashEffect() {
        try {
            slashSheetTex = new Texture(Gdx.files.internal("IMAGES/play/slash_anim.png"));
            slashSheetTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            slashAnimation = SlashAnimationActor.frames(slashSheetTex, 6, 1);
        } catch (Exception e) {
            Gdx.app.error("BattleScreen", "Could not load slash effect", e);
            if (slashSheetTex != null) slashSheetTex.dispose();
            slashSheetTex = null;
            slashAnimation = null;
        }
        try {
            slashSound = Gdx.audio.newSound(Gdx.files.internal("audio/slash.wav"));
            uiSound = Gdx.audio.newSound(Gdx.files.internal("audio/slash.wav"));
        } catch (Exception e) {
            Gdx.app.log("BattleScreen", "Slash sound unavailable; visual effects remain enabled.");
        }
    }

    private String[] playerFramePaths(String action, String[] existingNames) {
        String folder = "Character sprite/Protag/" + action + "/";
        String[] paths = numberedFramePaths(folder, action);
        // Prefer the requested names; also support the frames already in the project.
        if (!Gdx.files.internal(paths[0]).exists()) {
            for (int i = 0; i < paths.length; i++) paths[i] = folder + existingNames[i];
        }
        return paths;
    }

    private static String[] numberedFramePaths(String folder, String action) {
        String[] paths = new String[4];
        for (int i = 0; i < paths.length; i++) paths[i] = folder + action + "_" + i + ".png";
        return paths;
    }

    private void loadEnemyAnimation() {
        disposeEnemyAnimation();
        Map<String, EnemyFrames> sets = new HashMap<>();
        for (AbstractMonster monster : monsters.monsters) {
            String kind = monster.isBoss() || (isBossFight && monster == monsters.monsters.get(0))
                    ? "boss"
                    : monster instanceof MaskedPatient ? "masked"
                    : monster instanceof HeadNurse ? "nurse"
                    : monster instanceof FrenziedPatient ? "patient" : "chained";
            EnemyFrames frames = sets.computeIfAbsent(kind, this::loadEnemyFrames);
            if (frames != null) {
                enemyAnimations.put(monster, new EnemyAnimation(frames.idle, frames.attack,
                        frames.hurt, frames.buff, frames.scale, frames.frameScales));
            }
        }
    }

    private record EnemyFrames(Animation<TextureRegion> idle, Animation<TextureRegion> attack,
            Animation<TextureRegion> hurt, Animation<TextureRegion> buff, float scale,
            Map<TextureRegion, Float> frameScales) {}

    private EnemyFrames loadEnemyFrames(String kind) {
        try {
            Animation<TextureRegion> idle;
            Animation<TextureRegion> attack;
            if ("patient".equals(kind)) {
                TextureRegion[] poses = new TextureRegion[4];
                for (int i = 0; i < poses.length; i++) {
                    poses[i] = loadEnemyPose("Character sprite/enemy/patient/pose-" + (i + 1) + ".png");
                }
                idle = new Animation<>(0.4f, poses[3]);
                attack = new Animation<>(0.12f, poses[0], poses[1], poses[2], poses[3]);
            } else if ("masked".equals(kind)) {
                idle = loadEnemyFrames(MASKED_IDLE_FRAME_FILES, 0.4f);
                attack = loadEnemyFrames(MASKED_ATTACK_FRAME_FILES, 0.12f);
            } else if ("nurse".equals(kind)) {
                idle = loadEnemyFrames(NURSE_IDLE_FRAME_FILES, 0.4f);
                attack = loadEnemyFrames(NURSE_ATTACK_FRAME_FILES, 0.12f);
            } else {
                boolean boss = "boss".equals(kind);
                idle = loadEnemyFrames(boss ? BOSS_IDLE_FRAME_FILES : COMMON_CHAINED_IDLE_FILES, 0.4f);
                attack = loadEnemyFrames(boss ? BOSS_ATTACK_FRAME_FILES : COMMON_CHAINED_ATTACK_FILES, 0.12f);
            }
            idle.setPlayMode(Animation.PlayMode.LOOP);
            // Reuse existing poses where no dedicated action art is supplied.
            Animation<TextureRegion> hurt = new Animation<>(0.15f, attack.getKeyFrame(0), idle.getKeyFrame(0));
            Animation<TextureRegion> buff = "boss".equals(kind)
                    ? loadEnemyFrames(new String[] {
                        "Character sprite/Enemies/Boss/buff/buff1.png",
                        "Character sprite/Enemies/Boss/buff/buff2.png",
                        "Character sprite/Enemies/Boss/buff/buff3.png",
                        "Character sprite/Enemies/Boss/buff/buff4.png"
                    }, 0.2f)
                    : new Animation<>(0.2f, idle.getKeyFrames());
            boolean isElite = "masked".equals(kind) || "nurse".equals(kind);
            float targetWidth = "boss".equals(kind) ? 420f : isElite ? 340f : 280f;
            float targetHeight = "boss".equals(kind) ? 380f : isElite ? 350f : 320f;
            float scale = unifiedScale(targetWidth, targetHeight, idle, attack);
            Map<TextureRegion, Float> frameScales = new HashMap<>();
            if (!"patient".equals(kind)) {
                // These exports use different canvas scales for idle and attack. Match body
                // height to the standing attack pose, retaining one scale across the attack.
                float height = targetHeight;
                for (TextureRegion frame : idle.getKeyFrames()) {
                    frameScales.put(frame, height / frame.getRegionHeight());
                }
                float attackScale = height / attack.getKeyFrame(0).getRegionHeight();
                for (TextureRegion frame : attack.getKeyFrames()) frameScales.put(frame, attackScale);
                if ("boss".equals(kind)) {
                    float buffScale = height / buff.getKeyFrame(0).getRegionHeight();
                    for (TextureRegion frame : buff.getKeyFrames()) frameScales.put(frame, buffScale);
                }
            }
            return new EnemyFrames(idle, attack, hurt, buff, scale, Map.copyOf(frameScales));
        } catch (RuntimeException e) {
            Gdx.app.error("BattleScreen", "Could not load " + kind + " poses; using static texture.", e);
            return null;
        }
    }

    private Animation<TextureRegion> loadEnemyFrames(String[] paths, float duration) {
        TextureRegion[] frames = new TextureRegion[paths.length];
        for (int i = 0; i < paths.length; i++) frames[i] = loadEnemyPose(paths[i]);
        return new Animation<>(duration, frames);
    }

    /** Ignore transparent canvas margins while retaining the original art and one shared scale. */
    private TextureRegion loadEnemyPose(String path) {
        Pixmap pixels = new Pixmap(Gdx.files.internal(path));
        try {
            int left = pixels.getWidth(), top = pixels.getHeight(), right = -1, bottom = -1;
            for (int y = 0; y < pixels.getHeight(); y++) {
                for (int x = 0; x < pixels.getWidth(); x++) {
                    if ((pixels.getPixel(x, y) & 0xff) <= 8) continue;
                    left = Math.min(left, x);
                    top = Math.min(top, y);
                    right = Math.max(right, x);
                    bottom = Math.max(bottom, y);
                }
            }
            if (right < left) throw new IllegalArgumentException("Empty enemy pose: " + path);
            Texture texture = new Texture(pixels, true);
            enemyAnimationTextures.add(texture);
            texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
            return new TextureRegion(texture, left, top, right - left + 1, bottom - top + 1);
        } finally {
            pixels.dispose();
        }
    }

    private Animation<TextureRegion> loadAnimation(String[] paths, float frameDuration,
            boolean looping, List<Texture> ownedTextures) {
        List<Texture> loaded = new ArrayList<>();
        try {
            TextureRegion[] frames = new TextureRegion[paths.length];
            // Each file is one complete frame; do not split individual poses again.
            for (int i = 0; i < paths.length; i++) {
                Texture texture = new Texture(Gdx.files.internal(paths[i]));
                loaded.add(texture);
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                frames[i] = new TextureRegion(texture);
            }
            Animation<TextureRegion> animation = new Animation<>(frameDuration, frames);
            animation.setPlayMode(looping ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
            ownedTextures.addAll(loaded);
            return animation;
        } catch (Exception e) {
            for (Texture texture : loaded) texture.dispose();
            Gdx.app.error("BattleScreen", "Could not load animation " + paths[0]
                    + "; using idle animation or static texture.", e);
            return null;
        }
    }

    private void disposePlayerAnimation() {
        playerIdleAnim = playerAttackAnim = playerHurtAnim = playerDefendAnim = null;
        for (Texture texture : playerAnimationTextures) texture.dispose();
        playerAnimationTextures.clear();
    }

    private void disposeEnemyAnimation() {
        enemyAnimations.clear();
        for (Texture texture : enemyAnimationTextures) texture.dispose();
        enemyAnimationTextures.clear();
    }

    private void setPlayerState(PlayerAnimState state) {
        playerState = state;
        playerStateTime = 0f;
    }

    private Animation<TextureRegion> currentPlayerAnimation() {
        return switch (playerState) {
            case IDLE -> playerIdleAnim;
            case ATTACK -> playerAttackAnim;
            case HURT -> playerHurtAnim;
            case DEFEND -> playerDefendAnim;
        };
    }

    private void updateAnimations(float delta) {
        if (paused) return;
        playerStateTime += delta;
        Animation<TextureRegion> playerAnimation = currentPlayerAnimation();
        if (playerState != PlayerAnimState.IDLE
                && (playerAnimation == null || playerAnimation.isAnimationFinished(playerStateTime))) {
            setPlayerState(PlayerAnimState.IDLE);
        }
        for (EnemyAnimation animation : enemyAnimations.values()) animation.update(delta);
    }

    /** Computes a unified scale across multiple animations so they all render at the same size. */
    @SafeVarargs
    private static float unifiedScale(float targetW, float targetH, Animation<TextureRegion>... anims) {
        int maxWidth = 1;
        int maxHeight = 1;
        for (Animation<TextureRegion> anim : anims) {
            if (anim == null) continue;
            for (TextureRegion frame : anim.getKeyFrames()) {
                maxWidth = Math.max(maxWidth, frame.getRegionWidth());
                maxHeight = Math.max(maxHeight, frame.getRegionHeight());
            }
        }
        return Math.min(targetW / maxWidth, targetH / maxHeight);
    }

    private void togglePause() {
        paused = !paused;
        pauseOverlay.toggle();
    }

    private void buildPauseOverlay() {
        pauseOverlay = new PauseOverlay(new PauseOverlay.PauseCallback() {
            @Override public void onResume()  { paused = false; pauseOverlay.hide(); }
            @Override public void onEndRun()  { game.setScreen(new MainMenuScreen(game)); }
            @Override public void onExitGame(){ Gdx.app.exit(); }
        });
        stage.addActor(pauseOverlay);
    }

    private void buildUI() {
        targetingArrow = new TargetingArrow();

        handArea = new HandArea(new CardActor.OnDragCallback() {
            @Override
            public void onDragStart(CardActor actor) {
                if (paused || battleEnding || !gameState.isPlayerTurn()) return;
                if (uiSound != null) uiSound.play(0.12f);
                // If it's an attack, start targeting
                if (actor.getCard().cardType() == com.cardgame.data.CardType.ATTACK) {
                    targetingArrow.start.set(actor.getX() + actor.getWidth() / 2f, actor.getY() + actor.getHeight() / 2f);
                    targetingArrow.end.set(targetingArrow.start);
                    targetingArrow.setVisible(true);
                }
            }

            @Override
            public void onDrag(CardActor actor, float x, float y) {
                if (paused || battleEnding || !gameState.isPlayerTurn()) return;
                if (targetingArrow.isVisible()) {
                    targetingArrow.start.set(actor.getX() + actor.getWidth() / 2f, actor.getY() + actor.getHeight() / 2f);
                    com.cardgame.logic.monsters.AbstractMonster hovered = getHoveredMonster(x, y);
                    if (hovered != null) {
                        targetingArrow.end.set(hovered.drawX, hovered.drawY + 160f); // snap to center
                    } else {
                        targetingArrow.end.set(x, y);
                    }
                }
            }

            @Override
            public void onDragStop(CardActor actor, float x, float y) {
                if (paused || battleEnding || !gameState.isPlayerTurn()) {
                    updateUI();
                    return;
                }
                targetingArrow.setVisible(false);
                if (uiSound != null) uiSound.play(0.16f);
                
                // Play zone threshold
                if (y > com.cardgame.utils.Constants.VIEWPORT_HEIGHT * 0.35f) {
                    AbstractCard card = actor.getCard();
                    if (card.cardType() == com.cardgame.data.CardType.ATTACK) {
                        com.cardgame.logic.monsters.AbstractMonster hovered = getHoveredMonster(x, y);
                        if (hovered != null) {
                            List<GameEvent> events = combatResolver.playCard(gameState, card, hovered);
                            processEvents(events, hovered);
                        }
                    } else {
                        List<GameEvent> events = combatResolver.playCard(gameState, card, null);
                        processEvents(events);
                    }
                }
                updateUI(); // Snap back or refresh hand
            }
        });
        
        stage.addActor(handArea);
        stage.addActor(targetingArrow);

        hud = new HUD(gameState, new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (paused || battleEnding) return;
                if (uiSound != null) uiSound.play(0.18f);
                if (gameState.isPlayerTurn()) {
                    animateEnemyTurn();
                    List<GameEvent> events = turnManager.endPlayerTurn(gameState);
                    processEvents(events);
                    updateUI();
                }
            }
        }, combatUi);

        // Wire potion slots
        hud.setPotionCallback(new HUD.PotionClickCallback() {
            @Override
            public void onPotionClicked(int slotIndex) {
                if (paused || battleEnding) return;
                if (uiSound != null) uiSound.play(0.14f);
                if (gameState.isPlayerTurn()) {
                    int previousHp = gameState.playerHp;
                    int previousBlock = gameState.playerBlock;
                    RunManager.getInstance().usePotion(slotIndex, gameState);
                    int healed = gameState.playerHp - previousHp;
                    int blockGained = gameState.playerBlock - previousBlock;
                    if (healed > 0) {
                        spawnDamageLabel("+" + healed, hud.getPlayerX(), hud.getCharY() + 340f,
                                Color.GREEN, heartTex, false);
                    }
                    if (blockGained > 0) {
                        spawnDamageLabel("+" + blockGained, hud.getPlayerX(), hud.getCharY() + 300f,
                                Color.CYAN, blockTex, false);
                    }
                    if (healed <= 0 && blockGained <= 0) {
                        spawnDamageLabel("POTION!", hud.getPlayerX(), hud.getCharY() + 320f, Color.GREEN);
                    }
                    updateUI();
                }
            }
        });

        stage.addActor(hud);

        // Effects stay above combat actors and below pile/pause overlays.
        effectsLayer = new Group();
        effectsLayer.setTouchable(Touchable.disabled);
        stage.addActor(effectsLayer);

        // Wire pile viewer
        pileViewer = new PileViewerOverlay();
        stage.addActor(pileViewer);
        hud.setNavigationCallback(new HUD.NavigationCallback() {
            @Override public void onDeckClicked() {
                if (paused || battleEnding) return;
                pileViewer.show(RunManager.getInstance().getDeck(), "Deck");
            }
            @Override public void onMapClicked() {
                if (paused || battleEnding) return;
                if (mapPreview == null) {
                    mapPreview = new CombatMapOverlay();
                    stage.addActor(mapPreview);
                }
                mapPreview.show();
            }
        });

        hud.setPileCallback(new HUD.PileClickCallback() {
            @Override
            public void onDrawPileClicked() {
                if (paused) return;
                pileViewer.show(gameState.drawPile, "Draw Pile");
            }
            @Override
            public void onDiscardPileClicked() {
                if (paused) return;
                pileViewer.show(gameState.discardPile, "Discard Pile");
            }
        });
    }

    private void processEvents(List<GameEvent> events) {
        processEvents(events, null);
    }

    private void processEvents(List<GameEvent> events, AbstractMonster target) {
        if (battleEnding) return;
        Map<AbstractMonster, Integer> hitCounts = new HashMap<>();
        int playerHits = 0;
        AbstractCard playedCard = null;
        for (GameEvent e : events) {
            AbstractMonster hitTarget = e instanceof DamageDealtEvent damage && damage.targetMonster() != null
                    ? damage.targetMonster() : target;
            processAnimationEvent(e, hitTarget);
            if (e instanceof CardPlayedEvent played) {
                playedCard = played.card();
                spawnCardEffectCue(played.card());
            }
            if (e instanceof com.cardgame.logic.events.GameOverEvent) {
                com.cardgame.logic.events.GameOverEvent goe = (com.cardgame.logic.events.GameOverEvent) e;
                battleEnding = true;
                pendingWinner = goe.winnerIndex();
                // Keep the final impact visible, then transition after the render pass.
                effectsRemaining = Math.max(effectsRemaining, 0.3f);
                targetingArrow.setVisible(false);
                return;
            }
            if (e instanceof PlayerDamagedEvent damage && damage.amount() > 0) {
                if ("monster".equals(damage.source())) {
                    boolean heavy = damage.amount() >= 14;
                    float delay = playerHits++ * 0.1f;
                    spawnSlashEffect(hud.getPlayerX(), hud.getCharY() + 140f, 35f, heavy ? 340f : 240f,
                            new Color(1f, 0.25f, 0.2f, 1f), delay, () -> {
                                triggerImpactShake(heavy);
                                spawnDamageLabel("-" + damage.amount(), hud.getPlayerX(), hud.getCharY() + 340f,
                                        Color.RED, combatTex, heavy);
                            });
                } else {
                    spawnDamageLabel("-" + damage.amount(), hud.getPlayerX(), hud.getCharY() + 340f,
                            Color.GREEN, combatTex, false);
                }
            }
            if (e instanceof BlockGainedEvent block && "player".equals(block.target()) && block.amount() > 0) {
                spawnDamageLabel("+" + block.amount(), hud.getPlayerX(), hud.getCharY() + 300f,
                        Color.CYAN, blockTex, false);
            }
            if (e instanceof DamageDealtEvent dmg) {
                if ("player".equals(dmg.source()) && "monster".equals(dmg.target()) && dmg.amount() > 0 && hitTarget != null) {
                    int hit = hitCounts.getOrDefault(hitTarget, 0);
                    hitCounts.put(hitTarget, hit + 1);
                    impactTargets.add(hitTarget);
                    boolean heavy = playedCard != null ? playedCard.damage() >= 14 : dmg.amount() >= 14;
                    float angle = heavy ? 90f : (hit % 2 == 0 ? -35f : 35f);
                    float x = hitTarget.drawX;
                    float y = hitTarget.drawY + 160f;
                    spawnSlashEffect(x, y, angle, heavy ? 340f : 240f,
                            heavy ? Color.ORANGE : Color.CYAN, hit * 0.1f,
                            () -> {
                                triggerImpactShake(heavy);
                                spawnDamageLabel("-" + dmg.amount(), x, y + 100f, Color.ORANGE, combatTex, heavy);
                            });
                }
            }
        }
    }

    private void spawnCardEffectCue(AbstractCard card) {
        if (card == null || effectsLayer == null) return;
        String cue = null;
        String name = card.name().toLowerCase();
        if (name.contains("spiral")) cue = "TOP CARD  →  EXHAUST";
        else if (name.contains("parasite")) cue = "DISCARD  →  DRAW TOP";
        else if (name.contains("scream")) cue = "DRAW 1  →  PLACE ON TOP  →  EXHAUST";
        else if (name.contains("shard") || name.contains("pills")) cue = "DRAW 1";
        else if (name.contains("brick")) cue = "RANDOM HAND CARD  →  EXHAUST";
        else if (name.contains("leap")) cue = "TRAUMA  →  DRAW PILE";
        if (cue == null) return;
        BitmapFont cueFont = UiTheme.font(18f);
        cueFont.setColor(Color.valueOf("f3d49a"));
        Label cueLabel = new Label(cue, new Label.LabelStyle(cueFont, cueFont.getColor()));
        cueLabel.setSize(700f, 34f);
        cueLabel.setPosition(Constants.VIEWPORT_WIDTH / 2f - 350f, Constants.VIEWPORT_HEIGHT - 175f);
        cueLabel.getColor().a = 0f;
        cueLabel.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn(0.12f),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(0.55f),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut(0.25f),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.run(cueFont::dispose),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor()));
        cueLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        effectsLayer.addActor(cueLabel);
    }

    private void triggerImpactShake(boolean heavy) {
        shakeTimer = Math.max(shakeTimer, heavy ? 0.28f : 0.16f);
    }

    private void spawnSlashEffect(float x, float y, float angle, float size, Color tint,
                                  float delay, Runnable onImpact) {
        if (slashAnimation == null) {
            onImpact.run();
            return;
        }
        effectsRemaining = Math.max(effectsRemaining, delay + slashAnimation.getAnimationDuration());
        effectsLayer.addActor(new SlashAnimationActor(slashAnimation, x, y, angle, size, tint, true, delay, () -> {
            if (slashSound != null) slashSound.play(0.35f);
            onImpact.run();
        }));
    }

    private void finishCombat() {
        RunManager.getInstance().setCurrentHp(gameState.playerHp);
        game.setScreen(pendingWinner == 0 ? new RewardScreen(game) : new GameOverScreen(game, pendingWinner));
    }

    private void processAnimationEvent(GameEvent event, AbstractMonster target) {
        if (event instanceof CardPlayedEvent cpe && cpe.card() != null
                && cpe.card().cardType() == com.cardgame.data.CardType.ATTACK) {
            setPlayerState(PlayerAnimState.ATTACK);
        } else if (event instanceof com.cardgame.logic.events.PlayerDefendedEvent || event instanceof BlockGainedEvent) {
            setPlayerState(PlayerAnimState.DEFEND);
        } else if (event instanceof PlayerDamagedEvent damage && damage.amount() > 0) {
            setPlayerState(PlayerAnimState.HURT);
        } else if (event instanceof DamageDealtEvent damage && damage.amount() > 0
                && "monster".equals(damage.target())) {
            AbstractMonster hit = damage.targetMonster() != null ? damage.targetMonster() : target;
            EnemyAnimation animation = enemyAnimations.get(hit);
            if (animation != null) animation.play(EnemyAnimation.State.HURT);
        }
    }

    private void animateEnemyTurn() {
        for (AbstractMonster monster : monsters.monsters) {
            EnemyAnimation animation = enemyAnimations.get(monster);
            if (animation == null || monster.currentHp <= 0) continue;
            if (monster.intentType != null && monster.intentType.startsWith("ATTACK")) {
                animation.play(EnemyAnimation.State.ATTACK);
            } else {
                animation.play(EnemyAnimation.State.BUFF);
            }
        }
    }

    /** Spawns a floating damage number actor at the given position. */
    private void spawnDamageLabel(String text, float x, float y, Color color) {
        spawnDamageLabel(text, x, y, color, null, false);
    }

    private void spawnDamageLabel(String text, float x, float y, Color color, TextureRegion icon, boolean heavy) {
        DamageLabel label = new DamageLabel(text, color, icon, heavy);
        label.setPosition(x - label.getWidth() / 2f, y);
        effectsLayer.addActor(label);
        effectsRemaining = Math.max(effectsRemaining, DamageLabel.DURATION);
    }

    private com.cardgame.logic.monsters.AbstractMonster getHoveredMonster(float x, float y) {
        if (gameState.monsterGroup != null) {
            for (com.cardgame.logic.monsters.AbstractMonster m : gameState.monsterGroup.monsters) {
                if (m.currentHp > 0) {
                    EnemyAnimation animation = enemyAnimations.get(m);
                    TextureRegion frame = animation != null ? animation.frame() : null;
                    float width = frame != null ? Math.max(180f, frame.getRegionWidth() * animation.scale()) : 280f;
                    float height = frame != null ? frame.getRegionHeight() * animation.scale() : 320f;
                    float left = m.drawX - width / 2f;
                    float right = m.drawX + width / 2f;
                    float bottom = m.drawY;
                    float top = m.drawY + height;
                    if (x >= left && x <= right && y >= bottom && y <= top) {
                        return m;
                    }
                }
            }
        }
        return null;
    }

    private void updateUI() {
        handArea.syncWithState(gameState);
        hud.update(gameState);
    }

    @Override
    public void render(float delta) {
        updateAnimations(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.getViewport().apply();
        stage.getBatch().setProjectionMatrix(stage.getCamera().combined);

        // Update shake timer
        if (shakeTimer > 0) {
            if (!paused) shakeTimer = Math.max(0f, shakeTimer - delta);
            float progress = shakeTimer / SHAKE_DURATION;
            float angle = (float)(Math.sin(shakeTimer * 60f) * SHAKE_INTENSITY * progress);
            shakeOffsetX = angle;
            shakeOffsetY = (float)(Math.cos(shakeTimer * 50f) * SHAKE_INTENSITY * 0.5f * progress);
        } else {
            shakeOffsetX = 0;
            shakeOffsetY = 0;
        }

        Batch batch = stage.getBatch();
        batch.begin();

        // ── Background ────────────────────────────────────────
        if (bgTexture != null) {
            batch.setColor(1, 1, 1, 1);
            batch.draw(bgTexture, shakeOffsetX, shakeOffsetY,
                    Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        }

        // ── Player character (left side) ──────────────────────
        Animation<TextureRegion> playerAnimation = currentPlayerAnimation();
        if (playerAnimation == null) playerAnimation = playerIdleAnim;
        if (playerAnimation != null || playerTexture != null) {
            TextureRegion playerFrame = playerAnimation != null
                    ? playerAnimation.getKeyFrame(playerStateTime, playerAnimation == playerIdleAnim) : null;
            float frameWidth = playerFrame != null ? playerFrame.getRegionWidth() : playerTexture.getWidth();
            float frameHeight = playerFrame != null ? playerFrame.getRegionHeight() : playerTexture.getHeight();
            // Calculate a unified scale based on the idle frame, so all animations match the base character size.
            float scale = 1f;
            if (playerIdleAnim != null) {
                TextureRegion idleRef = playerIdleAnim.getKeyFrame(0);
                scale = Math.min(250f / idleRef.getRegionWidth(), 300f / idleRef.getRegionHeight());
            } else if (playerTexture != null) {
                scale = Math.min(250f / playerTexture.getWidth(), 300f / playerTexture.getHeight());
            }
            
            // The character takes up more canvas space in the attack frames.
            // Shrink only the attack animation (by 0.58x) so it visually matches the idle character's size.
            if (playerAnimation == playerAttackAnim && playerAnimation != null) {
                scale *= 0.58f;
            }
            
            float pw = frameWidth * scale;
            float ph = frameHeight * scale;
            float px = hud.getPlayerX() - pw / 2f + shakeOffsetX;
            float py = hud.getCharY() + shakeOffsetY;
            batch.setColor(1, 1, 1, 1);
            if (playerFrame != null) {
                batch.draw(playerFrame, px, py, pw, ph);
            } else {
                batch.draw(playerTexture, px, py, pw, ph);
            }
        }

        // ── Monster (right side) ─────────────────────────────
        
        if (gameState.monsterGroup != null) {
            for (com.cardgame.logic.monsters.AbstractMonster m : gameState.monsterGroup.monsters) {
                if (m.currentHp > 0 || impactTargets.contains(m)) {
                    float my = m.drawY;
                    batch.setColor(1, 1, 1, 1);
                    EnemyAnimation enemyAnimation = enemyAnimations.get(m);
                    TextureRegion enemyFrame = enemyAnimation != null ? enemyAnimation.frame() : null;
                    if (enemyFrame != null) {
                        float scale = enemyAnimation.scale();
                        float width = enemyFrame.getRegionWidth() * scale;
                        float height = enemyFrame.getRegionHeight() * scale;
                        batch.draw(enemyFrame, m.drawX - width / 2f + shakeOffsetX, my + shakeOffsetY, width, height);
                    } else {
                        batch.draw(m.getTexture(), m.drawX - 140f + shakeOffsetX, my + shakeOffsetY, 280f, 320f);
                    }

                    // ── Monster name ──
                    float nameY = my + (m.isBoss() ? 400f : m.isElite() ? 370f : 340f);
                    monsterFont.setColor(0.02f, 0.02f, 0.04f, 0.9f);
                    monsterFont.draw(batch, m.name, m.drawX - 130f + 1f, nameY - 1f,
                            260f, com.badlogic.gdx.utils.Align.center, false);
                    monsterFont.setColor(Color.WHITE);
                    monsterFont.draw(batch, m.name, m.drawX - 130f, nameY,
                            260f, com.badlogic.gdx.utils.Align.center, false);

                    // ── Intent icon ──
                    drawMonsterIntent(batch, m);

                    // ── HP bar ──
                    float barW = CombatHealthBar.WIDTH;
                    float barX = m.drawX - barW / 2f;
                    float barY = Math.max(my - CombatHealthBar.OFFSET_BELOW_FEET, hud.getCharY() - CombatHealthBar.OFFSET_BELOW_FEET);
                    monsterHealthBar.draw(batch, monsterSmallFont, barX, barY,
                            m.currentHp, m.maxHp, m.block, 1f);

                    // ── Status effects ──
                    String statusStr = m.status.summaryString();
                    if (!statusStr.isEmpty()) {
                        monsterSmallFont.setColor(new Color(0.9f, 0.7f, 0.2f, 1f));
                        monsterSmallFont.draw(batch, statusStr, barX, barY - 14f);
                    }
                }
            }
        }


        batch.end();

        // Freeze delayed impacts, sound callbacks, and screen transitions while paused.
        if (!paused) stage.act(delta);
        else pauseOverlay.act(delta);
        stage.draw();
        if (!paused) {
            effectsRemaining = Math.max(0f, effectsRemaining - delta);
            if (effectsRemaining == 0f) {
                impactTargets.clear();
                if (battleEnding) finishCombat();
            }
        }
    }

    private void drawMonsterIntent(Batch batch, AbstractMonster monster) {
        String intent = monster.intentType == null ? "" : monster.intentType;
        boolean attack = intent.contains("ATTACK");
        boolean defend = intent.contains("DEFEND");
        boolean debuff = intent.contains("DEBUFF") || intent.contains("MADNESS");
        TextureRegion icon = attack ? combatUi.attackIntent : defend ? combatUi.defendIntent
                : debuff ? combatUi.debuffIntent : combatUi.buffIntent;
        boolean showAmount = attack || defend;
        float width = attack && defend ? 104f : showAmount ? 64f : 36f;
        float x = monster.drawX - width / 2f;
        float y = monster.drawY + (monster.isBoss() ? 435f : monster.isElite() ? 405f : 375f);
        batch.setColor(Color.WHITE);
        CombatUiAssets.drawFitted(batch, icon, x, y, 36f, 36f);
        if (showAmount) {
            String amount = Integer.toString(monster.intentValue);
            monsterFont.setColor(0.02f, 0.025f, 0.04f, 1f);
            monsterFont.draw(batch, amount, x + 39f, y + 26f);
            monsterFont.setColor(Color.WHITE);
            monsterFont.draw(batch, amount, x + 38f, y + 27f);
        }
        if (attack && defend) CombatUiAssets.drawFitted(batch, combatUi.defendIntent, x + 68f, y, 36f, 36f);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (slashSheetTex != null) { slashSheetTex.dispose(); slashSheetTex = null; }
        if (combatUi != null) { combatUi.dispose(); combatUi = null; }
        if (slashSound != null) { slashSound.dispose(); slashSound = null; }
        if (uiSound != null) { uiSound.dispose(); uiSound = null; }
        slashAnimation = null;
        impactTargets.clear();
        if (monsters != null) monsters.disposeAll();
        if (playerTexture != null) playerTexture.dispose();
        disposePlayerAnimation();
        disposeEnemyAnimation();
        if (hud != null) hud.disposeResources();
        if (handArea != null) handArea.disposeAll();
        if (pauseOverlay != null) pauseOverlay.disposeResources();
        if (targetingArrow != null) targetingArrow.dispose();
        if (monsterHealthBar != null) monsterHealthBar.dispose();
        if (monsterFont != null) monsterFont.dispose();
        if (monsterSmallFont != null) monsterSmallFont.dispose();
        if (pileViewer != null) pileViewer.disposeResources();
        if (mapPreview != null) mapPreview.disposeResources();
    }
}
