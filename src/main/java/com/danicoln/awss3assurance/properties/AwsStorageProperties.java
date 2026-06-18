package com.danicoln.awss3assurance.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.aws")
public class AwsStorageProperties {

    private String region = "us-east-2";
    private final S3Properties s3 = new S3Properties();
    private final DynamoDbProperties dynamodb = new DynamoDbProperties();
    private final SqsProperties sqs = new SqsProperties();

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public S3Properties getS3() {
        return s3;
    }

    public DynamoDbProperties getDynamodb() {
        return dynamodb;
    }

    public SqsProperties getSqs() {
        return sqs;
    }

    public static class S3Properties {
        private String bucket = "";

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }
    }

    public static class DynamoDbProperties {
        private String tableName = "";

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }
    }

    public static class SqsProperties {
        private String queueUrl = "";

        public String getQueueUrl() {
            return queueUrl;
        }

        public void setQueueUrl(String queueUrl) {
            this.queueUrl = queueUrl;
        }
    }
}
