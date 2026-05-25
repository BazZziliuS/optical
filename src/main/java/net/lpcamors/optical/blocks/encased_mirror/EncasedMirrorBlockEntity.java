package net.lpcamors.optical.blocks.encased_mirror;

import javax.annotation.Nullable;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EncasedMirrorBlockEntity extends KineticBlockEntity {

    public float rotVelocity;
    public float oAngle;
    public float angle;
    private @Nullable State state = null;


    public EncasedMirrorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }



    @Override
    public void tick() {
        super.tick();
        float actualSpeed = getSpeed();
        rotVelocity = actualSpeed / (90F / 21F) ;

        this.oAngle = angle;
        this.angle = Mth.clamp(this.angle + rotVelocity, 0F, 90F);
        float i = (angle / 9F);
        if(i >= 9.5){
            this.state = State.PERPENDICULAR;
        } else if(i <= 0.5){
            this.state = State.PARALLEL;
        } else {
            this.state = null;
        }
    }


    public @Nullable State getState(){
        return this.state;
    }


    public float getIndependentAngle(float partialTicks) {
        return Mth.clamp(this.angle + partialTicks * this.rotVelocity, 0F, 90F);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider prov, boolean clientPacket) {
        super.read(compound, prov, clientPacket);
        if(compound.contains("AngularPosition")){
            this.angle = compound.getFloat("AngularPosition");
        }
        if(compound.contains("AngularVelocity")){
            this.rotVelocity = compound.getFloat("AngularVelocity");
        }

    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider prov, boolean clientPacket) {
        super.write(compound, prov, clientPacket);
        compound.putFloat("AngularPosition", this.angle);
        compound.putFloat("AngularVelocity", this.rotVelocity);

    }

    public enum State {

        PARALLEL(0),
        PERPENDICULAR(1);


        private final int id;
        State(int id){
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public State getPerpendicular(){
            if(this.getAngle() == 0){
                return PERPENDICULAR;
            }
            return PARALLEL;
        }

        public boolean isParallel(){
            return this.equals(PARALLEL);
        }

        public double getAngle(){
            return this.equals(PARALLEL) ? 0D : Math.PI / 2D;
        }
    }

}
