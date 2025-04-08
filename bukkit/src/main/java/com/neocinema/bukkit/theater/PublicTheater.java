package com.neocinema.bukkit.theater;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.theater.screen.Screen;

public class PublicTheater extends Theater {

    public PublicTheater(NeoCinemaPlugin neoCinemaPlugin, String id, String name, boolean hidden, Screen screen) {
        super(neoCinemaPlugin, id, name, hidden, screen);
    }

}
