import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

import services.IndexerService;
import services.PropertyService;

public class Indexer {

    private static Logger logger = LogManager.getRootLogger();

    public static void main(String[] args) {
        try {

            // check program arguments
            if(!checkProgramArguments(args)) {
                return;
            }

            // load conf properties
            PropertyService propertyService = new PropertyService(args[0]);

            logger.info("initializing es transportclient...");
            logger.info(propertyService.getEsHostname());
            logger.info(propertyService.getEsPort());

            // on startup
            TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName(
                            propertyService.getEsHostname()), propertyService.getEsPort()));

            logger.info("client is initialized");

            // initialize indexerService and index photos
            IndexerService indexerService = new IndexerService(propertyService);
            indexerService.indexPhotos(client);

            // on shutdown
            client.close();

        } catch (UnknownHostException uhe) {
            uhe.printStackTrace();
        }

        return;
    }

    /**
     * to check program arguments
     *  - config path must be defined
     * @param args
     */
    private static boolean checkProgramArguments(String[] args) {

        if ((args.length == 0) || (args[0] == null) || args[0].toString().compareTo("") == 0) {
            logger.error("Program arguments are not valid: config path is not defined");
            return false;
        }
        return true;
    }
}
