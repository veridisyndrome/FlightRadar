package ch.epfl.javions.gui;

import javafx.scene.image.Image;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Public: Represents a manager of the OSM tiles
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class TileManager {
    private static final int MAX_TILE = 100;
    private static final float LOAD_FACTOR= .75f;
    private final LinkedHashMap<TileId, Image> cacheMemory =
            new LinkedHashMap<>(MAX_TILE, LOAD_FACTOR, true);
    private final Path pathToFile;
    private final String serverName;

    /**
     * Default TileManager constructor.
     * Defines the tile manager
     *
     * @param pathToFile (Path): Path to the folder containing the disk cache
     * @param serverName (String): Name of the tiles server
     * @throws IOException If there is an Input/Output error
     */
    public TileManager(Path pathToFile, String serverName) throws IOException {
        this.pathToFile = Files.createDirectories(pathToFile);
        this.serverName = serverName;
    }

    /**
     * Represents the identity of an OSM tile
     *
     * @param zoomLevel (int): Zoom level of the tile
     * @param xIndex    (int): X index of the tile
     * @param yIndex    (int): Y index of the tile
     */
    public record TileId(int zoomLevel, int xIndex, int yIndex) {
        public static boolean isValid(int zoomLevel, int xIndex, int yIndex) {
            int maxIndex = 1 << zoomLevel;
            return (0 <= xIndex && xIndex < maxIndex) && (0 <= yIndex && yIndex < maxIndex);
        }
    }

    /**
     * Defines the correct image basing on the given identity tile
     *
     * @param id (TileId): Given tile identity
     * @return (Image): Image of the tile
     * @throws IOException If there is an Input/Output error
     */
    public Image imageForTileAt(TileId id) throws IOException {
        //Verifying if the tile is in the cache memory
        if (cacheMemory.containsKey(id)) return cacheMemory.get(id);

        //Verifying if the cache memory is not full
        if (MAX_TILE <= cacheMemory.size()) {
            Iterator<TileId> it = cacheMemory.keySet().iterator();
            cacheMemory.remove(it.next());
        }

        int zoomLevel = id.zoomLevel;
        int xIndex = id.xIndex;
        int yIndex = id.yIndex;

        Path zoomLevelPath = Path.of(String.valueOf(zoomLevel));
        Path xIndexPath = Path.of(String.valueOf(xIndex));


        //Creating the disk path for the tile
        Path diskPathImage = pathToFile
                .resolve(zoomLevelPath)
                .resolve(xIndexPath)
                .resolve(Path.of(yIndex + ".png"));

        //Verifying if the tile is in the disk memory
        if (Files.exists(diskPathImage)) {
            try (FileInputStream f = new FileInputStream(diskPathImage.toString())) {
                Image image = new Image(f);
                cacheMemory.put(id, image);

                return image;
            }
        }

        //Creating the directory for the tile
        Files.createDirectories(pathToFile
                .resolve(zoomLevelPath)
                .resolve(xIndexPath));

        //Formatting the url in order to download the tile
        String url = String.format("https://%s/%d/%d/%d.png", serverName, zoomLevel, xIndex, yIndex);

        //Creating the URL in order to download the tile
        URL u = new URL(url);

        //Connecting the URL to the tile server
        URLConnection c = u.openConnection();
        c.setRequestProperty("User-Agent", "Javions");

        //Downloading the tile in the server on the directory
        try (InputStream i = c.getInputStream();
             OutputStream o = new FileOutputStream(diskPathImage.toString());
             FileInputStream f = new FileInputStream(diskPathImage.toString())) {
            o.write(i.readAllBytes());

            Image image = new Image(new ByteArrayInputStream(f.readAllBytes()));
            cacheMemory.put(id, image);

            return image;
        }
    }
}
