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