package de.mennomax.astikorcarts;

import de.mennomax.astikorcarts.config.AstikorCartsConfig;
import de.mennomax.astikorcarts.entity.PostilionEntity;
import de.mennomax.astikorcarts.entity.ai.goal.PullCartGoal;
import de.mennomax.astikorcarts.entity.ai.goal.RideCartGoal;
import de.mennomax.astikorcarts.util.GoalAdder;
import de.mennomax.astikorcarts.world.AstikorWorld;
import de.mennomax.astikorcarts.world.SimpleAstikorWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolderRegistry;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class CommonInitializer implements Initializer {
    @Override
    public void init(final Context mod) {
        /* final ModContainer container = mod.context().getActiveContainer();
        ObjectHolderRegistry.addHandler(new Consumer<>() {
            boolean run = true;

            @Override
            public void accept(final Predicate<ResourceLocation> filter) {
                if (this.run && filter.test(ForgeRegistries.ENTITIES.getRegistryName())) {
                    container.addConfig(new ModConfig(ModConfig.Type.COMMON, AstikorCartsConfig.spec(), container));
                    this.run = false;
                    LogicalSidedProvider.WORKQUEUE.get(EffectiveSide.get())
                        .execute(() -> ObjectHolderRegistry.removeHandler(this));
                }
            }
        }); */ 
        /* //This, left for comparison purposes, could be the main culprit for the pig crash bug, as somebody on github stated in issue
        // #88 https://github.com/issork/astikor-carts/issues/88#issuecomment-1426866269
        
        // Thanks for Verph for figuring out this fix! I (xFirefalconx/xDonnervogelx) applied a part of their changes to the original repository.
        // TODO: It might still be bugged, but not fatal anymore. Refer to changes in this repository for reference or pick it from its parent repo, the TFC fork. 
        // https://github.com/xFirefalconx/astikor-carts-tfc-bugfixcomparisons/commit/4bb4b085b935d85468d0bbd2a616c2aa0c575486 */
        
        mod.modBus().<EntityAttributeCreationEvent>addListener(e -> {
            e.put(AstikorCarts.EntityTypes.POSTILION.get(), LivingEntity.createLivingAttributes().build());
        });
        mod.bus().<AttachCapabilitiesEvent<Level>, Level>addGenericListener(Level.class, e ->
            e.addCapability(new ResourceLocation(AstikorCarts.ID, "astikor"), AstikorWorld.createProvider(SimpleAstikorWorld::new))
        );
        GoalAdder.mobGoal(Mob.class)
            .add(1, PullCartGoal::new)
            .add(1, RideCartGoal::new)
            .build()
            .register(mod.bus());
        mod.bus().<PlayerInteractEvent.EntityInteract>addListener(e -> {
            final Entity rider = e.getTarget().getControllingPassenger();
            if (rider instanceof PostilionEntity) {
                rider.stopRiding();
            }
        });
        mod.bus().<TickEvent.WorldTickEvent>addListener(e -> {
            if (e.phase == TickEvent.Phase.END) {
                AstikorWorld.get(e.world).ifPresent(AstikorWorld::tick);
            }
        });
    }
}
