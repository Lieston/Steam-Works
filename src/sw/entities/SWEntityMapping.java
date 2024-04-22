package sw.entities;

import arc.func.*;
import arc.struct.*;
import arc.struct.ObjectMap.*;
import mindustry.gen.*;
import sw.entities.comp.*;


public class SWEntityMapping {
  public static int customUnits;
  public static ObjectIntMap<Class<? extends Entityc>> idMap = new ObjectIntMap<>();
  public static Entry<Class<? extends Entityc>, Prov<? extends Entityc>>[] entities = new Entry[]{

  };

  private static <T extends Entityc> Entry<Class<T>, Prov<T>> entry(Class<T> name, Prov<T> prov) {
    Entry<Class<T>, Prov<T>> out = new Entry<>();
    out.key = name;
    out.value = prov;
    return out;
  }

  public static void load() {
    for (Entry<Class<? extends Entityc>, Prov<? extends Entityc>> entry : entities) {
      customUnits++;
      idMap.put(entry.key, EntityMapping.register("CustomUnit:" + customUnits, entry.value));
    }
  }
}
