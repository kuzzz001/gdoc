package com.gdoc.server.controller;

import com.gdoc.common.result.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.upload.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final int AVATAR_MAX_SIZE = 512;

    @PostMapping("/upload")
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error(400, "请选择要上传的文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ApiResponse.error(400, "只能上传图片文件");
        }

        String[] allowedTypes = {"image/jpeg", "image/png", "image/gif", "image/webp"};
        boolean allowed = false;
        for (String type : allowedTypes) {
            if (type.equals(contentType)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            return ApiResponse.error(400, "不支持的图片格式，请上传 JPG 或 PNG 格式");
        }

        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return ApiResponse.error(400, "文件大小不能超过 5MB");
        }

        try {
            byte[] processedImage = compressAndResizeAvatar(file.getBytes(), contentType);

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String extension = ".jpg";
            if ("image/png".equals(contentType)) {
                extension = ".png";
            }

            String filename = UUID.randomUUID().toString().replace("-", "") + extension;
            Path filePath = uploadPath.resolve(filename);
            Files.write(filePath, processedImage);

            String fileUrl = baseUrl + "/" + uploadDir + "/" + filename;
            log.info("文件上传成功: {}", fileUrl);

            return ApiResponse.success("上传成功", fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ApiResponse.error(500, "文件上传失败，请重试");
        }
    }

    private byte[] compressAndResizeAvatar(byte[] imageData, String contentType) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        BufferedImage originalImage = ImageIO.read(bais);

        if (originalImage == null) {
            return imageData;
        }

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        int cropSize = Math.min(width, height);
        int x = (width - cropSize) / 2;
        int y = (height - cropSize) / 2;
        BufferedImage croppedImage = originalImage.getSubimage(x, y, cropSize, cropSize);

        int targetSize = Math.min(cropSize, AVATAR_MAX_SIZE);
        BufferedImage resizedImage = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(croppedImage, 0, 0, targetSize, targetSize, null);
        g.dispose();

        String formatName = "image/png".equals(contentType) ? "png" : "jpg";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if ("jpg".equals(formatName)) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (writers.hasNext()) {
                ImageWriter writer = writers.next();
                ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.85f);
                ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
                writer.setOutput(ios);
                writer.write(null, new IIOImage(resizedImage, null, null), param);
                writer.dispose();
                ios.close();
            } else {
                ImageIO.write(resizedImage, "jpg", baos);
            }
        } else {
            ImageIO.write(resizedImage, "png", baos);
        }

        byte[] result = baos.toByteArray();
        log.info("头像处理: 原始 {}x{} -> 裁剪+缩放 {}x{} -> 压缩 {}KB -> {}KB",
                width, height, targetSize, targetSize,
                imageData.length / 1024, result.length / 1024);

        return result;
    }
}