/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.core.player;

import com.mongodb.client.MongoCursor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.io.DBEntity;
import com.spleefleague.core.io.DBLoad;
import com.spleefleague.core.io.DBLoadable;
import com.spleefleague.core.io.EntityBuilder;
import com.spleefleague.core.utils.PlayerUtil;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

/**
 *
 * @author Jonas
 */
public class Rank extends DBEntity implements DBLoadable {
    
    @DBLoad(fieldName = "name")
    private String name;
    @DBLoad(fieldName = "displayName")
    private String displayName;
    @DBLoad(fieldName = "ladder")
    private int ladder;
    @DBLoad(fieldName = "hasOp")
    private boolean hasOp;
    @DBLoad(fieldName = "color")
    private ChatColor color;
    @DBLoad(fieldName = "permissions")
    private String[] permissions;
    @DBLoad(fieldName = "exclusivePermissions")
    private String[] exclusivePermissions;
    
    public Rank() {
        
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getLadder() {
        return ladder;
    }
    
    public boolean hasOp() {
        return hasOp;
    }
    
    public ChatColor getColor() {
        return color;
    }
    
    public boolean hasPermission(Rank rank) {
        return this == rank || this.getLadder() > rank.getLadder();
    }
    
    public boolean hasPermission(String permission) {
        for(String perm : exclusivePermissions) {
            if(perm.equals(permission)) {
                return true;
            }
        }
        for(Rank rank : Rank.values()) {
            if(rank.getLadder() < this.getLadder()) {
                for(String perm : rank.permissions) {
                    if(perm.equals(permission)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public List<String> getAllPermissions() {
        List<String> permissions = new ArrayList<>(Arrays.asList(exclusivePermissions));
        for(Rank rank : Rank.values()) {
            if(rank.getLadder() < this.getLadder()) {
                permissions.addAll(Arrays.asList(rank.permissions));
            }
        }
        return permissions;
    }
    
    private static Map<String, Rank> ranks = new HashMap<>();
    
    public static final Rank 
            ADMIN = new Rank(), 
            COUNCIL = new Rank(), 
            DEVELOPER = new Rank(), 
            SENIOR_MODERATOR = new Rank(),
            MODERATOR = new Rank(),
            VIP = new Rank(), 
            PREMIUM = new Rank(), 
            BUILDER = new Rank(), 
            ORGANIZER = new Rank(), 
            DEFAULT = new Rank();

    public static Rank valueOf(String name) {
        return ranks.get(name);
    }
    
    public static Rank[] values() {
        return ranks.values().toArray(new Rank[0]);
    }
    
    public static void init() {
        MongoCursor<Document> dbc = SpleefLeague.getInstance().getPluginDB().getCollection("Ranks").find().iterator();
        while(dbc.hasNext()) {
            Rank rank = EntityBuilder.load(dbc.next(), Rank.class);
            Rank staticRank = getField(rank.getName());
            if(staticRank != null) {
                staticRank.name = rank.name;
                staticRank.displayName = rank.displayName;
                staticRank.hasOp = rank.hasOp;
                staticRank.ladder = rank.ladder;
                staticRank.color = rank.color;
                staticRank.permissions = rank.permissions;
                staticRank.exclusivePermissions = rank.exclusivePermissions;
                rank = staticRank;
            }
            ranks.put(rank.getName(), rank);
        }
        SpleefLeague.getInstance().log("Loaded " + ranks.size() + " ranks!");
    }
    
    private static Rank getField(String name) {
        try {
            Field field = Rank.class.getField(name);
            Rank staticRank = (Rank) field.get(null);
            return staticRank;
        } catch (NoSuchFieldException e) {
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(Rank.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void managePermissions(Player player) {
        for(String permission : getAllPermissions()) {
            player.addAttachment(SpleefLeague.getInstance(), permission, true);
        }
        player.setOp(hasOp);
    }
}
