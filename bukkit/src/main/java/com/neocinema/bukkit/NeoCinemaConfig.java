package com.neocinema.bukkit;

public class NeoCinemaConfig {

    public String youtubeDataApiKey = "abc123";
    public boolean enableTabTheaterList = true;

    /*
     * Storage
     */
    public boolean useMysql = false;
    public String mysqlHost = "localhost";
    public int mysqlPort = 3306;
    public String mysqlDatabase = "neocinema";
    public String mysqlUsername = "root";
    public String mysqlPassword = "password";
    public boolean useSqlite = true;

    public boolean autogenCubicRegions = false;

}
