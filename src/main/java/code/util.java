package code;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class util {
    public static String getInitials(String fullName) {
        String[] parts = fullName.split(" ");
        String initials = "";
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials += part.charAt(0);
            }
        }
        return initials.toUpperCase();
    }
    /**
     * Function to convert a string to proper form
     * i.e each word's first letter is capitalized
     * while remainder is lower case
     * @param str The string to convert to proper form
     * @return The proper string
     */
    public static String proper(String str){
        str = str.replaceAll("\\s+", " ");
        String[] arr = str.split(" ");
        StringBuilder result = new StringBuilder("");
        int size = arr.length;
        for (int i = 0; i < size; i++){
            result.append(
                    arr[i].substring(0, 1).toUpperCase() +
                    arr[i].substring(1).toLowerCase()
            );
            if (!(i+1 == size)){
                result.append(" ");
            }
        }
        return result.toString();
    }

    public static int extractNumber(String str, int defaultNum) {
        // Regular expression to match a number
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(str);

        // If a number is found, return it, otherwise return the default number
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        } else {
            return defaultNum;
        }
    }
    public static String extractEmail(String str){
        String result = "N/A";

        if (!str.equalsIgnoreCase("") ){
            String emailRegex = "([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})";
            Pattern pattern = Pattern.compile(emailRegex);
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()){
                result = matcher.group();
            }
        }
        return result;
    }

    public static String extractYear(String str){
        String result = "";
        if (str.contains("1")){
            result = "1st";
        }
        else if (str.contains("2")){
            result = "2nd";
        }
        else if (str.contains("3")){
            result = "3rd";
        }
        else if (str.contains("4")){
            result = "4th+";
        }
        else{
            result = "Grad";
        }
        return result;
    }
}
