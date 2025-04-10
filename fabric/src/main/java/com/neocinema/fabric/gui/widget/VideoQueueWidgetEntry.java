package com.neocinema.fabric.gui.widget;

import com.neocinema.fabric.NeoCinema;
import com.neocinema.fabric.gui.VideoQueueScreen;
import com.neocinema.fabric.util.NetworkUtil;
import com.neocinema.fabric.video.queue.QueuedVideo;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

import static net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry.*;
import static net.minecraft.client.render.RenderPhase.*;
import static net.minecraft.client.render.RenderPhase.COLOR_MASK;

public class VideoQueueWidgetEntry extends ElementListWidget.Entry<VideoQueueWidgetEntry> implements Comparable<VideoQueueWidgetEntry> {

    private static final Identifier UPVOTE_TEXTURE = Identifier.of(NeoCinema.MODID, "textures/gui/upvote.png");
    private static final Identifier UPVOTE_SELECTED_TEXTURE = Identifier.of(NeoCinema.MODID, "textures/gui/upvote_selected.png");
    private static final Identifier UPVOTE_ACTIVE_TEXTURE = Identifier.of(NeoCinema.MODID, "textures/gui/upvote_active.png");
    private static final Identifier DOWNVOTE_TEXTURE = Identifier.of(NeoCinema.MODID, "textures/gui/downvote.png");
    private static final Identifier DOWNVOTE_SELECTED_TEXTURE = Identifier.of(NeoCinema.MODID, "textures/gui/downvote_selected.png");
    private static final Identifier DOWNVOTE_ACTIVE_TEXTURE = Identifier.of(NeoCinema.MODID, "textures/gui/downvote_active.png");
    private static final Identifier TRASH_TEXTURE = Identifier.of(NeoCinema.MODID, "textures/gui/trash.png");
    private static final Identifier TRASH_SELECTED_TEXTURE = Identifier.of(NeoCinema.MODID, "textures/gui/trash_selected.png");

    private final QueuedVideo queuedVideo;
    private final List<Element> children;
    protected MinecraftClient client;
    private boolean downVoteButtonSelected;
    private boolean upVoteButtonSelected;
    private boolean trashButtonSelected;

    private final Function<Identifier, RenderLayer> GUI_TEXTURED = Util.memoize((texture) -> RenderLayer.of("gui_textured_overlay", VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS, 1536, RenderLayer.MultiPhaseParameters.builder().texture(new Texture(texture, TriState.DEFAULT, false)).program(POSITION_TEXTURE_COLOR_PROGRAM).transparency(TRANSLUCENT_TRANSPARENCY).depthTest(ALWAYS_DEPTH_TEST).writeMaskState(COLOR_MASK).build(false)));

    public VideoQueueWidgetEntry(VideoQueueScreen parent, QueuedVideo queuedVideo, MinecraftClient client) {
        this.queuedVideo = queuedVideo;
        children = ImmutableList.of();
        this.client = client;
    }

    @Override
    public List<? extends Element> children() {
        return children;
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        int i = x + 4;
        int j = y + (entryHeight - 24) / 2;
        int m = y + (entryHeight - 7) / 2;
        int color = queuedVideo.owner() ? BLACK_COLOR : GRAY_COLOR;
        context.fill(x, y, x + entryWidth, y + entryHeight, color);
        context.drawText(client.textRenderer, queuedVideo.videoInfo().getTitleShort(), i, m, WHITE_COLOR, false);
        context.drawText(client.textRenderer, queuedVideo.videoInfo().getDurationString(), i + 118, m, WHITE_COLOR, false);
        context.drawText(client.textRenderer, queuedVideo.getScoreString(),  i + 165, m, WHITE_COLOR, false);
        renderDownVoteButton(context, mouseX, mouseY, i, j);
        renderUpVoteButton(context, mouseX, mouseY, i, j);
        renderTrashButton(context, mouseX, mouseY, i, j);
        if (mouseX > i && mouseX < i + 180 && mouseY > j && mouseY < j + 18) {
            context.drawTooltip(client.textRenderer, Text.of(queuedVideo.videoInfo().getTitle()), mouseX, mouseY);
        }
    }

    private void renderDownVoteButton(DrawContext context, int mouseX, int mouseY, int i, int j) {
        int downVoteButtonPosX = i + 185;
        int downVoteButtonPosY = j + 7;

        downVoteButtonSelected = mouseX > downVoteButtonPosX && mouseX < downVoteButtonPosX + 12 && mouseY > downVoteButtonPosY && mouseY < downVoteButtonPosY + 12;

        if (queuedVideo.clientState() == -1) {
            context.drawTexture(GUI_TEXTURED,DOWNVOTE_ACTIVE_TEXTURE, downVoteButtonPosX, downVoteButtonPosY, 32F, 32F, 12, 12, 8, 8, 8, 8);
        } else if (downVoteButtonSelected) {
            context.drawTexture(GUI_TEXTURED,DOWNVOTE_SELECTED_TEXTURE, downVoteButtonPosX, downVoteButtonPosY, 32F, 32F, 12, 12,  8, 8, 8, 8);
        } else {
            context.drawTexture(GUI_TEXTURED,DOWNVOTE_TEXTURE, downVoteButtonPosX, downVoteButtonPosY, 32F, 32F, 12, 12, 12, 8, 8, 8, 8);
        }
    }

    private void renderUpVoteButton(DrawContext context, int mouseX, int mouseY, int i, int j) {
        int upVoteButtonPosX = i + 200;
        int upVoteButtonPosY = j + 3;

        upVoteButtonSelected = mouseX > upVoteButtonPosX && mouseX < upVoteButtonPosX + 12 && mouseY > upVoteButtonPosY && mouseY < upVoteButtonPosY + 12;

        if (queuedVideo.clientState() == 1) {
            context.drawTexture(GUI_TEXTURED,UPVOTE_ACTIVE_TEXTURE, upVoteButtonPosX, upVoteButtonPosY, 32F, 32F, 12, 12, 8, 8, 8, 8);
        } else if (upVoteButtonSelected) {
            context.drawTexture(GUI_TEXTURED,UPVOTE_SELECTED_TEXTURE, upVoteButtonPosX, upVoteButtonPosY, 32F, 32F, 12, 12, 8, 8, 8, 8);
        } else {
            context.drawTexture(GUI_TEXTURED,UPVOTE_TEXTURE, upVoteButtonPosX, upVoteButtonPosY, 32F, 32F, 12, 12, 8, 8, 8, 8);
        }
    }

    private void renderTrashButton(DrawContext context, int mouseX, int mouseY, int i, int j) {
        if (queuedVideo.owner()) {
            int trashButtonPosX = i + 225;
            int trashButtonPosY = j + 5;

            trashButtonSelected = mouseX > trashButtonPosX && mouseX < trashButtonPosX + 12 && mouseY > trashButtonPosY && mouseY < trashButtonPosY + 12;

            if (trashButtonSelected) {
                context.drawTexture(GUI_TEXTURED,TRASH_SELECTED_TEXTURE, trashButtonPosX, trashButtonPosY, 32F, 32F, 12, 12, 8, 8, 8, 8);
            } else {
                context.drawTexture(GUI_TEXTURED,TRASH_TEXTURE, trashButtonPosX, trashButtonPosY, 32F, 32F, 12, 12, 8, 8, 8, 8);
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (downVoteButtonSelected) {
            NetworkUtil.sendVideoQueueVotePacket(queuedVideo.videoInfo(), -1);
        } else if (upVoteButtonSelected) {
            NetworkUtil.sendVideoQueueVotePacket(queuedVideo.videoInfo(), 1);
        } else if (trashButtonSelected) {
            NetworkUtil.sendVideoQueueRemovePacket(queuedVideo.videoInfo());
        }

        return true;
    }

    @Override
    public int compareTo(@NotNull VideoQueueWidgetEntry videoQueueWidgetEntry) {
        return queuedVideo.compareTo(videoQueueWidgetEntry.queuedVideo);
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return null;
    }

}
