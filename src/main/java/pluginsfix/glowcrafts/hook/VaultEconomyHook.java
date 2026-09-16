package pluginsfix.glowcrafts.hook;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class VaultEconomyHook {

    private final Economy economy;

    private VaultEconomyHook(Economy economy) {
        this.economy = economy;
    }

    public static VaultEconomyHook create() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return new VaultEconomyHook(null);
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return new VaultEconomyHook(null);
        }

        return new VaultEconomyHook(rsp.getProvider());
    }

    public boolean isAvailable() {
        return economy != null;
    }

    public boolean has(OfflinePlayer player, double amount) {
        if (economy == null || amount <= 0) {
            return true;
        }
        return economy.has(player, amount);
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (economy == null || amount <= 0) {
            return true;
        }
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }
}
