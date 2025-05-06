import com.fazecast.jSerialComm.SerialPort;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * SerialReader opens the serial port using jSerialComm, reads data line by line,
 * and parses each line so that the correct sensor value is stored.
 */
public class SerialReader implements Runnable {
    // The serial port to read from.
    private SerialPort serialPort;
    // Reference to our DataProcessor that stores the parsed sensor data.
    private DataProcessor dataProcessor;

    // Temporary variables for GPS data to hold latitude and longitude until both are available.
    private Double tempLat = null;
    private Double tempLng = null;

    /**
     * Constructor for SerialReader.
     * @param portName The name of the serial port (e.g., "/dev/ttyACM0").
     * @param dp Reference to a DataProcessor instance.
     */
    public SerialReader(String portName, DataProcessor dp) {
        this.dataProcessor = dp;
        // Get the serial port object based on the provided port name.
        serialPort = SerialPort.getCommPort("/dev/ttyACM0");
        // Set the baud rate (adjust this as needed to match your transmitter).
        serialPort.setBaudRate(9600);
        // Set the read timeout to 2000 ms (2 seconds) in semi-blocking mode.
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 999999999, 0);
        // Set the port to block indefinitely until data is available (indefinite timeout time)
        //serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
    }

    /**
     * The run method opens the serial port, reads incoming data line by line,
     * and processes each line with the parseLine() method.
     */
    public void run() {
        // Try to open the serial port.
        if (!serialPort.openPort()) {
            System.out.println("Could not open port: " + serialPort.getSystemPortName());
            return;
        }

        // Wrap the InputStream of the serial port with a BufferedReader to read complete lines.
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()))) {
            String line;
            // Read each line until the stream ends.
            while ((line = reader.readLine()) != null) {
                parseLine(line);
            }
        } catch (Exception e) {
            System.out.println("Error reading from serial port: " + e.getMessage());
        } finally {
            // Close the port when done.
            serialPort.closePort();
        }
    }

    /**
     * Parses a single line of data from the serial port.
     * Uses the start of the line to determine the type of sensor data and then stores the value.
     * For GPS, temporary buffers ensure a coordinate is only created when both parts are available.
     *
     * @param line The incoming line of serial data.
     */
    private void parseLine(String line) {
        // Print the received line for debugging.
        System.out.println("Received: " + line);

        // Check if the line is an acceleration reading.
        if (line.startsWith("Acc:")) {
            // Remove "Acc:" and trim any extra spaces.
            String value = line.substring("Acc:".length()).trim();
            // The format is "0.996 m/s^2"; split by space to isolate the numeric value.
            String[] parts = value.split(" ");
            try {
                double acceleration = Double.parseDouble(parts[0]);
                // Store the acceleration value using DataProcessor.
                dataProcessor.addAcceleration(acceleration);
            } catch (Exception e) {
                System.out.println("Error parsing acceleration: " + e.getMessage());
            }
        }
        // Check if the line is an altitude reading.
        else if (line.startsWith("Alt:")) {
            // Remove "Alt:" and trim the value.
            String value = line.substring("Alt:".length()).trim();
            // Format example: "6.515 ft"; split to get the numeric part.
            String[] parts = value.split(" ");
            try {
                double altitude = Double.parseDouble(parts[0]);
                // Store the altitude value.
                dataProcessor.addAltitude(altitude);
            } catch (Exception e) {
                System.out.println("Error parsing altitude: " + e.getMessage());
            }
        }
        // Check if the line is a latitude reading.
        else if (line.startsWith("Lat:")) {
            // Remove "Lat:" and trim the value.
            String value = line.substring("Lat:".length()).trim();
            try {
                // Parse the latitude value.
                tempLat = Double.parseDouble(value);
                // If longitude is already available, create a complete Coordinate.
                if (tempLng != null) {
                    dataProcessor.addCoordinate(new Coordinate(tempLat, tempLng));
                    // Reset temporary variables.
                    tempLat = null;
                    tempLng = null;
                }
            } catch (Exception e) {
                System.out.println("Error parsing latitude: " + e.getMessage());
            }
        }
        // Check if the line is a longitude reading.
        else if (line.startsWith("Lng:")) {
            // Remove "Lng:" and trim the value.
            String value = line.substring("Lng:".length()).trim();
            try {
                // Parse the longitude value.
                tempLng = Double.parseDouble(value);
                // If latitude has already been received, create a Coordinate.
                if (tempLat != null) {
                    dataProcessor.addCoordinate(new Coordinate(tempLat, tempLng));
                    // Reset temporary variables.
                    tempLat = null;
                    tempLng = null;
                }
            } catch (Exception e) {
                System.out.println("Error parsing longitude: " + e.getMessage());
            }
        } else {
            // If the line doesn't match any known format, print it.
            System.out.println("Unknown data: " + line);
        }
    }

    /**
     * Main method for testing the SerialReader.
     * This will create a DataProcessor, then a SerialReader, and start it in a new thread.
     */
    public static void main(String[] args) {
        // Create an instance of DataProcessor (assume this class is implemented).
        DataProcessor dp = new DataProcessor();
        // Create a SerialReader with the serial port name (change to your port as needed).
        SerialReader reader = new SerialReader("/dev/ttyACM0", dp);
        // Start the SerialReader on a new thread.
        Thread thread = new Thread(reader);
        thread.start();
    }
}
