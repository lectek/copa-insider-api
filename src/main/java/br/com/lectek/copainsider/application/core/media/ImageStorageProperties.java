package br.com.lectek.copainsider.application.core.media;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.media")
public class ImageStorageProperties {

    private String provider = "local";
    private String dir = "media/products";
    private String publicBase = "/media/products";
    private String userDir = "media/users";
    private String userPublicBase = "/media/users";
    private final S3 s3 = new S3();

    public String getProvider() {
        return this.provider;
    }

    public void setProvider(final String provider) {
        this.provider = provider;
    }

    public String getDir() {
        return this.dir;
    }

    public void setDir(final String dir) {
        this.dir = dir;
    }

    public String getPublicBase() {
        return this.publicBase;
    }

    public void setPublicBase(final String publicBase) {
        this.publicBase = publicBase;
    }

    public String getUserDir() {
        return this.userDir;
    }

    public void setUserDir(final String userDir) {
        this.userDir = userDir;
    }

    public String getUserPublicBase() {
        return this.userPublicBase;
    }

    public void setUserPublicBase(final String userPublicBase) {
        this.userPublicBase = userPublicBase;
    }

    public S3 getS3() {
        return this.s3;
    }

    public boolean isS3Provider() {
        return this.provider != null && "s3".equalsIgnoreCase(this.provider.trim());
    }

    public boolean isLocalProvider() {
        return !isS3Provider();
    }

    public static class S3 {

        private String bucket = "";
        private String region = "us-east-1";
        private String endpoint = "";
        private String accessKey = "";
        private String secretKey = "";
        private boolean pathStyleAccess = true;
        private String productPrefix = "products";
        private String userPrefix = "users";

        public String getBucket() {
            return this.bucket;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public String getRegion() {
            return this.region;
        }

        public void setRegion(final String region) {
            this.region = region;
        }

        public String getEndpoint() {
            return this.endpoint;
        }

        public void setEndpoint(final String endpoint) {
            this.endpoint = endpoint;
        }

        public String getAccessKey() {
            return this.accessKey;
        }

        public void setAccessKey(final String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return this.secretKey;
        }

        public void setSecretKey(final String secretKey) {
            this.secretKey = secretKey;
        }

        public boolean isPathStyleAccess() {
            return this.pathStyleAccess;
        }

        public void setPathStyleAccess(final boolean pathStyleAccess) {
            this.pathStyleAccess = pathStyleAccess;
        }

        public String getProductPrefix() {
            return this.productPrefix;
        }

        public void setProductPrefix(final String productPrefix) {
            this.productPrefix = productPrefix;
        }

        public String getUserPrefix() {
            return this.userPrefix;
        }

        public void setUserPrefix(final String userPrefix) {
            this.userPrefix = userPrefix;
        }
    }
}
