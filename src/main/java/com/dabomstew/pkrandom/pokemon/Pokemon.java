package com.dabomstew.pkrandom.pokemon;

/*----------------------------------------------------------------------------*/
/*--  Pokemon.java - represents an individual Pokemon, and contains         --*/
/*--                 common Pokemon-related functions.                      --*/
/*--                                                                        --*/
/*--  Part of "Universal Pokemon Randomizer" by Dabomstew                   --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2012.                                 --*/
/*--                                                                        --*/
/*--  The custom code written here is licensed under the terms of the GPL:  --*/
/*--                                                                        --*/
/*--  This program is free software: you can redistribute it and/or modify  --*/
/*--  it under the terms of the GNU General Public License as published by  --*/
/*--  the Free Software Foundation, either version 3 of the License, or     --*/
/*--  (at your option) any later version.                                   --*/
/*--                                                                        --*/
/*--  This program is distributed in the hope that it will be useful,       --*/
/*--  but WITHOUT ANY WARRANTY; without even the implied warranty of        --*/
/*--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          --*/
/*--  GNU General Public License for more details.                          --*/
/*--                                                                        --*/
/*--  You should have received a copy of the GNU General Public License     --*/
/*--  along with this program. If not, see <http://www.gnu.org/licenses/>.  --*/
/*----------------------------------------------------------------------------*/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Pokemon implements Comparable<Pokemon> {

    public String name;
    public int number;

    public Type primaryType, secondaryType;

    public int hp, attack, defense, spatk, spdef, speed, special;

    public int ability1, ability2, ability3;

    public int catchRate, expYield;

    public int guaranteedHeldItem, commonHeldItem, rareHeldItem, darkGrassHeldItem;

    public int genderRatio;

    public int frontSpritePointer, picDimensions;

    public ExpCurve growthCurve;

    public List<Evolution> evolutionsFrom = new ArrayList<Evolution>();
    public List<Evolution> evolutionsTo = new ArrayList<Evolution>();

    public List<Integer> shuffledStatsOrder = null;
    public int typeChanged;

    private static final double GENERAL_MEDIAN = 411.5;
    private static final double GENERAL_SD = 108.5;
    private static final double GENERAL_SKEW = -0.1;
    private static final double EVO1_2EVOS_MEDIAN = 300;
    private static final double EVO1_2EVOS_SD = 37;
    private static final double EVO1_2EVOS_SKEW = -0.9;
    private static final double PK_2EVOS_DIFF_MEDIAN = 100;
    private static final double PK_2EVOS_DIFF_SD = 44;
    private static final double PK_2EVOS_DIFF_SKEW = 0.7;
    private static final double EVO1_1EVO_MEDIAN = 310;
    private static final double EVO1_1EVO_SD = 44;
    private static final double EVO1_1EVO_SKEW = -0.6;
    private static final double PK_1EVO_DIFF_MEDIAN = 162.5;
    private static final double PK_1EVO_DIFF_SD = 36;
    private static final double PK_1EVO_DIFF_SKEW = 0.5;
    private static final double NO_EVO_MEDIAN = 487;
    private static final double NO_EVO_SD = 94.0;
    private static final double NO_EVO_SKEW = -0.2;
    private static final double MAX_EVO_MEDIAN = 490;
    private static final double MAX_EVO_SD = 43.5;
    private static final double MAX_EVO_SKEW = -0.3;

    // A flag to use for things like recursive stats copying.
    // Must not rely on the state of this flag being preserved between calls.
    public boolean temporaryFlag;

    private static final List<Integer> legendaries = Arrays.asList(144, 145, 146, 150, 151, 243,
            244, 245, 249, 250, 251, 377, 378, 379, 380, 381, 382, 383, 384, 385, 386, 480, 481,
            482, 483, 484, 485, 486, 487, 488, 489, 490, 491, 492, 493, 494, 638, 639, 640, 641,
            642, 643, 644, 645, 646, 647, 648, 649);

    public Pokemon() {
        shuffledStatsOrder = Arrays.asList(0, 1, 2, 3, 4, 5);
    }

    // Select a type from the ones on this pokemon
    public Type randomOfTypes(Random random) {
        if (secondaryType == null) {
            return primaryType;
        }

        return random.nextBoolean() ? primaryType : secondaryType;
    }

    public void assignTypeByReference(Pokemon ref, int typesDiffer,
            Supplier<Type> defaultFunction) {
        switch (typesDiffer) {
            case 0:
                this.primaryType = ref.primaryType;
                this.secondaryType = ref.secondaryType;
                this.typeChanged = ref.typeChanged;
                break;
            case 1:
                this.primaryType = ref.typeChanged == 1 ? defaultFunction.get() : this.primaryType;
                this.secondaryType = ref.typeChanged == 2 ? ref.secondaryType : this.secondaryType;
                this.typeChanged = ref.typeChanged;
                while (this.primaryType == this.secondaryType) {
                    this.primaryType =
                            this.typeChanged == 1 ? defaultFunction.get() : this.primaryType;
                    this.secondaryType =
                            this.typeChanged == 2 ? defaultFunction.get() : this.secondaryType;
                }
                break;
            case 2:
                this.primaryType = ref.typeChanged == 1 ? ref.primaryType
                        : this.secondaryType != null ? this.primaryType : ref.secondaryType;
                this.secondaryType =
                        ref.typeChanged == 2 && this.secondaryType != null ? defaultFunction.get()
                                : this.secondaryType;
                this.typeChanged = this.secondaryType != null ? ref.typeChanged : 1;
                while (this.primaryType == this.secondaryType) {
                    this.primaryType =
                            this.typeChanged == 1 ? defaultFunction.get() : this.primaryType;
                    this.secondaryType =
                            this.typeChanged == 2 ? defaultFunction.get() : this.secondaryType;
                }
                break;
            case 3:
                this.primaryType = ref.typeChanged == 1 ? this.primaryType : defaultFunction.get();
                this.secondaryType = ref.typeChanged == 1 ? ref.primaryType : this.secondaryType;
                this.typeChanged = ref.typeChanged == 1 ? 2 : 1;
                while (this.primaryType == this.secondaryType) {
                    this.primaryType =
                            this.typeChanged == 1 ? defaultFunction.get() : this.primaryType;
                    this.secondaryType =
                            this.typeChanged == 2 ? defaultFunction.get() : this.secondaryType;
                }
                break;
            case 4:
                this.primaryType = ref.typeChanged == 2 ? ref.secondaryType
                        : this.secondaryType != null ? ref.secondaryType : ref.primaryType;
                this.secondaryType =
                        ref.typeChanged == 1 && this.secondaryType != null ? defaultFunction.get()
                                : this.secondaryType;
                this.typeChanged = ref.typeChanged == 1 && this.secondaryType != null ? 2 : 1;
                while (this.primaryType == this.secondaryType) {
                    this.primaryType =
                            this.typeChanged == 1 ? defaultFunction.get() : this.primaryType;
                    this.secondaryType =
                            this.typeChanged == 2 ? defaultFunction.get() : this.secondaryType;
                }
                break;
        }
    }

    public Type getRandomWeakness(Random random, boolean useResistantType) {
        return Type.randomWeakness(random, useResistantType, this.primaryType, this.secondaryType);
    }

    public boolean isWeakTo(Pokemon pkmn) {
        boolean isWeak = Type.STRONG_AGAINST.get(this.primaryType).contains(pkmn.primaryType)
                || Type.STRONG_AGAINST.get(this.primaryType).contains(pkmn.secondaryType);
        if (this.secondaryType != null && !isWeak) {
            isWeak = Type.STRONG_AGAINST.get(this.secondaryType).contains(pkmn.primaryType)
                    || Type.STRONG_AGAINST.get(this.secondaryType).contains(pkmn.secondaryType);
        }
        return isWeak;
    }

    public int evolutionChainSize() {
        int length = 0;
        for (Evolution ev : this.evolutionsFrom) {
            int temp = ev.to.evolutionChainSize();
            if (temp > length) {
                length = temp;
            }
        }
        return length + 1;
    }

    public boolean isCyclic(Set<Pokemon> visited, Set<Pokemon> recStack) {
        if (!visited.contains(this)) {
            visited.add(this);
            recStack.add(this);
            for (Evolution ev : this.evolutionsFrom) {
                if (!visited.contains(ev.to) && ev.to.isCyclic(visited, recStack)) {
                    return true;
                } else if (recStack.contains(ev.to)) {
                    return true;
                }
            }
        }
        recStack.remove(this);
        return false;
    }

    public int minimumLevel() {
        int min = 1;
        for (Evolution evo : evolutionsTo) {
            int evoMin = 1;
            if (evo.type.usesLevel()) {
                evoMin = evo.extraInfo;
            } else {
                // TODO: Make this better (move MoveLearnt to Pokemon, etc.).
                switch (evo.type) {
                    case STONE:
                    case STONE_FEMALE_ONLY:
                    case STONE_MALE_ONLY:
                        evoMin = 24;
                        break;

                    case TRADE:
                    case TRADE_ITEM:
                    case TRADE_SPECIAL:
                        evoMin = 37;
                        break;

                    default:
                        evoMin = 33;
                        break;
                }
            }

            if (evoMin > min) {
                min = evoMin;
            }
        }
        return min;
    }

    public int nearestEvoTarget(int level) {
        int target = -1;
        int evoMin = -1;
        for (int i = 0; i < evolutionsFrom.size(); i++) {
            if (evolutionsFrom.get(i).type.usesLevel()) {
                evoMin = evolutionsFrom.get(i).extraInfo;
            } else {
                switch (evolutionsFrom.get(i).type) {
                    case STONE:
                    case STONE_FEMALE_ONLY:
                    case STONE_MALE_ONLY:
                        evoMin = 24;
                        break;

                    case TRADE:
                    case TRADE_ITEM:
                    case TRADE_SPECIAL:
                        evoMin = 37;
                        break;

                    default:
                        evoMin = 33;
                        break;
                }
            }

            // Target represents the evolution index
            target = evoMin <= level ? i : target;
        }
        return target;
    }

    public void shuffleStats(Random random) {
        Collections.shuffle(shuffledStatsOrder, random);
        applyShuffledOrderToStats();
    }

    public void copyShuffledStatsUpEvolution(Pokemon evolvesFrom) {
        shuffledStatsOrder = evolvesFrom.shuffledStatsOrder;
        applyShuffledOrderToStats();
    }

    private void applyShuffledOrderToStats() {
        List<Integer> stats = Arrays.asList(hp, attack, defense, spatk, spdef, speed);

        // Copy in new stats
        hp = stats.get(shuffledStatsOrder.get(0));
        attack = stats.get(shuffledStatsOrder.get(1));
        defense = stats.get(shuffledStatsOrder.get(2));
        spatk = stats.get(shuffledStatsOrder.get(3));
        spdef = stats.get(shuffledStatsOrder.get(4));
        speed = stats.get(shuffledStatsOrder.get(5));

        // make special the average of spatk and spdef
        special = (int) Math.ceil((spatk + spdef) / 2.0f);
    }

    public void randomizeStatsWithinBST(Random random) {
        if (number == 292) {
            // Shedinja is horribly broken unless we restrict him to 1HP.
            int bst = bst() - 51;

            // Make weightings
            double atkW = random.nextDouble(), defW = random.nextDouble();
            double spaW = random.nextDouble(), spdW = random.nextDouble(),
                    speW = random.nextDouble();

            double totW = atkW + defW + spaW + spdW + speW;

            hp = 1;
            attack = (int) Math.max(1, Math.round(atkW / totW * bst)) + 10;
            defense = (int) Math.max(1, Math.round(defW / totW * bst)) + 10;
            spatk = (int) Math.max(1, Math.round(spaW / totW * bst)) + 10;
            spdef = (int) Math.max(1, Math.round(spdW / totW * bst)) + 10;
            speed = (int) Math.max(1, Math.round(speW / totW * bst)) + 10;

            // Fix up special too
            special = (int) Math.ceil((spatk + spdef) / 2.0f);

        } else {
            // Minimum 10 everything not including HP
            int bst = bst() - 50;

            // Make weightings
            double hpW = random.nextDouble(), atkW = random.nextDouble(),
                    defW = random.nextDouble();
            double spaW = random.nextDouble(), spdW = random.nextDouble(),
                    speW = random.nextDouble();

            double totW = hpW + atkW + defW + spaW + spdW + speW;

            // Handle HP specially to avoid skewing
            float suggestedHP = Math.round(hpW / totW * bst);
            hp = suggestedHP < 35 ? 35 : (int) suggestedHP;
            // Remove any added stats from the remaining bst
            bst -= suggestedHP < 35 ? (35 - suggestedHP) : 0;

            // Handle the rest normally
            attack = (int) Math.max(1, Math.round(atkW / totW * bst)) + 10;
            defense = (int) Math.max(1, Math.round(defW / totW * bst)) + 10;
            spatk = (int) Math.max(1, Math.round(spaW / totW * bst)) + 10;
            spdef = (int) Math.max(1, Math.round(spdW / totW * bst)) + 10;
            speed = (int) Math.max(1, Math.round(speW / totW * bst)) + 10;

            // Fix up special too
            special = (int) Math.ceil((spatk + spdef) / 2.0f);
        }

        // Check for something we can't store
        if (hp > 255 || attack > 255 || defense > 255 || spatk > 255 || spdef > 255
                || speed > 255) {
            // re roll
            randomizeStatsWithinBST(random);
        }

    }

    public void copyRandomizedStatsUpEvolution(Pokemon evolvesFrom, Random random) {
        double ourBST = bst();
        double theirBST = evolvesFrom.bst();

        double bstRatio = ourBST / theirBST;

        // Lower HP growth by 10% to allow other stats a chance to grow (except when growth is
        // already under 10%)
        hp = (int) Math.min(283, Math.max(1, Math.round(evolvesFrom.hp * bstRatio)));
        int hpDiff = 1.1f < bstRatio ? Math.round(hp * 0.1f) : 0;
        hp -= hpDiff;

        // Convert HPDiff into series of ints
        int hpInt = hpDiff / 5;
        int hpRem = hpDiff % 5;
        int[] hpArray = new int[] {hpInt, hpInt, hpInt, hpInt, hpInt};

        // Add remainder to random spots in hpArray
        for (int i = 0; i < hpRem; i++) {
            hpArray[Math.abs(random.nextInt() % 5)]++;
        }


        // Add HPDiff to remaining stats
        attack = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.attack * bstRatio)))
                + hpArray[0];
        defense = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.defense * bstRatio)))
                + hpArray[1];
        speed = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.speed * bstRatio)))
                + hpArray[2];
        spatk = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.spatk * bstRatio)))
                + hpArray[3];
        spdef = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.spdef * bstRatio)))
                + hpArray[4];

        special = (int) Math.ceil((spatk + spdef) / 2.0f);
    }

    public void randomizeStatsNoRestrictions(Random random, boolean evolutionSanity) {
        double weightSd = 0.16;

        if (number == 292) {
            // Shedinja is horribly broken unless we restrict him to 1HP.
            int bst;

            if (evolutionSanity) {
                bst = (int) (PK_1EVO_DIFF_MEDIAN
                        + skewedGaussian(random.nextGaussian(), PK_1EVO_DIFF_SKEW) * PK_1EVO_DIFF_SD
                        - 51);
            } else {
                bst = (int) (GENERAL_MEDIAN
                        + skewedGaussian(random.nextGaussian(), GENERAL_SKEW) * GENERAL_SD - 51);
            }
            // Make weightings
            double atkW = Math.max(0, Math.min(1, random.nextGaussian() * weightSd + 0.5));
            double defW = Math.max(0, Math.min(1, random.nextGaussian() * weightSd + 0.5));
            double spaW = Math.max(0, Math.min(1, random.nextGaussian() * weightSd + 0.5));
            double spdW = Math.max(0, Math.min(1, random.nextGaussian() * weightSd + 0.5));
            double speW = Math.max(0, Math.min(1, random.nextGaussian() * weightSd + 0.5));

            double totW = atkW + defW + spaW + spdW + speW;

            hp = 1;
            attack = (int) Math.max(1, Math.round(atkW / totW * bst)) + 10;
            defense = (int) Math.max(1, Math.round(defW / totW * bst)) + 10;
            spatk = (int) Math.max(1, Math.round(spaW / totW * bst)) + 10;
            spdef = (int) Math.max(1, Math.round(spdW / totW * bst)) + 10;
            speed = (int) Math.max(1, Math.round(speW / totW * bst)) + 10;

            // Fix up special too
            special = (int) Math.ceil((spatk + spdef) / 2.0f);
        } else {
            // Minimum 10 everything not including HP
            int bst;
            if (evolutionSanity) {
                if (evolutionsFrom.size() > 0) {
                    boolean pk2Evos = false;

                    for (Evolution ev : evolutionsFrom) {
                        // If any of the targets here evolve, the original
                        // Pokemon has 2+ stages.
                        if (ev.to.evolutionsFrom.size() > 0) {
                            pk2Evos = true;
                            break;
                        }
                    }

                    if (pk2Evos) {
                        // First evo of 3 stages
                        bst = (int) (EVO1_2EVOS_MEDIAN
                                + skewedGaussian(random.nextGaussian(), EVO1_2EVOS_SKEW)
                                        * EVO1_2EVOS_SD
                                - 50);
                    } else {
                        // First evo of 2 stages
                        bst = (int) (EVO1_1EVO_MEDIAN
                                + skewedGaussian(random.nextGaussian(), EVO1_1EVO_SKEW)
                                        * EVO1_1EVO_SD
                                - 50);
                    }
                } else {
                    if (evolutionsTo.size() > 0) {
                        // Last evo, doesn't carry stats
                        bst = (int) (MAX_EVO_MEDIAN
                                + skewedGaussian(random.nextGaussian(), MAX_EVO_SKEW) * MAX_EVO_SD
                                - 50);
                    } else {
                        // No evolutions, no pre-evolutions
                        bst = (int) (NO_EVO_MEDIAN
                                + skewedGaussian(random.nextGaussian(), NO_EVO_SKEW) * NO_EVO_SD
                                - 50);
                    }
                }
            } else {
                // No 'Follow evolutions'
                bst = (int) (GENERAL_MEDIAN
                        + skewedGaussian(random.nextGaussian(), GENERAL_SKEW) * GENERAL_SD - 50);
            }

            // Make weightings
            double hpW = Math.max(0.01, Math.min(1, random.nextGaussian() * weightSd + 0.5));
            double atkW = Math.max(0.01, Math.min(1, random.nextGaussian() * weightSd + 0.5));
            double defW = Math.max(0.01, Math.min(1, random.nextGaussian() * weightSd + 0.5));
            double spaW = Math.max(0.01, Math.min(1, random.nextGaussian() * weightSd + 0.5));
            double spdW = Math.max(0.01, Math.min(1, random.nextGaussian() * weightSd + 0.5));
            double speW = Math.max(0.01, Math.min(1, random.nextGaussian() * weightSd + 0.5));

            double totW = hpW + atkW + defW + spaW + spdW + speW;

            // Handle HP specially to avoid skewing
            float suggestedHP = Math.round(hpW / totW * bst);
            hp = suggestedHP < 35 ? 35 : (int) suggestedHP;
            // Remove any added stats from the remaining bst
            bst -= suggestedHP < 35 ? (35 - suggestedHP) : 0;

            // Handle the rest normally
            attack = (int) Math.max(1, Math.round(atkW / totW * bst)) + 10;
            defense = (int) Math.max(1, Math.round(defW / totW * bst)) + 10;
            spatk = (int) Math.max(1, Math.round(spaW / totW * bst)) + 10;
            spdef = (int) Math.max(1, Math.round(spdW / totW * bst)) + 10;
            speed = (int) Math.max(1, Math.round(speW / totW * bst)) + 10;

            // Fix up special too
            special = (int) Math.ceil((spatk + spdef) / 2.0f);
        }

        // Check for something we can't store
        if (hp > 255 || attack > 255 || defense > 255 || spatk > 255 || spdef > 255
                || speed > 255) {
            // re roll
            randomizeStatsNoRestrictions(random, evolutionSanity);
        }
    }

    public void copyRandomizedStatsNoRestrictionsUpEvolution(Pokemon evolvesFrom, Random random) {
        double theirBST = evolvesFrom.bst();
        double ourBST;
        double bstRatio;

        do {
            if (evolutionsFrom.size() > 0 || (evolutionsTo.get(0).from.evolutionsTo.size() > 0)) {
                // 3 stages
                ourBST = theirBST + PK_2EVOS_DIFF_MEDIAN
                        + skewedGaussian(random.nextGaussian(), PK_2EVOS_DIFF_SKEW)
                                * PK_2EVOS_DIFF_SD;
            } else {
                // 2 stages
                ourBST = theirBST + PK_1EVO_DIFF_MEDIAN
                        + skewedGaussian(random.nextGaussian(), PK_1EVO_DIFF_SKEW)
                                * PK_1EVO_DIFF_SD;
            }
            bstRatio = ourBST / theirBST;
        } while (bstRatio < 1);

        hp = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.hp * bstRatio)));
        attack = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.attack * bstRatio)));
        defense = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.defense * bstRatio)));
        speed = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.speed * bstRatio)));
        spatk = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.spatk * bstRatio)));
        spdef = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.spdef * bstRatio)));

        special = (int) Math.ceil((spatk + spdef) / 2.0f);
    }

    private double skewedGaussian(double gaussian, double skew) {
        double skewedCdf = ((1 - Math.exp(-1.7 * gaussian * skew))
                / (2 * (1 + Math.exp(-1.7 * gaussian * skew))) + 0.5);
        return 2 * gaussian * skewedCdf;
    }

    public void copyCompletelyRandomizedStatsUpEvolution(Pokemon evolvesFrom, Random random,
            double meanBST) {
        double theirBST = evolvesFrom.bst();
        double ratio = meanBST / theirBST;
        double mean, stdDev;

        // mean < 201 e.g. Caterpie, Weedle, Metapod, Kakuna, Magikarp
        if (ratio > 1.7) {
            mean = 0.5;
            stdDev = 0.3; // Average multiplier = 1.5x (301), max multiplier = 2.7x (540)
        }
        // mean < 244 e.g. Rattata, Pidgey, Jigglypuff, Ekans, Diglett, Ditto
        else if (ratio > 1.4) {
            mean = 0.5;
            stdDev = 0.2; // Average multiplier = 1.5x, (366) max multiplier = 2.3x (561)
        }
        // mean < 342 e.g. starters, Koffing, Butterfree, Onyx, Raticate, Jynx
        else if (ratio > 1.0) {
            mean = 0.25;
            stdDev = 0.1; // Average multiplier = 1.25x (427), max multiplier = 1.65x (564)
        }
        // mean < 427 e.g. Raichu, Muk, Chansey, Machamp, Rapidash, Charizard
        else if (ratio > 0.8) {
            mean = 0;
            stdDev = 0.1; // Average multiplier = 1.0x (427), max multiplier = 1.4x (597)
        }
        // mean < 488 e.g. Tauros, Arcanine, Gyarados, Articuno
        else if (ratio > 0.7) {
            mean = -0.1;
            stdDev = 0.1; // Average multiplier = 0.9x (488), max multiplier = 1.3x (634) [clamped
                          // to 1.0x min]
        }
        // mean > 488 e.g. Dragonite, Moltres, Zapdos, Mew, Mewtwo
        else {
            mean = -0.2;
            stdDev = 0.1; // Average multiplier = 0.8x (~500), max multiplier = 1.2x (760 = max roll
                          // of above group max rolled again)
        }

        double multiplier = Math.max(1.05 + mean + (random.nextGaussian() * stdDev), 1.05);

        // Allow each stat to vary by +- 5% so stats vary a little between them
        hp = (int) Math.min(255, Math.max(1,
                Math.round(evolvesFrom.hp * multiplier * (0.95 + random.nextDouble() / 10))));
        attack = (int) Math.min(255, Math.max(1,
                Math.round(evolvesFrom.attack * multiplier * (0.95 + random.nextDouble() / 10))));
        defense = (int) Math.min(255, Math.max(1,
                Math.round(evolvesFrom.defense * multiplier * (0.95 + random.nextDouble() / 10))));
        speed = (int) Math.min(255, Math.max(1,
                Math.round(evolvesFrom.speed * multiplier * (0.95 + random.nextDouble() / 10))));
        spatk = (int) Math.min(255, Math.max(1,
                Math.round(evolvesFrom.spatk * multiplier * (0.95 + random.nextDouble() / 10))));
        spdef = (int) Math.min(255, Math.max(1,
                Math.round(evolvesFrom.spdef * multiplier * (0.95 + random.nextDouble() / 10))));

        special = (int) Math.ceil((spatk + spdef) / 2.0f);
    }

    public boolean isLegendary() {
        return legendaries.contains(this.number);
    }

    public boolean isBigPoke(boolean isGen1) {
        if (isGen1) {
            return gen1Bst() > 490
                    || Collections.max(Arrays.asList(hp, attack, defense, speed, special)) > 190;
        } else {
            return bst() > 590 || Collections
                    .max(Arrays.asList(hp, attack, defense, speed, spatk, spdef)) > 190;
        }
    }

    public int bst() {
        return hp + attack + defense + spatk + spdef + speed;
    }

    public int gen1Bst() {
        return hp + attack + defense + special + speed;
    }

    public int bstForPowerLevels() {
        // Take into account Shedinja's purposefully nerfed HP
        if (number == 292) {
            return (attack + defense + spatk + spdef + speed) * 6 / 5;
        } else {
            return hp + attack + defense + spatk + spdef + speed;
        }
    }

    @Override
    public String toString() {
        return "Pokemon [name=" + name + ", number=" + number + ", primaryType=" + primaryType
                + ", secondaryType=" + secondaryType + ", hp=" + hp + ", attack=" + attack
                + ", defense=" + defense + ", spatk=" + spatk + ", spdef=" + spdef + ", speed="
                + speed + "]";
    }

    public String toStringRBY() {
        return "Pokemon [name=" + name + ", number=" + number + ", primaryType=" + primaryType
                + ", secondaryType=" + secondaryType + ", hp=" + hp + ", attack=" + attack
                + ", defense=" + defense + ", special=" + special + ", speed=" + speed + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + number;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pokemon other = (Pokemon) obj;
        if (number != other.number)
            return false;
        return true;
    }

    @Override
    public int compareTo(Pokemon o) {
        return number - o.number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Type getPrimaryType() {
        return primaryType;
    }

    public void setPrimaryType(Type primaryType) {
        this.primaryType = primaryType;
    }

    public Type getSecondaryType() {
        return secondaryType;
    }

    public void setSecondaryType(Type secondaryType) {
        this.secondaryType = secondaryType;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getSpatk() {
        return spatk;
    }

    public void setSpatk(int spatk) {
        this.spatk = spatk;
    }

    public int getSpdef() {
        return spdef;
    }

    public void setSpdef(int spdef) {
        this.spdef = spdef;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getSpecial() {
        return special;
    }

    public void setSpecial(int special) {
        this.special = special;
    }

    public int getAbility1() {
        return ability1;
    }

    public void setAbility1(int ability1) {
        this.ability1 = ability1;
    }

    public int getAbility2() {
        return ability2;
    }

    public void setAbility2(int ability2) {
        this.ability2 = ability2;
    }

    public int getAbility3() {
        return ability3;
    }

    public void setAbility3(int ability3) {
        this.ability3 = ability3;
    }

    public int getCatchRate() {
        return catchRate;
    }

    public void setCatchRate(int catchRate) {
        this.catchRate = catchRate;
    }

    public int getExpYield() {
        return expYield;
    }

    public void setExpYield(int expYield) {
        this.expYield = expYield;
    }

    public int getGuaranteedHeldItem() {
        return guaranteedHeldItem;
    }

    public void setGuaranteedHeldItem(int guaranteedHeldItem) {
        this.guaranteedHeldItem = guaranteedHeldItem;
    }

    public int getCommonHeldItem() {
        return commonHeldItem;
    }

    public void setCommonHeldItem(int commonHeldItem) {
        this.commonHeldItem = commonHeldItem;
    }

    public int getRareHeldItem() {
        return rareHeldItem;
    }

    public void setRareHeldItem(int rareHeldItem) {
        this.rareHeldItem = rareHeldItem;
    }

    public int getDarkGrassHeldItem() {
        return darkGrassHeldItem;
    }

    public void setDarkGrassHeldItem(int darkGrassHeldItem) {
        this.darkGrassHeldItem = darkGrassHeldItem;
    }

    public int getGenderRatio() {
        return genderRatio;
    }

    public void setGenderRatio(int genderRatio) {
        this.genderRatio = genderRatio;
    }

    public List<Evolution> getEvolutionsFrom() {
        return evolutionsFrom;
    }

    public List<Evolution> getFilteredEvolutionsFrom() {
        // Filter out evolutions with a duplicate names
        HashSet<String> nameSet = new HashSet<String>();
        return evolutionsFrom.stream().filter(e -> nameSet.add(e.to.name))
                .collect(Collectors.toList());
    }

    public void setEvolutionsFrom(List<Evolution> evolutionsFrom) {
        this.evolutionsFrom = evolutionsFrom;
    }

    public List<Evolution> getEvolutionsTo() {
        return evolutionsTo;
    }

    public void setEvolutionsTo(List<Evolution> evolutionsTo) {
        this.evolutionsTo = evolutionsTo;
    }
}
