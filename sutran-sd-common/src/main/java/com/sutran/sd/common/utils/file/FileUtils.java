package com.sutran.sd.common.utils.file;

import cn.hutool.core.io.FileUtil;
import com.sutran.sd.common.exception.ServiceException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.compress.utils.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Stream;

/**
 * 文件处理工具类
 *
 * @author Lion Li
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils extends FileUtil {
    public static final String[] IMAGE_EXTENSIONS = {
        "jpg","JPG","jpeg","JPEG","png","PNG","gif","GIF","bmp","BMP","webp","WEBP","tiff","TIFF","tif","TIF"
    };

    /**
     * 下载文件名重新编码
     *
     * @param response     响应对象
     * @param realFileName 真实文件名
     */
    public static void setAttachmentResponseHeader(HttpServletResponse response, String realFileName) throws UnsupportedEncodingException {
        String percentEncodedFileName = percentEncode(realFileName);

        String contentDispositionValue = "attachment; filename=" +
            percentEncodedFileName +
            ";" +
            "filename*=" +
            "utf-8''" +
            percentEncodedFileName;

        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition,download-filename");
        response.setHeader("Content-disposition", contentDispositionValue);
        response.setHeader("download-filename", percentEncodedFileName);
    }

    /**
     * 百分号编码工具方法
     *
     * @param s 需要百分号编码的字符串
     * @return 百分号编码后的字符串
     */
    public static String percentEncode(String s) throws UnsupportedEncodingException {
        String encode = URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
        return encode.replaceAll("\\+", "%20");
    }

    /**
     * 图片压缩(等比缩放)
     * @param inputStream   文件输入流
     * @param desFileSize   目标文件大小(单位b，例：300kb = 300 * 1024)
     * @param accuracy      压缩比(例：0.8)
     * @Return: void
     **/
    public static InputStream compressPicCycle(InputStream inputStream, long desFileSize, double accuracy) {
        try{
            ByteArrayOutputStream outputStream = cloneInputStream(inputStream);
            // 用于读取长度的原始数据流
            InputStream readStream = new ByteArrayInputStream(outputStream.toByteArray());
            long l = readFileInputStreamLength(readStream);
            System.out.println(l);
            //如果小于指定大小不压缩；如果大于等于指定大小压缩
            if (l <= desFileSize) {
                return new ByteArrayInputStream(outputStream.toByteArray());
            }

            // 用于数据处理的原始数据流
            InputStream imageStream = new ByteArrayInputStream(outputStream.toByteArray());
            InputStream dataStream = new ByteArrayInputStream(outputStream.toByteArray());
            // 压缩后的输出流
            ByteArrayOutputStream returnOutputStream = new ByteArrayOutputStream();
            // 计算宽高
            BufferedImage bim = ImageIO.read(imageStream);
            int desWidth = new BigDecimal(bim.getWidth()).multiply(new BigDecimal(accuracy)).intValue();
            int desHeight = new BigDecimal(bim.getHeight()).multiply(new BigDecimal(accuracy)).intValue();
            Thumbnails.of(dataStream).scale(1f).size(desWidth, desHeight).outputQuality(accuracy).toOutputStream(returnOutputStream);
            return compressPicCycle(new ByteArrayInputStream(returnOutputStream.toByteArray()), desFileSize, accuracy);
        }
        catch (Exception e) {
            e.printStackTrace();
            return inputStream;
        }
    }

    /**
     * 图片压缩(压缩大小，不改宽高)
     * @param oldInputStream    原始文件输入流
     * @param accuracy          图片质量(0-1之间，1为最好)
     * @return
     */
    public static InputStream compressPic(InputStream oldInputStream, double accuracy) {
        try{
            // 生成一个临时字节数组，用于创建输入流(输入流只能使用一次)
            byte[] imageBytes = IOUtils.toByteArray(oldInputStream);

            ByteArrayInputStream byteInput = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(byteInput);
            // 如果图片空，返回空
            if (image == null) {
                return null;
            }
            final long srcSize = imageBytes.length;
            log.info("压缩前图片大小：{} B",srcSize);
            ByteArrayInputStream dataStream = new ByteArrayInputStream(imageBytes);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(dataStream).scale(1f).outputQuality(accuracy).outputFormat("jpg")
                .toOutputStream(outputStream);
            imageBytes = outputStream.toByteArray();
            log.info("压缩后图片大小：{} B",imageBytes.length);
            return new ByteArrayInputStream(imageBytes);
        }
        catch (Exception e) {
            e.printStackTrace();
            return oldInputStream;
        }
    }

    /** 计算压缩精度 **/
    private static double getAccuracy(long size, long desFileSize) {
        if (size<=desFileSize) {
            return 1;
        }
        return (double) desFileSize / size;
//        double accuracy = desFileSize / size;
//        //图片大小小于3M,压缩精度为0.44;否则精度为0.1
//        if (size <= 3072 * 1024) {
//            accuracy = 0.1;
//        } else {
//            accuracy = 0.1;
//        }
//        return accuracy;
    }

    private static ByteArrayOutputStream cloneInputStream(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static long readFileInputStreamLength(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        long len = 0;
        int bytesRead = 0;
        while ((bytesRead=inputStream.read(buffer))!=-1) {
            len += bytesRead;
        }
        return len;
    }

    public static File getGridFile(String gridsUrl) {
        File gridFile = null;
        File[] files = new File(gridsUrl).listFiles();
        if (files!=null) {
            List<File> list1 = new ArrayList<>(Arrays.asList(files));
            list1.sort((file, newFile) -> Long.compare(newFile.lastModified(), file.lastModified()));
            File[] files1 = list1.get(0).listFiles();
            if (files1!=null) {
                List<File> list = new ArrayList<>(Arrays.asList(files1));
                list.sort((file, newFile) -> Long.compare(newFile.lastModified(), file.lastModified()));
                gridFile = list.get(0);
            }
        }
        return gridFile;
    }

    public static InputStream base64ToInputStream(String base64) {
        base64 = base64.replace("data:image/jpeg;base64,","");
        ByteArrayInputStream stream = null;
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            stream = new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stream;
    }

    public static String inputStreamToBase64(InputStream inputStream) {
        if (inputStream==null) {
            throw new ServiceException("初始图片获取异常");
        }
        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new ServiceException("初始图片转base64失败");
        }
    }

    public static File inputStreamToTempFile(InputStream inputStream, String fileUrl) {
        try {
            String prefixName = fileUrl.substring(fileUrl.lastIndexOf("."));
            File file = File.createTempFile("tempFile","."+prefixName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            IOUtils.copy(inputStream,fileOutputStream);
            return file;
        } catch (IOException e) {
            log.error("stream流转file异常：", e);
            return null;
        }
    }

    public static File multipartFileToTempFile(MultipartFile multipartFile, String fileName, boolean isModifyFileName) {
        // 参数校验
        if (multipartFile == null) {
            log.error("文件不能不能为null");
            return null;
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            log.error("文件名不能为null或空");
            return null;
        }
        try {
            // 提取文件扩展名
            String prefixName = fileName.substring(fileName.lastIndexOf("."));
            // 获取文件名称(不包含扩展名)
            String fileNameNoExtension = fileName.substring(0, fileName.lastIndexOf("."));
            File file;
            if (isModifyFileName) {
                file = File.createTempFile("temp_",prefixName);
            }
            else {
                file = File.createTempFile(fileNameNoExtension+"_",prefixName);
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            IOUtils.copy(multipartFile.getInputStream(),fileOutputStream);
            return file;
        } catch (IOException e) {
            log.error("stream流转file异常：", e);
            return null;
        }
    }

    public static File bytesToTempFile(byte[] bytes, String fileName, boolean isModifyFileName) {
        // 参数校验
        if (bytes == null) {
            log.error("字节数组不能为null");
            return null;
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            log.error("文件名不能为null或空");
            return null;
        }
        File file = null;
        try {
            // 提取文件扩展名
            String prefixName = fileName.substring(fileName.lastIndexOf("."));
            // 获取文件名称(不包含扩展名)
            String fileNameNoExtension = fileName.substring(0, fileName.lastIndexOf("."));
            if (isModifyFileName) {
                file = File.createTempFile("temp_",prefixName);
            }
            else {
                file = File.createTempFile(fileNameNoExtension+"_",prefixName);
            }

            // 写入文件内容
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(bytes);
                fos.flush();
            }
            return file;
        }
        catch (IOException e) {
            log.error("字节流转文件异常，文件名：{}", fileName, e);
            // 如果创建文件过程中出现异常，删除可能已创建的不完整文件
            if (file != null && file.exists()) {
                if (!file.delete()) {
                    log.warn("无法删除临时文件：{}", file.getAbsolutePath());
                }
            }
            return null;
        }
    }

    public static void deleteFile(File file) {
        if (file==null) {
            return;
        }
        try{
            file.deleteOnExit();
        }
        catch (Exception ignored) {}
    }

    /**
     * 使用Stream API删除目录（Java 8+）
     * @param directoryPath 目录路径
     * @return 删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String directoryPath) {
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            log.error("目录不存在或不是目录: {}", directoryPath);
            return false;
        }

        try (Stream<Path> pathStream = Files.walk(directory)) {
            // 按反向顺序删除（先文件后目录）
            pathStream.sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        log.error("删除文件失败: {} - {}", path, e.getMessage(), e);
                    }
                });

            return true;

        }
        catch (IOException e) {
            log.error("删除目录时发生错误: {} - {}", directoryPath, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 移动指定目录中的所有文件到目标目录
     * @param sourceDir 源目录路径
     * @param targetDir 目标目录路径
     * @throws IOException 如果移动过程中发生错误
     */
    public static void moveAllFiles(@NotNull String sourceDir, @NotNull String targetDir) throws IOException {
        Path sourcePath = Paths.get(sourceDir);
        Path targetPath = Paths.get(targetDir);

        // 检查源目录是否存在
        if (!Files.exists(sourcePath) || !Files.isDirectory(sourcePath)) {
            throw new IOException("源目录不存在或不是目录: " + sourceDir);
        }

        // 创建目标目录（如果不存在）
        if (!Files.exists(targetPath)) {
            Files.createDirectories(targetPath);
        }

        // 遍历源目录中的所有文件
        Files.walkFileTree(sourcePath, EnumSet.noneOf(FileVisitOption.class), 1,
            new SimpleFileVisitor<Path>() {
                @NotNull
                @Override
                public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) {
                    // 跳过目录，只处理文件
                    if (!Files.isDirectory(file)) {
                        Path targetFile = targetPath.resolve(file.getFileName());

                        try {
                            // 如果目标文件已存在，先删除
                            if (Files.exists(targetFile)) {
                                Files.delete(targetFile);
                            }
                            // 移动文件
                            Files.move(file, targetFile, StandardCopyOption.REPLACE_EXISTING);

                        } catch (IOException e) {
                            log.error("移动文件失败: {} - {}", file, e.getMessage(), e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @NotNull
                @Override
                public FileVisitResult visitFileFailed(@NotNull Path file, @NotNull IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
    }

    /**
     * 递归移动目录中的所有文件和子目录
     * @param sourceDir 源目录路径
     * @param targetDir 目标目录路径
     * @throws IOException 如果移动过程中发生错误
     */
    public static void moveAllFilesRecursive(String sourceDir, String targetDir) throws IOException {
        Path sourcePath = Paths.get(sourceDir);
        Path targetPath = Paths.get(targetDir);

        if (!Files.exists(sourcePath) || !Files.isDirectory(sourcePath)) {
            throw new IOException("源目录不存在或不是目录: " + sourceDir);
        }

        if (!Files.exists(targetPath)) {
            Files.createDirectories(targetPath);
        }

        Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativePath = sourcePath.relativize(file);
                Path targetFile = targetPath.resolve(relativePath);

                // 创建目标文件的父目录
                Files.createDirectories(targetFile.getParent());

                try {
                    Files.move(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("移动文件: " + file + " -> " + targetFile);
                } catch (IOException e) {
                    System.err.println("移动文件失败: " + file + " - " + e.getMessage());
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (!dir.equals(sourcePath)) {
                    Path relativePath = sourcePath.relativize(dir);
                    Path targetDirPath = targetPath.resolve(relativePath);
                    Files.createDirectories(targetDirPath);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                // 删除空目录
                if (!dir.equals(sourcePath)) {
                    try {
                        Files.deleteIfExists(dir);
                        System.out.println("删除空目录: " + dir);
                    } catch (DirectoryNotEmptyException e) {
                        // 目录不为空，不删除
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                System.err.println("访问文件失败: " + file + " - " + exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static String getFirstImageByCreationTime(String directoryPath){
        try{
            Path dir = Paths.get(directoryPath);

            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                return null;
            }
            List<Path> imageFiles = new ArrayList<>();
            // 使用 Files.walk 遍历目录
            Files.walk(dir, 1) // 1 表示只遍历当前目录，不包含子目录
                .filter(Files::isRegularFile)
                .filter(e->isImageFile(e))
                .forEach(imageFiles::add);
            // 按创建时间排序（最早的在前）
            imageFiles.sort(Comparator.comparing(e->getCreationTime(e)));
            return imageFiles.isEmpty() ? null : imageFiles.get(0).getFileName().toString();
        }
        catch (Exception e){
            log.error("获取第一张图片异常：",e);
            return null;
        }
    }

    // 获取文件的创建时间
    private static FileTime getCreationTime(Path file) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
            return attrs.creationTime();
        } catch (IOException e) {
            return FileTime.fromMillis(0); // 如果无法获取创建时间，返回默认值
        }
    }

    // 检查文件是否为图片文件
    private static boolean isImageFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        for (String ext : IMAGE_EXTENSIONS) {
            if (fileName.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }
}
