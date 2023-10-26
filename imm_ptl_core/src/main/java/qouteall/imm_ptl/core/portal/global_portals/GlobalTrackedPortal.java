package qouteall.imm_ptl.core.portal.global_portals;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import qouteall.imm_ptl.core.platform_specific.IPRegistry;
import qouteall.imm_ptl.core.portal.Portal;

// NOTE don't use `instanceof GlobalTrackedPortal`. Use `portal.getIsGlobal()` instead
public class GlobalTrackedPortal extends Portal {
    public static final EntityType<GlobalTrackedPortal> entityType =
            IPRegistry.GLOBAL_TRACKED_PORTAL.get();

    public GlobalTrackedPortal(
        EntityType<?> entityType,
        Level world
    ) {
        super(entityType, world);
    }
    
}
