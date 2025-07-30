# Implementation Notes - Canteen Application

## Overview
This document describes the implementation of a canteen menu application with a Spring Boot backend and Svelte frontend.

## What Was Implemented

### Backend (Spring Boot)

#### 1. CanteenController.java
**Location:** `server/src/main/java/de/tum/aet/devops25/w03/controller/CanteenController.java`

**Implementation:**
```java
@GetMapping("/{canteenName}/today")
public ResponseEntity<List<Dish>> getTodayMeals(@PathVariable("canteenName") String canteenName) {
    List<Dish> todayMeals = canteenService.getTodayMeals(canteenName);
    
    if (todayMeals.isEmpty()) {
        return ResponseEntity.noContent().build();
    }
    
    return ResponseEntity.ok(todayMeals);
}
```

**Key Features:**
- REST endpoint: `GET /{canteenName}/today`
- Returns HTTP 204 (No Content) when no meals available
- Returns HTTP 200 with meal list when meals available
- Uses explicit parameter naming `@PathVariable("canteenName")` to avoid reflection issues

#### 2. Test Implementation
**Location:** `server/src/test/java/de/tum/aet/devops25/w03/CanteenControllerTest.java`

**Tests Added:**
- `testGetTodayMeals_ReturnsNoContent_WhenNoMealsAvailable()`: Tests empty response scenario
- `testGetTodayMeals_ReturnsOkWithMeals()`: Tests successful meal retrieval

**Key Test Features:**
- Mocks `RestTemplate` and `Clock` for consistent testing
- Uses helper method `getList()` for HTTP requests
- Verifies correct HTTP status codes and response content
- Uses `name()` accessor method for `Dish` record fields

#### 3. Build Configuration
**Location:** `server/build.gradle`

**Added Configuration:**
```gradle
tasks.withType(JavaCompile) {
    options.compilerArgs += ['-parameters']
}
```

**Purpose:** Enables parameter name preservation in bytecode for Spring's parameter resolution.

### Frontend (Svelte)

#### 1. Main Page Component
**Location:** `client/src/routes/+page.svelte`

**Implementation:**
```javascript
async function fetchMeals() {
    const res = await fetch(`${baseUrl}/mensa-garching/today`);
    if (res.ok) {
        meals = await res.json();
    }
}

onMount(async () => {
    await fetchMeals();
});
```

**Template:**
```svelte
{:else}
    <div class="food-grid">
        {#each meals as meal}
            <FoodCard {meal}/>
        {/each}
    </div>
{/if}
```

#### 2. Food Card Component
**Location:** `client/src/routes/FoodCard.svelte`

**Implementation:**
```svelte
<script lang="ts">
    import Icon from "@iconify/svelte";
    import type { Meal } from '$lib/types';

    interface Props {
        meal: Meal;
    }

    let { meal }: Props = $props();
</script>

<div class="food-card">
    <h2>{meal.name}</h2>
    <p class="dish-type">{meal.dish_type}</p>
    <div class="label-section">
        {#if meal.labels.includes("VEGETARIAN")}
            <span class="label diet-label">
                <Icon icon="mdi:leaf" />Vegetarian
            </span>
        {/if}
        {#if meal.labels.includes("VEGAN")}
            <span class="label diet-label">
                <Icon icon="mdi:sprout" />Vegan
            </span>
        {/if}
        {#if meal.labels.includes("MEAT")}
            <span class="label diet-label">
                <Icon icon="mdi:food-drumstick" />Meat
            </span>
        {/if}
    </div>
</div>
```

## Key Issues Resolved

### 1. Parameter Name Resolution Error
**Issue:** `IllegalArgumentException: Name for argument of type [java.lang.String] not specified`

**Solutions Applied:**
1. Added `-parameters` compiler flag to `build.gradle`
2. Used explicit parameter naming: `@PathVariable("canteenName")`
3. Clean rebuild to ensure bytecode includes parameter names

### 2. Java Record Accessor Methods
**Issue:** Test compilation error with `getName()` method

**Solution:** Used correct record accessor `name()` instead of `getName()` for the `Dish` record.

### 3. HTTP Status Code Mismatch
**Issue:** Test expected HTTP 200 but controller returned HTTP 204

**Solution:** Updated test to expect `HttpStatus.NO_CONTENT` and verify `null` result for empty meals.

## Running Instructions

### Backend
```bash
cd server
./gradlew clean build
./gradlew bootRun
```

### Frontend
```bash
cd client
npm install
npm run dev
```

### Testing
```bash
cd server
./gradlew test
```

## API Endpoints

### GET /{canteenName}/today
- **Description:** Get today's meals for a specific canteen
- **Example:** `GET /mensa-garching/today`
- **Response 200:** List of dishes (JSON)
- **Response 204:** No content (when no meals available)

## Data Models

### Dish Record
```java
public record Dish(String name, String dish_type, List<String> labels) {}
```

**Accessor Methods:**
- `name()` - dish name
- `dish_type()` - type of dish
- `labels()` - dietary labels (VEGETARIAN, VEGAN, MEAT, etc.)

## Notes for Future Development

1. **Parameter Resolution:** Always use explicit parameter names in `@PathVariable` annotations to avoid reflection issues
2. **Testing:** Remember that Java records use accessor methods like `name()`, not getter methods like `getName()`
3. **Build Configuration:** The `-parameters` compiler flag is essential for Spring parameter resolution
4. **HTTP Status Codes:** Use appropriate status codes (204 for no content, 200 for success with data)