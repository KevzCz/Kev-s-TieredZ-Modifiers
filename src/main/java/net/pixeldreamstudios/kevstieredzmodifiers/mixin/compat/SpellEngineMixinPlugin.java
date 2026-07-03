package net.pixeldreamstudios.kevstieredzmodifiers.mixin.compat;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;

public class SpellEngineMixinPlugin implements IMixinConfigPlugin {

    private boolean spellEngineLoaded;

    @Override
    public void onLoad(String mixinPackage) {
        this.spellEngineLoaded = FabricLoader.getInstance().isModLoaded("spell_engine");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return spellEngineLoaded;
    }

    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String t, ClassNode n, String m, IMixinInfo i) { }
    @Override public void postApply(String t, ClassNode n, String m, IMixinInfo i) { }
}
