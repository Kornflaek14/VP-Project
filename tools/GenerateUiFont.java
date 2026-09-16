import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/** Run from the project root: java tools/GenerateUiFont.java */
public class GenerateUiFont {
    public static void main(String[] args) throws Exception {
        Path directory = Path.of("assets/fonts");
        Font font = Font.createFont(Font.TRUETYPE_FONT, directory.resolve("Lato-Regular.ttf").toFile())
                .deriveFont(64f);
        BufferedImage atlas = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = atlas.createGraphics();
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        int ascent = graphics.getFontMetrics().getAscent();
        int lineHeight = graphics.getFontMetrics().getHeight();
        List<Integer> characters = new ArrayList<>();
        for (int code = 32; code <= 255; code++) {
            if (code < 127 || code >= 160) characters.add(code);
        }
        "\u2013\u2014\u2018\u2019\u201c\u201d\u2022\u2026\u2190\u2191\u2192\u2193".codePoints().forEach(characters::add);
        List<String> records = new ArrayList<>();
        int x = 8, y = 8, rowHeight = 0;
        for (int code : characters) {
            if (!font.canDisplay(code)) throw new IllegalStateException("Missing glyph: " + code);
            GlyphVector glyph = font.createGlyphVector(graphics.getFontRenderContext(), Character.toChars(code));
            Rectangle bounds = glyph.getPixelBounds(graphics.getFontRenderContext(), 0, 0);
            int width = Math.max(1, bounds.width), height = Math.max(1, bounds.height);
            if (x + width + 8 > atlas.getWidth()) {
                x = 8;
                y += rowHeight + 16;
                rowHeight = 0;
            }
            if (y + height + 8 > atlas.getHeight()) throw new IllegalStateException("Font atlas overflow");
            graphics.drawGlyphVector(glyph, x - bounds.x, y - bounds.y);
            records.add(String.format(java.util.Locale.ROOT,
                    "char id=%d x=%d y=%d width=%d height=%d xoffset=%d yoffset=%d xadvance=%d page=0 chnl=15",
                    code, x, y, width, height, bounds.x, ascent + bounds.y,
                    Math.round(glyph.getGlyphMetrics(0).getAdvanceX())));
            x += width + 16;
            rowHeight = Math.max(rowHeight, height);
        }
        graphics.dispose();
        ImageIO.write(atlas, "png", directory.resolve("locura-ui.png").toFile());
        String header = "info face=\"Locura UI\" size=64 bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=0,0\n"
                + "common lineHeight=" + lineHeight + " base=" + ascent + " scaleW=1024 scaleH=1024 pages=1 packed=0\n"
                + "page id=0 file=\"locura-ui.png\"\nchars count=" + records.size() + "\n";
        Files.writeString(directory.resolve("locura-ui.fnt"), header + String.join("\n", records) + "\n", StandardCharsets.UTF_8);
        System.out.println("Generated " + records.size() + " glyphs.");
    }
}
