package com.dabomstew.pkrandom.settings;

/*----------------------------------------------------------------------------*/
/*--  SettingsOption.java - Provides an interface to use with any Settings  --*/
/*--                        object while enabling each object to be defined --*/
/*--                        with functions that work with its value class   --*/
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
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import com.dabomstew.pkrandom.constants.GlobalConstants;
import com.dabomstew.pkrandom.pokemon.GenRestrictions;

public interface SettingsOption<T> {
    public String getName();

    public T getItem();

    public void setItem(T item);

    public Boolean isChild();

    public void setIsChild(Boolean bool);

    public ArrayList<PredicatePair> getMatches();

    public ArrayList<PredicateArray> getMultiMatches();

    public void randomValue(Random random, Integer generationOfRom);

    public void attemptRandomValue(Random random, SettingsOption item, Integer generationOfRom);

    public static class Builder {
        // Required parameters
        private String name;
        private Object value;

        // Optional parameters
        private PredicatePair[] matches;
        private List<PredicatePair[]> multiMatches;
        private IntStream validInts;
        private Integer[] validGenerations;
        private List validItems;

        public Builder(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        /**
         * Any PredicatePair which can be true independent of other conditions to pass
         * 
         * @param matches Any sequence of PredicatePairs where at least one must be true to be valid
         * @return Builder
         */
        public Builder addMatches(PredicatePair... matches) {
            this.matches = matches;
            return this;
        }

        /**
         * Requires all PredicatePairs provided to be true for this condition to pass.
         * 
         * Each usage of "addMultiMatches" is considered independent of other conditions. The group
         * is treated as a single unit of truth alongside anything in "addMatches" or other
         * "addMultiMatches"
         * 
         * @param matches Any sequence of PredicatePairs which must all be simultaneously true to be
         *            valid
         * @return Builder
         */
        public Builder addMultiMatch(PredicatePair... matches) {
            if (multiMatches == null) {
                multiMatches = new ArrayList<PredicatePair[]>();
            }
            multiMatches.add(matches);
            return this;
        }

        public Builder addValidInts(int min, int max) {
            this.validInts = IntStream.rangeClosed(min, max);
            return this;
        }

        public Builder addGenRestriction(Integer... validGenerations) {
            this.validGenerations = validGenerations;
            return this;
        }

        public Builder addValidItems(Object... validItems) {
            this.validItems = Arrays.asList(validItems);
            return this;
        }

        public SettingsOptionComposite build() {
            if (value instanceof Boolean) {
                return new SettingsOptionComposite<Boolean>(new BooleanSettingsOption(name,
                        (Boolean) value, matches, multiMatches, validGenerations));
            } else if (value instanceof GenRestrictions) {
                return new SettingsOptionComposite<GenRestrictions>(
                        new GenRestrictionsSettingsOption(name, (GenRestrictions) value, matches,
                                multiMatches, validGenerations));
            } else if (value instanceof Enum) {
                return new SettingsOptionComposite<Enum>(new EnumSettingsOption(name, (Enum) value,
                        matches, multiMatches, validGenerations));
            } else if (value instanceof int[]) {
                return new SettingsOptionComposite<int[]>(new IntArraySettingsOption(name,
                        (int[]) value, validInts, matches, multiMatches, validGenerations));
            } else if (value instanceof Integer) {
                return new SettingsOptionComposite<Integer>(new IntSettingsOption(name,
                        (Integer) value, validInts, matches, multiMatches, validGenerations));
            } else if (value instanceof List) {
                return new SettingsOptionComposite<List>(new ListSettingsOption(name, (List) value,
                        matches, multiMatches, validItems, validGenerations));
            } else {
                String className = value == null ? "Null" : value.getClass().toString();
                throw new RuntimeException(className + " has no supported factory.");
            }
        }
    }
}


abstract class AbstractSettingsOption<T> implements SettingsOption<T> {

    protected String name;
    protected T defaultValue;
    protected T value;
    protected Boolean isChild;
    protected ArrayList<PredicatePair> matches;
    protected ArrayList<PredicateArray> multiMatches;
    protected ArrayList<Integer> validGenerations;

    protected AbstractSettingsOption(String name, T value, PredicatePair[] matches,
            List<PredicatePair[]> multiMatches, Integer[] validGenerations) {
        this.name = name;
        this.defaultValue = value;
        this.value = value;
        this.isChild = true;
        if (matches != null) {
            this.matches = new ArrayList<PredicatePair>(Arrays.asList(matches));
        } else {
            this.matches = new ArrayList<PredicatePair>();
        }
        this.multiMatches = new ArrayList<PredicateArray>();
        if (multiMatches != null) {
            multiMatches.forEach(p -> this.multiMatches.add(new PredicateArray(p)));
        }
        if (validGenerations != null) {
            this.validGenerations = new ArrayList<Integer>(Arrays.asList(validGenerations));
        } else {
            this.validGenerations = new ArrayList<Integer>(GlobalConstants.SUPPORTED_GENERATIONS);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public T getItem() {
        return value;
    }

    @Override
    public void setItem(T item) {
        this.value = item;
    }

    @Override
    public Boolean isChild() {
        return isChild;
    }

    @Override
    public void setIsChild(Boolean value) {
        this.isChild = value;
    }

    @Override
    public ArrayList<PredicatePair> getMatches() {
        return matches;
    }

    @Override
    public ArrayList<PredicateArray> getMultiMatches() {
        return multiMatches;
    }

    @Override
    public void attemptRandomValue(Random random, SettingsOption item, Integer generationOfRom) {
        if (matches.stream().anyMatch((match) -> match.test(item))
                || multiMatches.stream().anyMatch(arr -> arr.isValueRandomizable(item))) {
            randomValue(random, generationOfRom);
        } else {
            setItem(defaultValue);
        }
    }
}


class BooleanSettingsOption extends AbstractSettingsOption<Boolean> {

    public BooleanSettingsOption(String name, Boolean value, PredicatePair[] matches,
            List<PredicatePair[]> multiMatches, Integer[] validGenerations) {
        super(name, value, matches, multiMatches, validGenerations);
    }

    @Override
    public void randomValue(Random random, Integer generationOfRom) {
        if (validGenerations.contains(generationOfRom)) {
            setItem(random.nextBoolean());
        }
    }
}


class EnumSettingsOption extends AbstractSettingsOption<Enum> {

    public EnumSettingsOption(String name, Enum value, PredicatePair[] matches,
            List<PredicatePair[]> multiMatches, Integer[] validGenerations) {
        super(name, value, matches, multiMatches, validGenerations);
    }

    /**
     * Uses Java reflection to get the values of the actual Enum
     */
    @Override
    public void randomValue(Random random, Integer generationOfRom) {
        if (validGenerations.contains(generationOfRom)) {
            Enum[] values = ((Enum) value).getClass().getEnumConstants();
            setItem(values[random.nextInt(values.length)]);
        }
    }
}


class IntArraySettingsOption extends AbstractSettingsOption<int[]> {

    private IntStream allowedValues;

    public IntArraySettingsOption(String name, int[] value, IntStream allowedValues,
            PredicatePair[] matches, List<PredicatePair[]> multiMatches,
            Integer[] validGenerations) {
        super(name, value, matches, multiMatches, validGenerations);
        if (allowedValues == null) {
            throw new IllegalArgumentException(
                    "IntArraySettingsOption must contain a non-null allowedValues");
        }
        this.allowedValues = allowedValues;
    }

    @Override
    public void randomValue(Random random, Integer generationOfRom) {
        if (validGenerations.contains(generationOfRom)) {
            int[] allowedInts = allowedValues.toArray();
            int[] newVal = new int[value.length];
            for (int i = 0; i < value.length; i++) {
                newVal[i] = allowedInts[random.nextInt(allowedInts.length)];
            }
            setItem(newVal);
        }
    }

    public void setAllowedValues(IntStream validInts) {
        this.allowedValues = validInts;
    }
}


class IntSettingsOption extends AbstractSettingsOption<Integer> {

    private IntStream allowedValues;

    public IntSettingsOption(String name, int value, IntStream allowedValues,
            PredicatePair[] matches, List<PredicatePair[]> multiMatches,
            Integer[] validGenerations) {
        super(name, value, matches, multiMatches, validGenerations);
        if (allowedValues == null) {
            throw new IllegalArgumentException(
                    "IntSettingsOption must contain a non-null allowedValues");
        }
        this.allowedValues = allowedValues;
    }

    @Override
    public void randomValue(Random random, Integer generationOfRom) {
        if (validGenerations.contains(generationOfRom)) {
            int[] allowedInts = allowedValues.toArray();
            setItem(allowedInts[random.nextInt(allowedInts.length)]);
        }
    }
}


/**
 * WARNING Due to the untyped nature of this class, there is no type safety available. If you have
 * an illegal element in your allowedItems parameter, it will be allowed regardless of what it is.
 * This means if you are expecting a list of Pokemon but provide TrainerPokemon, these will be
 * allowed even though they are not compatible, thus pushing the error further along in the code.
 * Best case scenario is a NoOp, worst case is an application crash with a gibberish error.
 */
class ListSettingsOption<T> extends AbstractSettingsOption<List<T>> {

    private List<T> allowedItems;

    public ListSettingsOption(String name, List<T> value, PredicatePair[] matches,
            List<PredicatePair[]> multiMatches, List<T> allowedItems, Integer[] validGenerations) {
        super(name, value, matches, multiMatches, validGenerations);
        if (allowedItems == null) {
            throw new IllegalArgumentException(
                    "ListSettingsOption must contain a non-null allowedItems");
        }
        this.allowedItems = allowedItems;
    }

    @Override
    public void randomValue(Random random, Integer generationOfRom) {
        if (validGenerations.contains(generationOfRom)) {
            try {
                List<T> randomValues = defaultValue.getClass().newInstance();
                for (T obj : allowedItems) {
                    if (random.nextBoolean()) {
                        randomValues.add(obj);
                    }
                }
                setItem(randomValues);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}


class GenRestrictionsSettingsOption extends AbstractSettingsOption<GenRestrictions> {

    public GenRestrictionsSettingsOption(String name, GenRestrictions value,
            PredicatePair[] matches, List<PredicatePair[]> multiMatches,
            Integer[] validGenerations) {
        super(name, value, matches, multiMatches, validGenerations);
    }

    @Override
    public void randomValue(Random random, Integer generationOfRom) {
        GenRestrictions newRestrictions = new GenRestrictions();
        // Deliberately fall through to capture all valid selections
        // E.g. Gen 3 will capture gen 3, gen 2, and gen 1 leaving 4 and 5 false by default
        switch (generationOfRom) {
            case 5:
                newRestrictions.allow_gen5 = random.nextBoolean();
            case 4:
                newRestrictions.allow_gen4 = random.nextBoolean();
            case 3:
                newRestrictions.allow_gen3 = random.nextBoolean();
            case 2:
                newRestrictions.allow_gen2 = random.nextBoolean();
            case 1:
                newRestrictions.allow_gen1 = random.nextBoolean();
                break;
        }

        // Automatically accept all related Gen 1 options if this is true
        // Any optional associations are random
        if (newRestrictions.allow_gen1) {
            newRestrictions.assoc_g2_g1 = true;
            newRestrictions.assoc_g4_g1 = true;
            newRestrictions.assoc_g1_g2 = random.nextBoolean();
            newRestrictions.assoc_g1_g4 = random.nextBoolean();
        }
        // Automatically accept all related Gen 2 options if this is true
        // Any optional associations are random
        if (newRestrictions.allow_gen2) {
            newRestrictions.assoc_g3_g2 = true;
            newRestrictions.assoc_g4_g2 = true;
            newRestrictions.assoc_g2_g3 = random.nextBoolean();
            newRestrictions.assoc_g2_g4 = random.nextBoolean();
            if (!newRestrictions.allow_gen1) {
                newRestrictions.assoc_g2_g1 = random.nextBoolean();
            }
        }
        // Automatically accept all related Gen 3 options if this is true
        // Any optional associations are random
        if (newRestrictions.allow_gen3) {
            newRestrictions.assoc_g4_g3 = true;
            newRestrictions.assoc_g3_g4 = random.nextBoolean();
            if (!newRestrictions.allow_gen2) {
                newRestrictions.assoc_g3_g2 = random.nextBoolean();
            }
        }

        // Gen 4 is not automatically accepted by anything
        if (newRestrictions.allow_gen4) {
            // If Gen1 is false, then we're allowed to try to set a value
            // otherwise we'd be overriding the true from above
            if (!newRestrictions.allow_gen1) {
                newRestrictions.assoc_g4_g1 = random.nextBoolean();
            }
            // Similar for gen 2
            if (!newRestrictions.allow_gen2) {
                newRestrictions.assoc_g4_g2 = random.nextBoolean();
            }
            // Similar for gen 3
            if (!newRestrictions.allow_gen3) {
                newRestrictions.assoc_g4_g3 = random.nextBoolean();
            }
        }

        // Set item at end to enable easier mock for unit testing
        setItem(newRestrictions);
    }
}


class SettingsOptionComposite<T> implements SettingsOption<T> {
    private ArrayList<SettingsOption> childOptions = new ArrayList<SettingsOption>();

    SettingsOption<T> value;

    public SettingsOptionComposite(SettingsOption<T> value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return value.getName();
    }

    @Override
    public T getItem() {
        return value.getItem();
    }

    @Override
    public void setItem(T item) {
        value.setItem(item);
    }

    @Override
    public Boolean isChild() {
        return value.isChild();
    }

    @Override
    public void setIsChild(Boolean isChild) {
        value.setIsChild(isChild);
    }

    @Override
    public ArrayList<PredicatePair> getMatches() {
        return value.getMatches();
    }

    @Override
    public ArrayList<PredicateArray> getMultiMatches() {
        return value.getMultiMatches();
    }

    @Override
    public void randomValue(Random random, Integer generationOfRom) {
        value.randomValue(random, generationOfRom);
        childOptions.forEach((option) -> option.attemptRandomValue(random, this, generationOfRom));
    }

    @Override
    public void attemptRandomValue(Random random, SettingsOption item, Integer generationOfRom) {
        value.attemptRandomValue(random, item, generationOfRom);
        childOptions.forEach((option) -> option.attemptRandomValue(random, this, generationOfRom));
    }

    public void add(SettingsOption childOption) {
        childOptions.add(childOption);
    }

    public SettingsOption<T> getCompositeValue() {
        return value;
    }
}
