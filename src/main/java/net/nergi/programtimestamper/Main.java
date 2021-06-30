package net.nergi.programtimestamper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // First, we hand off the args array to the process builder creator.
        final ProcessBuilder builder = generateProcess(args);

        // Then, we run the process.
        try {
            final var process = builder.start();
            final var outThread = new Thread(
                () -> {
                    try (var reader = new Scanner(process.getInputStream())) {
                        while (reader.hasNextLine()) {
                            System.out.printf("[%s %s] %s%n", LocalDate.now(), LocalTime.now(), reader.nextLine());
                        }
                    } catch (Exception e) {
                        System.err.println("Error when reading from program output:\n" + e);
                    }
                }
            );

            outThread.start();

            int exitCode = -1;
            try {
                exitCode = process.waitFor();
                outThread.join();
            } catch (Exception e) {
                System.err.println("Error when program is terminating:\n" + e);
            }

            if (exitCode != 0) {
                throw new IllegalThreadStateException("Program did not exit correctly! Exit code: " + exitCode);
            }
        } catch (IOException e) {
            System.err.println("Error:\n" + e);
        }
    }

    private static ProcessBuilder generateProcess(String[] args) {
        final var builder = new ProcessBuilder(args);
        builder.redirectErrorStream(true);

        return builder;
    }
}
