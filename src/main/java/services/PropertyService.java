package services;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyService {

    /**
     * private variableles
     */
    private String rootPath;

    /**
     * private static variables
     * to be used to refer to key in conf files
     */
    private static String CONF_FILE_NAME = "conf.properties";
    private static String FACES_FILE_NAME = "knownFaces.properties";

    private static String ES_HOSTNAME = "eshostname";
    private static String ES_PORT = "esport";
    private static String ES_PHOTO_INDEX = "esphotoindex";
    private static String ES_PHOTO_INDEX_TYPE = "esphotoindextype";
    private static String IMAGE_PATH = "imagepath";

    /**
     * variables to be used in other class which must refer to configuration
     */
    private String esHostname;
    private Integer esPort;
    private String esPhotoIndex;
    private String esPhotoIndexType;
    private String imagePath;

    public PropertyService(String confPath) {
        // set rootPath for config files from program arguments
        rootPath = confPath;
        loadConf();
    }

    /**
     * load configuration file
     */
    private void loadConf() {
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(rootPath + CONF_FILE_NAME);

            // load a properties file
            prop.load(input);

            // get the property values
            esHostname = prop.getProperty(ES_HOSTNAME);
            esPort = Integer.valueOf(prop.getProperty(ES_PORT));
            esPhotoIndex = prop.getProperty(ES_PHOTO_INDEX);
            esPhotoIndexType = prop.getProperty(ES_PHOTO_INDEX_TYPE);
            imagePath = prop.getProperty(IMAGE_PATH);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getEsHostname() {
        return esHostname;
    }

    public Integer getEsPort() {
        return esPort;
    }

    public String getEsPhotoIndex() {
        return esPhotoIndex;
    }

    public String getEsPhotoIndexType() {
        return esPhotoIndexType;
    }

    public String getImagePath() {
        return imagePath;
    }
}
