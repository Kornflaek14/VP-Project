import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/** Creates the original, short noise-based blade swoosh used by the slash VFX. */
public class GenerateSlashSound {
    public static void main(String[] args) throws Exception {
        int sampleRate = 44100;
        int count = (int) (sampleRate * 0.18);
        byte[] samples = new byte[count * 2];
        Random noise = new Random(42);
        double filtered = 0;
        for (int i = 0; i < count; i++) {
            double time = (double) i / count;
            double white = noise.nextDouble() * 2 - 1;
            filtered += (white - filtered) * (0.65 - time * 0.5);
            double envelope = Math.sin(Math.PI * time) * Math.exp(-4 * time);
            short value = (short) (filtered * envelope * 26000);
            samples[i * 2] = (byte) value;
            samples[i * 2 + 1] = (byte) (value >> 8);
        }
        Path output = Path.of("assets/audio/slash.wav");
        Files.createDirectories(output.getParent());
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        try (AudioInputStream input = new AudioInputStream(new ByteArrayInputStream(samples), format, count)) {
            AudioSystem.write(input, AudioFileFormat.Type.WAVE, output.toFile());
        }
    }
}
