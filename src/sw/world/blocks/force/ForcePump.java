package sw.world.blocks.force;

import arc.util.io.*;
import mindustry.graphics.*;
import mindustry.world.blocks.production.*;
import sw.world.interfaces.*;
import sw.world.meta.*;
import sw.world.modules.*;

public class ForcePump extends Pump {
	public ForceConfig forceConfig = new ForceConfig();

	public ForcePump(String name) {
		super(name);
	}

	@Override
	public void setStats() {
		super.setStats();
		forceConfig.addStats(stats);
	}

	@Override public void drawOverlay(float x, float y, int rotation) {
		if (forceConfig.outputsForce) Drawf.dashCircle(x, y, forceConfig.range, Pal.accent);
	}

	@Override
	public void init() {
		super.init();
		configurable = forceConfig.outputsForce;
	}

	public class ForcePumpBuild extends PumpBuild implements HasForce {
		ForceModule force = new ForceModule();

		public ForceModule force() {
			return force;
		}
		public ForceConfig fConfig() {return forceConfig;}

		@Override
		public void onProximityAdded() {
			super.onProximityAdded();
			fGraph().addBuild(this);
		}
		@Override
		public void onProximityRemoved() {
			super.onProximityRemoved();
			force().links.each(link -> fGraph().removeLink(link));
			fGraph().removeBuild(this, false);
		}


		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);
			force.read(read);
		}
		@Override
		public void write(Writes write) {
			super.write(write);
			force.write(write);
		}
	}
}
