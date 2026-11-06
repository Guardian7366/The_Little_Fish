package main;

import entity.Entity;
import entity.NPC_TV;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;

public final class MarketShop {
    private record Product(String id, String effect) {}
    private record Offer(boolean selling, int index, Entity item, String id, int price) {}
    private static final List<Product> PRODUCTS = List.of(
        new Product("red-potion", "+5 HP"),
        new Product("strong-potion", "+10 HP"),
        new Product("water-tonic", "+3 water"));
    private static final Set<String> PROTECTED = Set.of(
        "rock-tool", "iron-tool", "key", "lantern", "toxic-bubble", "super-shield");

    private static final BufferedImage SHELL = art("shell");
    private static final BufferedImage TAB = art("tab"), ACTIVE_TAB = art("tab-active");
    private static final BufferedImage ROW = art("row"), ACTIVE_ROW = art("row-active");
    private static final BufferedImage SLOT = art("slot"), ACTIVE_SLOT = art("slot-active");
    private static final BufferedImage LOCK = art("lock"), BROADCAST = art("broadcast-panel");
    private static final BufferedImage NOTICE = art("notice"), CONFIRM = art("confirm-panel");
    private static final Color INK = new Color(239, 251, 227);
    private static final Color GOLD = new Color(255, 221, 151);
    private final GamePanel gp;
    private List<Entity> catalog;
    private int product, bagCol, bagRow, frame, saleTicks;
    private boolean selling;
    private Offer pending;
    private String notice = "";

    MarketShop(GamePanel gp) { this.gp = gp; }

    public void open() {
        if (catalog == null) catalog = PRODUCTS.stream().map(p -> gp.eGenerator.requireObject(p.id())).toList();
        selling = false;
        product = bagCol = bagRow = frame = saleTicks = 0;
        pending = null;
        notice = "TV SUPPLY.\nStill on the air.";
        gp.keyH.clearPlayKeys();
        gp.gameState = gp.tradeState;
    }

    void update() {
        frame = (frame + 1) % 160;
        if (saleTicks > 0) saleTicks--;
    }

    void cancelConfirmation() { pending = null; }

    void keyPressed(int code) {
        if (pending != null) {
            if (code == KeyEvent.VK_ENTER) confirm();
            else if (code == KeyEvent.VK_ESCAPE) cancelConfirmation();
            return;
        }
        switch (code) {
            case KeyEvent.VK_ESCAPE -> {
                gp.keyH.clearPlayKeys();
                gp.gameState = gp.playState;
            }
            case KeyEvent.VK_Q -> { selling = false; notice = ""; }
            case KeyEvent.VK_E -> { selling = true; notice = ""; }
            case KeyEvent.VK_ENTER -> selectOffer();
            case KeyEvent.VK_W -> move(0, -1);
            case KeyEvent.VK_S -> move(0, 1);
            case KeyEvent.VK_A -> move(-1, 0);
            case KeyEvent.VK_D -> move(1, 0);
            default -> { }
        }
    }

    private void move(int col, int row) {
        notice = "";
        if (selling) {
            bagCol = Math.floorMod(bagCol + col, 5);
            bagRow = Math.floorMod(bagRow + row, 4);
        } else product = Math.floorMod(product + row, PRODUCTS.size());
    }

    private Entity bagItem(int index) {
        return index < gp.player.inventory.size() ? gp.player.inventory.get(index) : null;
    }

    private String protection(Entity item, String id) {
        if (item == gp.player.currentWeapon || item == gp.player.currentShield || item == gp.player.currentLight)
            return "Equipped item";
        return PROTECTED.contains(id) ? "Unique item" : "";
    }

    private void selectOffer() {
        int index = selling ? bagRow * 5 + bagCol : product;
        Entity item = selling ? bagItem(index) : catalog.get(index);
        if (item == null) { notice = "This slot is empty."; return; }
        String id = gp.eGenerator.idOf(item);
        if (selling) {
            String reason = protection(item, id);
            if (!reason.isEmpty()) { notice = reason + "."; return; }
        }
        notice = "";
        pending = new Offer(selling, index, item, id, selling ? item.price / 2 : item.price);
    }

    private void confirm() {
        Offer offer = pending;
        pending = null;
        if (offer.selling()) {
            Entity item = bagItem(offer.index());
            if (item == null || item != offer.item() || !offer.id().equals(gp.eGenerator.idOf(item))
                    || item.amount <= 0 || item.price / 2 != offer.price()) {
                notice = "The slot has changed.";
                return;
            }
            String reason = protection(item, offer.id());
            if (!reason.isEmpty()) { notice = reason + "."; return; }
            if (offer.price() < 0 || gp.player.coin > Integer.MAX_VALUE - offer.price()) {
                notice = "Cannot add those coins.";
                return;
            }
            if (item.amount > 1) item.amount--;
            else gp.player.inventory.remove(offer.index());
            gp.player.coin += offer.price();
        } else {
            Entity item = gp.eGenerator.requireObject(offer.id());
            if (item.price != offer.price()) { notice = "The price has changed."; return; }
            if (gp.player.coin < item.price) { notice = "Not enough coins."; return; }
            if (!gp.player.obtainFreshItem(item)) { notice = "No room in your bag."; return; }
            gp.player.coin -= item.price;
        }
        notice = "Thanks for tuning in.";
        saleTicks = 90;
        gp.playSE(2);
    }

    void draw(Graphics2D graphics) {
        if (catalog == null) return;
        Graphics2D g = (Graphics2D) graphics.create();
        g.scale(3, 3);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        blit(g, SHELL, 0, 0);
        label(g, "TV SUPPLY", 22, 34, 15, INK);
        String balance = "Coins: " + gp.player.coin;
        g.setFont(gp.ui.purisaB.deriveFont(8f));
        label(g, balance, 295 - g.getFontMetrics().stringWidth(balance), 31, 8, GOLD);
        blit(g, selling ? TAB : ACTIVE_TAB, 22, 49);
        blit(g, selling ? ACTIVE_TAB : TAB, 97, 49);
        label(g, "Q  BUY", 29, 60, 8, INK);
        label(g, "E  SELL", 104, 60, 8, INK);

        Entity focus = selling ? drawBag(g) : drawCatalog(g);
        blit(g, BROADCAST, 186, 71);
        label(g, "ON AIR", 195, 84, 9, GOLD);
        g.drawImage(NPC_TV.portrait(frame, saleTicks > 0), 258, 76, 29, 29, null);
        if (focus != null) {
            label(g, focus.name, 195, 115, 8, INK);
            String reason = selling ? protection(focus, gp.eGenerator.idOf(focus)) : "";
            label(g, reason.isEmpty() ? (selling ? "One unit: " + focus.price / 2 + " coins" : PRODUCTS.get(product).effect()) : reason,
                195, 128, 8, reason.isEmpty() ? INK : GOLD);
        } else label(g, "Empty slot", 195, 117, 8, INK);
        if (!notice.isEmpty()) {
            blit(g, NOTICE, 186, 132);
            String[] lines = notice.split("\\n");
            int baseline = lines.length == 1 ? 148 : 142;
            for (String line : lines) {
                label(g, line, 192, baseline, 7.5f, GOLD);
                baseline += 10;
            }
        }
        label(g, (selling ? "WASD Select" : "W/S Select") + "    Enter Confirm    Esc Back", 22, 173, 8, INK);

        if (pending != null) {
            blit(g, CONFIRM, 81, 65);
            label(g, pending.selling() ? "CONFIRM SALE" : "CONFIRM PURCHASE", 90, 81, 10, GOLD);
            label(g, "1 x " + pending.item().name, 90, 96, 9, INK);
            label(g, (pending.selling() ? "Receive " : "Pay ") + pending.price() + " coins",
                90, 108, 8, INK);
            label(g, "Enter Yes    Esc Cancel", 90, 120, 8, GOLD);
        }
        g.dispose();
    }

    private Entity drawCatalog(Graphics2D g) {
        for (int i = 0; i < catalog.size(); i++) {
            Entity item = catalog.get(i);
            int y = 72 + 27 * i;
            blit(g, i == product ? ACTIVE_ROW : ROW, 22, y);
            g.drawImage(item.down, 26, y + 4, 16, 16, null);
            label(g, item.name, 46, y + 11, 8, INK);
            label(g, PRODUCTS.get(i).effect(), 46, y + 20, 7, INK);
            label(g, item.price + " coins", 132, y + 16, 8, GOLD);
        }
        return catalog.get(product);
    }

    private Entity drawBag(Graphics2D g) {
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                int x = 22 + 29 * col, y = 71 + 22 * row;
                blit(g, col == bagCol && row == bagRow ? ACTIVE_SLOT : SLOT, x, y);
                Entity item = bagItem(row * 5 + col);
                if (item == null) continue;
                g.drawImage(item.down, x + 2, y + 2, 15, 15, null);
                if (!protection(item, gp.eGenerator.idOf(item)).isEmpty()) blit(g, LOCK, x + 15, y + 2);
                if (item.amount > 1) {
                    String amount = Integer.toString(item.amount);
                    float size = Math.min(6f, 36f / amount.length());
                    g.setFont(gp.ui.purisaB.deriveFont(size));
                    label(g, amount, x + 23 - g.getFontMetrics().stringWidth(amount), y + 17, size, INK);
                }
            }
        }
        return bagItem(bagRow * 5 + bagCol);
    }

    private void label(Graphics2D g, String text, int x, int y, float size, Color color) {
        g.setFont(gp.ui.purisaB.deriveFont(size));
        g.setColor(color);
        g.drawString(text, x, y);
    }

    private static BufferedImage art(String name) { return Market.image("ui/" + name); }
    private static void blit(Graphics2D g, BufferedImage image, int x, int y) {
        g.drawImage(image, x, y, null);
    }
}
