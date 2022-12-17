package cc.carm.plugin.minesql;

import cc.carm.plugin.minesql.conf.PluginConfiguration;
import co.aikar.commands.BungeeCommandManager;
import co.aikar.commands.CommandManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

public class MineSQLBungee extends Plugin implements MineSQLPlatform {

    protected static MineSQLBungee instance;

    protected MineSQLCore core;
    protected BungeeCommandManager commandManager;

    @Override
    public void onLoad() {
        MineSQLBungee.instance = this;

        getLogger().info("加载基础核心...");
        this.core = new MineSQLCore(this);

        getLogger().info("初始化MineSQL API...");
        MineSQL.initializeAPI(this.core);
    }

    @Override
    public void onEnable() {
        getLogger().info("初始化指令管理器...");
        this.commandManager = new BungeeCommandManager(this);

        getLogger().info("注册相关指令...");
        this.core.initializeCommands(getCommandManager());

        if (getConfiguration().METRICS.getNotNull()) {
            getLogger().info("启用统计数据...");
            Metrics metrics = new Metrics(this, 14076);
            metrics.addCustomChart(new SimplePie("update_check",
                    () -> getConfiguration().UPDATE_CHECKER.getNotNull() ? "ENABLED" : "DISABLED")
            );
            metrics.addCustomChart(new SimplePie("properties_configuration",
                    () -> getConfiguration().PROPERTIES.ENABLE.getNotNull() ? "ENABLED" : "DISABLED")
            );
        }

        if (getConfiguration().PROPERTIES.ENABLE.getNotNull()) {
            getLogger().info("开始检查更新，可能需要一小段时间...");
            getLogger().info("   如不希望检查更新，可在配置文件中关闭。");
            ProxyServer.getInstance().getScheduler().runAsync(
                    this, () -> this.core.checkUpdate(getDescription().getVersion())
            );
        } else {
            getLogger().info("已禁用检查更新，跳过。");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("终止全部数据库连接...");
        this.core.getRegistry().shutdownAll();
    }

    public static MineSQLBungee getInstance() {
        return instance;
    }

    @Override
    public @NotNull Logger getLogger() {
        return super.getLogger();
    }

    public @NotNull PluginConfiguration getConfiguration() {
        return this.core.getConfig();
    }

    @Override
    public @NotNull File getPluginFolder() {
        return getDataFolder();
    }

    @Override
    public @NotNull CommandManager<?, ?, ?, ?, ?, ?> getCommandManager() {
        return this.commandManager;
    }


}