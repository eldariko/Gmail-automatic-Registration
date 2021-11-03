import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class GmailServiceTest {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String propFileName = "config.properties";
    private static final String propApplicationName = "APPLICATION_NAME";

    private static Properties prop;
    private static InputStream inputStream;
    private static Gmail service;

    private static GmailService gmailService;

    @BeforeAll
    static void setUp() throws GeneralSecurityException, IOException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        try {
            prop = new Properties();

            inputStream = GmailServiceTest.class.getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
                gmailService = new GmailService(prop);
                service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, gmailService.getCredentials((HTTP_TRANSPORT)))
                        .setApplicationName(prop.getProperty(propApplicationName))
                        .build();
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

    }

    @AfterEach
    void tearDown() {
    }

    @ParameterizedTest
    @CsvFileSource(resources = "validEmails.csv", numLinesToSkip = 1)
    void validDestinationTest(String emailTo,String emailFrom,String emailSubject,String emailBodyText) throws Exception {
        Message message = gmailService.
        sendMessage
        (service, prop.getProperty("userId"), gmailService.createEmail(emailTo, emailFrom, emailSubject, emailBodyText));
        System.out.println("Waiting for email validation...");
        TimeUnit.SECONDS.sleep(5);
        assertFalse(GmailService.isBounced(service, message.getThreadId()),"Message for"+emailTo+" has received");
        System.out.println("Message for"+emailTo+" has received");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "invalidEmails.csv", numLinesToSkip = 1)
    void inValidDestinationTest(String emailTo,String emailFrom,String emailSubject,String emailBodyText) throws Exception {
        Message message = gmailService.
                sendMessage
                        (service, prop.getProperty("userId"), gmailService.createEmail(emailTo, emailFrom, emailSubject, emailBodyText));
        System.out.println("Waiting for email validation...");
        TimeUnit.SECONDS.sleep(5);
        assertTrue(GmailService.isBounced(service, message.getThreadId()),"The email account that you tried to reach does not exist: "+emailTo);
        System.out.println("The email account that you tried to reach does not exist: "+emailTo);
    }
}