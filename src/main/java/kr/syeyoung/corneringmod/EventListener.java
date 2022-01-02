package kr.syeyoung.corneringmod;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.util.*;

public class EventListener {

    public static volatile boolean ZHFState = false;
    public static volatile boolean blockAlarm = false;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event)
    {
        if (Keybinds.toggleCornering.isPressed())
        {
            ZHFState = !ZHFState;
            Minecraft.getMinecraft().thePlayer.addChatMessage(new TextComponentString("Toggled Cornering to "+(ZHFState ? "on" : "off")));
        }

        if (Keybinds.toggleBlockAlarm.isPressed())
        {
            blockAlarm = !blockAlarm;
            Minecraft.getMinecraft().thePlayer.addChatMessage(new TextComponentString("Toggled Block Alarm to "+(blockAlarm ? "on" : "off")));
        }

    }


    @SubscribeEvent
    public void onRenderGui(RenderGameOverlayEvent.Post event)
    {
        try {
            if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) return;
            String str = "Cornering " + (ZHFState ? "on" : "off");
            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            fr.drawStringWithShadow(str, new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth() - fr.getStringWidth(str), 8, 0xFFFF55);
            str = "Block Alarm " + (blockAlarm ? "on" : "off");
            fr.drawStringWithShadow(str, new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth() - fr.getStringWidth(str), 16, 0xFFFF55);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!blockAlarm) return;
        Scoreboard scoreboard = Minecraft.getMinecraft().thePlayer.getWorldScoreboard();
        ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(Scoreboard.getObjectiveDisplaySlotNumber("sidebar"));
        if (scoreObjective != null && scoreObjective.getDisplayName().replaceAll("§.", "").equalsIgnoreCase("ZOMBIES")) {
            int rev = 0;
            int dead = 0;
            int quit = 0;
            for (Score score : scoreboard.getSortedScores(scoreObjective))
            {
                ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
                String s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName());
                if (score.getScorePoints() <= 10 && score.getScorePoints() >= 7) {
                    s = s.replaceAll("§.", "");
                    try {
                        s = s.split(":")[1].replaceAll("[^a-zA-Z0-9]", "").trim();
                    } catch (Exception e) {
                        return;
                    }
                    if (s.equalsIgnoreCase("revive")) {
                        rev ++;
                    } else if (s.equalsIgnoreCase("dead")) {
                        dead ++;
                    } else if (s.equalsIgnoreCase("quit")) {
                        quit ++;
                    }
                }
            }


            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            fr.drawStringWithShadow("REV: "+rev, (sr.getScaledWidth() + fr.getStringWidth("REV: "+rev)) / 2.0f - 100, sr.getScaledHeight() / 2.0f, 0xFFFF55);
            fr.drawStringWithShadow("DEAD: "+dead, (sr.getScaledWidth() + fr.getStringWidth("DEAD: "+dead)) / 2.0f - 100, sr.getScaledHeight() / 2.0f + 8, 0xFFFF55);

            ReflectionHelper.getPrivateValue(GuiIngame.class, Minecraft.getMinecraft().ingameGUI, 0);

            boolean isREV = false;
            try {
                isREV= isReviving();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }


            if (rev + dead >= (quit == 0 ? 2 : quit == 1 ? 1 : quit == 2 ? 1 : quit == 3 ? 0 : 0) || isREV) {
                System.out.println("rev rev rev");
                GL11.glPushMatrix(); //Start new matrix
                GL11.glScalef(7F, 7F, 7F); //scale it to 0.5 size on each side. Must be float e.g.: 2.0F
                fr.drawStringWithShadow("BLOCK", (sr.getScaledWidth() + fr.getStringWidth("BLOCK")) / 14.0f, (sr.getScaledHeight() / 2.0f - 8)/7, rev + dead == 2 ? 0xFF5555 : 0xAA0000);
                GL11.glPopMatrix();
                fr.drawStringWithShadow("BLOCK", (sr.getScaledWidth() + fr.getStringWidth("BLOCK")) / 14.0f, (sr.getScaledHeight() / 2.0f - 8)/7, rev + dead == 2 ? 0xFF5555 : 0xAA0000);

            }
        }
    }
    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Pre event) {
        if(ZHFState && event.getEntity() instanceof EntityPlayer) {
            double distSq = event.getEntity().getDistanceSqToEntity(Minecraft.getMinecraft().thePlayer);
            if (distSq < 6.25) {
                event.setCanceled(true);
            }
        }
    }

    private static final Field RECORD_PLAYING_TEXT = ReflectionHelper.findField(GuiIngame.class, "recordPlaying", "field_73838_g", "o");
    private static final Field RECORD_PLAYING_TICK_LEFT = ReflectionHelper.findField(GuiIngame.class, "recordPlayingUpFor", "field_73845_h", "p");

    private boolean isReviving() throws IllegalAccessException {
        GuiIngame ingame = Minecraft.getMinecraft().ingameGUI;
        int ticksLeft = RECORD_PLAYING_TICK_LEFT.getInt(ingame);
        if (ticksLeft < 0) return false;

        String text = (String) RECORD_PLAYING_TEXT.get(ingame);
        if (text.contains("Reviving")) return true;
        return false;
    }
}
