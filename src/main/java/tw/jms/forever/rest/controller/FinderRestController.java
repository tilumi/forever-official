package tw.jms.forever.rest.controller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tw.jms.forever.model.FileMeta;
import tw.jms.forever.util.EnvKeys;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/rest/finder")
public class FinderRestController {

    static Logger LOG = LogManager.getLogger(FinderRestController.class
            .getName());

    @Autowired
    Environment env;
    private String rootDir;

    @PostConstruct
    public void init() {
        this.rootDir = env.getProperty(EnvKeys.FILE_SYSTEM_ROOT_DIR);
    }

    @RequestMapping(value = "/list.html/", method = RequestMethod.GET)
    public List<FileMeta> listFiles(
            @RequestParam(value = "dir", defaultValue = "") String dirName)
            throws IOException {
        List<FileMeta> result = new ArrayList<FileMeta>();
        String fullPath = FilenameUtils.concat(rootDir, dirName);
        if (fullPath == null || !fullPath.startsWith(rootDir)) {
            throw new IOException(fullPath + " is not valid");
        }
        File directory = new File(FilenameUtils.concat(rootDir, dirName));
        File[] files = directory.listFiles();
        if (!directory.getAbsolutePath().equals(
                new File(rootDir).getAbsolutePath())) {
            FileMeta upDir = new FileMeta();
            upDir.setIsDir(true);
            upDir.setName("up");
            upDir.setPath(new File(rootDir).toURI()
                    .relativize(directory.getParentFile().toURI()).getPath());
            result.add(upDir);
        }
        for (File file : files) {
            FileMeta fileMeta = new FileMeta();
            fileMeta.setIsDir(file.isDirectory());
            fileMeta.setName(file.getName());
            fileMeta.setPath(new File(rootDir).toURI().relativize(file.toURI())
                    .getPath());
            result.add(fileMeta);
        }
        return result;
    }

    @RequestMapping(value = "/mkdir.html/", method = RequestMethod.POST)
    public boolean createDir(@RequestParam("path") String path, @RequestParam("dirName") String dirName)
            throws IOException {
        String fullPath = FilenameUtils.concat(FilenameUtils.concat(rootDir, path),dirName);
        if (fullPath == null || !fullPath.startsWith(rootDir)) {
            throw new IOException(path + " is not valid");
        }
        FileUtils.forceMkdir(new File(fullPath));
        return true;
    }

    @RequestMapping(value = "/upload.html/", method = RequestMethod.POST)
    public boolean upload(@RequestParam("dir") String dir,
                          @RequestParam("file") MultipartFile file) throws IOException {
        String fullPath = FilenameUtils.concat(rootDir, FilenameUtils.concat(dir, file.getOriginalFilename()));
        if (fullPath == null || !fullPath.startsWith(rootDir)) {
            throw new IOException(fullPath + " is not valid");
        }
        if (!file.isEmpty()) {
            byte[] bytes = file.getBytes();
            BufferedOutputStream stream = new BufferedOutputStream(
                    new FileOutputStream(new File(fullPath)));
            stream.write(bytes);
            stream.close();
            return true;
        }
        return false;
    }

    @RequestMapping(value = "/delete.html/", method = RequestMethod.POST)
    public boolean delete(@RequestParam("path") String path) throws IOException {
        String fullPath = FilenameUtils.concat(rootDir, path);
        if (fullPath == null || !fullPath.startsWith(rootDir)) {
            throw new IOException(path + " is not valid");
        }
        FileUtils.forceDelete(new File(fullPath));
        return true;
    }

    @RequestMapping(value = "/download.html/", method = RequestMethod.GET)
    public void download(@RequestParam("path") String path, HttpServletRequest request,HttpServletResponse response) throws IOException {
        String fullPath = FilenameUtils.concat(rootDir, path);
        if (fullPath == null || !fullPath.startsWith(rootDir)) {
            throw new IOException(path + " is not valid");
        }
        File file = new File(fullPath);
        FileInputStream in = new FileInputStream(new File(fullPath));

        ServletContext context = request.getServletContext();
        String mimeType = context.getMimeType(fullPath);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        response.setContentType(mimeType);
        response.setContentLength((int) file.length());
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"",
                URLEncoder.encode(file.getName(), "UTF-8"));
        response.setHeader(headerKey, headerValue);
        OutputStream out = response.getOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        in.close();
        out.close();

    }

    @RequestMapping(value = "/rename.html/", method = RequestMethod.POST)
    public boolean rename(@RequestParam("path") String path, @RequestParam("newName") String newName) throws IOException {
        String fullPath = FilenameUtils.concat(rootDir, path);
        if (fullPath == null || !fullPath.startsWith(rootDir)) {
            throw new IOException(path + " is not valid");
        }
        String newFullPath = FilenameUtils.concat(new File(fullPath).getParent(), newName);
        new File(fullPath).renameTo(new File(newFullPath));
        return true;
    }


}
