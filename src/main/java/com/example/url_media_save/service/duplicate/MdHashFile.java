package com.example.url_media_save.service.duplicate;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Slf4j
@EqualsAndHashCode(callSuper = false)
public class MdHashFile extends File {
    private String hashMD;

    public MdHashFile(String pathname) {
        super(pathname);
        if (hashMD == null) {
            setHashMD();
        }
    }

    public MdHashFile(URI uri) {
        super(uri);
        if (hashMD == null) {
            setHashMD();
        }
    }

    /**
     * @return file MD5 hash
     */
    public String getHashMD() {
        return hashMD;
    }

    public void setHashMD() {
        this.hashMD = hashFuncMD5(this);
    }

    /**
     * @return size in MB
     */
    public double getFileSizeMB() {
        return super.length() / 1e6;
    }

    private String hashFuncMD5(MdHashFile file) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
        }
        byte[] fileInArray = new byte[(int) file.length()];

        Objects.requireNonNull(md).update(fileInArray);

        byte[] byteData = Objects.requireNonNull(md).digest();

        StringBuilder hexString = new StringBuilder();
        for (byte aByteData : byteData) {
            String hex = Integer.toHexString(0xff & aByteData);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
