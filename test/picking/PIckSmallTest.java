package picking;

import bot.BotStarter;
import bot.BotState;
import junit.framework.TestCase;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by home on 2/1/15.
 */
public class PIckSmallTest extends TestCase {
    public void test() throws Exception {
        String input = readFileAsString("PickSmall.txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        System.setIn(new ByteArrayInputStream(input.getBytes()));
        System.setOut(new PrintStream(out));
        BotStarter.main(new String[]{});
        String output = new String(out.toByteArray());

        String information = "";
        String actual  = output.trim();
        boolean correct = false;
        Matcher matcher = Pattern.compile("(?m)^# Valid: (.*)$").matcher(input);
        while (matcher.find()) {
            String expected = matcher.group(1);
            if (expected.equals(actual)) {
                correct = true;
                break;
            } else {
                if (!information.isEmpty()) {
                    information += " OR ";
                }
                information += expected;
            }
        }
        if (!correct) {
            assertEquals(information, actual);
        }
    }

    private String readFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer();
        BufferedReader reader = new BufferedReader( new InputStreamReader(this.getClass().getResourceAsStream(filePath)));

        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }
}
