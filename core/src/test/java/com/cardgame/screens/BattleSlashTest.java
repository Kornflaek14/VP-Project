package com.cardgame.screens;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.cardgame.logic.GameState;
import com.cardgame.logic.cards.PsychoticBreakCard;
import com.cardgame.logic.cards.SplitPersonalityCard;
import com.cardgame.logic.events.CardPlayedEvent;
import com.cardgame.logic.events.DamageDealtEvent;
import com.cardgame.logic.events.GameEvent;
import com.cardgame.logic.monsters.AbstractMonster;
import com.cardgame.logic.monsters.FrenziedPatient;
import com.cardgame.logic.monsters.MonsterGroup;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class BattleSlashTest {
    @Test
    void multiHitCardRetainsTargetAndCreatesOpposingSlashes() throws Exception {
        FrenziedPatient enemy = new FrenziedPatient(1000, 280);
        List<GameEvent> events = new SplitPersonalityCard().use(new GameState(), enemy);
        assertEquals(2, events.size());
        for (GameEvent event : events) assertSame(enemy, ((DamageDealtEvent) event).targetMonster());
        Group effects = process(events, null);
        assertEquals(2, effects.getChildren().size);
        assertEquals(-35f, effects.getChildren().get(0).getRotation());
        assertEquals(35f, effects.getChildren().get(1).getRotation());
        assertEquals(0f, stateTime(effects.getChildren().get(0)), 0.0001f);
        assertEquals(-0.1f, stateTime(effects.getChildren().get(1)), 0.0001f);
    }

    @Test
    void eachEnemyGetsItsOwnPositionEvenWhenSelectionPointsElsewhere() throws Exception {
        FrenziedPatient first = new FrenziedPatient(800, 250);
        FrenziedPatient second = new FrenziedPatient(1150, 280);
        Group effects = process(List.of(new DamageDealtEvent("player", "monster", 7, first),
                new DamageDealtEvent("player", "monster", 7, second)), first);
        assertEquals(680f, effects.getChildren().get(0).getX());
        assertEquals(1030f, effects.getChildren().get(1).getX());
        assertEquals(0f, stateTime(effects.getChildren().get(1)), 0.0001f);
    }

    @Test
    void heavyAttackUsesLargeVerticalSlash() throws Exception {
        FrenziedPatient enemy = new FrenziedPatient(1000, 280);
        Group effects = process(List.of(new CardPlayedEvent(new PsychoticBreakCard(), 0, 0),
                new DamageDealtEvent("player", "monster", 14, enemy)), enemy);
        assertEquals(340f, effects.getChildren().first().getWidth());
        assertEquals(90f, effects.getChildren().first().getRotation());
    }

    @Test
    void blockedAndPoisonDamageDoNotCreatePlayerAttackSlashes() throws Exception {
        FrenziedPatient enemy = new FrenziedPatient(1000, 280);
        Group effects = process(List.of(new DamageDealtEvent("player", "monster", 0, enemy),
                new DamageDealtEvent("poison", "monster", 3, enemy)), enemy);
        assertEquals(0, effects.getChildren().size);
    }

    private Group process(List<GameEvent> events, AbstractMonster selected) throws Exception {
        BattleScreen screen = new BattleScreen(null, new MonsterGroup());
        Group effects = new Group();
        Field layer = BattleScreen.class.getDeclaredField("effectsLayer");
        layer.setAccessible(true);
        layer.set(screen, effects);
        Field animation = BattleScreen.class.getDeclaredField("slashAnimation");
        animation.setAccessible(true);
        animation.set(screen, new Animation<>(0.035f, new TextureRegion(), new TextureRegion(),
                new TextureRegion(), new TextureRegion(), new TextureRegion(), new TextureRegion()));
        Method process = BattleScreen.class.getDeclaredMethod("processEvents", List.class, AbstractMonster.class);
        process.setAccessible(true);
        process.invoke(screen, events, selected);
        return effects;
    }

    private float stateTime(Object actor) throws Exception {
        Field time = actor.getClass().getDeclaredField("stateTime");
        time.setAccessible(true);
        return time.getFloat(actor);
    }
}
