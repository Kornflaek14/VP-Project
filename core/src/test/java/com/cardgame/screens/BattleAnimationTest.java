package com.cardgame.screens;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.cardgame.logic.cards.FranticStrikeCard;
import com.cardgame.logic.events.BlockGainedEvent;
import com.cardgame.logic.events.CardPlayedEvent;
import com.cardgame.logic.events.DamageDealtEvent;
import com.cardgame.logic.events.GameEvent;
import com.cardgame.logic.events.PlayerDamagedEvent;
import com.cardgame.logic.monsters.AbstractMonster;
import com.cardgame.logic.monsters.CrawlingEye;
import com.cardgame.logic.monsters.FrenziedPatient;
import com.cardgame.logic.monsters.MonsterGroup;
import com.cardgame.ui.EnemyAnimation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BattleAnimationTest {
    private BattleScreen screen;
    private FrenziedPatient enemy;
    private EnemyAnimation enemyAnimation;

    @BeforeEach
    void setUp() throws Exception {
        enemy = new FrenziedPatient(1000f, 250f);
        screen = new BattleScreen(null, new MonsterGroup(enemy));
        set("playerIdleAnim", animation(0.4f));
        set("playerAttackAnim", animation(0.12f));
        set("playerHurtAnim", animation(0.15f));
        set("playerDefendAnim", animation(0.35f));
        enemyAnimation = new EnemyAnimation(animation(0.4f), animation(0.12f), animation(0.15f), animation(0.2f), 1f);
        enemyAnimations().put(enemy, enemyAnimation);
    }

    @Test
    void playedCardRestartsAttackAndReturnsToIdle() throws Exception {
        event(new CardPlayedEvent(new FranticStrikeCard(), 0, 0), null);
        advance(0.3f);
        event(new CardPlayedEvent(new FranticStrikeCard(), 0, 0), null);
        assertEquals(0f, get("playerStateTime"));
        advance(0.47f);
        assertEquals("ATTACK", get("playerState").toString());
        advance(0.02f);
        assertEquals("IDLE", get("playerState").toString());
        assertEquals(0f, get("playerStateTime"));
        advance(10f);
        assertEquals("IDLE", get("playerState").toString());
    }

    @Test
    void playerAndEnemyTimersAdvanceIndependently() throws Exception {
        event(new CardPlayedEvent(new FranticStrikeCard(), 0, 0), null);
        advance(0.3f);
        event(new DamageDealtEvent("player", "monster", 7), enemy);
        advance(0.2f);
        assertEquals("IDLE", get("playerState").toString());
        assertEquals("HURT", get("enemyState").toString());
        advance(0.41f);
        assertEquals("IDLE", get("enemyState").toString());
    }

    @Test
    void blockPlaysDefendAndIncomingDamageInterruptsIt() throws Exception {
        event(new BlockGainedEvent("player", 8), null);
        advance(1.39f);
        assertEquals("DEFEND", get("playerState").toString());
        advance(0.02f);
        assertEquals("IDLE", get("playerState").toString());
        event(new BlockGainedEvent("player", 8), null);
        event(new PlayerDamagedEvent(3), null);
        assertEquals("HURT", get("playerState").toString());
        advance(0.61f);
        assertEquals("IDLE", get("playerState").toString());
    }

    @Test
    void pausedAnimationsDoNotAdvance() throws Exception {
        event(new CardPlayedEvent(new FranticStrikeCard(), 0, 0), null);
        event(new DamageDealtEvent("player", "monster", 7), enemy);
        advance(0.2f);
        set("paused", true);
        advance(5f);
        assertEquals(0.2f, get("playerStateTime"));
        assertEquals(0.2f, get("enemyStateTime"));
        set("paused", false);
        advance(0.5f);
        assertEquals("IDLE", get("playerState").toString());
        assertEquals("IDLE", get("enemyState").toString());
    }

    @Test
    void missingActionFramesReturnToIdle() throws Exception {
        set("playerAttackAnim", null);
        enemyAnimation = new EnemyAnimation(animation(0.4f), animation(0.12f), null, null, 1f);
        enemyAnimations().put(enemy, enemyAnimation);
        event(new CardPlayedEvent(new FranticStrikeCard(), 0, 0), null);
        event(new DamageDealtEvent("player", "monster", 7), enemy);
        advance(0.01f);
        assertEquals("IDLE", get("playerState").toString());
        assertEquals("IDLE", get("enemyState").toString());
    }

    @Test
    void unrelatedTargetsAndZeroDamageDoNotTriggerHurt() throws Exception {
        event(new DamageDealtEvent("player", "monster", 7), new CrawlingEye(850f, 250f));
        event(new DamageDealtEvent("player", "monster", 0), enemy);
        event(new PlayerDamagedEvent(0), null);
        assertEquals("IDLE", get("enemyState").toString());
        assertEquals("IDLE", get("playerState").toString());
    }

    @Test
    void enemyAttackPlaysOnce() throws Exception {
        enemyAnimation.play(EnemyAnimation.State.ATTACK);
        advance(0.47f);
        assertEquals("ATTACK", get("enemyState").toString());
        advance(0.02f);
        assertEquals("IDLE", get("enemyState").toString());
    }

    private Animation<TextureRegion> animation(float duration) {
        // Empty regions let timing tests run without textures or an OpenGL context.
        return new Animation<>(duration, new TextureRegion(), new TextureRegion(),
                new TextureRegion(), new TextureRegion());
    }

    @Test
    void onlyDamagedEnemyReactsInMixedEncounter() throws Exception {
        CrawlingEye second = new CrawlingEye(1200f, 280f);
        EnemyAnimation secondAnimation = new EnemyAnimation(animation(0.4f), animation(0.12f),
                animation(0.15f), animation(0.2f), 1f);
        enemyAnimations().put(second, secondAnimation);
        enemyAnimation.play(EnemyAnimation.State.ATTACK);
        advance(0.2f);
        event(new DamageDealtEvent("player", "monster", 7, second), enemy);
        assertEquals(EnemyAnimation.State.ATTACK, enemyAnimation.state());
        assertEquals(0.2f, enemyAnimation.time());
        assertEquals(EnemyAnimation.State.HURT, secondAnimation.state());
        assertEquals(0f, secondAnimation.time());
        advance(0.3f);
        assertEquals(EnemyAnimation.State.IDLE, enemyAnimation.state());
        assertEquals(EnemyAnimation.State.HURT, secondAnimation.state());
    }

    @Test
    void allLivingEnemiesAnimateTheirOwnIntent() throws Exception {
        CrawlingEye second = new CrawlingEye(1200f, 280f);
        FrenziedPatient dead = new FrenziedPatient(800f, 280f);
        dead.currentHp = 0;
        set("monsters", new MonsterGroup(enemy, second, dead));
        enemy.intentType = "ATTACK_DEFEND";
        second.intentType = "BUFF";
        EnemyAnimation secondAnimation = new EnemyAnimation(animation(0.4f), animation(0.12f),
                animation(0.15f), animation(0.2f), 1f);
        EnemyAnimation deadAnimation = new EnemyAnimation(animation(0.4f), animation(0.12f), null, null, 1f);
        enemyAnimations().put(second, secondAnimation);
        enemyAnimations().put(dead, deadAnimation);
        Method method = BattleScreen.class.getDeclaredMethod("animateEnemyTurn");
        method.setAccessible(true);
        method.invoke(screen);
        assertEquals(EnemyAnimation.State.ATTACK, enemyAnimation.state());
        assertEquals(EnemyAnimation.State.BUFF, secondAnimation.state());
        assertEquals(EnemyAnimation.State.IDLE, deadAnimation.state());
        advance(0.81f);
        assertEquals(EnemyAnimation.State.IDLE, secondAnimation.state());
    }

    @SuppressWarnings("unchecked")
    private Map<AbstractMonster, EnemyAnimation> enemyAnimations() throws Exception {
        return (Map<AbstractMonster, EnemyAnimation>) get("enemyAnimations");
    }

    private void event(GameEvent event, AbstractMonster target) throws Exception {
        Method method = BattleScreen.class.getDeclaredMethod("processAnimationEvent", GameEvent.class, AbstractMonster.class);
        method.setAccessible(true);
        method.invoke(screen, event, target);
    }

    private void advance(float delta) throws Exception {
        Method method = BattleScreen.class.getDeclaredMethod("updateAnimations", float.class);
        method.setAccessible(true);
        method.invoke(screen, delta);
    }

    private Field field(String name) throws Exception {
        Field field = BattleScreen.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private void set(String name, Object value) throws Exception { field(name).set(screen, value); }
    private Object get(String name) throws Exception {
        if (name.equals("enemyState")) return enemyAnimation.state();
        if (name.equals("enemyStateTime")) return enemyAnimation.time();
        return field(name).get(screen);
    }
}
