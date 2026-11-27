package lab.cq.refactor;

/**
 * Shared immutable-reference domain item from the classic Gilded Rose Kata.
 */
public class Item {

    public String name;
    public int sellIn;
    public int quality;

    public Item(String name, int sellIn, int quality) {
        this.name = name;
        this.sellIn = sellIn;
        this.quality = quality;
    }

    @Override
    public String toString() {
        return name + ", " + sellIn + ", " + quality;
    }
}
