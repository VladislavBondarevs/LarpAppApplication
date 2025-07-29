package de.larphelden.larp_app.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


import de.larphelden.larp_app.exceptions.StorageException;
import de.larphelden.larp_app.exceptions.StorageFileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService{

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties){

        if(properties.getLocation().trim().length() == 0) {
            throw new StorageException("File upload location can not be Empty");

        }
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void store(MultipartFile file, String subDirectoryName) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }

            Path subDirectory = this.rootLocation.resolve(subDirectoryName);
            if (!Files.exists(subDirectory)) {
                Files.createDirectories(subDirectory);
            }

            Path destinationFile = subDirectory.resolve(
                    Paths.get(file.getOriginalFilename())).normalize().toAbsolutePath();

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new StorageException("Failed to store file", e);
        }
    }
    @Override
    public List<String> storeMultiple(List<MultipartFile> files, String entityType, Long entityId) {
        List<String> savedFilePaths = new ArrayList<>();

        try {
            Path entityDirectory = this.rootLocation
                    .resolve(entityType)
                    .resolve(entityType + "_" + entityId);

            Files.createDirectories(entityDirectory);

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = file.getOriginalFilename();

                    Path destinationFile = entityDirectory.resolve(fileName).normalize();

                    if (!destinationFile.startsWith(entityDirectory)) {
                        throw new StorageException("Cannot store file outside current directory");
                    }

                    Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

                    savedFilePaths.add(entityType + "/" + entityType + "_" + entityId + "/" + fileName);
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to store files.", e);
        }
        return savedFilePaths;
    }



    @Override
    public Stream<Path> loadAll(){
        try{
            return Files.walk(this.rootLocation,1).filter(path ->!path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        } catch (IOException e) {
            throw new RuntimeException("failed to read stored files",e);
        }

    }
    @Override
    public Path load(String filename){
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }
    @Override
    public void deleteAll(){
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init(){
        try{
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage",e);
        }
    }
}
