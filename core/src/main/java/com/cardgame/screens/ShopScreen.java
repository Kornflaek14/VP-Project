package com.cardgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cardgame.CardBattlerGame;
import com.cardgame.logic.RunManager;
import com.cardgame.logic.cards.AbstractCard;
import com.cardgame.logic.potions.AbstractPotion;
import com.cardgame.ui.CardActor;
import com.cardgame.ui.GameArt;
import com.cardgame.ui.UiTheme;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

/** Merchandise sits on the clear area of the supplied pharmacy counter painting. */
public class ShopScreen implements Screen {
    private static final Color INK = Color.valueOf("243c3b");
    private static final Color PAPER = Color.valueOf("ede0c8");
    private static final Color BRASS = Color.valueOf("dfba75");
    private final CardBattlerGame game;
    private final List<CardActor> cardActors = new ArrayList<>();
    private final List<Offer> offers = new ArrayList<>();
    private Stage stage;
    private Texture background;
    private BitmapFont font;
    private BitmapFont titleFont;
    private BitmapFont smallFont;
    private Label goldLabel;
    private Label hpLabel;
    private Label bagLabel;
    private Label statusLabel;

    private static final class Offer {
        final TextButton button;
        final int price;
        final boolean potion;
        final TextTooltip availability;
        boolean sold;
        Offer(TextButton button, int price, boolean potion, TextTooltip availability) {
            this.button = button; this.price = price; this.potion = potion;
            this.availability = availability;
        }
    }

    public ShopScreen(CardBattlerGame game) { this.game = game; }

    @Override public void show() {
        RunManager.getInstance().ensureRunStarted(game);
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);
        background = new Texture(Gdx.files.internal(GameArt.SHOP));
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font = UiTheme.font(16f);
        titleFont = UiTheme.font(30f);
        smallFont = UiTheme.font(11f);
        buildUI();
    }

    private void buildUI() {
        RunManager run = RunManager.getInstance();
        Table header = new Table();
        header.setBounds(448f, 749f, 766f, 119f);
        header.setBackground(UiTheme.panel(Color.valueOf("172b2bee"), Color.valueOf("697970")));
        header.pad(18f, 24f, 16f, 24f);
        Table heading = new Table();
        heading.add(label("THE DISPENSARY", smallFont, BRASS)).left().padBottom(8f).row();
        heading.add(label("A little help to survive.", titleFont, PAPER)).left();
        header.add(heading).expandX().left();
        Table wallet = new Table();
        wallet.add(GameArt.image(GameArt.GOLD)).size(34f).padRight(10f);
        goldLabel = label(Integer.toString(run.getGold()), font, BRASS);
        goldLabel.setName("shop-gold");
        wallet.add(goldLabel).minWidth(44f).left().row();
        wallet.add(GameArt.image(GameArt.HEART)).size(24f).padRight(10f).padTop(10f);
        hpLabel = label(run.getCurrentHp() + "/" + run.getMaxHp(), smallFont, PAPER);
        wallet.add(hpLabel).left().padTop(10f);
        header.add(wallet).right().padLeft(20f);
        stage.addActor(header);

        addSection("CARDS", "Add a new possibility to your deck", 695f);
        String character = run.getSelectedCharacter() != null ? run.getSelectedCharacter().name() : "";
        List<AbstractCard> pool = game.getCardsForCharacter(character);
        List<AbstractCard> cards = new ArrayList<>(pool.isEmpty() ? game.getAllCards() : pool);
        Collections.shuffle(cards);
        for (int i = 0; i < Math.min(5, cards.size()); i++) addCard(cards.get(i), i);

        addSection("BOTTLED REMEDIES", "Single use  /  Drink during combat", 372f);
        List<AbstractPotion> potions = game.getAllPotions();
        for (int i = 0; i < potions.size(); i++) addPotion(potions.get(i), i);

        Table footer = new Table();
        footer.setBounds(448f, 34f, 766f, 86f);
        footer.setBackground(UiTheme.panel(Color.valueOf("172b2bee"), Color.valueOf("697970")));
        footer.pad(16f, 22f, 16f, 22f);
        Table message = new Table();
        statusLabel = label("Hover to inspect. Select a price to buy.", smallFont, PAPER);
        statusLabel.setName("shop-status");
        statusLabel.setWrap(true);
        message.add(statusLabel).width(460f).left().padBottom(8f).row();
        bagLabel = label("", smallFont, BRASS);
        message.add(bagLabel).left();
        footer.add(message).expandX().left();
        TextButton leave = new TextButton("Leave shop", UiTheme.button(font));
        leave.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { game.setScreen(new MapScreen(game)); }
        });
        footer.add(leave).size(190f, 48f);
        stage.addActor(footer);
        refreshOffers();
    }

    private void addSection(String title, String detail, float y) {
        Table section = new Table();
        section.setBounds(460f, y, 722f, 34f);
        section.add(label(title, smallFont, INK)).left();
        section.add(label(detail, smallFont, INK)).expandX().right();
        stage.addActor(section);
    }

    private void addCard(AbstractCard card, int index) {
        Table item = new Table();
        item.setBounds(456f + index * 147f, 427f, 138f, 261f);
        CardActor art = new CardActor(card, (CardActor.OnClickCallback) null);
        art.isUiElement = true;
        art.setOrigin(62f, 85f);
        cardActors.add(art);
        item.add(art).size(124f, 170f).padBottom(12f).row();
        Label name = label(card.name(), smallFont, INK);
        name.setAlignment(Align.center);
        name.setWrap(true);
        item.add(name).width(136f).height(34f).padBottom(6f).row();
        int price = 45 + Math.max(0, card.energyCost()) * 5;
        TextButton buy = purchaseButton(price, false, card.name(),
                () -> RunManager.getInstance().buyCard(card, price), art);
        buy.setName("card-offer-" + index);
        item.add(buy).size(128f, 39f);
        art.addListener(tooltip(card.name() + "  /  " + card.energyCost() + " energy\n" + card.description()));
        stage.addActor(item);
    }

    private void addPotion(AbstractPotion potion, int index) {
        Table item = new Table();
        item.setBounds(476f + index * 237f, 151f, 210f, 211f);
        Image bottle = GameArt.image(potion.imagePath);
        item.add(bottle).size(76f, 76f).padBottom(9f).row();
        item.add(label(potion.name, font, INK)).padBottom(6f).row();
        Label detail = label(potion.description, smallFont, INK);
        detail.setAlignment(Align.center);
        detail.setWrap(true);
        item.add(detail).width(210f).height(35f).padBottom(9f).row();
        int price = index == 0 ? 40 : 55;
        TextButton buy = purchaseButton(price, true, potion.name,
                () -> RunManager.getInstance().buyPotion(potion, price), bottle);
        buy.setName("potion-offer-" + potion.id);
        item.add(buy).size(150f, 39f);
        item.addListener(tooltip(potion.name + "\n" + potion.description));
        stage.addActor(item);
    }

    private TextButton purchaseButton(int price, boolean potion, String name, BooleanSupplier purchase, Actor art) {
        TextButton.TextButtonStyle style = UiTheme.button(font);
        style.up = UiTheme.panel(INK, Color.valueOf("788476"));
        style.over = UiTheme.panel(Color.valueOf("365651"), BRASS);
        style.fontColor = PAPER;
        style.disabledFontColor = Color.valueOf("a6a599");
        TextButton button = new TextButton(Integer.toString(price), style);
        button.clearChildren();
        button.add(GameArt.image(GameArt.GOLD)).size(25f).padRight(10f);
        button.add(button.getLabel());
        TextTooltip availability = tooltip("Buy " + name + " for " + price + " gold.");
        button.addListener(availability);
        Offer offer = new Offer(button, price, potion, availability);
        offers.add(offer);
        button.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (offer.sold) return;
                if (!purchase.getAsBoolean()) {
                    statusLabel.setText(potion && RunManager.getInstance().getPotions().size() >= 3
                            ? "Potion belt full. Use a potion in combat to free a slot."
                            : "You need more gold for this item.");
                    return;
                }
                offer.sold = true;
                art.addAction(Actions.alpha(0.32f, 0.2f));
                statusLabel.setText(name + (potion ? " added to your potion belt." : " added to your deck."));
                refreshOffers();
            }
        });
        return button;
    }

    private void refreshOffers() {
        RunManager run = RunManager.getInstance();
        goldLabel.setText(Integer.toString(run.getGold()));
        bagLabel.setText("Potion belt  " + run.getPotions().size() + " / 3");
        for (Offer offer : offers) {
            boolean full = offer.potion && run.getPotions().size() >= 3;
            offer.button.setDisabled(offer.sold || full || run.getGold() < offer.price);
            offer.button.setText(offer.sold ? "Sold" : full ? "Full" : Integer.toString(offer.price));
            offer.availability.getActor().setText(offer.sold ? "Already purchased."
                    : full ? "Potion belt full (3/3). Use a potion in combat to free a slot."
                    : run.getGold() < offer.price ? "You need " + (offer.price - run.getGold()) + " more gold."
                    : "Buy for " + offer.price + " gold.");
        }
    }

    private TextTooltip tooltip(String text) {
        TextTooltip.TextTooltipStyle style = new TextTooltip.TextTooltipStyle();
        style.label = new Label.LabelStyle(font, PAPER);
        style.background = UiTheme.panel(Color.valueOf("142423f5"), BRASS);
        style.wrapWidth = 270f;
        TextTooltip tooltip = new TextTooltip(text, style);
        tooltip.setInstant(true);
        return tooltip;
    }

    private Label label(String text, BitmapFont type, Color color) {
        return new Label(text, new Label.LabelStyle(type, color));
    }

    public void updateHUD() {
        RunManager run = RunManager.getInstance();
        if (goldLabel != null) goldLabel.setText(Integer.toString(run.getGold()));
        if (hpLabel != null) hpLabel.setText(run.getCurrentHp() + "/" + run.getMaxHp());
        refreshOffers();
    }

    @Override public void render(float delta) {
        updateHUD();
        Gdx.gl.glClearColor(0.05f, 0.08f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.getViewport().apply();
        Batch batch = stage.getBatch();
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        batch.setColor(Color.WHITE);
        GameArt.cover(batch, background, 0f, 0f, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        batch.end();
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }
    @Override public void dispose() {
        if (stage != null) { stage.dispose(); stage = null; }
        if (background != null) { background.dispose(); background = null; }
        if (font != null) { font.dispose(); font = null; }
        if (titleFont != null) { titleFont.dispose(); titleFont = null; }
        if (smallFont != null) { smallFont.dispose(); smallFont = null; }
        for (CardActor actor : cardActors) actor.dispose();
        cardActors.clear(); offers.clear();
    }
}
