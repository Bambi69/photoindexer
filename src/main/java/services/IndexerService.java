package services;

import beans.LocationBean;
import beans.PhotoBean;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.Rational;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.IndexNotFoundException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import utils.DateUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * this service can be used to:
 *    - index photos
 */
public class IndexerService {

    private Logger logger = LogManager.getRootLogger();
    private PropertyService propertyService;

    private List<PhotoBean> photos = new ArrayList<PhotoBean>();

    /**
     * constructor
     * @param propertyService
     */
    public IndexerService(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    /**
     * index photos in elasticsearch
     * @param client
     */
    public void indexPhotos(TransportClient client) {
        try {
            // create index
            createPhotoIndex(client);

            // initialize list of jpg photos from imagePath
            setPhotosFromRepository();

            // browse photos to index
            Iterator<PhotoBean> itPhotoBean = photos.iterator();
            while (itPhotoBean.hasNext()) {

                // retrieve current
                PhotoBean photoToBeIndexed = itPhotoBean.next();

                // instance a json mapper
                ObjectMapper mapper = new ObjectMapper(); // create once, reuse

                // generate json
                byte[] json = mapper.writeValueAsBytes(photoToBeIndexed);

                // index your document
                client.prepareIndex(
                        propertyService.getEsPhotoIndex(),
                        propertyService.getEsPhotoIndexType(),
                        photoToBeIndexed.getDirectory() + photoToBeIndexed.getName())
                            .setSource(json, XContentType.JSON)
                            .get();
            }

        } catch (JsonProcessingException jpe) {
            jpe.printStackTrace();
        }
    }

    /**
     * create ES index to store photos
     * @param client
     */
    private void createPhotoIndex(TransportClient client) {

        // delete index before starting
        try {
            DeleteIndexResponse deleteResponse = client.admin().indices()
                    .delete(new DeleteIndexRequest(propertyService.getEsPhotoIndex())).actionGet();
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

        String locationType =
                "{\n" +
                "    \""+propertyService.getEsPhotoIndexType()+"\": {\n" +
                "      \"properties\": {\n" +
                "        \"location\": {\n" +
                "          \"type\": \"geo_point\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }";

        logger.info(locationType);

        client.admin().indices().prepareCreate(propertyService.getEsPhotoIndex())
                .addMapping(propertyService.getEsPhotoIndexType(),locationType,XContentType.JSON)
                .get();
    }

    /**
     * retrieve photo from imagePath
     */
    private void setPhotosFromRepository() {
        try {
            // initialize folder and list of files
            File folder = new File(propertyService.getImagePath());
            File[] listOfFiles = folder.listFiles();

            // read list of files
            for (int i = 0; i < listOfFiles.length; i++) {

                // analyzing only jpg files
                if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".jpg")) {
                    logger.info("File " + listOfFiles[i].getName());

                    // initialize photoBean
                    PhotoBean p = new PhotoBean();
                    p.setName(listOfFiles[i].getName());
                    p.setDirectory(propertyService.getImagePath());
                    p.setDateIndexed(new Date());

                    // read image metadata
                    Metadata metadata = ImageMetadataReader.readMetadata(listOfFiles[i]);

                    // read exif metadata
                    ExifIFD0Directory exifFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                    if (exifFD0Directory != null) {

                        // retrieve original date time
                        Date originalDateTime = exifFD0Directory.getDate(ExifIFD0Directory.TAG_DATETIME);
                        Calendar c = Calendar.getInstance();
                        c.setTime(originalDateTime);
                        p.setDateTimeOriginal(DateUtils.convertDateToEsFormat(originalDateTime));
                        p.setYearTimeOriginal(c.get(Calendar.YEAR));
                        p.setMonthTimeOriginal(c.get(Calendar.MONTH)+1);

                        // retrieve camera model
                        String cameraModel = exifFD0Directory.getString(ExifIFD0Directory.TAG_MAKE);
                        cameraModel += " - " + exifFD0Directory.getString(ExifIFD0Directory.TAG_MODEL);
                        p.setCameraModel(cameraModel);
                    }

                    // retrieve keywords (with identified faces)
                    IptcDirectory iptcDirectory = metadata.getFirstDirectoryOfType(IptcDirectory.class);
                    if (iptcDirectory != null) {
                        String[] keywords = iptcDirectory.getStringArray(IptcDirectory.TAG_KEYWORDS);

                        if (keywords != null) {
                            for (int j = 0; j < keywords.length; j++) {
                                logger.info("keywords " + keywords[j]);
                                p.getFaces().add(keywords[j]);
                            }
                        }
                    }

                    // retrieve GPS coordinates
                    GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
                    if (gpsDirectory != null) {

                        // init location
                        p.setLocation(new LocationBean());

                        // calculate longitude
                        Rational[] longRat = gpsDirectory.getRationalArray(GpsDirectory.TAG_LONGITUDE);
                        Double longitude = longRat[0].doubleValue()
                                + longRat[1].doubleValue()/60
                                + longRat[2].doubleValue()/3600;
                        /*
                        if (gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE_REF).equalsIgnoreCase("N")) {
                            longitude = longitude * -1;
                        }
                        */
                        p.getLocation().setLon(longitude);
                        logger.info("longitude: " + longitude);

                        // calculate latitude
                        Rational[] latRat = gpsDirectory.getRationalArray(GpsDirectory.TAG_LATITUDE);
                        Double latitude = latRat[0].doubleValue()
                                + latRat[1].doubleValue()/60
                                + latRat[2].doubleValue()/3600;
                        /*
                        if (gpsDirectory.getString(GpsDirectory.TAG_LATITUDE_REF).equalsIgnoreCase("N")) {
                            latitude = latitude * -1;
                        }
                        */
                        p.getLocation().setLat(latitude);
                        logger.info("latitude: " + latitude);
                    }

                    // add photoBean into the list
                    photos.add(p);
                }
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ImageProcessingException ipe) {
            ipe.printStackTrace();
        }
    }

}
