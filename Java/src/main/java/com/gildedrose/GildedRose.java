package com.gildedrose;

/**
 * Système de mise à jour de l'inventaire de la Rose Dorée.
 *
 * <h2>Stratégie de refactoring</h2>
 * Le code original était une unique méthode {@code updateQuality()} de ~50 lignes
 * avec des {@code if/else} imbriqués sur 4 niveaux, illisible et non extensible.
 * Ajouter "Conjured" dans ce code aurait signifié rajouter une couche de conditions
 * supplémentaires dans un bloc déjà ingérable.
 *
 * <h2>Approche choisie : extraction de méthodes privées</h2>
 * Plutôt qu'un pattern Strategy ou de l'héritage (qui nécessiterait de modifier
 * {@code Item}, interdit par les contraintes du projet), on extrait chaque
 * responsabilité dans une méthode privée nommée explicitement.
 * Résultat : {@code updateQuality()} se lit comme une spécification métier,
 * pas comme du code machine.
 *
 * <h2>Invariants garantis</h2>
 * <ul>
 *   <li>quality ∈ [0, 50] pour tous les items sauf Sulfuras (quality = 80 fixe)</li>
 *   <li>sellIn décrémente chaque jour sauf pour Sulfuras</li>
 *   <li>La dégradation double après expiration (sellIn &lt; 0)</li>
 * </ul>
 */
class GildedRose {
    // Constantes pour éviter les "magic strings" dispersées dans le code.
    // Un typo sur un nom d'item est un bug silencieux — centraliser ici
    // garantit qu'une seule correction suffit si le nom change.
    static final String AGED_BRIE = "Aged Brie";
    static final String BACKSTAGE_PASS = "Backstage passes to a TAFKAL80ETC concert";
    static final String SULFURAS = "Sulfuras, Hand of Ragnaros";
    static final String CONJURED = "Conjured Mana Cake";

    Item[] items;

    public GildedRose(Item[] items) {
        this.items = items;
    }

    /**
     * Met à jour l'inventaire pour une journée.
     *
     * Délègue à {@link #updateItem(Item)} pour chaque article,
     * séparant ainsi la logique d'itération de la logique métier.
     */
    public void updateQuality() {
        for (Item item : items) {
            updateItem(item);
        }
    }

    /**
     * Applique les règles métier à un item selon son type.
     *
     * <p>Sulfuras est traité en premier et sort immédiatement : c'est un item
     * légendaire qui ne suit aucune règle commune. Court-circuiter le traitement
     * ici évite de polluer toutes les autres branches avec une vérification Sulfuras.
     *
     * <p>L'ordre des branches reflète l'ordre de priorité des règles métier :
     * items spéciaux d'abord, item normal en dernier comme cas par défaut.
     *
     * @param item l'article à mettre à jour
     */
    private void updateItem(Item item) {
        if (isSulfuras(item)) return;

        decreaseSellIn(item);

        if (isAgedBrie(item)) {
            // Aged Brie est le seul item dont la qualité augmente avec le temps.
            // Après expiration, elle augmente deux fois plus vite (symétrie avec
            // la dégradation double des items normaux).
            increaseQuality(item, isExpired(item) ? 2 : 1);
        } else if (isBackstagePass(item)) {
            updateBackstagePass(item);
        } else if (isConjured(item)) {
            // Conjured se dégrade comme un item normal mais au double de la vitesse.
            // Donc : 2x avant expiration, 4x après (2 * la dégradation normale post-expiration).
            decreaseQuality(item, isExpired(item) ? 4 : 2);
        } else {
            // Cas par défaut : item normal.
            // Dégradation de 1 avant expiration, de 2 après.
            decreaseQuality(item, isExpired(item) ? 2 : 1);
        }
    }

     /**
     * Gère la logique spécifique des Backstage passes.
     *
     * <p>Trois paliers d'augmentation selon la proximité du concert,
     * puis remise à zéro après le concert. La logique des paliers est
     * vérifiée APRÈS le {@code decreaseSellIn} — donc {@code sellIn < 5}
     * correspond bien à "il reste 5 jours ou moins" dans les specs.
     *
     * <p>Pourquoi séparer cette méthode : la logique à paliers est trop
     * complexe pour tenir lisiblement dans un {@code if/else} inline.
     * Une méthode dédiée avec son propre nom documente l'intention.
     *
     * @param item le backstage pass à mettre à jour
     */
    private void updateBackstagePass(Item item) {
        if (isExpired(item)) {
            // Après le concert, le pass n'a plus aucune valeur.
            item.quality = 0;
            return;
        }
        if (item.sellIn < 5) {
            increaseQuality(item, 3); // 5 jours ou moins : +3
        } else if (item.sellIn < 10) {
            increaseQuality(item, 2); // entre 6 et 10 jours : +2
        } else {
            increaseQuality(item, 1); // plus de 10 jours : +1
        }
    }
    
     /**
     * Décrémente le sellIn de 1.
     * Centralisé ici pour que toute modification future (ex: items qui
     * vieillissent deux fois plus vite) n'impacte qu'un seul endroit.
     */
    private void decreaseSellIn(Item item) {
        item.sellIn--;
    }

    /**
     * Augmente la qualité d'un item en respectant le plafond de 50.
     *
     * <p>Utilisation de {@code Math.min} plutôt qu'un {@code if} : plus concis,
     * pas de risque d'oublier le cas limite. Le plafond est une règle métier
     * globale — la centraliser ici garantit qu'elle s'applique partout.
     *
     * @param item   l'article à modifier
     * @param amount la quantité à ajouter
     */
    private void increaseQuality(Item item, int amount) {
        item.quality = Math.min(50, item.quality + amount);
    }

    /**
     * Diminue la qualité d'un item en respectant le plancher de 0.
     *
     * <p>Même logique que {@link #increaseQuality} : {@code Math.max} élimine
     * le besoin d'un {@code if (quality > 0)} avant chaque décrémentation,
     * pattern répété 4 fois dans le code original.
     *
     * @param item   l'article à modifier
     * @param amount la quantité à soustraire
     */
    private void decreaseQuality(Item item, int amount) {
        item.quality = Math.max(0, item.quality - amount);
    }

    // -------------------------------------------------------------------------
    // Prédicats — nommer les conditions métier plutôt que comparer des strings
    // inline dans le flux principal. Si le nom d'un item change, on corrige
    // la constante + le prédicat, pas 5 endroits dans le code.
    // -------------------------------------------------------------------------

    private boolean isExpired(Item item) {
        return item.sellIn < 0;
    }

    private boolean isSulfuras(Item item) {
        return item.name.equals(SULFURAS);
    }

    private boolean isAgedBrie(Item item) {
        return item.name.equals(AGED_BRIE);
    }

    private boolean isBackstagePass(Item item) {
        return item.name.equals(BACKSTAGE_PASS);
    }

    /**
     * Détecte un item "Conjured" par préfixe plutôt que par nom exact.
     *
     * <p>Les specs ne fixent pas de nom canonique unique pour les items invoqués
     * ("Conjured Mana Cake", "Conjured Sword"...). Utiliser {@code startsWith}
     * couvre tous les items de cette catégorie sans avoir à maintenir une liste.
     * Si les specs imposent un nom exact, remplacer par {@code equals} avec
     * une constante dédiée.
     */
    private boolean isConjured(Item item) {
        return item.name.startsWith("Conjured");
    }
}
