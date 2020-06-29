package world.bentobox.bskyblock;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.admin.DefaultAdminCommand;
import world.bentobox.bentobox.api.commands.island.DefaultPlayerCommand;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.bskyblock.commands.IslandAboutCommand;
import world.bentobox.bskyblock.generators.ChunkGeneratorWorld;

/**
 * BSkyBlock 主类 - 空岛游戏扩展
 * @author tastybento
 * @author Poslovitch
 */
public class BSkyBlock extends GameModeAddon implements Listener {

    private static final String NETHER = "_nether";
    private static final String THE_END = "_the_end";

    // 设置
    private Settings settings;
    private ChunkGeneratorWorld chunkGenerator;
    private Config<Settings> configObject = new Config<>(this, Settings.class);

    @Override
    public void onLoad() {
        // 输出默认配置文件
        saveDefaultConfig();
        // 从 config.yml 中读取设置. 同时也将检查配置文件错误.
        loadSettings();
        // 区块生成器
        chunkGenerator = settings.isUseOwnGenerator() ? null : new ChunkGeneratorWorld(this);
        // 声明指令
        playerCommand = new DefaultPlayerCommand(this)

        {
            @Override
            public void setup()
            {
                super.setup();
                new IslandAboutCommand(this);
            }
        };
        adminCommand = new DefaultAdminCommand(this) {};
    }

    private boolean loadSettings() {
        // Load settings again to get worlds
        settings = configObject.loadConfigObject();
        if (settings == null) {
            // Disable
            logError("BSkyBlock 设置无法读取! 扩展已禁用.");
            setState(State.DISABLED);
            return false;
        }
        return true;
    }

    @Override
    public void onEnable(){
        // Register this
        registerListener(this);
    }

    @Override
    public void onDisable() {
        // Nothing to do here
    }

    @Override
    public void onReload() {
        if (loadSettings()) {
            log("重载成功");
        }
    }

    /**
     * @return the settings
     */
    public Settings getSettings() {
        return settings;
    }

    @Override
    public void createWorlds() {
        String worldName = settings.getWorldName().toLowerCase();
        if (getServer().getWorld(worldName) == null) {
            log("生成空岛世界中 ...");
        }

        // Create the world if it does not exist
        islandWorld = getWorld(worldName, World.Environment.NORMAL, chunkGenerator);
        // Make the nether if it does not exist
        if (settings.isNetherGenerate()) {
            if (getServer().getWorld(worldName + NETHER) == null) {
                log("生成空岛下界中...");
            }
            netherWorld = settings.isNetherIslands() ? getWorld(worldName, World.Environment.NETHER, chunkGenerator) : getWorld(worldName, World.Environment.NETHER, null);
        }
        // Make the end if it does not exist
        if (settings.isEndGenerate()) {
            if (getServer().getWorld(worldName + THE_END) == null) {
                log("生成空岛末地中...");
            }
            endWorld = settings.isEndIslands() ? getWorld(worldName, World.Environment.THE_END, chunkGenerator) : getWorld(worldName, World.Environment.THE_END, null);
        }
    }

    /**
     * Gets a world or generates a new world if it does not exist
     * @param worldName2 - the overworld name
     * @param env - the environment
     * @param chunkGenerator2 - the chunk generator. If <tt>null</tt> then the generator will not be specified
     * @return world loaded or generated
     */
    private World getWorld(String worldName2, Environment env, ChunkGeneratorWorld chunkGenerator2) {
        // Set world name
        worldName2 = env.equals(World.Environment.NETHER) ? worldName2 + NETHER : worldName2;
        worldName2 = env.equals(World.Environment.THE_END) ? worldName2 + THE_END : worldName2;
        WorldCreator wc = WorldCreator.name(worldName2).type(WorldType.FLAT).environment(env);
        World w = settings.isUseOwnGenerator() ? wc.createWorld() : wc.generator(chunkGenerator2).createWorld();
        // Set spawn rates
        if (w != null) {
            w.setMonsterSpawnLimit(getSettings().getSpawnLimitMonsters());
            w.setAmbientSpawnLimit(getSettings().getSpawnLimitAmbient());
            w.setAnimalSpawnLimit(getSettings().getSpawnLimitAnimals());
            w.setWaterAnimalSpawnLimit(getSettings().getSpawnLimitWaterAnimals());
            w.setTicksPerAnimalSpawns(getSettings().getTicksPerAnimalSpawns());
            w.setTicksPerMonsterSpawns(getSettings().getTicksPerMonsterSpawns());
        }
        return w;

    }

    @Override
    public WorldSettings getWorldSettings() {
        return getSettings();
    }

    @Override
    public @Nullable ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return chunkGenerator;
    }

    @Override
    public void saveWorldSettings() {
        if (settings != null) {
            configObject.saveConfigObject(settings);
        }
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.addons.Addon#allLoaded()
     */
    @Override
    public void allLoaded() {
        // Reload settings and save them. This will occur after all addons have loaded
        this.loadSettings();
        this.saveWorldSettings();
    }

}
