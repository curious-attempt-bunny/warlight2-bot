import bot.BotStarter;
import junit.framework.TestCase;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by home on 2/1/15.
 */
public class IntegrationTest extends TestCase {
    public void testPickSmallStartingSuperRegions() throws Exception {
        testCase("PickSmall.txt");
    }

    public void testAttackToWinSuperRegion() throws Exception {
        testCase("AttackToWinSuperRegion.txt");
    }

    public void testAttackToWinSuperRegion2() throws Exception {
        testCase("AttackToWinSuperRegion2.txt");
    }

    public void testPlaceArmiesInHighestPriorityRegions() throws Exception {
        testCase("PlaceArmiesInHighestPriorityRegions.txt");
    }

    private void testCase(String filename) throws Exception {
        String input = readFileAsString(filename);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        System.setIn(new ByteArrayInputStream(input.getBytes()));
        System.setOut(new PrintStream(out));
        BotStarter.main(new String[]{});
        String output = new String(out.toByteArray());



        String information = "";
        String[] outputCommands = output.split("\n");
        String actual  = outputCommands[outputCommands.length-1].trim();
        boolean correct = false;
        Matcher matcher = Pattern.compile("(?m)^# Valid: (.*)$").matcher(input);
        while (matcher.find()) {
            String expected = matcher.group(1);
            if (matches(expected, actual)) {
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

    private boolean matches(String expected, String actual) {
        if (expected.startsWith("!")) {
            return !matches(expected.substring(1), actual);
        }

        if (expected.startsWith("[")) {
            return actual.contains(expected.substring(1, expected.length()-1));
        } else if (actual.contains(expected)) {
            return true;
        }

        return false;
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
