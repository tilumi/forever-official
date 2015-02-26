package tw.jms.forever.rest.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import tw.jms.forever.model.FileMeta;
import tw.jms.forever.util.EnvKeys;

@RestController
@RequestMapping("/rest/finder")
public class FinderRestController {

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
        String path = FilenameUtils.concat(rootDir, dirName);
        if (path == null || !path.startsWith(rootDir)) {
            throw new IOException(path + " is not valid");
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
    public boolean createDir(@RequestParam("dir") String dirName)
            throws IOException {
        String path = FilenameUtils.concat(rootDir, dirName);
        if (path == null || !path.startsWith(rootDir)) {
            throw new IOException(path + " is not valid");
        }
        FileUtils.forceMkdir(new File(path));
        return true;
    }

    @RequestMapping(value = "/upload.html/", method = RequestMethod.POST)
    public boolean upload(@RequestParam("dir") String dir,
                          @RequestParam("file") MultipartFile file) throws IOException {
        String path = FilenameUtils.concat(rootDir, FilenameUtils.concat(dir, file.getOriginalFilename()));
        if (path == null || !path.startsWith(rootDir)) {
            throw new IOException(path + " is not valid");
        }
        if (!file.isEmpty()) {
            byte[] bytes = file.getBytes();
            BufferedOutputStream stream = new BufferedOutputStream(
                    new FileOutputStream(new File(path)));
            stream.write(bytes);
            stream.close();
            return true;
        }
        return false;
    }

}
