package net.craftmaster08.itemsoflegends.reality;

public final class RealityFractureClientState {
    private static boolean configured;
    private static String startDimension = "";
    private static String destinationDimension = "";

    private RealityFractureClientState() {
    }

    public static void update(boolean configured, String startDimension, String destinationDimension) {
        RealityFractureClientState.configured = configured;
        RealityFractureClientState.startDimension = startDimension;
        RealityFractureClientState.destinationDimension = destinationDimension;
    }

    public static boolean isConfigured() {
        return configured;
    }

    public static String getStartDimension() {
        return startDimension;
    }

    public static String getDestinationDimension() {
        return destinationDimension;
    }
}

