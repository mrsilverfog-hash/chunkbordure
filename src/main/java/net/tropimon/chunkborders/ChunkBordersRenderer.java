package net.tropimon.chunkborders;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Rendu custom des bordures de chunks.
 *
 * Deux choix importants :
 *  - tout est dessine en quads (petits paves) et non en lignes GL, qui cassent
 *    avec Sodium/Iris et rendent le F3+G vanilla invisible ;
 *  - le rendu se fait a la main via Tessellator, sans test de profondeur, pour
 *    que les traits restent visibles a travers les blocs (indispensable sous terre).
 */
public final class ChunkBordersRenderer {

    // ------------------------------------------------------------------
    // Reglages
    // ------------------------------------------------------------------

    /** Zone affichee autour du joueur : 1 = 3x3 chunks. */
    private static final int CHUNK_RADIUS = 1;

    /** Epaisseur des traits, en blocs. Monte a 0.10 si tu veux encore plus gras. */
    private static final float THICKNESS = 0.06F;

    /** Espacement des verticales secondaires le long des bords, en blocs. */
    private static final int VERTICAL_STEP = 4;

    /** Espacement vertical des contours horizontaux, en blocs. */
    private static final int LEVEL_STEP = 16;

    /** Longueur de chaque branche de la croix au sol, en blocs. */
    private static final double CROSS_ARM = 1.0D;

    /** Hauteur de la croix au-dessus du sol, pour eviter le z-fighting. */
    private static final double CROSS_OFFSET = 0.02D;

    /** Profondeur de recherche du sol sous les pieds du joueur, en blocs. */
    private static final int GROUND_SCAN = 32;

    private ChunkBordersRenderer() {
    }

    // ------------------------------------------------------------------
    // Rendu
    // ------------------------------------------------------------------

    public static void render(WorldRenderContext context) {
        if (!ChunkBordersClient.enabled) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || context.camera() == null) {
            return;
        }

        MatrixStack matrices = context.matrixStack();
        if (matrices == null) {
            matrices = new MatrixStack();
        }

        Vec3d cam = context.camera().getPos();
        double bottom = mc.world.getBottomY();
        double top = mc.world.getTopY();

        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f m = matrices.peek().getPositionMatrix();

        // Etat GL : pas de test de profondeur, pas d'ecriture dans le depth
        // buffer, pas de culling. Les traits passent donc a travers les blocs.
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        BufferBuilder buffer = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        ChunkPos center = mc.player.getChunkPos();

        for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
            for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
                drawChunk(buffer, m, new ChunkPos(center.x + dx, center.z + dz),
                        bottom, top, dx == 0 && dz == 0);
            }
        }

        // Croix au sol a chaque intersection de 4 chunks. On parcourt les
        // sommets de la grille (un de plus que les chunks dans chaque sens)
        // pour ne dessiner chaque coin qu'une seule fois.
        int feet = MathHelper.floor(mc.player.getY());
        for (int cx = center.x - CHUNK_RADIUS; cx <= center.x + CHUNK_RADIUS + 1; cx++) {
            for (int cz = center.z - CHUNK_RADIUS; cz <= center.z + CHUNK_RADIUS + 1; cz++) {
                drawGroundCross(buffer, m, mc.world, cx * 16, cz * 16, feet);
            }
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();

        matrices.pop();
    }

    private static void drawChunk(VertexConsumer vc, Matrix4f m, ChunkPos pos,
                                  double bottom, double top, boolean isCenter) {
        double x0 = pos.getStartX();
        double z0 = pos.getStartZ();
        double x1 = x0 + 16.0D;
        double z1 = z0 + 16.0D;

        float alpha = isCenter ? 1.0F : 0.75F;

        // Coins du chunk : jaune, pleine hauteur.
        vLine(vc, m, x0, z0, bottom, top, 1.0F, 0.95F, 0.15F, alpha);
        vLine(vc, m, x1, z0, bottom, top, 1.0F, 0.95F, 0.15F, alpha);
        vLine(vc, m, x0, z1, bottom, top, 1.0F, 0.95F, 0.15F, alpha);
        vLine(vc, m, x1, z1, bottom, top, 1.0F, 0.95F, 0.15F, alpha);

        // Verticales intermediaires le long des 4 bords : cyan.
        float ia = alpha * 0.8F;
        for (int i = VERTICAL_STEP; i < 16; i += VERTICAL_STEP) {
            vLine(vc, m, x0 + i, z0, bottom, top, 0.20F, 0.75F, 1.0F, ia);
            vLine(vc, m, x0 + i, z1, bottom, top, 0.20F, 0.75F, 1.0F, ia);
            vLine(vc, m, x0, z0 + i, bottom, top, 0.20F, 0.75F, 1.0F, ia);
            vLine(vc, m, x1, z0 + i, bottom, top, 0.20F, 0.75F, 1.0F, ia);
        }

        // Contours horizontaux qui relient les verticales.
        float ha = alpha * 0.7F;
        int first = (int) (Math.floor(bottom / LEVEL_STEP) * LEVEL_STEP);
        for (int y = first; y <= top; y += LEVEL_STEP) {
            if (y < bottom) {
                continue;
            }
            square(vc, m, x0, z0, x1, z1, y, 1.0F, 0.95F, 0.15F, ha);
        }
    }

    /**
     * Croix rouge posee au sol, centree sur le point ou 4 chunks se rejoignent.
     * Le sol est cherche sous les pieds du joueur, pas en surface : sous terre
     * la croix reste donc a hauteur de la grotte ou tu te trouves.
     */
    private static void drawGroundCross(VertexConsumer vc, Matrix4f m, ClientWorld world,
                                        int blockX, int blockZ, int feetY) {
        double y = findGround(world, blockX, blockZ, feetY) + CROSS_OFFSET;

        float h = THICKNESS / 2.0F;
        float r = 1.0F, g = 0.20F, b = 0.15F, a = 1.0F;

        // Branche est-ouest.
        box(vc, m,
                blockX - CROSS_ARM, y - h, blockZ - h,
                blockX + CROSS_ARM, y + h, blockZ + h,
                r, g, b, a);

        // Branche nord-sud.
        box(vc, m,
                blockX - h, y - h, blockZ - CROSS_ARM,
                blockX + h, y + h, blockZ + CROSS_ARM,
                r, g, b, a);
    }

    /** Premier bloc non vide sous {@code startY} : renvoie le dessus de ce bloc. */
    private static int findGround(ClientWorld world, int x, int z, int startY) {
        int min = Math.max(world.getBottomY(), startY - GROUND_SCAN);
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int y = startY; y >= min; y--) {
            pos.set(x, y - 1, z);
            if (!world.getBlockState(pos).isAir()) {
                return y;
            }
        }
        return startY;
    }

    // ------------------------------------------------------------------
    // Primitives
    // ------------------------------------------------------------------

    private static void vLine(VertexConsumer vc, Matrix4f m, double x, double z,
                              double bottom, double top,
                              float r, float g, float b, float a) {
        float h = THICKNESS / 2.0F;
        box(vc, m, x - h, bottom, z - h, x + h, top, z + h, r, g, b, a);
    }

    private static void square(VertexConsumer vc, Matrix4f m,
                               double x0, double z0, double x1, double z1, double y,
                               float r, float g, float b, float a) {
        float h = THICKNESS / 2.0F;
        box(vc, m, x0 - h, y - h, z0 - h, x1 + h, y + h, z0 + h, r, g, b, a);
        box(vc, m, x0 - h, y - h, z1 - h, x1 + h, y + h, z1 + h, r, g, b, a);
        box(vc, m, x0 - h, y - h, z0 - h, x0 + h, y + h, z1 + h, r, g, b, a);
        box(vc, m, x1 - h, y - h, z0 - h, x1 + h, y + h, z1 + h, r, g, b, a);
    }

    /** Pave plein, 6 faces. Le culling est desactive, une passe suffit. */
    private static void box(VertexConsumer vc, Matrix4f m,
                            double minX, double minY, double minZ,
                            double maxX, double maxY, double maxZ,
                            float r, float g, float b, float a) {
        float ax = (float) minX, ay = (float) minY, az = (float) minZ;
        float bx = (float) maxX, by = (float) maxY, bz = (float) maxZ;

        quad(vc, m, ax, ay, az, bx, ay, az, bx, ay, bz, ax, ay, bz, r, g, b, a);
        quad(vc, m, ax, by, az, ax, by, bz, bx, by, bz, bx, by, az, r, g, b, a);
        quad(vc, m, ax, ay, az, ax, by, az, bx, by, az, bx, ay, az, r, g, b, a);
        quad(vc, m, ax, ay, bz, bx, ay, bz, bx, by, bz, ax, by, bz, r, g, b, a);
        quad(vc, m, ax, ay, az, ax, ay, bz, ax, by, bz, ax, by, az, r, g, b, a);
        quad(vc, m, bx, ay, az, bx, by, az, bx, by, bz, bx, ay, bz, r, g, b, a);
    }

    private static void quad(VertexConsumer vc, Matrix4f m,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float r, float g, float b, float a) {
        vc.vertex(m, x1, y1, z1).color(r, g, b, a);
        vc.vertex(m, x2, y2, z2).color(r, g, b, a);
        vc.vertex(m, x3, y3, z3).color(r, g, b, a);
        vc.vertex(m, x4, y4, z4).color(r, g, b, a);
    }
}
