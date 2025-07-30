package de.tum.aet.devops25.w03;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tum.aet.devops25.w03.model.Day;
import de.tum.aet.devops25.w03.model.Dish;
import de.tum.aet.devops25.w03.model.Week;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CanteenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    public void setup() {
        // Mock the clock to avoid flaky tests based on timing issues (because the service uses 'today')
        when(clock.instant()).thenReturn(Instant.parse("2025-05-08T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    public void testGetTodayMeals_ReturnsNoContent_WhenNoMealsAvailable() throws Exception {
        // Mock the RestTemplate to return null (no data)
        when(restTemplate.getForObject(anyString(), eq(Week.class))).thenReturn(null);

        // Test the endpoint
        List<Dish> result = getList("/{canteenName}/today", HttpStatus.NO_CONTENT, Dish.class, "mensa-garching");

        // Verify null result (NO_CONTENT returns null from getList helper)
        assertThat(result).isNull();
    }

    @Test
    public void testGetTodayMeals_ReturnsOkWithMeals() throws Exception {
        // Create test data for today
        LocalDate today = LocalDate.of(2025, 5, 8); // Matches our mocked clock
        int weekNumber = today.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        int year = today.getYear();
        Week testWeek = createTestData(today, weekNumber, year);

        // Mock the RestTemplate to return the test data
        when(restTemplate.getForObject(anyString(), eq(Week.class))).thenReturn(testWeek);

        // Test the endpoint
        List<Dish> result = getList("/{canteenName}/today", HttpStatus.OK, Dish.class, "mensa-garching");

        // Verify the result contains today's meals
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Vegetarian Pasta");
        assertThat(result.get(1).name()).isEqualTo("Salad");
    }

    private <T> List<T> getList(String path, HttpStatus expectedStatus, Class<T> listElementType, Object... uriVariables) throws Exception {
        MvcResult res = mockMvc.perform(get(path, uriVariables)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expectedStatus.value()))
                .andReturn();

        if (expectedStatus.value() != 200) {
            if (res.getResponse().getContentType() != null && !res.getResponse().getContentType().equals("application/problem+json")) {
                assertThat(res.getResponse().getContentAsString()).isNullOrEmpty();
            }
            return null;
        }
        var mapper = new ObjectMapper();
        return mapper.readValue(res.getResponse().getContentAsString(), mapper.getTypeFactory().constructCollectionType(List.class, listElementType));
    }

    private static Week createTestData(LocalDate date, int weekNumber, int year) {
        List<String> vegetarianLabels = List.of("VEGETARIAN");
        Dish dish1 = new Dish("Vegetarian Pasta", "Main Dish", vegetarianLabels);
        Dish dish2 = new Dish("Salad", "Side Dish", vegetarianLabels);
        List<Dish> todayDishes = Arrays.asList(dish1, dish2);

        // Create a day with date's date
        Day today_menu = new Day(date, todayDishes);

        // Create a day with tomorrow's date (shouldn't be returned in results)
        Day tomorrow_menu = new Day(date.plusDays(1),
                List.of(new Dish("Tomorrow's Special", "Main Dish", List.of("MEAT"))));

        // Create the week object
        return new Week(weekNumber, year, Arrays.asList(today_menu, tomorrow_menu));
    }
}
