import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesHelper {

    private final Properties prop = new Properties();
    PropertiesHelper() {
        try (InputStream input = new FileInputStream("/config.properties")) {

            // load a properties file
            prop.load(input);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Properties getProp() {
        return prop;
    }
}
