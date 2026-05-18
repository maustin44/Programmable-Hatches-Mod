package reobf.proghatches.main.mixin.mixins.part2;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.slots.VirtualMEMonitorableSlot;
import appeng.container.AEBaseContainer;
import appeng.core.AEConfig;
import appeng.util.FluidUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.ae.ICtrlInverted;

@Mixin(value = GuiMEMonitorable.class, remap = false)
public abstract class MixinTerminalCtrlInvert extends AEBaseGui {

    public MixinTerminalCtrlInvert(Container container) {
        super(container);
    }

    public abstract void drawHoveringText(List textLines, int x, int y, FontRenderer font);

    /**
     * Get the terminal host from the container's tile entity reference.
     * The 'host' field was removed from GuiMEMonitorable in GT daily #520,
     * so we obtain it via AEBaseContainer.getTileEntity() instead.
     */
    private ITerminalHost getTerminalHost() {
        if (this.inventorySlots instanceof AEBaseContainer abc) {
            Object te = abc.getTileEntity();
            if (te instanceof ITerminalHost th) return th;
        }
        return null;
    }

    @WrapOperation(method = "handleMonitorableSlotClick", at = @At(
        value = "INVOKE",
        target = "Lappeng/client/gui/implementations/GuiMEMonitorable;isCtrlKeyDown()Z"
    ))
    private boolean handleMonitorableSlotClick(Operation<Boolean> original,
            @Local VirtualMEMonitorableSlot t,
            @Local(argsOnly = true, ordinal = 0) LocalIntRef mouseButton) {

        ITerminalHost host = getTerminalHost();
        if ((host instanceof ICtrlInverted ta) ? ta.invert() : false) {
            ItemStack hand = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();
            FluidStack fs;
            if (hand != null && (fs = FluidUtils.getFluidFromContainer(hand)) != null && fs.amount > 0) {
                return original.call() ^ true;
            } else if (t.getAEStack() instanceof IAEFluidStack) {
                return original.call() ^ true;
            }
        }
        return original.call();
    }

    @Inject(method = "drawFG", at = { @At("HEAD") })
    public void A(CallbackInfo a, @Share(value = "showContainerInteractionTooltips") LocalRef<Boolean> tmp) {
        ITerminalHost host = getTerminalHost();
        if ((host instanceof ICtrlInverted ta) ? ta.invert() : false) {
            tmp.set(AEConfig.instance.showContainerInteractionTooltips);
            AEConfig.instance.showContainerInteractionTooltips = false;
        }
    }

    @Inject(method = "drawFG", at = { @At("RETURN") })
    public void B(CallbackInfo a, @Share(value = "showContainerInteractionTooltips") LocalRef<Boolean> tmp) {
        ITerminalHost host = getTerminalHost();
        if ((host instanceof ICtrlInverted ta) ? ta.invert() : false) {
            AEConfig.instance.showContainerInteractionTooltips = tmp.get();
        }
    }
}
