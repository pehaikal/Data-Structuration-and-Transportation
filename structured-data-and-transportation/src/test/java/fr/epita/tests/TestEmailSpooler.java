package fr.epita.tests;

import fr.epita.datamodels.Contact;
import fr.epita.services.EmailSender;
import org.apache.log4j.BasicConfigurator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestEmailSpooler {

    public static void main(String[] args) {

        try {
            Connection cnt = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "qwerty");

            BasicConfigurator.configure();

            String emailTemplate = "src\\main\\campaign-def\\.camel\\campaign-free-gift-template.html";
            String outputTemplate = "src/main/email-spool/email-%s-output.html";

            Map<String, String> mapAge = getYearRange();

            List<Contact> contactsList = getContacts(cnt, mapAge.get("minimum_age"), mapAge.get("maximum_age"));

            for (Contact contact : contactsList) {
                System.out.println("Writing file to email " + contact.getContact_email().split("@")[0]);
                System.out.println("*******************************************************");
                System.out.println("Working on record [" + contact + "]");
                System.out.println("*******************************************************");

                Stream<String> readLines;

                Map<String, String> variableMap = fill(contact.getContact_first_name(), contact.getContact_city());
                Path pathTemplate = Paths.get(emailTemplate);

                readLines = Files.lines(pathTemplate, StandardCharsets.UTF_8);
                List<String> replacedLines = readLines.map(
                                line -> replaceWildcard(line, variableMap))
                        .collect(Collectors.toList());

                Files.write(Paths.get(String.format(
                        outputTemplate,
                        contact.getContact_email().split("@")[0])), replacedLines, StandardCharsets.UTF_8);

                readLines.close();
                System.out.println("File successfully modified");
                System.out.println("*******************************************************");
            }

        } catch (Exception e) {
            System.out.println("Unable to write emails");
            e.printStackTrace();
        }

        try {
            // once all template created start the email sender
            EmailSender.sendEmails();

        } catch (Exception e) {
            System.out.println("Unable to send emails");
            e.printStackTrace();
        }
    }

    private static List<Contact> getContacts(Connection cnt, String minYear, String maxYear) throws SQLException {
        PreparedStatement preparedStatement = cnt
                .prepareStatement("SELECT contact_email, contact_first_name, contact_last_name, contact_address, "
                        + " contact_city, contact_country, contact_birthdate FROM CONTACTS "
                        + " where substring(contact_birthdate,0,5) > ? "
                        + " and substring(contact_birthdate,0,5) < ? ");

        preparedStatement.setString(1, maxYear);
        preparedStatement.setString(2, minYear);

        ResultSet resultSet = preparedStatement.executeQuery();
        List<Contact> contacts = new ArrayList<>();

        while (resultSet.next()) {
            contacts.add(getContact(resultSet));
        }
        return contacts;
    }

    private static Contact getContact(ResultSet resultSet) throws SQLException {
        return new Contact(
                resultSet.getString("contact_email"),
                resultSet.getString("contact_first_name"),
                resultSet.getString("contact_last_name"),
                resultSet.getString("contact_address"),
                resultSet.getString("contact_city"),
                resultSet.getString("contact_country"),
                resultSet.getString("contact_birthdate"));
    }

    public static Map<String, String> fill(String firstName, String city) {
        Map<String, String> map = new HashMap<>();
        map.put("${firstName}", firstName);
        map.put("${city}", city);
        return map;
    }

    private static String replaceWildcard(String contactInfo, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (contactInfo.contains(entry.getKey())) {
                contactInfo = contactInfo.replace(entry.getKey(), entry.getValue());
            }
        }
        return contactInfo;
    }

    private static Map<String, String> getYearRange() {

        int currenYear = Calendar.getInstance().get(Calendar.YEAR);

        Map<String, String> map = new HashMap<>();
        // JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("src/main/campaign-def/campaign-free-gift-meta.json")) {
            // Read JSON file
            Object obj = jsonParser.parse(reader);

            JSONObject target = (JSONObject) obj;
            System.out.println(target);

            // Get target object within list
            JSONObject targetObject = (JSONObject) target.get("target");

            // Get target minimum age
            Long minimumAge = (Long) targetObject.get("minimum_age");
            System.out.println("age = " + minimumAge + " year = " + (currenYear - minimumAge));

            map.put("minimum_age", "" + (currenYear - minimumAge));
            // Get target maximum age
            Long maximumAge = (Long) targetObject.get("maximum_age");
            System.out.println("age = " + maximumAge + " year = " + (currenYear - maximumAge));

            map.put("maximum_age", "" + (currenYear - maximumAge));
            return map;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;

        } catch (IOException e) {
            e.printStackTrace();
            return null;

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}