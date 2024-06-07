package utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ApiResponseDeserializer {
    // Generic method to deserialize the response body to a specified type (responseType).
    public static <T> T deserializeResponse(Response response, Class<T> responseType) {
        // ObjectMapper is used to map JSON to Java objects.
        ObjectMapper objectMapper = new ObjectMapper();
        // Extract the JSON response body.
        Object jsonResponse = response.getBody().jsonPath().get();

        T responseObject;
        // Check if the JSON response is a Map, which indicates it's a JSON object.
        if (jsonResponse instanceof Map) {
            // Convert the JSON object to the specified responseType.
            responseObject = objectMapper.convertValue(jsonResponse, responseType);
        } else {
            // Throw exception if the JSON structure is not supported (i.e., not a JSON object).
            throw new IllegalArgumentException("Unsupported JSON structure");
        }

        // Set the status code in the responseObject if it has a "statusCode" field.
        setFieldIfExists(responseType, responseObject, "statusCode", response.getStatusCode());

        // Convert the response headers to a Map and set it in the responseObject if it has a "headers" field.
        Map headersMap = convertHeadersToMap(response.getHeaders());
        setFieldIfExists(responseType, responseObject, "headers", headersMap);

        return responseObject;
    }

    // Private method to set a field's value in the responseObject if the field exists.
    private static <T> void setFieldIfExists(Class responseType, T responseObject, String fieldName, Object value) {
        try {
            // Get the declared field by name.
            Field field = responseType.getDeclaredField(fieldName);
            // Make the field accessible (in case it is private or protected).
            field.setAccessible(true);
            // Set the value of the field in the responseObject.
            field.set(responseObject, value);
            // Reset the field's accessibility to its original state.
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Print stack trace if there's an error accessing the field.
            e.printStackTrace();
        }
    }

    // Converts Headers object from io.restassured.http.Headers to a Map.
    private static Map<String, Object> convertHeadersToMap(Headers headers) {
        Map<String, Object> headersMap = new HashMap<>();
        // Iterate through each Header and put its name and value in the headersMap.
        for (Header header : headers) {
            headersMap.put(header.getName(), header.getValue());
        }
        return headersMap;
    }
}
