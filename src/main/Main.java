


package main;

import drones.StandardDrone;
import drones.ExpressDrone;
import drones.HeavyLiftDrone;
import drones.Drone;
import manager.DeliveryPackage;
import manager.DeliveryManager;
import components.Location;
import exceptions.DroneDeliveryException;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // ==============================
        // MEMBER 1: Create Drones
        // All drones start at WAREHOUSE (0,0) by default
        // ==============================
        int id1 = readPositiveInt(sc, "Enter ID for StandardDrone:  ");
        int id2 = readPositiveInt(sc, "Enter ID for ExpressDrone:   ");
        while (id2 == id1) {
            System.out.println("Duplicate drone ID detected. Please enter a unique ID.");
            id2 = readPositiveInt(sc, "Enter ID for ExpressDrone:   ");
        }
        int id3 = readPositiveInt(sc, "Enter ID for HeavyLiftDrone: ");
        while (id3 == id1 || id3 == id2) {
            System.out.println("Duplicate drone ID detected. Please enter a unique ID.");
            id3 = readPositiveInt(sc, "Enter ID for HeavyLiftDrone: ");
        }

        StandardDrone  d1 = new StandardDrone(id1);
        ExpressDrone   d2 = new ExpressDrone(id2);
        HeavyLiftDrone d3 = new HeavyLiftDrone(id3);

        // ==============================
        // MEMBER 2: Setup Fleet
        // ==============================
        DeliveryManager manager = new DeliveryManager();
        manager.addDrone(d1);
        manager.addDrone(d2);
        manager.addDrone(d3);

        manager.showAllDrones();

        // ==============================
        // TEST 1: URGENT small package
        // ==============================
        System.out.println("\n========== TEST 1: ==========");
        try {
            int pid1 = readPositiveInt(sc, "Enter package ID:       ");
            double w1 = readPositiveDouble(sc, "Enter weight (kg):      ");
            int x1 = readBoundedInt(sc, "Enter dest X:           ");
            int y1 = readBoundedInt(sc, "Enter dest Y:           ");
            String pref1 = readPreferredType(sc);

            DeliveryPackage pkg1 = new DeliveryPackage(pid1, w1, "URGENT", new Location(x1, y1));
            System.out.println(pkg1);

            boolean delivered = manager.processDelivery(pkg1, pref1);
            System.out.println("Package 1 Delivered: " + delivered);
        } catch (DroneDeliveryException ex) {
            System.out.println(ex.getMessage());
        } catch (RuntimeException ex) {
            System.out.println("Unexpected error: " + ex.getMessage());
        }

        // ==============================
        // TEST 2: NORMAL heavy package
        // ==============================
        System.out.println("\n========== TEST 2: ==========");
        try {
            int pid2 = readPositiveInt(sc, "Enter package ID:       ");
            double w2 = readPositiveDouble(sc, "Enter weight (kg):      ");
            int x2 = readBoundedInt(sc, "Enter dest X:           ");
            int y2 = readBoundedInt(sc, "Enter dest Y:           ");
            String pref2 = readPreferredType(sc);
            DeliveryPackage pkg2 = new DeliveryPackage(pid2, w2, "NORMAL", new Location(x2, y2));
            System.out.println(pkg2);

            boolean delivered = manager.processDelivery(pkg2, pref2);
            System.out.println("Package 2 Delivered: " + delivered);
        } catch (DroneDeliveryException ex) {
            System.out.println(ex.getMessage());
        } catch (RuntimeException ex) {
            System.out.println("Unexpected error: " + ex.getMessage());
        }

        // ==============================
        // TEST 3: URGENT — simulate busy ExpressDrone
        // ==============================
        System.out.println("\n========== TEST 3: ==========");
        try {
            int pid3 = readPositiveInt(sc, "Enter package ID:       ");
            double w3 = readPositiveDouble(sc, "Enter weight (kg):      ");
            int x3 = readBoundedInt(sc, "Enter dest X:           ");
            int y3 = readBoundedInt(sc, "Enter dest Y:           ");

            d2.setStatus("DELIVERING");   // mark express as busy
            String pref3 = readPreferredType(sc);

            DeliveryPackage pkg3 = new DeliveryPackage(pid3, w3, "URGENT", new Location(x3, y3));
            System.out.println(pkg3);

            boolean delivered = manager.processDelivery(pkg3, pref3);
            System.out.println("Package 3 Delivered: " + delivered);
        } catch (DroneDeliveryException ex) {
            System.out.println(ex.getMessage());
        } catch (RuntimeException ex) {
            System.out.println("Unexpected error: " + ex.getMessage());
        }

        // ==============================
        // TEST 4: No suitable drone (low battery)
        // ==============================
        System.out.println("\n========== TEST 4:  ==========");

        try {
            int pid4 = readPositiveInt(sc, "Enter package ID:       ");
            double w4 = readPositiveDouble(sc, "Enter weight (kg):      ");
            int x4 = readBoundedInt(sc, "Enter dest X:           ");
            int y4 = readBoundedInt(sc, "Enter dest Y:           ");
            String pref4 = readPreferredType(sc);

            DeliveryPackage pkg4 = new DeliveryPackage(pid4, w4, "NORMAL", new Location(x4, y4));
            System.out.println(pkg4);

            Drone assigned4 = manager.assignDrone(pkg4, pref4);
            if (assigned4 == null) {
                System.out.println("No drone assigned — battery is low, they are charging. Please wait.");
            }
        } catch (DroneDeliveryException ex) {
            System.out.println(ex.getMessage());
        } catch (RuntimeException ex) {
            System.out.println("Unexpected error: " + ex.getMessage());
        }

        // ==============================
        // FINAL FLEET STATUS
        // ==============================
        System.out.println("\n========== FINAL FLEET STATUS ==========");
        manager.showAllDrones();
    }

    private static int readPositiveInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = sc.nextLine().trim();
            try {
                int parsed = Integer.parseInt(value);
                if (parsed <= 0) {
                    System.out.println("Please enter a positive whole number.");
                    continue;
                }
                return parsed;
            } catch (NumberFormatException ex) {
                System.out.println("Invalid numeric input. Please enter a whole number.");
            }
        }
    }

    private static int readBoundedInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = sc.nextLine().trim();
            try {
                int parsed = Integer.parseInt(value);
                if (parsed < components.Location.MIN_COORDINATE || parsed > components.Location.MAX_COORDINATE) {
                    System.out.println("Coordinate out of bounds. Allowed range: "
                            + components.Location.MIN_COORDINATE + " to " + components.Location.MAX_COORDINATE + ".");
                    continue;
                }
                return parsed;
            } catch (NumberFormatException ex) {
                System.out.println("Invalid numeric input. Please enter a whole number.");
            }
        }
    }

    private static double readPositiveDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = sc.nextLine().trim();
            try {
                double parsed = Double.parseDouble(value);
                if (!Double.isFinite(parsed) || parsed <= 0) {
                    System.out.println("Please enter a positive numeric value.");
                    continue;
                }
                return parsed;
            } catch (NumberFormatException ex) {
                System.out.println("Invalid numeric input. Please enter a decimal value.");
            }
        }
    }

    private static String readPreferredType(Scanner sc) {
        while (true) {
            System.out.print("Preferred drone type (STANDARD/EXPRESS/HEAVY/ANY): ");
            String value = sc.nextLine().trim().toUpperCase(Locale.ROOT);
            if (value.isEmpty()) {
                System.out.println("Please enter a valid drone type.");
                continue;
            }

            switch (value) {
                case "STANDARD":
                case "EXPRESS":
                case "HEAVY":
                case "ANY":
                    return value;
                default:
                    System.out.println("Invalid choice. Please enter STANDARD, EXPRESS, HEAVY, or ANY.");
            }
        }
    }
}