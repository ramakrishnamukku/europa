/*
  $Id: $
  @file AjaxErrors.java
  @brief Contains the AjaxErrors.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.ajax;

public class AjaxErrors
{
    public static final class Codes {
        public static final String RepoAlreadyExists = "RepoAlreadyExists";
        public static final String PipelineAlreadyExists = "PipelineAlreadyExists";
        public static final String RepoAlreadyConnected = "RepoAlreadyConnected";
        public static final String TokenIsActive = "TokenIsActive";
        public static final String RepoNotFound = "RepoNotFound";
        public static final String BadRepoName = "BadRepoName";
        public static final String BadPipelineName = "BadPipelineName";
        public static final String BadS3Credentials = "BadS3Credentials";
        public static final String BadS3Bucket = "BadS3Bucket";
        public static final String BadS3Settings = "BadS3Settings";
        public static final String WebhookDeliveryFailed = "WebhookDeliveryFailed";
    }
}
