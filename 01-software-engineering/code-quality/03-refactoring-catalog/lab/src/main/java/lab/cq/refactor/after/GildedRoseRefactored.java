package lab.cq.refactor.after;

import lab.cq.refactor.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Refactored Gilded Rose applying Extract Method + Replace Conditional with Polymorphism.
 * Behavior is 100% preserved for existing categories and cleanly extensible for new items.
 */
public class GildedRoseRefactored {

    @FunctionalInterface
    public interface ItemUpdater {
        void update(Item item);
    }

    private final Item[] items;
    private final Map<String, ItemUpdater> updaters;
    private final ItemUpdater defaultUpdater = item -> {
        decrementQuality(item, 1);
        item.sellIn--;
        if (item.sellIn < 0) {
            decrementQuality(item, 1);
        }
    };

    public GildedRoseRefactored(Item[] items) {
        this(items, Map.of());
    }

    public GildedRoseRefactored(Item[] items, Map<String, ItemUpdater> customUpdaters) {
        this.items = items;
        this.updaters = new HashMap<>(builtinUpdaters());
        this.updaters.putAll(customUpdaters);
    }

    private static Map<String, ItemUpdater> builtinUpdaters() {
        return Map.of(
                "Aged Brie", item -> {
                    incrementQuality(item, 1);
                    item.sellIn--;
                    if (item.sellIn < 0) {
                        incrementQuality(item, 1);
                    }
                },
                "Backstage passes to a TAFKAL80ETC concert", item -> {
                    incrementQuality(item, 1);
                    if (item.sellIn < 11) {
                        incrementQuality(item, 1);
                    }
                    if (item.sellIn < 6) {
                        incrementQuality(item, 1);
                    }
                    item.sellIn--;
                    if (item.sellIn < 0) {
                        item.quality = 0;
                    }
                },
                "Sulfuras, Hand of Ragnaros", item -> {
                    // Legendary item: neither sellIn nor quality changes
                }
        );
    }

    public void updateQuality() {
        for (Item item : items) {
            updaters.getOrDefault(item.name, defaultUpdater).update(item);
        }
    }

    public static void incrementQuality(Item item, int step) {
        for (int i = 0; i < step && item.quality < 50; i++) {
            item.quality++;
        }
    }

    public static void decrementQuality(Item item, int step) {
        for (int i = 0; i < step && item.quality > 0; i++) {
            item.quality--;
        }
    }
}
