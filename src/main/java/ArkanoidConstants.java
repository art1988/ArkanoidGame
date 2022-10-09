import java.io.File;

public interface ArkanoidConstants {
    int WINDOW_WIDTH = 650;
    int WINDOW_HEIGHT = 565;

    int IMG_WIDTH = 640;
    int IMG_HEIGHT = 502;

    int STICK_STATIC_HEIGHT = 15;

    int BORDER_INDENTATION = 5;

    int CELL_WIDTH = IMG_WIDTH / 15;
    int CELL_HEIGHT = IMG_HEIGHT / 24;

    int MAX_STICK_SIZE = 120;
    int MIN_STICK_SIZE = 40;


    File levelsXMLFile = new File(System.getProperty("user.dir") + "/src/main/resources/levels.xml");
}