package sw.world.graph;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import sw.world.interfaces.*;
import sw.world.modules.*;

/**
 * TODO visual speed setting
 */
public class ForceGraph extends Graph {
	public final Seq<Building> builds = new Seq<>(false, 16, Building.class);
	public final Seq<ForceModule> modules = new Seq<>(false, 16, ForceModule.class);
	public final Seq<ForceLink> links = new Seq<>(false, 16, ForceLink.class);

	public float rotation = 0;

	private WindowedMean mean;

	public ForceGraph() {
		super();
		addGraph();
	}

	public Seq<Building> floodFill(Building build) {
		Seq<Building> out = new Seq<>();
		Seq<Building> next = Seq.with(build);
		while (!next.isEmpty()) {
			Building b = next.pop();
			out.addUnique(b);
			if (b instanceof HasForce add) {
				out.addUnique(b);
				add.force().links.each(i -> {
					out.addUnique((Building) i.l1());
					out.addUnique((Building) i.l2());
				});
			}
		}
		return out;
	}

	public void add(Building build) {
		if (build instanceof HasForce add) {
			add.force().graph.removeBuild(build);
			add.force().graph = this;
			builds.addUnique(build);
			modules.addUnique(add.force());
		}
	}
	public void addGraph(ForceGraph graph) {
		if (graph == this) return;
		if (graph.builds.size > builds.size) {
			graph.addGraph(this);
			return;
		}

		graph.entity.remove();

		for (Building build : graph.builds) add(build);
		for (ForceLink forceLink : graph.links) links.addUnique(forceLink);
		addGraph();
	}
	public void removeBuild(Building build) {
		if (builds.remove(build)) modules.remove(((HasForce) build).force());
	}
	public void remove(Building build) {
		ForceGraph graph = new ForceGraph();
		graph.addGraph();
		graph.add(build);
		graph.entity.update();
	}

	public void update() {
		rotation += Mathf.maxZero(Math.abs(getSpeed()) - getResistance()) * Time.delta * (getSpeed() >= 0 ? 1 : -1);
		if (mean == null || mean.getWindowSize() != builds.size) mean = new WindowedMean(builds.size);
		for (Building b : builds) {
			HasForce build = (HasForce) b;
			mean.add(build.force().speed);
			if (mean.hasEnoughData()) build.force().speed = mean.mean();
			build.force().speed = Math.min(Math.abs(build.force().speed), build.forceConfig().maxForce) * (build.speed() > 0 ? 1 : -1);
			build.force().speed = Mathf.approachDelta(build.force().speed, 0, build.forceConfig().friction);
		}
	}

	public float getResistance() {
		float resistance = 0.0001f;
		if (modules.isEmpty()) return resistance;
    for (HasForce b : builds.map(b -> (HasForce) b)) if (b != null) resistance += b.forceConfig().friction;
    return resistance/modules.size;
	}
	public float getSpeed() {
		float speed = 0.0001f;
		if (modules.isEmpty()) return speed;
		for (ForceModule b : modules) speed += b.speed;
		return speed/modules.size;
	}

	public static class ForceLink {
		public int l1, l2;

		public ForceLink(HasForce l1, HasForce l2) {
			this(l1.pos(), l2.pos());
		}
		public ForceLink(int l1, int l2) {
			this.l1 = l1;
			this.l2 = l2;
		}

		public HasForce l1() {
			return (HasForce) Vars.world.build(l1);
		}
		public HasForce l2() {
			return (HasForce) Vars.world.build(l2);
		}

		public HasForce other(HasForce build) {
			return build == l1() ? l2() : l1();
		}
		public boolean has(HasForce build) {
			return build == l1() || build == l2();
		}
		public float ratio(HasForce from, boolean reverse) {
			return !reverse ?
				       from.beltSize()/other(from).beltSize():
				       other(from).beltSize()/from.beltSize();
		}

		@Override public boolean equals(Object obj) {
			return obj instanceof ForceLink forceLink && (forceLink.l1 == l1 || forceLink.l1 == l2) && (forceLink.l2 == l2 || forceLink.l2 == l1);
		}
		@Override public String toString() {
			return "link1: " + l1 + "; link2: " + l2;
		}
	}
}
