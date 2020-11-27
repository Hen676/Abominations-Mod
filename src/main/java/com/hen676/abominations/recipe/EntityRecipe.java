package com.hen676.abominations.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hen676.abominations.init.BlockInit;
import com.hen676.abominations.init.RecipeInit;
import com.hen676.abominations.util.RecipeUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EntityRecipe implements IRecipe<RecipeWrapper> {
    private int inputLength;
    private List<ResourceLocation> entities;
    private ResourceLocation result;
    private ResourceLocation id;

    public EntityRecipe(int inputLength, List<ResourceLocation> input, ResourceLocation output, ResourceLocation id) {
        this.inputLength = inputLength;
        this.entities = input;
        this.result = output;
        this.id = id;
    }

    public boolean matches(List<ResourceLocation> input) {
        return entities.equals(input);
    }

    public ResourceLocation getResult() {
        return result;
    }

    @Override
    public boolean matches(RecipeWrapper inv, World worldIn) {
        return false;
    }

    @Override
    public ItemStack getCraftingResult(RecipeWrapper inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(BlockInit.FORGE.get());
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return RecipeInit.ENTITY_RECIPE_SERIALIZER.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return RecipeInit.ENTITY_RECIPE;
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<EntityRecipe> {
        @Override
        public EntityRecipe read(ResourceLocation recipeId, JsonObject json) {
            List<ResourceLocation> inputs = new ArrayList<>();
            final JsonArray list = JSONUtils.getJsonArray(json, "entities");
            for(int i=0; i<list.size(); i++) {
                inputs.add(new ResourceLocation(JSONUtils.getString(list.get(i).getAsJsonObject(),"entity")));
            }
            inputs.sort(new RecipeUtil.ResourceLocationComparator());
            final ResourceLocation output = new ResourceLocation(JSONUtils.getString(json, "result"));

            for (ResourceLocation input: inputs) {
                if (ForgeRegistries.ENTITIES.getValue(input) == null) {
                    error(input);
                }
            }
            if (ForgeRegistries.ENTITIES.getValue(output) == null) {
                error(output);
            }
            return new EntityRecipe(list.size(),inputs,output,recipeId);
        }

        @Nullable
        @Override
        public EntityRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            List<ResourceLocation> inputs = new ArrayList<>();
            final int inputLength = buffer.readInt();
            for(int i=0; i<inputLength; i++) {
                inputs.add(buffer.readResourceLocation());
            }
            final ResourceLocation output = buffer.readResourceLocation();

            for (ResourceLocation input: inputs) {
                if (ForgeRegistries.ENTITIES.getValue(input) == null) {
                    error(input);
                }
            }
            if (ForgeRegistries.ENTITIES.getValue(output) == null) {
                error(output);
            }
            return new EntityRecipe(inputLength,inputs,output,recipeId);
        }

        @Override
        public void write(PacketBuffer buffer, EntityRecipe recipe) {
            buffer.writeInt(recipe.inputLength);
            for(int i=0; i<recipe.inputLength; i++) {
                buffer.writeResourceLocation(recipe.entities.get(i));
            }
            buffer.writeResourceLocation(recipe.result);
        }

        private void error(ResourceLocation entity) {
            throw new IllegalStateException("The Entity " + entity + " does not exist.");
        }
    }
}
