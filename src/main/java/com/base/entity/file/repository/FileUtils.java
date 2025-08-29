package com.base.entity.file.repository;


import com.base.core.exception.ErrorException;
import org.apache.commons.lang3.Strings;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author YISivlay
 */
public class FileUtils {

    public static void isValidEntityName(final String entity) {
        for (final ENTITY entities : ENTITY.values()) {
            if (entities.name().equalsIgnoreCase(entity)) {
                return;
            }
        }
        throw new ErrorException("msg.internal.error");
    }

    static void write(InputStream is, String filePath) {
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        } catch (IOException e) {
            throw new ErrorException("validation.file.content.error");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    static InputStream resize(String extension, InputStream is) {
        InputStream inputStream = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int bytesRead;
            byte[] buffer = new byte[8192];
            while ((bytesRead = is.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            BufferedImage src = ImageIO.read(bis);

            int width = (src.getWidth() * 50) / 100;
            int height = (src.getHeight() * 50) / 100;

            src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage bf;
            if (extension.equals(FILE_EXTENSION.PNG.getValueWithoutDot())) {
                bf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            } else {
                bf = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            }

            final Graphics2D graphics2D = bf.createGraphics();
            graphics2D.setComposite(AlphaComposite.Src);
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            graphics2D.drawImage(src, 0, 0, width, height, null);
            graphics2D.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bf, extension, out);

            inputStream = new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            e.getMessage();
        }

        return inputStream;
    }

    public static void isValidateMimeType(final String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            throw new IllegalArgumentException("MIME type cannot be null or empty");
        }

        boolean isValid = Arrays.stream(MIME_TYPE.values()).anyMatch(type -> type.getValue().equalsIgnoreCase(mimeType));

        if (!isValid) {
            throw new IllegalArgumentException("Unsupported MIME type: " + mimeType);
        }
    }

    private static String mimeType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg" -> "image/jpg";
            case "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }

    public enum MIME_TYPE {
        JPEG("image/jpeg"),
        JPG("image/jpg"),
        PNG("image/png"),
        PDF("application/pdf"),
        XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        private final String value;

        MIME_TYPE(final String value) {
            this.value = value;
        }

        public static MIME_TYPE extension(FILE_EXTENSION fileExtension) {
            return switch (fileExtension) {
                case JPG -> MIME_TYPE.JPG;
                case JPEG -> MIME_TYPE.JPEG;
                case PNG -> MIME_TYPE.PNG;
                case PDF -> MIME_TYPE.PDF;
                case XLSX -> MIME_TYPE.XLSX;
                case DOCX -> MIME_TYPE.DOCX;
                default -> throw new ErrorException("validation.file.error.extension", fileExtension);
            };
        }

        public String getValue() {
            return this.value;
        }
    }

    public enum FILE_EXTENSION {
        JPEG(".jpeg"),
        JPG(".jpg"),
        PNG(".png"),
        PDF(".pdf"),
        XLSX(".xlsx"),
        DOCX(".docx");

        private final String value;

        FILE_EXTENSION(final String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public String getValueWithoutDot() {
            return this.value.substring(1);
        }

        public FILE_EXTENSION extension() {
            return switch (this) {
                case JPG -> FILE_EXTENSION.JPG;
                case JPEG -> FILE_EXTENSION.JPEG;
                case PNG -> FILE_EXTENSION.PNG;
                case PDF -> FILE_EXTENSION.PDF;
                case XLSX -> FILE_EXTENSION.XLSX;
                case DOCX -> FILE_EXTENSION.DOCX;
                default -> throw new ErrorException("validation.file.error.extension");
            };
        }
    }

    public enum URI_SUFFIX {
        JPG("data:" + MIME_TYPE.JPG.getValue() + ";base64,"),
        JPEG("data:" + MIME_TYPE.JPEG.getValue() + ";base64,"),
        PNG("data:" + MIME_TYPE.PNG.getValue() + ";base64,"),
        PDF("data:" + MIME_TYPE.PDF + ";base64,"),
        XLSX("data:" + MIME_TYPE.XLSX + ";base64,"),
        DOCX("data:" + MIME_TYPE.DOCX + ";base64,");

        private final String value;

        URI_SUFFIX(final String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public static byte[] bytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] temp = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(temp)) != -1) {
            buffer.write(temp, 0, bytesRead);
        }
        return buffer.toByteArray();
    }

    public static byte[] byteArray(File file) throws IOException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Invalid file provided");
        }
        byte[] fileBytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead = fis.read(fileBytes);

            if (bytesRead != fileBytes.length) {
                throw new IOException("File read length mismatch");
            }
        }

        return fileBytes;
    }

    public static String fileContentType(String fileName) {
        String extension = extension(fileName);
        return switch (extension) {
            case ".jpg" -> MIME_TYPE.JPG.getValue();
            case ".png" -> MIME_TYPE.PNG.getValue();
            case ".pdf" -> MIME_TYPE.PDF.getValue();
            case ".xlsx" -> MIME_TYPE.XLSX.getValue();
            case ".docx" -> MIME_TYPE.DOCX.getValue();
            default -> "image/jpeg";
        };
    }

    public static String suffix(String fileName) {
        String extension = extension(fileName);
        return switch (extension) {
            case ".jpg" -> URI_SUFFIX.JPG.getValue();
            case ".png" -> URI_SUFFIX.PNG.getValue();
            case ".pdf" -> URI_SUFFIX.PDF.getValue();
            case ".xlsx" -> URI_SUFFIX.XLSX.getValue();
            case ".docx" -> URI_SUFFIX.DOCX.getValue();
            default -> URI_SUFFIX.JPEG.getValue();
        };
    }

    private static String extension(String fileName) {
        String extension = FILE_EXTENSION.JPEG.getValue();
        if (Strings.CS.endsWith(fileName, FILE_EXTENSION.JPG.value)) {
            extension = FILE_EXTENSION.JPG.getValue();
        } else if (Strings.CS.endsWith(fileName, FILE_EXTENSION.PNG.value)) {
            extension = FILE_EXTENSION.PNG.getValue();
        } else if (Strings.CS.endsWith(fileName, FILE_EXTENSION.PDF.value)) {
            extension = FILE_EXTENSION.PDF.getValue();
        } else if (Strings.CS.endsWith(fileName, FILE_EXTENSION.XLSX.value)) {
            extension = FILE_EXTENSION.XLSX.getValue();
        } else if (Strings.CS.endsWith(fileName, FILE_EXTENSION.DOCX.value)) {
            extension = FILE_EXTENSION.DOCX.getValue();
        }
        return extension;
    }

    public static String fileName(String path) {
        return Paths.get(path).getFileName().toString();
    }

    public enum ENTITY {
        USER;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

}
