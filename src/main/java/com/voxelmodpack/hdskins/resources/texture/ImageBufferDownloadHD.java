package com.voxelmodpack.hdskins.resources.texture;

import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.voxelmodpack.hdskins.HDSkins;

import javax.annotation.Nullable;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ImageBufferDownloadHD implements ISkinAvailableCallback {

    private int scale;
    private Graphics graphics;
    private BufferedImage image;

    private ISkinAvailableCallback callback = null;

    private Type skinType = Type.SKIN;

    public ImageBufferDownloadHD() {

    }

    public ImageBufferDownloadHD(Type type, ISkinAvailableCallback callback) {
        this.callback = callback;
        this.skinType = type;
    }

    @Override
    @Nullable
    @SuppressWarnings({"SuspiciousNameCombination", "NullableProblems"})
    public BufferedImage parseUserSkin(@Nullable BufferedImage downloadedImage) {
        // TODO: Do we want to convert other skin types?
        if (downloadedImage == null || skinType != Type.SKIN) {
            return downloadedImage;
        }

        int imageWidth = downloadedImage.getWidth();
        int imageHeight = downloadedImage.getHeight();
        if (imageHeight == imageWidth) {
            return downloadedImage;
        }
        scale = imageWidth / 64;
        image = new BufferedImage(imageWidth, imageWidth, BufferedImage.TYPE_INT_ARGB);
        graphics = image.getGraphics();
        graphics.drawImage(downloadedImage, 0, 0, null);

        // copy layers
        // leg
        drawImage(24, 48, 20, 52,  4, 16,  8, 20); // top
        drawImage(28, 48, 24, 52,  8, 16, 12, 20); // bottom
        drawImage(20, 52, 16, 64,  8, 20, 12, 32); // inside
        drawImage(24, 52, 20, 64,  4, 20,  8, 32); // front
        drawImage(28, 52, 24, 64,  0, 20,  4, 32); // outside
        drawImage(32, 52, 28, 64, 12, 20, 16, 32); // back
        // arm
        drawImage(40, 48, 36, 52, 44, 16, 48, 20); // top
        drawImage(44, 48, 40, 52, 48, 16, 52, 20); // bottom
        drawImage(36, 52, 32, 64, 48, 20, 52, 32);
        drawImage(40, 52, 36, 64, 44, 20, 48, 32);
        drawImage(44, 52, 40, 64, 40, 20, 44, 32);
        drawImage(48, 52, 44, 64, 52, 20, 56, 32);

        // mod things
        HDSkins.instance.getSkinManager().convertSkin(image, graphics);

        graphics.dispose();

        if (callback != null) {
            return callback.parseUserSkin(image);
        }

        return image;
    }

    private void drawImage(int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2) {
        graphics.drawImage(image,
                dx1 * scale, dy1 * scale, dx2 * scale, dy2 * scale,
                sx1 * scale, sy1 * scale, sx2 * scale, sy2 * scale,
                null);
    }

    @Override
    public void skinAvailable() {
        if (callback != null) {
            callback.skinAvailable();
        }
    }
}
