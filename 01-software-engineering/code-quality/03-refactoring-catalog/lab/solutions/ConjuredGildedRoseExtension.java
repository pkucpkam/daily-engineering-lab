import lab.cq.refactor.Item;
import lab.cq.refactor.after.GildedRoseRefactored;

import java.util.Map;

/**
 * L3 Solution: plugs in "Conjured Mana Cake" (degrades twice as fast as normal items)
 * cleanly without touching GildedRoseRefactored's existing item handlers.
 */
public class ConjuredGildedRoseExtension {

    public static void main(String[] args) {
        Item conjured = new Item("Conjured Mana Cake", 2, 10);
        Item normal = new Item("Elixir of the Mongoose", 2, 10);

        Map<String, GildedRoseRefactored.ItemUpdater> custom = Map.of(
                "Conjured Mana Cake", item -> {
                    GildedRoseRefactored.decrementQuality(item, 2);
                    item.sellIn--;
                    if (item.sellIn < 0) {
                        GildedRoseRefactored.decrementQuality(item, 2);
                    }
                }
        );

        GildedRoseRefactored rose = new GildedRoseRefactored(new Item[]{conjured, normal}, custom);
        for (int day = 1; day <= 4; day++) {
            rose.updateQuality();
            System.out.printf("Day %d -> conjured=[%s] | normal=[%s]%n", day, conjured, normal);
        }
    }
}
