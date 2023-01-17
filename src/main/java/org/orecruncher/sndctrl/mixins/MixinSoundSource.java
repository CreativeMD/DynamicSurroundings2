/*
 * Dynamic Surroundings
 * Copyright (C) 2020 OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.sndctrl.mixins;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.openal.AL10;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.audio.handlers.SoundFXProcessor;
import org.orecruncher.sndctrl.audio.handlers.SourceContext;
import org.orecruncher.sndctrl.misc.IMixinSoundContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.audio.AudioStreamBuffer;
import net.minecraft.sounds.SoundSource;

@Mixin(SoundSource.class)
public class MixinSoundSource implements IMixinSoundContext {
    
    private SourceContext sndctrl_data = null;
    
    @Shadow
    @Final
    public int id;
    
    @Nullable
    @Override
    public SourceContext getData() {
        return this.sndctrl_data;
    }
    
    @Override
    public void setData(@Nullable SourceContext data) {
        this.sndctrl_data = data;
    }
    
    /** Called when the sound is ticked by the sound engine. This will set the sound effect properties for the sound
     * at the time of play.
     * 
     * @param ci
     *            Ignored */
    @Inject(method = "play()V", at = @At("HEAD"))
    public void onPlay(CallbackInfo ci) {
        try {
            SoundFXProcessor.tick((SoundSource) ((Object) this));
        } catch (@Nonnull final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in onPlay()!");
        }
    }
    
    /** Called when the sound is ticked by the sound engine. This will set the sound effect properties for the sound
     * at the time of tick.
     * 
     * @param ci
     *            Ignored */
    @Inject(method = "tick()V", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        try {
            SoundFXProcessor.tick((SoundSource) ((Object) this));
        } catch (@Nonnull final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in onTick()!");
        }
    }
    
    /** Called when a sounds stops playing. Any context information sndctrl has generated will be cleaned up.
     * 
     * @param ci
     *            Ignored */
    @Inject(method = "stop()V", at = @At("HEAD"))
    public void onStop(CallbackInfo ci) {
        try {
            SoundFXProcessor.stopSoundPlay((SoundSource) ((Object) this));
        } catch (@Nonnull final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in onStop()!");
        }
    }
    
    /** Called after the audio stream buffer has been generated by the sound engine. If the sound has non-linear
     * attenuation and is not mono, it will be converted to mono format. Non-mono sounds will be played in the sound engine
     * as if they are non-linear because it cannot convert non-mono sounds for 3D environmental play.
     * 
     * @param p_216429_1_
     *            Buffer to convert to mono if needed.
     * @param ci
     *            Call will always be cancelled. */
    @Inject(method = "bindBuffer(Lnet/minecraft/client/audio/AudioStreamBuffer;)V", at = @At("HEAD"), cancellable = true)
    public void onPlayBuffer(AudioStreamBuffer p_216429_1_, CallbackInfo ci) {
        try {
            final SoundSource src = (SoundSource) ((Object) this);
            
            p_216429_1_ = SoundFXProcessor.playBuffer(src, p_216429_1_);
            
            p_216429_1_.getBuffer().ifPresent((p_216431_1_) -> {
                AL10.alSourcei(src.id, 4105, p_216431_1_);
            });
            
            ci.cancel();
        } catch (@Nonnull final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in onPlayBuffer()!");
        }
    }
}
