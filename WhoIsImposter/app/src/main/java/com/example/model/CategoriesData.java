package com.example.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CategoriesData {

    public static class WordPair {
        public final String innocentWord;
        public final String imposterWord;
        public final boolean drawable;

        public WordPair(String innocentWord, String imposterWord, boolean drawable) {
            this.innocentWord = innocentWord;
            this.imposterWord = imposterWord;
            this.drawable = drawable;
        }
    }

    private static final HashMap<String, List<WordPair>> categoryWords = new HashMap<>();

    static {
        // Places
        List<WordPair> places = new ArrayList<>();
        places.add(new WordPair("Hospital", "Clinic", true));
        places.add(new WordPair("Zoo", "National Park", true));
        places.add(new WordPair("Airport", "Railway Station", true));
        places.add(new WordPair("School", "College", true));
        places.add(new WordPair("Restaurant", "Kitchen", true));
        places.add(new WordPair("Gym", "Playground", true));
        places.add(new WordPair("Library", "Bookstore", true));
        places.add(new WordPair("Museum", "Art Gallery", true));
        categoryWords.put("Places", places);

        // Professions
        List<WordPair> professions = new ArrayList<>();
        professions.add(new WordPair("Doctor", "Nurse", true));
        professions.add(new WordPair("Firefighter", "Police Officer", true));
        professions.add(new WordPair("Chef", "Baker", true));
        professions.add(new WordPair("Pilot", "Astronaut", true));
        professions.add(new WordPair("Farmer", "Gardener", true));
        professions.add(new WordPair("Painter", "Sculptor", true));
        categoryWords.put("Professions", professions);

        // Animals
        List<WordPair> animals = new ArrayList<>();
        animals.add(new WordPair("Bengal Tiger", "Asiatic Lion", true));
        animals.add(new WordPair("Dog", "Wolf", true));
        animals.add(new WordPair("Cat", "Leopard", true));
        animals.add(new WordPair("Cow", "Goat", true));
        animals.add(new WordPair("Horse", "Donkey", true));
        animals.add(new WordPair("Eagle", "Hawk", true));
        categoryWords.put("Animals", animals);

        // Household Items
        List<WordPair> household = new ArrayList<>();
        household.add(new WordPair("Sofa", "Armchair", true));
        household.add(new WordPair("Mug", "Teacup", true));
        household.add(new WordPair("Stove", "Microwave", true));
        household.add(new WordPair("Clock", "Wristwatch", true));
        household.add(new WordPair("Spoon", "Fork", true));
        categoryWords.put("Household Items", household);

        // Foods & Drinks
        List<WordPair> foods = new ArrayList<>();
        foods.add(new WordPair("Tea", "Coffee", true));
        foods.add(new WordPair("Pizza", "Burger", true));
        foods.add(new WordPair("Biryani", "Pulao", true));
        foods.add(new WordPair("Ice Cream", "Milkshake", true));
        categoryWords.put("Foods & Drinks", foods);

        // Vehicles
        List<WordPair> vehicles = new ArrayList<>();
        vehicles.add(new WordPair("Bicycle", "Scooter", true));
        vehicles.add(new WordPair("Car", "Taxi", true));
        vehicles.add(new WordPair("Bus", "Train", true));
        vehicles.add(new WordPair("Helicopter", "Airplane", true));
        categoryWords.put("Vehicles", vehicles);
    }

    public static List<String> getCategoriesList() {
        return new ArrayList<>(categoryWords.keySet());
    }

    public static WordPair getRandomWordPair(String category, String difficulty, boolean forceDrawing) {
        List<WordPair> words = categoryWords.get(category);
        if (words == null || words.isEmpty()) {
            return new WordPair("Biryani", "Pulao", true); // Default fallback
        }

        List<WordPair> candidates = new ArrayList<>();
        if (forceDrawing) {
            for (WordPair wp : words) {
                if (wp.drawable) {
                    candidates.add(wp);
                }
            }
        }

        if (candidates.isEmpty()) {
            candidates.addAll(words); // fallback if none are designated or found
        }

        Random rand = new Random();
        WordPair pair = candidates.get(rand.nextInt(candidates.size()));

        if ("Easy".equals(difficulty)) {
            return pair;
        } else if ("Medium".equals(difficulty)) {
            return pair;
        } else {
            // Hard difficulty: Imposter gets a blank clue
            return new WordPair(pair.innocentWord, "??? (Imposter Mode)", pair.drawable);
        }
    }

    public static WordPair getRandomWordPair(String category, String difficulty) {
        return getRandomWordPair(category, difficulty, false);
    }
}
