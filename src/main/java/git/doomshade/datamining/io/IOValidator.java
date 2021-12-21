package git.doomshade.datamining.io;

final class IOValidator {

    /**
     * Validates the file name
     *
     * @param fileName the file name
     * @throws IllegalArgumentException if the file name is null or empty
     */
    public static void validateFileName(String fileName) throws IllegalArgumentException {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("The file name must not be empty!");
        }
    }


}
