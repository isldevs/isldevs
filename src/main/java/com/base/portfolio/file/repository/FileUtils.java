/*
 * Copyright 2025 iSLDevs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.base.portfolio.file.repository;


import com.base.core.exception.ErrorException;
import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpStatus;

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
        throw new ErrorException(HttpStatus.BAD_REQUEST, "msg.internal.error", "Entity type is unsupported", entity);
    }

    static InputStream resize(String extension, InputStream is) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); InputStream input = is) {
            BufferedImage src = ImageIO.read(input);
            if (src == null) {
                throw new ErrorException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "msg.internal.error",
                        "Can not read file content"
                );
            }

            int width = src.getWidth() / 2;
            int height = src.getHeight() / 2;

            BufferedImage resized;
            if ("png".equalsIgnoreCase(extension)) {
                resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            } else {
                resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            }

            Graphics2D g2d = resized.createGraphics();
            g2d.setComposite(AlphaComposite.Src);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.drawImage(src, 0, 0, width, height, null);
            g2d.dispose();

            ImageIO.write(resized, extension, bos);
            return new ByteArrayInputStream(bos.toByteArray());

        } catch (IOException e) {
            throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "msg.internal.error", "Resizing failed", e.getMessage());
        }
    }


    public static void isValidateMimeType(final String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "msg.internal.error", "MIME type cannot be null or empty", mimeType);
        }

        boolean isValid = Arrays.stream(MIME_TYPE.values()).anyMatch(type -> type.getValue().equalsIgnoreCase(mimeType));

        if (!isValid) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "msg.internal.error", "Unsupported MIME type", mimeType);
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
                default -> throw new ErrorException(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "msg.internal.error",
                        "Unsupported Media Type",
                        fileExtension
                );
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
                default -> throw new ErrorException(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "msg.internal.error",
                        "Unsupported Media Type"
                );
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
            throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR,"msg.internal.error", "Invalid file provided");
        }
        byte[] fileBytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead = fis.read(fileBytes);

            if (bytesRead != fileBytes.length) {
                throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR,"msg.internal.error", "File read length mismatch");
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
        USER,
        OFFICE;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

}
