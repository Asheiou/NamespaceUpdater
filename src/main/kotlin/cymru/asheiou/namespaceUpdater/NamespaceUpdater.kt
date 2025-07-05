package cymru.asheiou.namespaceUpdater

import net.luckperms.api.LuckPermsProvider
import org.bukkit.plugin.java.JavaPlugin

class NamespaceUpdater : JavaPlugin() {

  override fun onEnable() {
    getCommand("updatenamespace")?.setExecutor(UpdateNamespaceCommandExecutor(
      LuckPermsProvider.get()
    ))
    logger.info("Loaded!")
  }

  override fun onDisable() {
    logger.info("ttyl")
  }
}
