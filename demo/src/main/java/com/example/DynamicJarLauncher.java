package com.example;

import java.lang.reflect.Method;

public class DynamicJarLauncher {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java DynamicJarLauncher <ClassName>");
            return;
        }

        String className = args[0];
        try {
            // Load the specified class dynamically
            Class<?> clazz = Class.forName(className);
            // Find the main method in the specified class
            Method mainMethod = clazz.getMethod("main", String[].class);
            // Invoke the main method with an empty String array as arguments
            mainMethod.invoke(null, (Object) new String[]{});
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            System.out.println("Class not found or does not contain a main method.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}