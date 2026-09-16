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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BattleAnimationTest {
    private BattleScreen screen;
    private FrenziedPatient enemy;

    @BeforeEach
    void setUp() throws Exception {
        enemy = new FrenziedPatient(1000f, 250f);
        screen = new BattleScreen(null, new MonsterGroup(enemy));
        set("animatedMonster", enemy);
        set("playerIdleAnim", animation(0.4f));
        set("playerAttackAnim", animation(0.12f));
        set("playerHurtAnim", animation(0.15f));
        set("playerDefendAnim", animation(0.35f));
        set("enemyIdleAnim", animation(0.4f));
        set("enemyAttackAnim", animation(0.12f));
        set("enemyHurtAnim", animation(0.15f));
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
        set("enemyHurtAnim", null);
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
        Field state = field("enemyState");
        Object attack = state.getType().getEnumConstants()[1];
        Method setter = BattleScreen.class.getDeclaredMethod("setEnemyState", state.getType());
        setter.setAccessible(true);
        setter.invoke(screen, attack);
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
    private Object get(String name) throws Exception { return field(name).get(screen); }
}
