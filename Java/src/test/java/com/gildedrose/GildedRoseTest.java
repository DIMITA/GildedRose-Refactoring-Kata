package com.gildedrose;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GildedRoseTest {

    private Item updateOnce(String name, int sellIn, int quality) {
        Item[] items = new Item[]{ new Item(name, sellIn, quality) };
        new GildedRose(items).updateQuality();
        return items[0];
    }

    @Nested
    @DisplayName("Item normal")
    class NormalItem {

        @Test
        @DisplayName("sellIn et quality diminuent de 1 chaque jour")
        void decreasesByOneBeforeExpiry() {
            Item item = updateOnce("foo", 10, 20);
            assertEquals(9, item.sellIn);
            assertEquals(19, item.quality);
        }

        @Test
        @DisplayName("quality se dégrade 2x plus vite après péremption")
        void degradesTwiceAsFastAfterExpiry() {
            Item item = updateOnce("foo", 0, 20);
            assertEquals(-1, item.sellIn);
            assertEquals(18, item.quality);
        }

        @Test
        @DisplayName("quality ne peut pas être négative")
        void qualityNeverNegative() {
            Item item = updateOnce("foo", 5, 0);
            assertEquals(0, item.quality);
        }

        @Test
        @DisplayName("quality ne peut pas être négative après péremption")
        void qualityNeverNegativeAfterExpiry() {
            Item item = updateOnce("foo", 0, 0);
            assertEquals(0, item.quality);
        }

        @Test
        @DisplayName("quality à 1 avant péremption tombe à 0")
        void qualityAtOneGoesToZero() {
            Item item = updateOnce("foo", 5, 1);
            assertEquals(0, item.quality);
        }

        @Test
        @DisplayName("quality à 1 après péremption tombe à 0 (plancher)")
        void qualityAtOneAfterExpiryGoesToZero() {
            Item item = updateOnce("foo", 0, 1);
            assertEquals(0, item.quality);
        }
    }

    @Nested
    @DisplayName("Aged Brie")
    class AgedBrie {

        @Test
        @DisplayName("quality augmente de 1 chaque jour")
        void qualityIncreasesWithAge() {
            Item item = updateOnce("Aged Brie", 10, 20);
            assertEquals(9, item.sellIn);
            assertEquals(21, item.quality);
        }

        @Test
        @DisplayName("quality augmente de 2 après péremption")
        void qualityIncreasesTwiceAfterExpiry() {
            Item item = updateOnce("Aged Brie", 0, 20);
            assertEquals(-1, item.sellIn);
            assertEquals(22, item.quality);
        }

        @Test
        @DisplayName("quality ne dépasse jamais 50")
        void qualityNeverExceedsFifty() {
            Item item = updateOnce("Aged Brie", 10, 50);
            assertEquals(50, item.quality);
        }

        @Test
        @DisplayName("quality à 49 monte à 50 max")
        void qualityAt49GoesTo50() {
            Item item = updateOnce("Aged Brie", 10, 49);
            assertEquals(50, item.quality);
        }

        @Test
        @DisplayName("quality à 49 après péremption monte à 50 max")
        void qualityAt49AfterExpiryGoesTo50() {
            Item item = updateOnce("Aged Brie", 0, 49);
            assertEquals(50, item.quality);
        }
    }

    @Nested
    @DisplayName("Sulfuras")
    class Sulfuras {

        private static final String NAME = "Sulfuras, Hand of Ragnaros";

        @Test
        @DisplayName("sellIn ne change jamais")
        void sellInNeverChanges() {
            Item item = updateOnce(NAME, 10, 80);
            assertEquals(10, item.sellIn);
        }

        @Test
        @DisplayName("quality reste à 80")
        void qualityAlways80() {
            Item item = updateOnce(NAME, 10, 80);
            assertEquals(80, item.quality);
        }

        @Test
        @DisplayName("quality ne change pas avec sellIn négatif")
        void qualityUnchangedWhenSellInNegative() {
            Item item = updateOnce(NAME, -1, 80);
            assertEquals(80, item.quality);
        }
    }

    @Nested
    @DisplayName("Backstage passes")
    class BackstagePasses {

        private static final String NAME = "Backstage passes to a TAFKAL80ETC concert";

        @Test
        @DisplayName("quality +1 quand sellIn > 10")
        void increasesByOneWhenMoreThan10Days() {
            Item item = updateOnce(NAME, 15, 20);
            assertEquals(14, item.sellIn);
            assertEquals(21, item.quality);
        }

        @Test
        @DisplayName("quality +2 quand sellIn = 10")
        void increasesByTwoAt10Days() {
            Item item = updateOnce(NAME, 10, 20);
            assertEquals(22, item.quality);
        }

        @Test
        @DisplayName("quality +2 quand sellIn entre 6 et 10")
        void increasesByTwoBetween6And10Days() {
            Item item = updateOnce(NAME, 7, 20);
            assertEquals(22, item.quality);
        }

        @Test
        @DisplayName("quality +3 quand sellIn = 5")
        void increasesByThreeAt5Days() {
            Item item = updateOnce(NAME, 5, 20);
            assertEquals(23, item.quality);
        }

        @Test
        @DisplayName("quality +3 quand sellIn entre 1 et 5")
        void increasesByThreeBetween1And5Days() {
            Item item = updateOnce(NAME, 3, 20);
            assertEquals(23, item.quality);
        }

        @Test
        @DisplayName("quality tombe à 0 après le concert")
        void qualityDropsToZeroAfterConcert() {
            Item item = updateOnce(NAME, 0, 40);
            assertEquals(0, item.quality);
        }

        @Test
        @DisplayName("quality plafonnée à 50 avec bonus +2")
        void qualityNeverExceeds50WithDoubleBonus() {
            Item item = updateOnce(NAME, 10, 49);
            assertEquals(50, item.quality);
        }

        @Test
        @DisplayName("quality plafonnée à 50 avec bonus +3")
        void qualityNeverExceeds50WithTripleBonus() {
            Item item = updateOnce(NAME, 5, 49);
            assertEquals(50, item.quality);
        }
    }

    @Nested
    @DisplayName("Conjured")
    class Conjured {

        @Test
        @DisplayName("quality se dégrade de 2 avant péremption")
        void degradesByTwoBeforeExpiry() {
            Item item = updateOnce("Conjured Mana Cake", 10, 20);
            assertEquals(9, item.sellIn);
            assertEquals(18, item.quality);
        }

        @Test
        @DisplayName("quality se dégrade de 4 après péremption")
        void degradesByFourAfterExpiry() {
            Item item = updateOnce("Conjured Mana Cake", 0, 20);
            assertEquals(-1, item.sellIn);
            assertEquals(16, item.quality);
        }

        @Test
        @DisplayName("quality ne peut pas être négative")
        void qualityNeverNegative() {
            Item item = updateOnce("Conjured Mana Cake", 5, 1);
            assertEquals(0, item.quality);
        }
    }
}
