package cymru.asheiou.namespaceUpdater

import net.kyori.adventure.audience.Audience
import net.luckperms.api.LuckPerms
import net.luckperms.api.model.PermissionHolder
import net.luckperms.api.model.group.Group
import net.luckperms.api.model.user.User
import net.luckperms.api.node.Node
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import xyz.aeolia.lib.manager.UserMapManager
import xyz.aeolia.lib.sender.MessageSender
import java.util.UUID


class UpdateNamespaceCommandExecutor (val api: LuckPerms) : CommandExecutor {
  override fun onCommand(
    sender: CommandSender,
    command: Command,
    label: String,
    args: Array<out String>?
  ): Boolean {
    val gm = api.groupManager
    val um = api.userManager
    gm.loadAllGroups().thenAcceptAsync {
      val groups = gm.loadedGroups
      MessageSender.sendMessage(sender, "Loaded ${groups.size} groups")
      val users = mutableListOf<User>()
      UserMapManager.getUserMap().forEach { user ->
        users.add(um.loadUser(UUID.fromString(user.value)).join())
      }
      MessageSender.sendMessage(sender, "Loaded ${users.size} users")

      groups.forEach { group ->
        MessageSender.sendMessage(sender, "Processing group ${group.name}")
        scanAndReplace(group, sender)
      }

      users.forEach { user ->
        MessageSender.sendMessage(sender, "Processing user ${user.username}")
        scanAndReplace(user, sender)
      }
    }.exceptionally { throwable ->
      throwable.printStackTrace()
      MessageSender.sendMessage(sender, throwable.message!!)
      return@exceptionally null
    }
    return true
  }

  fun scanAndReplace(holder: PermissionHolder, audience: Audience) {
    val listOfChanged = mutableListOf<String>()
    holder.nodes.forEach { node ->
      if (node.key.startsWith("ashutils.")) {
        listOfChanged.add(node.key)
        val topLevelStripped = node.key.substringAfter("ashutils.")
        val newKey = "lib.$topLevelStripped"
        holder.data().remove(node)
        holder.data().add(Node.builder(newKey)
          .value(node.value)
          .context(node.contexts)
          .negated(node.isNegated)
          .build())
        when (holder) {
          is User -> api.userManager.saveUser(holder)
          is Group -> api.groupManager.saveGroup(holder)
        }
      }
    }
    MessageSender.sendMessage(audience, "Found and replaced from ${holder.friendlyName}: " +
            listOfChanged.joinToString(" ")
    )
  }
}