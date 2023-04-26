package sw.world.graph;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import sw.entities.comp.*;
import sw.world.interfaces.*;
import sw.world.modules.*;

public class ForceGraph {
	public final Seq<Building> builds = new Seq<>(false, 16, Building.class);
	public final Seq<ForceModule> modules = new Seq<>(false, 16, ForceModule.class);

	public final @Nullable ForceGraphUpdater entity;

	public float rotation = 0;

	private final int id;
	private static int lastId;

	public ForceGraph() {
		id = lastId++;
		entity = new ForceGraphUpdater();
		entity.graph = this;
		hasEntity();
	}

	public void hasEntity() {
		if (entity != null) entity.add();
	}

	public int getId() {
		return id;
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
					out.addUnique(i.l1);
					out.addUnique(i.l2);
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
		hasEntity();
	}
	public void removeBuild(Building build) {
		if (builds.remove(build)) modules.remove(((HasForce) build).force());
	}
	public void remove(Building build) {
		ForceGraph graph = new ForceGraph();
		graph.hasEntity();
		graph.add(build);
		graph.entity.update();
	}

	public void update() {
		rotation += Mathf.maxZero(getSpeed() - getResistance()) * Time.delta;
	}

	public float getResistance() {
		float resistance = 0f;
    for (Building b : builds) {
    	if (b instanceof HasForce a) {
	 	  	resistance += a.forceConfig().baseResistance;
 	    	resistance += a.forceConfig().resistanceScl/getSpeed();
 	    }
    }
    return resistance;
	}
	public float getSpeed() {
		float speed = 0.0001f;
		for (ForceModule b : modules) speed += b.speed;
		return speed;
	}
}