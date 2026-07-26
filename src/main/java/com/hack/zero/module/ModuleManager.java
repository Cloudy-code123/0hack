package com.hack.zero.module;

import com.hack.zero.module.combat.*;
import com.hack.zero.module.movement.*;
import com.hack.zero.module.player.*;
import com.hack.zero.module.render.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public void init() {
        // Movement
        modules.add(new Jesus());
        modules.add(new Fly());
        modules.add(new Speed());
        modules.add(new BoatFly());
        modules.add(new NoFall());
        modules.add(new Scaffold());
        modules.add(new Velocity());

        // Combat
        modules.add(new KillAura());
        modules.add(new AutoTotem());

        // Player
        modules.add(new AirPlace());

        // Render
        modules.add(new ESP());
        modules.add(new Tracers());
        modules.add(new Fullbright());
        modules.add(new XRay());

        System.out.println("[0hack] ModuleManager: Registered " + modules.size() + " modules");
        for (Module m : modules) {
            System.out.println("[0hack]   " + m.getName() + " [" + m.getCategory() + "]: " + m.getSettings().size() + " settings");
        }
    }

    public List<Module> getModules() {
        return modules;
    }

    public Optional<Module> getModule(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<Module> getModulesByCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }
}
