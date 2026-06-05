package me.abood8001.mobtag.display;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PacketDisplay implements TagDisplay {

    private final int entityId;
    private final UUID uuid;
    private final int mobEntityId;
    private final float heightOffset;
    private Location location;
    private String text;
    private boolean dead = false;

    // Track which players currently see this display
    private final Set<UUID> viewers = new HashSet<>();

    public PacketDisplay(Location loc, String text, int mobEntityId, float heightOffset) {
        this.entityId = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE / 2, Integer.MAX_VALUE);
        this.uuid = UUID.randomUUID();
        this.location = loc;
        this.text = text;
        this.mobEntityId = mobEntityId;
        this.heightOffset = heightOffset;
    }

    @Override
    public void update(Location newLoc, String newText, Player viewer) {
        if (dead || viewer == null) return;
        this.location = newLoc;

        UUID viewerUUID = viewer.getUniqueId();

        if (!viewers.contains(viewerUUID)) {
            // First time this viewer sees the tag — spawn it
            spawnFor(viewer);
            viewers.add(viewerUUID);
        }

        // Update text if changed
        if (!newText.equals(this.text)) {
            this.text = newText;
            sendMetaTo(viewer);
        }
    }

    private void spawnFor(Player p) {
        var user = PacketEvents.getAPI().getPlayerManager().getUser(p);
        if (user == null) return;

        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                entityId,
                Optional.of(uuid),
                EntityTypes.TEXT_DISPLAY,
                new Vector3d(location.getX(), location.getY(), location.getZ()),
                0f, 0f, 0f, 0,
                Optional.empty()
        );

        WrapperPlayServerSetPassengers passengersPacket = new WrapperPlayServerSetPassengers(
                mobEntityId, new int[]{entityId}
        );

        user.sendPacket(spawnPacket);
        user.sendPacket(buildMetaPacket());
        user.sendPacket(passengersPacket);
    }

    private void sendMetaTo(Player p) {
        var user = PacketEvents.getAPI().getPlayerManager().getUser(p);
        if (user != null) {
            user.sendPacket(buildMetaPacket());
        }
    }

    public void removeFor(Player p) {
        if (p == null) return;
        if (!viewers.remove(p.getUniqueId())) return;
        var user = PacketEvents.getAPI().getPlayerManager().getUser(p);
        if (user != null) {
            user.sendPacket(new WrapperPlayServerDestroyEntities(entityId));
        }
    }

    @Override
    public void remove() {
        if (dead) return;
        dead = true;
        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(entityId);
        for (UUID viewerId : viewers) {
            Player p = org.bukkit.Bukkit.getPlayer(viewerId);
            if (p != null && p.isOnline()) {
                var user = PacketEvents.getAPI().getPlayerManager().getUser(p);
                if (user != null) {
                    user.sendPacketSilently(destroyPacket);
                }
            }
        }
        viewers.clear();
    }

    @Override
    public boolean isDead() {
        return dead;
    }

    public Set<UUID> getViewers() {
        return viewers;
    }

    private WrapperPlayServerEntityMetadata buildMetaPacket() {
        net.kyori.adventure.text.Component component = LegacyComponentSerializer.legacySection().deserialize(text);

        List<EntityData<?>> data = List.of(
                new EntityData<>(11, EntityDataTypes.VECTOR3F, new Vector3f(0f, heightOffset, 0f)),
                new EntityData<>(15, EntityDataTypes.BYTE, (byte) 3),
                new EntityData<>(23, EntityDataTypes.ADV_COMPONENT, component),
                new EntityData<>(27, EntityDataTypes.BYTE, (byte) 1)
        );
        return new WrapperPlayServerEntityMetadata(entityId, data);
    }
}