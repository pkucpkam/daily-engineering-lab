package lab.cq.refactor;

import lab.cq.refactor.after.GildedRoseRefactored;
import lab.cq.refactor.before.GildedRoseLegacy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GildedRoseCharacterizationTest {

    private static List<Item> buildMatrix() {
        String[] names = {
                "+5 Dexterity Vest",
                "Aged Brie",
                "Backstage passes to a TAFKAL80ETC concert",
                "Sulfuras, Hand of Ragnaros"
        };
        int[] sellIns = {-1, 0, 1, 5, 6, 10, 11, 15};
        int[] qualities = {0, 1, 48, 49, 50};

        List<Item> list = new ArrayList<>();
        for (String name : names) {
            if ("Sulfuras, Hand of Ragnaros".equals(name)) {
                list.add(new Item(name, 0, 80));
                list.add(new Item(name, -1, 80));
                continue;
            }
            for (int sellIn : sellIns) {
                for (int quality : qualities) {
                    list.add(new Item(name, sellIn, quality));
                }
            }
        }
        return list;
    }

    @Test
    void refactoredMatchesLegacyAcrossAll122CombinationsOver15Days() {
        List<Item> legacyList = buildMatrix();
        List<Item> refactoredList = buildMatrix();

        assertThat(legacyList).hasSize(122);

        GildedRoseLegacy legacy = new GildedRoseLegacy(legacyList.toArray(Item[]::new));
        GildedRoseRefactored refactored = new GildedRoseRefactored(refactoredList.toArray(Item[]::new));

        for (int day = 0; day < 15; day++) {
            legacy.updateQuality();
            refactored.updateQuality();
            for (int i = 0; i < legacyList.size(); i++) {
                assertThat(refactoredList.get(i).toString())
                        .as("Day %d index %d", day, i)
                        .isEqualTo(legacyList.get(i).toString());
            }
        }
    }
}
