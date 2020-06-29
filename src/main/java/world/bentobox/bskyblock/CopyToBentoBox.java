package world.bentobox.bskyblock;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.Files;

import world.bentobox.bentobox.api.events.BentoBoxReadyEvent;

public class CopyToBentoBox extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onBentoBoxReady(BentoBoxReadyEvent e) {
        getLogger().severe("BSkyBlock.jar 必须在 BentoBox/addons 文件夹里! 正在尝试移动...");
        File addons = new File(getFile().getParent(), "BentoBox/addons");
        if (addons.exists()) {
            File to = new File(addons, getFile().getName());
            if (!to.exists()) {

                try {
                    Files.move(getFile(), to);
                    getLogger().severe(getFile().getName() + " 移动成功. 重启服务器以激活扩展!");

                } catch (IOException ex) {
                    getLogger().severe("移动失败. " + ex.getMessage());
                    getLogger().severe("请自行移动 " + getFile().getName() + " 到 BentoBox/addons 文件夹内. 然后重启服务器.");
                }
            }
        } else {
            getLogger().severe("BentoBox 文件夹不存在! 你看 Wiki 了吗" + addons.getAbsolutePath());
        }

    }
}
