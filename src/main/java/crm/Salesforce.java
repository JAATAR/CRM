package crm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.force.api.ApiConfig;
import com.force.api.ForceApi;
import com.force.api.QueryResult;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Salesforce {

    private static List<String> createdDeelnemersList = new ArrayList<>();
    private static List<String> deletedUsersList = new ArrayList<>();

    private xmlValidation xmlValidation = new xmlValidation();

    private final String HOST = System.getenv("DEV_HOST");
    private final String RABBITMQ_USERNAME = System.getenv("RABBITMQ_USERNAME");
    private final String RABBITMQ_PASSWORD = System.getenv("RABBITMQ_PASSWORD");
    private final int RABBITMQ_PORT = Integer.parseInt(System.getenv("RABBITMQ_PORT"));
    private String EXCHANGE = System.getenv("EXCHANGE");
    private String ROUTINGKEY = System.getenv("ROUTINGKEY");

   private String oldJsonDeelnemer = "{\"Name\":\"John Doe\",\"familie_naam__c\":\"Doe\",\"Phone__c\":\"123456789\",\"Email__c\":\"john.doe@example.com\",\"Bedrijf__c\":\"ABC Company\",\"date_of_birth__c\":\"1990-01-01\",\"Deelnemer_uuid__c\":\"ca5378c7-c079-4d62-b4e1-de8cb4004eee\"}";


    Map<String, Object> lastDeelnemer = null;

    public ForceApi connectToSalesforce() {
        String SALESFORCE_USERNAME = System.getenv("SALESFORCE_USERNAME");
        String SALESFORCE_PASSWORD = System.getenv("SALESFORCE_PASSWORD");
        String SALESFORCE_SECURITY_TOKEN = System.getenv("SALESFORCE_SECURITY_TOKEN");
        String LOGIN_URL = System.getenv("LOGIN_URL");
        String CONSUMER_KEY = System.getenv("CONSUMER_KEY");
        String CONSUMER_SECRET = System.getenv("CONSUMER_SECRET");

        // Combineer wachtwoord en beveiligingstoken
        String loginPassword = SALESFORCE_PASSWORD + SALESFORCE_SECURITY_TOKEN;

        // Configureer de API-configuratie
        ApiConfig config = new ApiConfig()
                .setClientId(CONSUMER_KEY)
                .setClientSecret(CONSUMER_SECRET)
                .setUsername(SALESFORCE_USERNAME)
                .setPassword(loginPassword)
                .setLoginEndpoint(LOGIN_URL);

        ForceApi api = new ForceApi(config);
        return api;

    }

    public void createDeelnemer(Participant participant) {
        ForceApi api = connectToSalesforce();

        Map<String, Object> deelnemerFields = new HashMap<>();
        deelnemerFields.put("Name", participant.getFirstname());
        deelnemerFields.put("familie_naam__c", participant.getLastname());
        deelnemerFields.put("Phone__c", participant.getPhone());
        deelnemerFields.put("Email__c", participant.getEmail());
        deelnemerFields.put("Bedrijf__c", participant.getBusiness());
        deelnemerFields.put("date_of_birth__c", participant.getDateOfBirth());
        deelnemerFields.put("Deelnemer_uuid__c", participant.getUuid());
        deelnemerFields.put("from_business__c", participant.getFromBusiness());

        // Maak de Deelnemer aan in Salesforce
        api.createSObject("Deelnemer__c", deelnemerFields);
    }

    public void createBusiness(Business business) {
        ForceApi api = connectToSalesforce();
        Map<String, Object> businessFields = new HashMap<>();
        businessFields.put("Name", business.getName());
        businessFields.put("VAT__c", business.getVat());
        businessFields.put("Email__c", business.getEmail());
        businessFields.put("Access_Code__c", business.getAccessCode());
        businessFields.put("Address__c", business.getAddress());
        businessFields.put("Bedrijf_uuid__c", business.getUuid());

        // Maak het Business object aan in Salesforce
        api.createSObject("Business__c", businessFields);
    }

    public void createConsumption(Consumption consumption) {
        ForceApi api = connectToSalesforce();
        Map<String, Object> consumptionFields = new HashMap<>();
        consumptionFields.put("Timestamp__c", new Date());
        consumptionFields.put("Name", "food");
        consumptionFields.put("Products__c", consumption.getProducts());
        consumptionFields.put("Consumer_uuid__c", consumption.getUuid());

        // Maak het Consumption object aan in Salesforce
        api.createSObject("Consumption__c", consumptionFields);
    }

    public void updateDeelnemer(String uuid, Participant updatedParticipant) {
        ForceApi api = connectToSalesforce();

        // Retrieve the Deelnemer record JSON string based on UUID
        String deelnemerJson = getDeelnemer(uuid);

        if (deelnemerJson != null) {
            try {
                // Convert JSON string to Map
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> deelnemerRecord = objectMapper.readValue(deelnemerJson, new TypeReference<Map<String, Object>>() {
                });

                // Prepare fields to update
                Map<String, Object> updatedFields = new HashMap<>();
                updatedFields.put("Name", updatedParticipant.getFirstname());
                updatedFields.put("familie_naam__c", updatedParticipant.getLastname());
                updatedFields.put("Phone__c", updatedParticipant.getPhone());
                updatedFields.put("Email__c", updatedParticipant.getEmail());
                updatedFields.put("Bedrijf__c", updatedParticipant.getBusiness());
                updatedFields.put("date_of_birth__c", updatedParticipant.getDateOfBirth());
                updatedFields.put("from_business__c", updatedParticipant.getFromBusiness());

                // Get the record ID
                String id = (String) deelnemerRecord.get("Id");

                // Update the Deelnemer in Salesforce using the retrieved ID and updated fields
                api.updateSObject("Deelnemer__c", id, updatedFields);
                System.out.println("Participant updated");
            } catch (Exception e) {
                e.printStackTrace();
                e.getMessage();
            }
        } else {
            System.out.println("No Deelnemer record found with UUID: " + uuid);
        }
    }

    public void deleteDeelnemer(String uuid) {
        ForceApi api = connectToSalesforce();
        // Retrieve Deelnemer by UUID
        String deelnemerJson = getDeelnemer(uuid);
        if (deelnemerJson != null) {
            try {
                // Convert JSON string to Map
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> deelnemerRecord = objectMapper.readValue(deelnemerJson, new TypeReference<Map<String, Object>>() {
                });
                String deelnemerId = (String) deelnemerRecord.get("Id");
                // Delete the Deelnemer
                api.deleteSObject("Deelnemer__c", deelnemerId);
                System.out.println("Deelnemer deleted successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                e.getMessage();
            }

        } else {
            System.out.println("Deelnemer not found.");
        }
    }

    public QueryResult<Map<String, Object>> retrieveDeelnemerByUUID(ForceApi api, String uuid) {
        // Query the Deelnemer__c record by UUID
        String query = "SELECT Name, familie_naam__c, Phone__c, Email__c, Bedrijf__c, date_of_birth__c, Deelnemer_uuid__c FROM Deelnemer__c WHERE Deelnemer_uuid__c = '" + uuid + "'";

        // Perform the query
        QueryResult<Map> queryResult = api.query(query);

        // Cast the QueryResult to the appropriate generic type
        return (QueryResult<Map<String, Object>>) (QueryResult<?>) queryResult;
    }

    public String getDeelnemer(String uuid) {
        ForceApi api = connectToSalesforce();
        // Query the newest Deelnemer__c record and order by CreatedDate in descending order
        String query = "SELECT Id, Name, familie_naam__c, Phone__c, Email__c, Bedrijf__c, date_of_birth__c, Deelnemer_uuid__c FROM Deelnemer__c WHERE Deelnemer_uuid__c = '" + uuid + "'";

        // Perform the query and get the first result
        QueryResult<Map> queryResult = api.query(query);
        ObjectMapper objectMapper = new ObjectMapper();
        if (queryResult.getTotalSize() > 0) {
            Map<String, Object> deelnemer = queryResult.getRecords().get(0);
            try {
                // Convert the map object to JSON string
                String jsonDeelnemer = objectMapper.writeValueAsString(deelnemer);
                return jsonDeelnemer;

            } catch (Exception e) {
                e.printStackTrace(); // Handle the exception appropriately
            }
        } else {
            System.out.println("No records found.");
        }
        return null; // Or handle the case where no records are found

    }

    // Method to query the newest Deelnemer__c record
    public String getNewestDeelnemerAsJson(String customObject) {

        ForceApi api = connectToSalesforce();
        // Query the newest Deelnemer__c record and order by CreatedDate in descending order
        String query = "SELECT Id, Name, familie_naam__c, Phone__c, Email__c, Bedrijf__c, date_of_birth__c, Deelnemer_uuid__c FROM " + customObject + " ORDER BY CreatedDate DESC LIMIT 1";

        // Perform the query and get the first result
        QueryResult<Map> queryResult = api.query(query);
        ObjectMapper objectMapper = new ObjectMapper();
        if (queryResult.getTotalSize() > 0) {
            Map<String, Object> deelnemer = queryResult.getRecords().get(0);
            // Check if the UUID is not null
            if (deelnemer.get("Deelnemer_uuid__c") != null) {
                try {
                    // Convert the map object to JSON string
                    String jsonDeelnemer = objectMapper.writeValueAsString(deelnemer);
                    return jsonDeelnemer;
                } catch (Exception e) {
                    e.printStackTrace(); // Handle the exception appropriately
                }
            } else {
                System.out.println("Deelnemer UUID is null. Generating a new UUID...");
                try {
                    // Create a new UUID by making a POST request to the provided URL
                    URL url = new URL("http://10.2.160.10:55000/create");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    // Construct the JSON payload
                    String payload = "{\"service\": \"" + "crm" + "\", \"id\": \"" + deelnemer.get("Id") + "\"}";
                    System.out.println(payload);

                    // Write the payload to the connection
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = payload.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    // Get the response code
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Get the response body
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                            StringBuilder response = new StringBuilder();
                            String responseLine;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }

                            // Parse the response JSON to get the UUID
                            JSONObject jsonResponse = new JSONObject(response.toString());
                            String newUUID = jsonResponse.getString("uuid");

                            // Update the deelnemer map with the new UUID
                            deelnemer.put("Deelnemer_uuid__c", newUUID);

                            String deelnemerId = deelnemer.get("Id").toString();
                            // Remove the Id field from the map
                            deelnemer.remove("Id");
                            // Update the Salesforce object without the Id field
                            api.updateSObject(customObject, deelnemerId, deelnemer);

                            // Convert the updated map object to JSON string
                            String jsonDeelnemer = objectMapper.writeValueAsString(deelnemer);
                            return jsonDeelnemer;
                        }
                    } else {
                        // Handle the case where the HTTP request fails
                        System.out.println("HTTP request failed with response code: " + responseCode);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("No records found.");
        }
        return null; // Or handle the case where no records are found
    }


    public void showDeelnemer(String givenuuid) {
        ForceApi api = connectToSalesforce(); // Assume you have a method to connect to Salesforce

        String uuid = givenuuid;
        QueryResult<Map<String, Object>> queryResult = retrieveDeelnemerByUUID(api, uuid);

        // Print the retrieved records
        System.out.println("Retrieved Deelnemer records:");
        for (Map<String, Object> record : queryResult.getRecords()) {
            System.out.println("Name: " + record.get("Name"));
            System.out.println("Familie Naam: " + record.get("familie_naam__c"));
            System.out.println("Phone: " + record.get("Phone__c"));
            System.out.println("Email: " + record.get("Email__c"));
            System.out.println("Bedrijf: " + record.get("Bedrijf__c"));
            System.out.println("Date of Birth: " + record.get("date_of_birth__c"));
            System.out.println("Deelnemer UUID: " + record.get("Deelnemer_uuid__c"));
            System.out.println("-----------------------------");
        }
    }


    // Method to update a Business__c object
    public void updateBusiness(Business business) {
        ForceApi api = connectToSalesforce();

        // Retrieve the Business__c record by UUID
        QueryResult<Map> businessQueryResult = retrieveBusinessByUUID(api, business.getUuid());

        // Check if a Business with the provided UUID exists
        if (businessQueryResult.getTotalSize() > 0) {
            // Get the Business__c record ID
            String businessUUID = (String) businessQueryResult.getRecords().get(0).get("Bedrijf_uuid__c");

            // Prepare fields to update
            Map<String, Object> businessFields = new HashMap<>();
            businessFields.put("Name", business.getName());
            businessFields.put("VAT__c", business.getVat());
            businessFields.put("Email__c", business.getEmail());
            businessFields.put("Access_Code__c", business.getAccessCode());
            businessFields.put("Address__c", business.getAddress());

            // Update the Business__c object in Salesforce
            api.updateSObject("Business__c", businessUUID, businessFields);
            System.out.println("Business updated successfully.");
        } else {
            System.out.println("No Business found with UUID " + business.getUuid());

        }
    }

    // Method to delete a Business__c object
    public void deleteBusinessByUUID(String uuid) {

        ForceApi api = connectToSalesforce();
        // Retrieve the Business__c record by UUID
        QueryResult<Map> businessQueryResult = retrieveBusinessByUUID(api, uuid);

        // Check if a Business with the provided UUID exists
        if (businessQueryResult.getTotalSize() > 0) {
            // Get the Business__c record ID
            String businessUUID = (String) businessQueryResult.getRecords().get(0).get("Bedrijf_uuid__c");

            // Delete the Business__c object in Salesforce
            api.deleteSObject("Business__c", businessUUID);
            System.out.println("Business deleted successfully.");
        } else {
            System.out.println("No Business found with UUID " + uuid);
        }
    }

    public static QueryResult<Map> retrieveBusinessByUUID(ForceApi api, String uuid) {
        // SOQL query to retrieve Business__c record by Bedrijf_uuid__c field
        String query = "SELECT Id, Name, VAT__c, Email__c, Access_Code__c, Address__c FROM Business__c WHERE Bedrijf_uuid__c = '" + uuid + "'";

        // Perform the query
        return api.query(query);
    }

    public void continuouslyCheckForNewUsers(String customObject) {
        while (true) {
            String method = "create";

            // Get the JSON representation of the current Deelnemer__c record
            String jsonDeelnemer = getNewestDeelnemerAsJson(customObject);

            System.out.println(jsonDeelnemer);

            // Check if jsonDeelnemer is null, if so, continue the loop
            if (jsonDeelnemer == null) {
                continue; // Skip the rest of the loop and start from the beginning
            }


            // Print the JSON representation of the new Deelnemer__c record
            System.out.println("New " + customObject + " record:");
            System.out.println(jsonDeelnemer);
            String message = jsonToXml(jsonDeelnemer, method);
            String uuid = extractUUID(message);

                // Check if the message is not already in the list
                if (!createdDeelnemersList.contains(uuid)) {
                    // Send the message to the exchange
                    sendToExchange(message);
                    // Add the message to the list
                    createdDeelnemersList.add(uuid);
                }


            // Check if the list size exceeds the maximum allowed size (5)
            if (createdDeelnemersList.size() > 5) {
                // Remove the oldest message from the list
                createdDeelnemersList.remove(0);
            }

            // Wait for 30 seconds before checking again
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void continuouslyCheckForNewUpdatedUsers(String customObject) {
        while (true) {
            String method = "update";

            // Get the JSON representation of the current Deelnemer__c record
            String jsonDeelnemer = getNewestDeelnemerAsJson(customObject);

            System.out.println(jsonDeelnemer);

            // Check if jsonDeelnemer is null, if so, continue the loop
            if (jsonDeelnemer == null) {
                continue; // Skip the rest of the loop and start from the beginning
            }

            try {
                JSONObject oldJson = new JSONObject(oldJsonDeelnemer);
                JSONObject newJson = new JSONObject(jsonDeelnemer);

                String oldName = oldJson.optString("Name");
                String newName = newJson.optString("Name");

                String oldFamilieNaam = oldJson.optString("familie_naam__c");
                String newFamilieNaam = newJson.optString("familie_naam__c");

                String oldPhone = oldJson.optString("Phone__c");
                String newPhone = newJson.optString("Phone__c");

                String oldEmail = oldJson.optString("Email__c");
                String newEmail = newJson.optString("Email__c");

                String oldBedrijf = oldJson.optString("Bedrijf__c");
                String newBedrijf = newJson.optString("Bedrijf__c");

                String oldDateOfBirth = oldJson.optString("date_of_birth__c");
                String newDateOfBirth = newJson.optString("date_of_birth__c");

                if (!oldName.equals(newName) ||
                        !oldFamilieNaam.equals(newFamilieNaam) ||
                        !oldPhone.equals(newPhone) ||
                        !oldEmail.equals(newEmail) ||
                        !oldBedrijf.equals(newBedrijf) ||
                        !oldDateOfBirth.equals(newDateOfBirth)) {

                    System.out.println("Changes detected in " + customObject + " record:");

                    // Print the JSON representation of the new Deelnemer__c record
                    System.out.println("New " + customObject + " record:");
                    System.out.println(jsonDeelnemer);
                    String message = jsonToXml(jsonDeelnemer, method);

                    // Send the message to the exchange
                    sendToExchange(message);

                }else System.out.println("No changes detected in " + customObject + " record.");

            }catch (JSONException e){
                e.printStackTrace();
                e.getMessage();
            }

            // Set oldJsonDeelnemer for the next iteration
            oldJsonDeelnemer = jsonDeelnemer;

            // Wait for 30 seconds before checking again
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void continuouslyCheckForDeletedUsers(String customObject) {
        while (true) {

            String method = "delete";

            // Get the JSON representation of the current Deelnemer__c record
            String jsonDeelnemer = getNewestDeelnemerAsJson(customObject);

            System.out.println(jsonDeelnemer);

            // Check if jsonDeelnemer is null, if so, continue the loop
            if (jsonDeelnemer == null) {
                continue; // Skip the rest of the loop and start from the beginning
            }

                // Print the JSON representation of the new Deelnemer__c record
                System.out.println("New " + customObject + " record:");
                System.out.println(jsonDeelnemer);
                String message = jsonToXml(jsonDeelnemer, method);
                String uuid = extractUUID(message);


                // Check if the message is not already in the list
                if (!deletedUsersList.contains(uuid)) {
                    // Send the message to the exchange
                    sendToExchange(message);
                    // Add the message to the list
                    deletedUsersList.add(uuid);
                }


            // Check if the list size exceeds the maximum allowed size (5)
            if (deletedUsersList.size() > 5) {
                // Remove the oldest message from the list
                deletedUsersList.remove(0);
            }

            // Wait for 30 seconds before checking again
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static String jsonToXml(String jsonString, String method) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            String xmlData = String.format(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n" +
                            "<participant xmlns=\"http://ehb.local\" fromBusiness=\"%s\" uuid=\"%s\">%n" +
                            "    <service>crm</service>%n" +
                            "    <method>%s</method>%n" +
                            "    <firstname>%s</firstname>%n" +
                            "    <lastname>%s</lastname>%n" +
                            "    <email>%s</email>%n" +
                            "    <phone>%s</phone>%n" +
                            "    <business>%s</business>%n" +
                            "    <date_of_birth>%s</date_of_birth>%n" +
                            "</participant>%n",
                    jsonObject.has("Bedrijf__c") && !jsonObject.optString("Bedrijf__c", "").isEmpty() ? "true" : "false",
                    jsonObject.optString("Deelnemer_uuid__c", ""),
                    method,
                    jsonObject.optString("Name", ""),
                    jsonObject.optString("familie_naam__c", ""),
                    jsonObject.optString("Email__c", ""),
                    jsonObject.optString("Phone__c", ""),
                    jsonObject.optString("Bedrijf__c", ""),
                    jsonObject.optString("date_of_birth__c", "")
            );


            return xmlData;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void sendToExchange(String message) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setUsername(RABBITMQ_USERNAME);
        factory.setPassword(RABBITMQ_PASSWORD);
        factory.setPort(RABBITMQ_PORT);// Set the RabbitMQ server host
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // Declare the topic exchange
            //channel.exchangeDeclare(exchange, "topic");
            // Publish the message to the topic exchange with the specified routing key
            channel.basicPublish(EXCHANGE, ROUTINGKEY, null, message.getBytes());
            System.out.println(" [x] Sent '" + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void editDeelnemer(String uuid, Participant updatedParticipant) {

        ForceApi api = connectToSalesforce();
        // Retrieve the Deelnemer record JSON string based on UUID
        String deelnemerJson = getDeelnemer(uuid);

        if (deelnemerJson != null) {
            try {
                // Convert JSON string to Map
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> deelnemerRecord = objectMapper.readValue(deelnemerJson, new TypeReference<Map<String, Object>>() {});

                // Prepare fields to update
                Map<String, Object> updatedFields = new HashMap<>();
                updatedFields.put("Name", updatedParticipant.getFirstname());
                updatedFields.put("familie_naam__c", updatedParticipant.getLastname());
                updatedFields.put("Phone__c", updatedParticipant.getPhone());
                updatedFields.put("Email__c", updatedParticipant.getEmail());
                updatedFields.put("Bedrijf__c", updatedParticipant.getBusiness());
                updatedFields.put("date_of_birth__c", updatedParticipant.getDateOfBirth());
                updatedFields.put("from_business__c", updatedParticipant.getFromBusiness());

                // Get the record ID
                String id = (String) deelnemerRecord.get("Id");

                // Prepare the request URL
                String url = "https://ehb-dev-ed.my.salesforce.com/services/data/v55.0/sobjects/Deelnemer__c/" + id;

                // Prepare the request body
                String requestBody = objectMapper.writeValueAsString(updatedFields);

                // Create HttpClient
                HttpClient httpClient = HttpClient.newHttpClient();

                // Create HttpRequest
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", "Bearer " + api.getSession().getAccessToken()) // Assuming ForceApi has a method like getSession() to get session details
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                // Send the request and handle the response
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                // Check the response status
                if (response.statusCode() == 200) {
                    System.out.println("Participant updated");
                } else {
                    System.out.println("Failed to update participant: " + response.body());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No Deelnemer record found with UUID: " + uuid);
        }
    }

    public static String extractUUID(String inputString) {
        String pattern = "uuid=\"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})\"";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(inputString);

        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
    }


}



