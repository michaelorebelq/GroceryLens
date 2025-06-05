package com.example.grocerylens;

import org.json.JSONArray;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
//(original written tests)
public class NLPTest {

    // testing if method correctly returns a list of ingredients
    @Test
    public void getIngredientsFromJson_validList() {
        JSONArray input = new JSONArray(Arrays.asList("tomato", "onion", "cheese"));
        List<String> result = NLP.getIngredientsFromJson(input);

        assertEquals(3, result.size());
        assertTrue(result.contains("tomato"));
        assertTrue(result.contains("onion"));
        assertTrue(result.contains("cheese"));
    }

    // testing no ingredients spoken input, empty array
    @Test
    public void getIngredientsFromJson_withEmptyArray() {
        JSONArray input = new JSONArray();
        List<String> result = NLP.getIngredientsFromJson(input);
        assertTrue(result.isEmpty());
    }

    // testing with null input
    @Test
    public void getIngredientsFromJson_withNullInput() {
        List<String> result = NLP.getIngredientsFromJson(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
