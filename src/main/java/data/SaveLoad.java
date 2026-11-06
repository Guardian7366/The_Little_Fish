package data;

import data.SaveSnapshot.Actor;
import data.SaveSnapshot.Item;
import data.SaveSnapshot.World;
import entity.Entity;
import entity.NPC_BigRock;
import entity.NPC_TV;
import entity.Player;
import entity.PlayerDummy;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import main.EventHandler;
import main.GamePanel;
import main.ExtraRooms;
import main.RefugeChallenges;
import main.FinalRefuges;
import main.FirstZone;
import main.SecondZone;
import main.ThirdZone;
import main.FourthZone;
import main.FifthZone;
import main.Market;
import main.FinalArena;
import object.OBJ_Door;
import object.OBJ_DoorIron;
import object.OBJ_Key;
import main.Temple;
import monster.MON_GodFish;
import object.OBJ_Chest;
import tile_interactive.IT_MetalPlate;
import tile_interactive.InteractiveTile;

/**
 * Checkpoint persistence. Reading, validation and construction finish before live state changes.
 * Versioned files coexist with the original save.dat, which is only ever read.
 */
public final class SaveLoad {
    private static final int PRE_MARKET_MAPS = 10, MARKET_REVISION = 1;
    private final GamePanel gp;
    private final Path file;
    private final Path legacyFile;
    private final WorldEntityRegistry registry = new WorldEntityRegistry();
    private World initialWorld;
    private String lastError = "";
    private String lastWarning = "";

    public SaveLoad(GamePanel gp) { this(gp, Path.of("save-v1.dat")); }

    /** Alternate destination is useful for isolated tests and portable profiles. */
    public SaveLoad(GamePanel gp, Path file) {
        this.gp = gp;
        this.file = file.toAbsolutePath().normalize();
        this.legacyFile = this.file.resolveSibling("save.dat");
        if (this.file.equals(legacyFile))
            throw new IllegalArgumentException("The original save.dat is reserved for import");
    }

    public String getLastError() { return lastError; }
    public String getLastWarning() { return lastWarning; }
    public Path getSavePath() { return file; }

    /** Call after the original world has been placed, before the first player action. */
    public void captureInitialWorld() { initialWorld = captureWorld(); }

    public void save() { trySave(); }
    public void load() { tryLoad(); }

    public synchronized boolean trySave() {
        lastError = "";
        lastWarning = "";
        Path temporary = null;
        try {
            require(gp.gameState!=gp.cutsceneState && gp.gameState!=gp.transitionState && !gp.bossBattleOn,
                "Finish the encounter or transition before saving");
            SaveSnapshot snapshot = capture();
            prepare(snapshot); // Reject invalid state before touching the last good checkpoint.
            Files.createDirectories(file.getParent());
            temporary = Files.createTempFile(file.getParent(), "fish-save-", ".tmp");
            try (FileOutputStream bytes = new FileOutputStream(temporary.toFile());
                 ObjectOutputStream out = new ObjectOutputStream(bytes)) {
                out.writeObject(snapshot);
                out.flush();
                bytes.getFD().sync();
            }
            backupPreviousCheckpoint();
            replace(temporary, file);
            return true;
        } catch (Exception error) {
            return fail("No se pudo guardar la partida", error);
        } finally {
            if (temporary != null) try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
        }
    }

    public synchronized boolean tryLoad() {
        lastError = "";
        lastWarning = "";
        try {
            Path source = Files.exists(file) ? file : legacyFile;
            Object stored = read(source);
            SaveSnapshot snapshot;
            if (stored instanceof SaveSnapshot current) {
                snapshot = current;
            } else if (stored instanceof DataStorage legacy) {
                snapshot = importLegacy(legacy);
                lastWarning = "Partida original importada. Ese formato no guardaba la zona ni los "
                    + "rompecabezas: se reanuda al inicio con tus objetos y progreso recuperable.";
            } else {
                throw new IOException("Unknown save format");
            }
            Prepared prepared = prepare(snapshot);
            apply(snapshot, prepared);
            if (snapshot.firstZoneRevision == 0 && snapshot.map == 0 && lastWarning.isEmpty())
                lastWarning = "Arrecife renovado: vuelves a la esmeralda con tu progreso conservado.";
            if (snapshot.secondZoneRevision == 0 && snapshot.map == 1)
                lastWarning = "Segunda zona renovada: vuelves a la esmeralda con tu progreso conservado.";
            if (!lastWarning.isEmpty()) gp.ui.addMessage(lastWarning);
            return true;
        } catch (Exception error) {
            return fail("No se pudo cargar la partida", error);
        }
    }

    private boolean fail(String context, Exception error) {
        lastError = context + ": " + (error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
        System.err.println(lastError);
        if (gp.ui != null) gp.ui.addMessage(context + ". La partida actual se conserva.");
        return false;
    }

    private static void replace(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException unsupported) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void backupPreviousCheckpoint() throws IOException {
        if (!Files.exists(file)) return;
        try {
            Object previous = read(file);
            if (!(previous instanceof SaveSnapshot snapshot)) return;
            prepare(snapshot);
        } catch (IOException | ClassNotFoundException | IllegalArgumentException invalid) {
            // A damaged current file must not replace a previously usable backup.
            return;
        }
        Path backupTemporary = Files.createTempFile(file.getParent(), "fish-backup-", ".tmp");
        try {
            Files.copy(file, backupTemporary, StandardCopyOption.REPLACE_EXISTING);
            replace(backupTemporary, file.resolveSibling(file.getFileName() + ".bak"));
        } finally {
            Files.deleteIfExists(backupTemporary);
        }
    }

    private Object read(Path source) throws IOException, ClassNotFoundException {
        if (Files.size(source) > 5_000_000) throw new IOException("Save exceeds the supported size");
        // The underlying stream must close even if the serialization header is corrupt.
        try (InputStream bytes = Files.newInputStream(source);
             ObjectInputStream in = new ObjectInputStream(bytes)) {
            in.setObjectInputFilter(info -> {
                if (info.depth() > 20 || info.references() > 30_000 || info.arrayLength() > 3_000)
                    return ObjectInputFilter.Status.REJECTED;
                Class<?> type = info.serialClass();
                if (type == null) return ObjectInputFilter.Status.UNDECIDED;
                while (type.isArray()) type = type.getComponentType();
                if (type.isPrimitive() || type == String.class || type == Object.class ||
                    type == Integer.class || type == Number.class || type == ArrayList.class ||
                    type == DataStorage.class || type == SaveSnapshot.class ||
                    type == World.class || type == Actor.class || type == Item.class)
                    return ObjectInputFilter.Status.ALLOWED;
                return ObjectInputFilter.Status.REJECTED;
            });
            return in.readObject();
        }
    }

    private SaveSnapshot capture() {
        Player player = gp.player;
        SaveSnapshot s = new SaveSnapshot();
        s.marketRevision = MARKET_REVISION;
        s.arenaRevision = FinalArena.REVISION;
        s.firstZoneRevision = FirstZone.REVISION;
        s.secondZoneRevision = SecondZone.REVISION;
        s.thirdZoneRevision = ThirdZone.REVISION;
        s.fourthZoneRevision = FourthZone.REVISION;
        s.fifthZoneRevision = FifthZone.REVISION;
        s.sixthZoneRevision = main.SixthZone.REVISION;
        s.templeRevision = main.Temple.REVISION;
        s.extraRoomsRevision = ExtraRooms.REVISION;
        s.extraRoomsFlags = gp.extraRooms.flags();
        var later=gp.extraRooms.later.state();
        s.pumpBindings=later.bindings(); s.honguiBlocks=later.blocks();
        s.honguiDefeated=later.defeated(); s.ghostPrefix=later.prefix();
        var ending=gp.extraRooms.ending.state();
        s.handyLatches=ending.latches(); s.handyDamaged=ending.damaged();
        s.icMarks=ending.marks(); s.handySentryDefeated=ending.sentryDefeated();
        s.templeExtensionRevision = main.TempleExtension.REVISION;
        s.templeReliefs = gp.temple.reliefs;
        s.templeDefeats = gp.temple.defeats;
        s.map = gp.currentMap; s.area = gp.currentArea;
        s.x = player.worldX; s.y = player.worldY; s.direction = player.direction;
        s.level = player.level; s.maxLife = player.maxLife; s.life = player.life;
        s.maxWater = player.maxWater; s.water = player.water;
        s.strength = player.strength; s.dexterity = player.dexterity;
        s.exp = player.exp; s.nextLevelExp = player.nextLevelExp; s.coin = player.coin;
        s.inventory = captureItems(player.inventory);
        s.weaponSlot = player.inventory.indexOf(player.currentWeapon);
        s.shieldSlot = player.inventory.indexOf(player.currentShield);
        s.lightSlot = player.currentLight == null ? -1 : player.inventory.indexOf(player.currentLight);
        if (player.currentLight != null && s.lightSlot == -1)
            throw new IllegalArgumentException("Equipped lantern is not in the inventory");
        s.godFishDefeated = Progress.GodFishDefeated; s.bossBattleOn = gp.bossBattleOn;
        s.world = captureWorld();
        return s;
    }

    private World captureWorld() {
        World world = new World();
        world.objects = captureActors(gp.obj, "object");
        world.npcs = captureActors(gp.npc, "npc");
        world.monsters = captureActors(gp.monster, "monster");
        world.tiles = captureActors(gp.iTile, "tile");
        return world;
    }

    private Actor[][] captureActors(Entity[][] actors, String category) {
        Actor[][] result = new Actor[actors.length][];
        for (int map = 0; map < actors.length; map++) {
            result[map] = new Actor[actors[map].length];
            for (int slot = 0; slot < actors[map].length; slot++) {
                Entity entity = actors[map][slot];
                if (entity == null) continue;
                if (entity instanceof PlayerDummy)
                    throw new IllegalArgumentException("Finish the cinematic before saving");
                Actor a = new Actor();
                a.id = category.equals("object") ? gp.eGenerator.idOf(entity) : registry.idOf(entity);
                a.x = entity.worldX; a.y = entity.worldY; a.direction = entity.direction;
                a.life = Math.max(0, entity.life); a.amount = entity.amount;
                a.alive = entity.alive; a.dying = entity.dying;
                a.opened = entity.opened; a.temp = entity.temp; a.sleep = entity.sleep;
                a.inRage = entity.inRage; a.onPath = entity.onPath;
                a.dialogueSet = entity.dialogueSet; a.dialogueIndex = entity.dialogueIndex;
                a.loot = entity.loot == null ? null : captureItem(entity.loot);
                a.inventory = captureItems(entity.inventory);
                if (entity.linkedEntity != null) {
                    a.linkedTile = Arrays.asList(gp.iTile[map]).indexOf(entity.linkedEntity);
                    require(a.linkedTile >= 0, "Linked plate is absent from the world");
                }
                result[map][slot] = a;
            }
        }
        return result;
    }

    private Item captureItem(Entity entity) {
        Item item = new Item();
        item.id = gp.eGenerator.idOf(entity); item.amount = entity.amount;
        return item;
    }

    private Item[] captureItems(List<Entity> entities) {
        Item[] items = new Item[entities.size()];
        for (int i = 0; i < items.length; i++) items[i] = captureItem(entities.get(i));
        return items;
    }

    private Prepared prepare(SaveSnapshot s) {
        require(s.version == SaveSnapshot.CURRENT_VERSION, "Unsupported save version: " + s.version);
        require(s.firstZoneRevision >= 0 && s.firstZoneRevision <= FirstZone.REVISION,
            "Unsupported first-zone layout");
        require(s.secondZoneRevision >= 0 && s.secondZoneRevision <= SecondZone.REVISION, "Unsupported second-zone layout");
        require(s.marketRevision == 0 || s.marketRevision == MARKET_REVISION, "Unsupported market layout");
        require(gp.tileM.isMapLoaded(s.map) && (s.marketRevision != 0 || s.map < PRE_MARKET_MAPS), "Invalid map");
        require(s.extraRoomsRevision>=0 && s.extraRoomsRevision<=ExtraRooms.REVISION &&
            (s.extraRoomsFlags & ~ExtraRooms.ALL_FLAGS)==0,"Unsupported refuge state");
        require(s.fourthZoneRevision>=0 && s.fourthZoneRevision<=FourthZone.REVISION,"Unsupported fourth-zone layout");
        require(s.templeRevision >= 0 && s.templeRevision <= main.Temple.REVISION, "Unsupported temple layout");
        require(s.templeExtensionRevision >= 0 && s.templeExtensionRevision <= main.TempleExtension.REVISION,
            "Unsupported temple extension layout");
        require(s.map!=main.TempleExtension.MAP || s.templeExtensionRevision>0,"Missing temple extension state");
        require((s.templeReliefs & ~3) == 0 && (s.templeDefeats & ~3) == 0, "Invalid temple flags");
        require(s.sixthZoneRevision >= 0 && s.sixthZoneRevision <= main.SixthZone.REVISION, "Unsupported sixth-zone layout");
        require(s.fifthZoneRevision>=0 && s.fifthZoneRevision<=FifthZone.REVISION,"Unsupported fifth-zone layout");
        require(s.area == gp.outside || s.area == gp.indoor || s.area == gp.dungeons, "Invalid area");
        coordinates(s.x, s.y); direction(s.direction);
        require(s.level > 0 && s.level <= 1_000_000 && s.strength > 0 && s.dexterity > 0,
            "Invalid player attributes");
        require(s.maxLife > 0 && s.life >= 0 && s.life <= s.maxLife &&
            s.maxWater >= 0 && s.water >= 0 && s.water <= s.maxWater, "Invalid life or water");
        require(s.exp >= 0 && s.nextLevelExp > 0 && s.coin >= 0, "Invalid progression");
        require(s.arenaRevision==0 || s.arenaRevision==FinalArena.REVISION, "Unsupported final arena layout");
        require(!s.bossBattleOn || (s.arenaRevision==0 && s.map==FinalArena.MAP && !s.godFishDefeated),
            "Invalid active boss checkpoint");
        Player player = new Player(gp, gp.keyH);
        player.inventory = prepareItems(s.inventory);
        require(player.inventory.size() <= player.maxinventorySize, "Inventory is too large");
        player.currentWeapon = slot(player.inventory, s.weaponSlot);
        player.currentShield = slot(player.inventory, s.shieldSlot);
        require(player.currentWeapon.type == player.type_bubble || player.currentWeapon.type == player.type_rock ||
            player.currentWeapon.type == player.type_iron, "Invalid equipped weapon");
        require(player.currentShield.type == player.type_shield, "Invalid equipped shield");
        player.currentLight = s.lightSlot == -1 ? null : slot(player.inventory, s.lightSlot);
        require(player.currentLight == null || player.currentLight.type == player.type_light, "Invalid equipped lantern");
        player.worldX = s.x; player.worldY = s.y; player.direction = s.direction;
        player.level = s.level; player.maxLife = s.maxLife; player.life = s.life;
        player.maxWater = s.maxWater; player.water = s.water;
        player.strength = s.strength; player.dexterity = s.dexterity;
        player.exp = s.exp; player.nextLevelExp = s.nextLevelExp; player.coin = s.coin;
        player.getAttack(); player.getDefense(); player.getAttackImage(); player.setDialogue();
        player.lightUpdated = true;
        require(s.world != null, "Missing world");
        World world = s.marketRevision == 0 ? expandPreMarketWorld(s.world) : s.world;
        Entity[][] objects = prepareActors(world.objects, gp.obj, "object");
        Entity[][] npcs = prepareActors(world.npcs, gp.npc, "npc");
        Entity[][] monsters = prepareActors(world.monsters, gp.monster, "monster");
        Entity[][] restoredTiles = prepareActors(world.tiles, gp.iTile, "tile");
        InteractiveTile[][] tiles = new InteractiveTile[gp.maxMap][gp.iTile[0].length];
        for (int map = 0; map < tiles.length; map++)
            for (int slot = 0; slot < tiles[map].length; slot++)
                tiles[map][slot] = (InteractiveTile)restoredTiles[map][slot];
        for (int map = 0; map < npcs.length; map++) {
            for (int slot = 0; slot < npcs[map].length; slot++) {
                Actor actor = world.npcs[map][slot];
                if (actor == null || actor.linkedTile == -1) continue;
                require(npcs[map][slot] instanceof NPC_BigRock && actor.linkedTile >= 0 &&
                    actor.linkedTile < tiles[map].length && tiles[map][actor.linkedTile] instanceof IT_MetalPlate,
                    "Invalid rock/plate link");
                Entity rock = npcs[map][slot], plate = tiles[map][actor.linkedTile];
                require(Math.abs(rock.worldX - plate.worldX) < 8 && Math.abs(rock.worldY - plate.worldY) < 8,
                    "Rock is not on its linked plate");
                rock.linkedEntity = plate;
            }
            for (Entity monster : monsters[map])
                require(!s.godFishDefeated || !(monster instanceof MON_GodFish), "Defeated boss is still present");
        }
        prepareMarketTV(npcs, s.marketRevision == 0);
        prepareArena(s,player,objects,monsters);
        if (s.map == Market.MAP) {
            var body = Temple.bounds(player, player.worldX, player.worldY);
            Entity tv = npcs[Market.MAP][Market.TV_SLOT];
            require(s.area == gp.indoor && !s.bossBattleOn && Temple.terrainClear(gp, Market.MAP, body)
                && !body.intersects(Temple.bounds(tv, tv.worldX, tv.worldY)), "Invalid market position");
        }
        FirstZone.adaptSavedActors(gp, player, s.map, npcs, monsters, objects, s.firstZoneRevision == 0);
        SecondZone.adapt(gp, player, s.map, monsters, objects, s.secondZoneRevision == 0);
        require(s.thirdZoneRevision>=0 && s.thirdZoneRevision<=ThirdZone.REVISION,"Unsupported third-zone layout");
        ThirdZone.adapt(gp,player,s.map,monsters,objects,tiles,s.thirdZoneRevision==0);
        FourthZone.adapt(gp,monsters,tiles,s.fourthZoneRevision==0);
        FifthZone.adapt(gp,player,s.map,monsters,objects,tiles,s.fifthZoneRevision);
        if (s.sixthZoneRevision == 0) {
            main.SixthZone.populate(gp, monsters);
            main.SixthZone.obstacles(gp, tiles);
            main.SixthZone.reward(gp, objects);
            FifthZone.restoreIronReward(player, objects);
        } else {
            main.SixthZone.configure(gp, monsters, false);
        }
        int defeats = s.templeDefeats;
        int reliefs = s.templeReliefs;
        if (s.templeRevision == 0) {
            defeats = 0;
            reliefs = 0;
            for (int i=0; i<main.Temple.SECRET_SLOTS.length; i++) {
                Entity secret = monsters[5][main.Temple.SECRET_SLOTS[i]];
                if (secret == null || !secret.alive || secret.dying || secret.life == 0) defeats |= 1<<i;
            }
            boolean oldGateOpen = !(objects[4][0] instanceof object.OBJ_DoorIron) || s.map == 6 || s.godFishDefeated;
            boolean[] oldPlates = new boolean[4];
            for (int i=0; i<4; i++) {
                Entity plate = tiles[4][main.Temple.LANES[i].plateSlot()];
                if (plate != null) for (Entity npc : npcs[4])
                    if (npc instanceof NPC_BigRock && npc.linkedEntity == plate) oldPlates[i] = true;
            }
            main.Temple.fixtures(gp,npcs,tiles);
            for (int i=0; i<4; i++) {
                if (!oldGateOpen && !oldPlates[i]) continue;
                var lane = main.Temple.LANES[i];
                Entity rock = main.Temple.place(npcs[4][lane.rockSlot()],lane.endX(),lane.endY());
                rock.linkedEntity = tiles[4][lane.plateSlot()];
            }
            main.Temple.monsters(gp,monsters,defeats);
        } else {
            for (var lane : main.Temple.LANES) {
                Entity rock = npcs[4][lane.rockSlot()], plate = tiles[4][lane.plateSlot()];
                require(rock instanceof NPC_BigRock && main.Temple.onLane(rock,lane) && plate instanceof IT_MetalPlate &&
                    plate.worldX==lane.endX()*48 && plate.worldY==lane.endY()*48, "Invalid temple lane");
                boolean atEnd = rock.worldX==plate.worldX && rock.worldY==plate.worldY;
                require(atEnd ? rock.linkedEntity==plate : rock.linkedEntity==null, "Invalid temple pair");
            }
            for (int i=0; i<main.Temple.SECRET_SLOTS.length; i++) {
                int slot=main.Temple.SECRET_SLOTS[i];
                monsters[5][slot]=null;
                if ((defeats & (1<<i))!=0) monsters[4][slot]=null;
            }
            for (Entity enemy : monsters[4])
                if (enemy instanceof monster.MON_Tella)
                    require(enemy.life<=monster.MON_Tella.ABYSS_MAX_LIFE, "Invalid temple Fishtella health");
            main.Temple.configure(gp,monsters);
        }
        boolean open=main.Temple.solved(npcs,tiles);
        if (open) {
            if (objects[4][0] instanceof object.OBJ_DoorIron) objects[4][0]=null;
        } else {
            require(objects[4][0]==null || objects[4][0] instanceof object.OBJ_DoorIron, "Temple gate slot occupied");
            objects[4][0]=main.Temple.gate(gp);
        }
        if (s.templeRevision==0 && s.map==4) {
            var body=main.Temple.bounds(player,player.worldX,player.worldY);
            boolean valid=main.Temple.terrainClear(gp,body) && (open || player.worldY>5*48) &&
                player.worldX>=8*48 && player.worldX<42*48;
            for (Entity npc : npcs[4]) if (npc!=null && body.intersects(main.Temple.bounds(npc,npc.worldX,npc.worldY))) valid=false;
            for (Entity enemy : monsters[4]) if (enemy!=null && body.intersects(main.Temple.bounds(enemy,enemy.worldX,enemy.worldY))) valid=false;
            if (!valid) main.Temple.place(player,24,42);
        }
        prepareTempleExtension(s,player,objects,npcs,monsters,tiles);
        RefugeState refuges = prepareExtraRooms(s,player,objects,npcs,monsters);
        return new Prepared(player, objects, npcs, monsters, tiles, new EventHandler(gp),reliefs,defeats,refuges);
    }

    private record RefugeState(int flags,RefugeChallenges.State later,FinalRefuges.State ending) {}

    private RefugeState prepareExtraRooms(SaveSnapshot s, Player player, Entity[][] objects, Entity[][] npcs, Entity[][] monsters) {
        int flags = s.extraRoomsFlags;
        if (s.extraRoomsRevision==0) {
            flags = ExtraRooms.openedRewards(objects);
            ExtraRooms.placeFixtures(npcs,objects);
            ExtraRooms.populate(gp,monsters,flags);
        } else {
            ExtraRooms.configure(monsters,flags);
            require((flags&ExtraRooms.FLOWER_DONE)!=0 || monsters[ExtraRooms.MAP][ExtraRooms.GREENY] instanceof monster.MON_Normalillo,
                "Missing refuge Greeny");
        }
        var later=new RefugeChallenges.State(s.pumpBindings,s.honguiBlocks,s.honguiDefeated,s.ghostPrefix);
        require(later.valid(),"Invalid later refuge progress");
        if (s.extraRoomsRevision<2) {
            later=RefugeChallenges.migrate(objects);
            RefugeChallenges.placeFixtures(npcs,objects);
            RefugeChallenges.populate(gp,monsters,later);
        } else {
            RefugeChallenges.configure(monsters,later);
            require(later.defeated() || monsters[ExtraRooms.MAP][RefugeChallenges.PICARON] instanceof monster.MON_Picaron,
                "Missing refuge Picaron");
        }
        var ending=new FinalRefuges.State(s.handyLatches,s.handyDamaged,s.icMarks,s.handySentryDefeated);
        require(ending.valid(),"Invalid final refuge progress");
        if (s.extraRoomsRevision<3) {
            ending=FinalRefuges.migrate(objects,player,s.map);
            FinalRefuges.placeFixtures(npcs,objects);
            FinalRefuges.populate(gp,monsters,ending);
        } else {
            FinalRefuges.configure(monsters,ending);
            require(ending.sentryDefeated() || monsters[ExtraRooms.MAP][FinalRefuges.SENTRY] instanceof monster.MON_Enfadillo,
                "Missing workshop sentry");
        }
        if (s.map==ExtraRooms.MAP) {
            var room=ExtraRooms.at(player.getCenterX()/48,player.getCenterY()/48);
            require(room!=null,"Invalid side-room position");
            if (room.art!=null && !ExtraRooms.safePosition(gp,room,player,npcs,objects,monsters,flags,later,ending)) {
                boolean redesigned=s.extraRoomsRevision<2 && (room==ExtraRooms.Room.PUMP ||
                    room==ExtraRooms.Room.HONGUI || room==ExtraRooms.Room.GHOST);
                boolean finalRedesign=s.extraRoomsRevision<3 && (room==ExtraRooms.Room.IC || room==ExtraRooms.Room.HANDY);
                require(s.extraRoomsRevision==0 || redesigned || finalRedesign,"Invalid refuge position");
                main.Temple.place(player,room.arrivalX,room.arrivalY);
            }
        }
        return new RefugeState(flags,later,ending);
    }

    private void prepareTempleExtension(SaveSnapshot s, Player player, Entity[][] objects, Entity[][] npcs,
                                        Entity[][] monsters, InteractiveTile[][] tiles) {
        int map = main.TempleExtension.MAP;
        if (s.templeExtensionRevision==0) {
            main.TempleExtension.rocks(gp,npcs);
            main.TempleExtension.plates(gp,tiles);
            main.TempleExtension.populate(gp,monsters);
        } else {
            for (var lane : main.TempleExtension.LANES)
                require(main.TempleExtension.valid(npcs[map][lane.rockSlot()],tiles[map][lane.plateSlot()],lane),
                    "Invalid temple extension lane or pair");
            main.TempleExtension.configure(monsters);
        }
        boolean open = main.TempleExtension.solved(npcs,tiles);
        if (open) {
            if (objects[map][0] instanceof object.OBJ_DoorIron) objects[map][0]=null;
        } else {
            require(objects[map][0]==null || objects[map][0] instanceof object.OBJ_DoorIron,"Extension gate slot occupied");
            objects[map][0]=main.Temple.gate(gp,main.TempleExtension.GATE_X,main.TempleExtension.GATE_Y);
        }
        if (s.map==map) {
            var body = main.Temple.bounds(player,player.worldX,player.worldY);
            require(main.Temple.terrainClear(gp,map,body) &&
                (open || body.y>=7*48),"Invalid temple extension player position");
            for (var lane : main.TempleExtension.LANES) {
                Entity rock = npcs[map][lane.rockSlot()];
                require(!body.intersects(main.Temple.bounds(rock,rock.worldX,rock.worldY)),"Player inside extension rock");
            }
        }
    }

    private void prepareArena(SaveSnapshot s, Player player, Entity[][] objects, Entity[][] monsters) {
        int map=FinalArena.MAP;
        Entity[] arenaObjects=objects[map], arenaMonsters=monsters[map];
        boolean legacy=s.arenaRevision==0;
        if (legacy) {
            for (int i=0; i<arenaObjects.length; i++) {
                Entity actor=arenaObjects[i];
                if (actor instanceof OBJ_DoorIron && actor.temp && actor.worldX==25*48 && actor.worldY==26*48)
                    arenaObjects[i]=null;
            }
            if (!s.godFishDefeated) arenaMonsters[FinalArena.BOSS_SLOT]=new MON_GodFish(gp);
            if (s.godFishDefeated && arenaObjects[FinalArena.DOOR_SLOT] instanceof OBJ_Door) {
                boolean keyExists=player.inventory.stream().anyMatch(OBJ_Key.class::isInstance);
                for (Entity actor:arenaObjects) {
                    if (!(actor instanceof OBJ_Key)) continue;
                    keyExists=true;
                    if (!Temple.terrainClear(gp,map,Temple.bounds(actor,actor.worldX,actor.worldY))
                            || actor.worldY<=FinalArena.DOOR_Y*48)
                        Temple.place(actor,FinalArena.KEY_X,FinalArena.KEY_Y);
                }
                if (!keyExists) {
                    int slot=FinalArena.KEY_SLOT;
                    if (arenaObjects[slot]!=null) {
                        for (int i=0; i<arenaObjects.length; i++) if (arenaObjects[i]==null) { slot=i; break; }
                    }
                    require(arenaObjects[slot]==null,"No slot for the unclaimed God Fish key");
                    arenaObjects[slot]=Temple.place(new OBJ_Key(gp),FinalArena.KEY_X,FinalArena.KEY_Y);
                }
            }
        } else {
            require(arenaMonsters[FinalArena.SUMMON_SLOT]==null && arenaObjects[FinalArena.SEAL_SLOT]==null,
                "Transient arena actors cannot be saved");
            Entity boss=arenaMonsters[FinalArena.BOSS_SLOT];
            require(s.godFishDefeated ? boss==null : boss instanceof MON_GodFish && boss.life==boss.maxLife
                && boss.worldX==FinalArena.BOSS_X && boss.worldY==FinalArena.BOSS_Y && boss.sleep && boss.alive && !boss.dying,
                "Invalid final encounter state");
        }
        if (s.map!=map) return;
        var body=Temple.bounds(player,player.worldX,player.worldY);
        boolean valid=Temple.terrainClear(gp,map,body)
            && (s.godFishDefeated || body.y>=(FinalArena.THRESHOLD_Y+1)*48);
        for (Entity actor:arenaObjects)
            if (actor!=null && actor.collision && body.intersects(Temple.bounds(actor,actor.worldX,actor.worldY))) valid=false;
        if (legacy && (!valid || s.bossBattleOn)) {
            Temple.place(player,FinalArena.ARRIVAL_X,s.godFishDefeated?FinalArena.KEY_Y+1:FinalArena.SAFE_Y);
            if (s.bossBattleOn) {
                player.life=player.maxLife;
                player.water=player.maxWater;
            }
        } else require(valid,"Invalid final arena position");
    }

    private World expandPreMarketWorld(World old) {
        World expanded = new World();
        expanded.objects = appendMarketRow(old.objects, gp.obj[Market.MAP].length);
        expanded.npcs = appendMarketRow(old.npcs, gp.npc[Market.MAP].length);
        expanded.monsters = appendMarketRow(old.monsters, gp.monster[Market.MAP].length);
        expanded.tiles = appendMarketRow(old.tiles, gp.iTile[Market.MAP].length);
        return expanded;
    }

    private static Actor[][] appendMarketRow(Actor[][] old, int slots) {
        require(old != null && old.length == PRE_MARKET_MAPS, "Invalid pre-market map count");
        Actor[][] expanded = Arrays.copyOf(old, PRE_MARKET_MAPS + 1);
        expanded[Market.MAP] = new Actor[slots];
        return expanded;
    }

    private void prepareMarketTV(Entity[][] npcs, boolean migrating) {
        NPC_TV merchant = null;
        for (int map = 0; map < npcs.length; map++) {
            for (int slot = 0; slot < npcs[map].length; slot++) {
                if (!(npcs[map][slot] instanceof NPC_TV tv)) continue;
                require(merchant == null, "Duplicate TV");
                merchant = tv;
                if (migrating) npcs[map][slot] = null;
                else require(map == Market.MAP && slot == Market.TV_SLOT
                    && tv.worldX == 22 * gp.tileSize && tv.worldY == 23 * gp.tileSize, "Invalid TV position");
            }
        }
        if (migrating) {
            if (merchant == null) merchant = new NPC_TV(gp);
            merchant.inventory.clear();
            npcs[Market.MAP][Market.TV_SLOT] = Market.placeTV(merchant, gp.tileSize);
        } else require(merchant != null, "Missing market TV");
    }

    private Entity[][] prepareActors(Actor[][] saved, Entity[][] shape, String category) {
        require(saved != null && saved.length == shape.length, "Invalid " + category + " map count");
        Entity[][] result = new Entity[shape.length][];
        for (int map = 0; map < shape.length; map++) {
            require(saved[map] != null && saved[map].length == shape[map].length, "Invalid " + category + " slots");
            result[map] = new Entity[shape[map].length];
            for (int slot = 0; slot < saved[map].length; slot++) {
                Actor a = saved[map][slot];
                if (a == null) continue;
                coordinates(a.x, a.y); direction(a.direction);
                require(a.amount > 0 && a.dialogueSet >= -1 && a.dialogueSet < 30 &&
                    a.dialogueIndex >= 0 && a.dialogueIndex < 30 && a.life >= 0, "Invalid entity state");
                Entity entity = category.equals("object") ? gp.eGenerator.requireObject(a.id) : registry.create(a.id, category, gp);
                int maxLife = (map == main.SixthZone.MAP || map == main.TempleExtension.MAP) && entity instanceof monster.MON_Tella
                    ? monster.MON_Tella.ABYSS_MAX_LIFE : entity.maxLife;
                require(!category.equals("monster") || a.life <= maxLife, "Invalid enemy health");
                entity.worldX = a.x; entity.worldY = a.y; entity.direction = a.direction;
                entity.life = a.life; entity.amount = a.amount;
                entity.alive = a.alive; entity.dying = a.dying;
                entity.opened = a.opened; entity.temp = a.temp; entity.sleep = a.sleep;
                entity.onPath = a.onPath; entity.dialogueSet = a.dialogueSet; entity.dialogueIndex = a.dialogueIndex;
                entity.inventory = prepareItems(a.inventory);
                if (a.loot != null) entity.setLoot(prepareItem(a.loot));
                if (entity instanceof OBJ_Chest) {
                    require(a.loot != null, "Chest is missing its loot definition");
                    if (a.opened) entity.down = entity.image2;
                }
                require(!a.inRage || entity instanceof MON_GodFish, "Only God Fish had a rage phase");
                result[map][slot] = entity;
            }
        }
        return result;
    }

    private Entity prepareItem(Item item) {
        require(item != null && item.amount > 0, "Invalid inventory item");
        Entity entity = gp.eGenerator.requireObject(item.id);
        entity.amount = item.amount;
        return entity;
    }

    private ArrayList<Entity> prepareItems(Item[] items) {
        require(items != null && items.length <= 20, "Invalid inventory");
        ArrayList<Entity> result = new ArrayList<>();
        for (Item item : items) result.add(prepareItem(item));
        return result;
    }

    private static Entity slot(List<Entity> inventory, int index) {
        require(index >= 0 && index < inventory.size(), "Invalid equipment slot");
        return inventory.get(index);
    }

    private void coordinates(int x, int y) {
        require(x >= 0 && y >= 0 && x < gp.maxWorldCol * gp.tileSize && y < gp.maxWorldRow * gp.tileSize,
            "Position is outside the world");
    }

    private static void direction(String direction) {
        require(List.of("up", "down", "left", "right").contains(direction == null ? "" : direction), "Invalid direction");
    }

    private static void require(boolean valid, String message) {
        if (!valid) throw new IllegalArgumentException(message);
    }

    private record Prepared(Player player, Entity[][] objects, Entity[][] npcs, Entity[][] monsters,
                            InteractiveTile[][] tiles, EventHandler events, int reliefs, int defeats, RefugeState refuges) { }

    private void apply(SaveSnapshot s, Prepared prepared) {
        // All operations which can fail have finished. Publish the prepared checkpoint together.
        gp.temple.resetCombat();
        gp.finalArena.abandon();
        gp.csManager.reset();
        gp.csManager.clearIntroMemory(true);
        gp.flow.resetDialogue();
        gp.player = prepared.player;
        gp.obj = prepared.objects; gp.npc = prepared.npcs; gp.monster = prepared.monsters;
        gp.iTile = prepared.tiles; gp.eHandler = prepared.events;
        gp.currentMap = s.map; gp.currentArea = s.area; gp.nextArea = s.area;
        gp.temple.reliefs=prepared.reliefs; gp.temple.defeats=prepared.defeats;
        gp.temple.syncReliefs();
        gp.extraRooms.restore(prepared.refuges.flags);
        gp.extraRooms.later.restore(prepared.refuges.later);
        gp.extraRooms.ending.restore(prepared.refuges.ending);
        gp.prepareAreaLighting();
        gp.bossBattleOn = false; Progress.GodFishDefeated = s.godFishDefeated;
        for (Entity[] row : gp.projectile) Arrays.fill(row, null);
        gp.particleList.clear();
        gp.keyH.releaseAll();
        gp.ui.npc = null;
        gp.gameState = gp.playState;
    }

    private SaveSnapshot importLegacy(DataStorage old) {
        require(initialWorld != null, "Initial world was not captured for legacy import");
        SaveSnapshot s = new SaveSnapshot();
        s.map = 0; s.area = gp.outside; s.x = 13 * gp.tileSize; s.y = 31 * gp.tileSize; s.direction = "right";
        s.level = old.level; s.maxLife = old.maxLife; s.life = old.life;
        s.maxWater = old.maxWater; s.water = old.water; s.strength = old.strenght;
        // Original code omitted this field. Each original level adds exactly one dexterity.
        s.dexterity = old.dexterity == 0 ? old.level : old.dexterity;
        s.exp = old.exp; s.nextLevelExp = old.nextLevelExp; s.coin = old.coin;
        require(old.itemNames != null && old.itemAmounts != null && old.itemNames.size() == old.itemAmounts.size(),
            "Invalid original inventory");
        s.inventory = new Item[old.itemNames.size()];
        for (int i = 0; i < s.inventory.length; i++) {
            Item item = new Item(); item.id = old.itemNames.get(i);
            require(old.itemAmounts.get(i) != null, "Invalid original item amount");
            item.amount = old.itemAmounts.get(i); s.inventory[i] = item;
        }
        s.weaponSlot = old.currentWeaponSlot; s.shieldSlot = old.currentShieldSlot;
        World world = new World();
        world.npcs = initialWorld.npcs; world.monsters = initialWorld.monsters; world.tiles = initialWorld.tiles;
        world.objects = new Actor[gp.maxMap][gp.obj[0].length];
        Object[][] original = {old.mapObjectNames, old.mapObjectWorldX, old.mapObjectWorldY,
            old.mapObjectOpened, old.mapObjectLootNames};
        for (Object[] rows : original) {
            require(rows != null && rows.length == PRE_MARKET_MAPS, "Invalid original world");
            for (int map = 0; map < rows.length; map++)
                require(rows[map] != null && java.lang.reflect.Array.getLength(rows[map]) == gp.obj[map].length,
                    "Invalid original object slots");
        }
        for (int map = 0; map < PRE_MARKET_MAPS; map++) {
            for (int slot = 0; slot < gp.obj[map].length; slot++) {
                String name = old.mapObjectNames[map][slot];
                require(name != null, "Missing original object identifier");
                if (name.equals("NA")) continue;
                Actor a = new Actor(); a.id = name; a.amount = 1; a.alive = true; a.direction = "down";
                a.x = old.mapObjectWorldX[map][slot]; a.y = old.mapObjectWorldY[map][slot];
                a.opened = old.mapObjectOpened[map][slot]; a.inventory = new Item[0];
                if (old.mapObjectLootNames[map][slot] != null) {
                    a.loot = new Item(); a.loot.id = old.mapObjectLootNames[map][slot]; a.loot.amount = 1;
                }
                world.objects[map][slot] = a;
            }
        }
        s.templeRevision = main.Temple.REVISION;
        s.marketRevision = MARKET_REVISION;
        world.objects[4][0] = initialWorld.objects[4][0];
        s.world = world;
        return s;
    }
}
