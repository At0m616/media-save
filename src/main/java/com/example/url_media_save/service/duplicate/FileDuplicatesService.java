package com.example.url_media_save.service.duplicate;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FileDuplicatesService {

    public void deleteFilesSize(String folder, Integer sizeKb) {
        List<MdHashFile> folderFiles = findFilesInFolder(folder);

        folderFiles.stream().filter(f -> f.length() < sizeKb * 1000)
                .peek(file -> log.debug("File {} too small: {} kb", file.getName(), file.length()))
                .forEach(File::delete);
    }
    public int deleteDuplicatesInFolder(String folder) {
        List<MdHashFile> folderFiles = findFilesInFolder(folder);

        List<MdHashFile> uniqueFiles = folderFiles
                .stream()
                .distinct()
                .toList();

        List<MdHashFile> duplicates = new ArrayList<>();
        int count = 0;
        for (MdHashFile uniq : uniqueFiles) {
            for (MdHashFile all : folderFiles) {
                if (all.hashCode() == uniq.hashCode()) {
                    count++;
                }
                if (count > 1 && all.hashCode() == uniq.hashCode()) {
                    duplicates.add(all);
                }
            }
            count = 0;
        }
        return removeDuplicates(duplicates);
    }

    @SneakyThrows
    private List<MdHashFile> findFilesInFolder(String folder) {
        return Files.walk(Paths.get(folder))
                .parallel()
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .map(MdHashFile::new)
                .toList();

    }

    private int removeDuplicates(List<MdHashFile> duplicateFiles) {
        int deleteCount = 0;
        for (MdHashFile f : duplicateFiles) {
            if (f.delete()) {
                deleteCount++;
                log.debug(f.getName() + " был удален");
            } else {
                log.debug(f.getName() + " удалить не удалось");
            }
        }
        return deleteCount;
    }
}
