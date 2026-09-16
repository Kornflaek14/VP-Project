package com.cardgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
import com.cardgame.ui.CardActor;
import com.cardgame.ui.DamageLabel;
import com.cardgame.ui.HUD;
import com.cardgame.ui.HandArea;
import com.cardgame.ui.PauseOverlay;
import com.cardgame.ui.PileViewerOverlay;
import com.cardgame.ui.TargetingArrow;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class BattleScreen implements Screen {

    private final CardBattlerGame game;
    private Stage stage;
    private Texture bgTexture;
    
    private Texture playerTexture;
    private static final String[] PLAYER_IDLE_FRAME_FILES = {
        "Character sprite/idle/ChatGPT Image Sep 12, 2026, 09_47_47 PM_06.png",
        "Character sprite/idle/ChatGPT Image Sep 12, 2026, 09_47_47 PM_08.png",
        "Character sprite/idle/ChatGPT Image Sep 12, 2026, 09_47_47 PM_10.png",
        "Character sprite/idle/ChatGPT Image Sep 12, 2026, 09_47_47 PM_12.png"
    };
    private final List<Texture> playerAnimationTextures = new ArrayList<>();
    private final List<Texture> enemyAnimationTextures = new ArrayList<>();
    private Animation<TextureRegion> playerIdleAnim;
    private Animation<TextureRegion> playerAttackAnim;
    private Animation<TextureRegion> playerHurtAnim;
    private Animation<TextureRegion> playerDefendAnim;
    private Animation<TextureRegion> enemyIdleAnim;
    private Animation<TextureRegion> enemyAttackAnim;
    private Animation<TextureRegion> enemyHurtAnim;

    private enum PlayerAnimState { IDLE, ATTACK, HURT, DEFEND }
    private enum EnemyAnimState { IDLE, ATTACK, HURT }

    private PlayerAnimState playerState = PlayerAnimState.IDLE;
    private EnemyAnimState enemyState = EnemyAnimState.IDLE;
    private float playerStateTime = 0f;
    private float enemyStateTime = 0f;
    private AbstractMonster animatedMonster;
    // Unified scale: computed once across ALL animation states so every pose
    // displays at the same size regardless of individual frame dimensions.
    private float playerUnifiedScale = 1f;
    private float enemyUnifiedScale = 1f;

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

    // Enemy HP bar textures
    private Texture hpBarBgTex;
    private Texture hpBarFillTex;
    private Texture solidWhiteTex;
    private com.badlogic.gdx.graphics.g2d.BitmapFont monsterFont;
    private com.badlogic.gdx.graphics.g2d.BitmapFont monsterSmallFont;

    // Pile viewer
    private PileViewerOverlay pileViewer;

    private com.cardgame.logic.monsters.MonsterGroup monsters;
    public BattleScreen(CardBattlerGame game, com.cardgame.logic.monsters.MonsterGroup monsters) {
        this.game = game;
        this.monsters = monsters;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));

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

        // Init monster HP bar rendering resources
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.2f, 0.05f, 0.05f, 0.9f));
        pm.fill();
        hpBarBgTex = new Texture(pm);
        pm.setColor(new Color(0.85f, 0.15f, 0.15f, 1f));
        pm.fill();
        hpBarFillTex = new Texture(pm);
        pm.setColor(Color.WHITE);
        pm.fill();
        solidWhiteTex = new Texture(pm);
        pm.dispose();

        monsterFont = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        monsterFont.getData().setScale(1.0f);
        monsterSmallFont = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        monsterSmallFont.getData().setScale(0.8f);

        buildUI();
        buildPauseOverlay();

        // Set up input
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) { togglePause(); return true; }
                return false;
            }
        });
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

        // Start combat
        turnManager.startCombat(gameState);
        updateUI();
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

    private String[] playerFramePaths(String action, String[] existingNames) {
        String folder = "Character sprite/" + action + "/";
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
        setEnemyState(EnemyAnimState.IDLE);
        animatedMonster = null;
        for (AbstractMonster monster : monsters.monsters) {
            if (monster instanceof FrenziedPatient) {
                animatedMonster = monster;
                break;
            }
        }
        if (animatedMonster == null) return;
        enemyIdleAnim = loadAnimation(numberedFramePaths("Character sprite/enemy/idle/", "idle"),
                0.4f, true, enemyAnimationTextures);
        enemyAttackAnim = loadAnimation(numberedFramePaths("Character sprite/enemy/attack/", "attack"),
                0.12f, false, enemyAnimationTextures);
        enemyHurtAnim = loadAnimation(numberedFramePaths("Character sprite/enemy/hurt/", "hurt"),
                0.15f, false, enemyAnimationTextures);
        // One scale for ALL enemy states.
        enemyUnifiedScale = unifiedScale(280f, 320f,
                enemyIdleAnim, enemyAttackAnim, enemyHurtAnim);
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
        enemyIdleAnim = enemyAttackAnim = enemyHurtAnim = null;
        for (Texture texture : enemyAnimationTextures) texture.dispose();
        enemyAnimationTextures.clear();
    }

    private void setPlayerState(PlayerAnimState state) {
        playerState = state;
        playerStateTime = 0f;
    }

    private void setEnemyState(EnemyAnimState state) {
        enemyState = state;
        enemyStateTime = 0f;
    }

    private Animation<TextureRegion> currentPlayerAnimation() {
        return switch (playerState) {
            case IDLE -> playerIdleAnim;
            case ATTACK -> playerAttackAnim;
            case HURT -> playerHurtAnim;
            case DEFEND -> playerDefendAnim;
        };
    }

    private Animation<TextureRegion> currentEnemyAnimation() {
        return switch (enemyState) {
            case IDLE -> enemyIdleAnim;
            case ATTACK -> enemyAttackAnim;
            case HURT -> enemyHurtAnim;
        };
    }

    private void updateAnimations(float delta) {
        if (paused) return;
        playerStateTime += delta;
        enemyStateTime += delta;
        Animation<TextureRegion> playerAnimation = currentPlayerAnimation();
        if (playerState != PlayerAnimState.IDLE
                && (playerAnimation == null || playerAnimation.isAnimationFinished(playerStateTime))) {
            setPlayerState(PlayerAnimState.IDLE);
        }
        Animation<TextureRegion> enemyAnimation = currentEnemyAnimation();
        if (enemyState != EnemyAnimState.IDLE
                && (enemyAnimation == null || enemyAnimation.isAnimationFinished(enemyStateTime))) {
            setEnemyState(EnemyAnimState.IDLE);
        }
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
                if (paused || !gameState.isPlayerTurn()) return;
                // If it's an attack, start targeting
                if (actor.getCard().cardType() == com.cardgame.data.CardType.ATTACK) {
                    targetingArrow.start.set(actor.getX() + actor.getWidth() / 2f, actor.getY() + actor.getHeight() / 2f);
                    targetingArrow.end.set(targetingArrow.start);
                    targetingArrow.setVisible(true);
                }
            }

            @Override
            public void onDrag(CardActor actor, float x, float y) {
                if (paused || !gameState.isPlayerTurn()) return;
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
                if (paused || !gameState.isPlayerTurn()) {
                    updateUI();
                    return;
                }
                targetingArrow.setVisible(false);
                
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
                if (paused) return;
                if (gameState.isPlayerTurn()) {
                    if (animatedMonster != null && animatedMonster.currentHp > 0
                            && animatedMonster.intentType.startsWith("ATTACK")) {
                        setEnemyState(EnemyAnimState.ATTACK);
                    }
                    List<GameEvent> events = turnManager.endPlayerTurn(gameState);
                    processEvents(events);
                    updateUI();
                }
            }
        });

        // Wire potion slots
        hud.setPotionCallback(new HUD.PotionClickCallback() {
            @Override
            public void onPotionClicked(int slotIndex) {
                if (paused) return;
                if (gameState.isPlayerTurn()) {
                    RunManager.getInstance().usePotion(slotIndex, gameState);
                    // Show a floating "POTION!" label
                    spawnDamageLabel("POTION!", hud.getPlayerX(), hud.getCharY() + 320f, Color.GREEN);
                    updateUI();
                }
            }
        });

        stage.addActor(hud);

        // Wire pile viewer
        pileViewer = new PileViewerOverlay();
        stage.addActor(pileViewer);

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
        for (GameEvent e : events) {
            processAnimationEvent(e, target);
            if (e instanceof com.cardgame.logic.events.GameOverEvent) {
                com.cardgame.logic.events.GameOverEvent goe = (com.cardgame.logic.events.GameOverEvent) e;
                if (goe.winnerIndex() == 0) {
                    // Player won!
                    RunManager.getInstance().getRelics().forEach(r -> r.onVictory());
                }
                RunManager.getInstance().setCurrentHp(gameState.playerHp);
                if (goe.winnerIndex() == 0) {
                    game.setScreen(new RewardScreen(game));
                } else {
                    game.setScreen(new GameOverScreen(game, goe.winnerIndex()));
                }
                return;
            }
            if (e instanceof PlayerDamagedEvent) {
                // Trigger screen shake
                shakeTimer = SHAKE_DURATION;
                // Spawn damage number
                PlayerDamagedEvent dmgEvt = (PlayerDamagedEvent) e;
                spawnDamageLabel("-" + dmgEvt.amount(), hud.getPlayerX(), hud.getCharY() + 340f, Color.RED);
            }
            if (e instanceof DamageDealtEvent dmg) {
                if ("player".equals(dmg.source()) && "monster".equals(dmg.target()) && dmg.amount() > 0) {
                    float x = target != null ? target.drawX : 1000f;
                    float y = target != null ? target.drawY + 250f : 500f;
                    spawnDamageLabel("-" + dmg.amount(), x, y, Color.ORANGE);
                }
            }
        }
    }

    private void processAnimationEvent(GameEvent event, AbstractMonster target) {
        if (event instanceof CardPlayedEvent cpe && cpe.card().cardType == com.cardgame.data.CardType.ATTACK) {
            setPlayerState(PlayerAnimState.ATTACK);
        } else if (event instanceof com.cardgame.logic.events.PlayerDefendedEvent) {
            setPlayerState(PlayerAnimState.DEFEND);
        } else if (event instanceof PlayerDamagedEvent damage && damage.amount() > 0) {
            setPlayerState(PlayerAnimState.HURT);
        } else if (event instanceof DamageDealtEvent damage && damage.amount() > 0
                && "monster".equals(damage.target()) && animatedMonster != null
                && (target == null || target == animatedMonster)) {
            setEnemyState(EnemyAnimState.HURT);
        }
    }

    /** Spawns a floating damage number actor at the given position. */
    private void spawnDamageLabel(String text, float x, float y, Color color) {
        DamageLabel label = new DamageLabel(text, color);
        label.setPosition(x - 20f, y);
        stage.addActor(label);
    }

    private com.cardgame.logic.monsters.AbstractMonster getHoveredMonster(float x, float y) {
        if (gameState.monsterGroup != null) {
            for (com.cardgame.logic.monsters.AbstractMonster m : gameState.monsterGroup.monsters) {
                if (m.currentHp > 0) {
                    float left = m.drawX - 140f;
                    float right = m.drawX + 140f;
                    float bottom = m.drawY;
                    float top = m.drawY + 320f;
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

        // Update shake timer
        if (shakeTimer > 0) {
            shakeTimer -= delta;
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
                if (m.currentHp > 0) {
                    float my = m.drawY;
                    batch.setColor(1, 1, 1, 1);
                    Animation<TextureRegion> enemyAnimation = m == animatedMonster ? currentEnemyAnimation() : null;
                    if (m == animatedMonster && enemyAnimation == null) enemyAnimation = enemyIdleAnim;
                    if (enemyAnimation != null) {
                        TextureRegion enemyFrame = enemyAnimation.getKeyFrame(enemyStateTime, enemyAnimation == enemyIdleAnim);
                        float scale = 1f;
                        if (enemyIdleAnim != null) {
                            TextureRegion idleRef = enemyIdleAnim.getKeyFrame(0);
                            scale = Math.min(280f / idleRef.getRegionWidth(), 320f / idleRef.getRegionHeight());
                        } else {
                            scale = Math.min(280f / enemyFrame.getRegionWidth(), 320f / enemyFrame.getRegionHeight());
                        }
                        float width = enemyFrame.getRegionWidth() * scale;
                        float height = enemyFrame.getRegionHeight() * scale;
                        batch.draw(enemyFrame, m.drawX - width / 2f, my, width, height);
                    } else {
                        batch.draw(m.getTexture(), m.drawX - 140f, my, 280f, 320f);
                    }

                    // ── Monster name ──
                    monsterFont.setColor(Color.WHITE);
                    monsterFont.draw(batch, m.name, m.drawX - 60f, my + 340f);

                    // ── Intent icon ──
                    Color intentColor = "ATTACK".equals(m.intentType) ? Color.RED : Color.CYAN;
                    monsterFont.setColor(intentColor);
                    String intentStr = "ATTACK".equals(m.intentType) 
                        ? "ATK " + m.intentValue 
                        : "DEF " + m.intentValue;
                    monsterFont.draw(batch, intentStr, m.drawX - 40f, my + 360f);

                    // ── HP bar ──
                    float barW = 180f, barH = 16f;
                    float barX = m.drawX - barW / 2f;
                    float barY = my - 25f;

                    batch.setColor(1, 1, 1, 1);
                    batch.draw(hpBarBgTex, barX - 2, barY - 2, barW + 4, barH + 4);

                    float fillRatio = Math.max(0, Math.min(1, (float) m.currentHp / m.maxHp));
                    batch.draw(hpBarFillTex, barX, barY, barW * fillRatio, barH);

                    monsterSmallFont.setColor(Color.WHITE);
                    monsterSmallFont.draw(batch, m.currentHp + "/" + m.maxHp, barX + barW / 2f - 20f, barY + barH - 1f);

                    // ── Block indicator ──
                    if (m.block > 0) {
                        monsterSmallFont.setColor(Color.CYAN);
                        monsterSmallFont.draw(batch, "BLK " + m.block, barX + barW + 8, barY + barH - 1f);
                    }

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

        stage.act(delta);
        stage.draw();
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
        if (monsters != null) monsters.disposeAll();
        if (playerTexture != null) playerTexture.dispose();
        disposePlayerAnimation();
        disposeEnemyAnimation();
        if (hud != null) hud.disposeResources();
        if (handArea != null) handArea.disposeAll();
        if (pauseOverlay != null) pauseOverlay.disposeResources();
        if (targetingArrow != null) targetingArrow.dispose();
        if (hpBarBgTex != null) hpBarBgTex.dispose();
        if (hpBarFillTex != null) hpBarFillTex.dispose();
        if (solidWhiteTex != null) solidWhiteTex.dispose();
        if (monsterFont != null) monsterFont.dispose();
        if (monsterSmallFont != null) monsterSmallFont.dispose();
        if (pileViewer != null) pileViewer.disposeResources();
    }
}
