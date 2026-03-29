package org.sk.race.reader;

import org.sk.race.entities.Race;
import org.sk.race.entities.RaceItem;
import org.sk.race.entities.Runner;

import java.io.InputStream;
import java.util.Scanner;

public class TxtReader {
    public static Race readRace() {
        Race race = null;

        try {
            race = new Race("Minsk Marathon");
            InputStream inputStream = TxtReader.class.getClassLoader().getResourceAsStream("MinskMaraphon.txt");
            if (inputStream == null) {
                return race;
            }
            Scanner sc = new Scanner(inputStream);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 8) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        String name = parts[1];
                        String LastName = parts[2];
                        int totalSeconds = RaceItem.parseTimeToSeconds(parts[7].trim());
                        int age = Integer.parseInt(parts[4].trim());
                        String fullName = name + " " + LastName;
                        Runner runner = new Runner(fullName, age, "", null);
                        RaceItem raceItem = new RaceItem(id, runner, totalSeconds);
                        race.addResult(raceItem);

                    } catch (Exception e) {
                        System.err.println(line);
                        e.printStackTrace();
                    }
                }
            }
            sc.close();
        } catch (Exception e) {
            }
        return race;
        }
    }