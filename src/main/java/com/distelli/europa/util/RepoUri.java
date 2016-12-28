/*
  $Id: $
  @file RepoUri.java
  @brief Contains the RepoUri.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.util;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URISyntaxException;

import com.distelli.europa.models.*;
import lombok.extern.log4j.Log4j;
import org.eclipse.jgit.transport.URIish;

/**
   There are 3 main types of URIs that we work with

   1. Repo Homepage URI - https://github.com/Distelli/RsinghGithubTest
   2. Repo Commit Page Uri - https://github.com/Distelli/RsinghGithubTest/commit/04118f708c314d494fac38f1c08ff3c2ac24dd50
   3. Repo SSH Clone URI - git@github.com:Distelli/RsinghGithubTest.git
   4. Repo HTTP Clone URI - https://github.com/Distelli/RsinghGithubTest.git
*/
@Log4j
public class RepoUri
{
    protected RepositoryProvider _repoProvider;
    protected RepositoryType _repoType;
    protected String _host;
    protected String _scheme;
    protected int _port;
    protected String _authUser;
    protected String _repoName;
    protected String _repoOwner;

    private RepoUri() {}
    public static RepoUri fromString(String uri)
    {
        return fromString(uri, null);
    }

    public static RepositoryProvider toRepositorProvider(String repoProvider)
    {
        if(repoProvider == null)
            return null;
        if(repoProvider.equalsIgnoreCase(RepositoryProvider.GITHUB.toString()))
            return RepositoryProvider.GITHUB;
        if(repoProvider.equalsIgnoreCase(RepositoryProvider.BITBUCKET.toString()))
            return RepositoryProvider.BITBUCKET;
        if(repoProvider.equalsIgnoreCase(RepositoryProvider.BITBUCKETSERVER.toString()))
            return RepositoryProvider.BITBUCKETSERVER;
        if(repoProvider.equalsIgnoreCase(RepositoryProvider.GITHUBENTERPRISE.toString()))
            return RepositoryProvider.GITHUBENTERPRISE;
        if(repoProvider.equalsIgnoreCase(RepositoryProvider.GITLAB.toString()))
            return RepositoryProvider.GITLAB;
        return null;
    }

    public static class Builder
    {
        private int _port = -1;
        private String _repoName = null;
        private String _repoOwner = null;
        private String _host = null;
        private RepositoryProvider _repoProvider;
        private RepositoryType _repoType = null;

        public Builder withPort(int port) { _port = port; return this;}
        public Builder withRepoName(String repoName) { _repoName = repoName; return this;}
        public Builder withRepoOwner(String repoOwner) { _repoOwner = repoOwner; return this;}
        public Builder withHost(String host) { _host = host; return this;}
        public Builder withRepoProvider(RepositoryProvider repoProvider) { _repoProvider = repoProvider; return this;}
        public Builder withRepoType(RepositoryType repoType) { _repoType = repoType; return this;}

        public RepoUri build()
        {
            if(_repoName == null)
                throw(new IllegalStateException("Cannot build RepoUri without a repoName"));
            if(_repoOwner == null)
                throw(new IllegalStateException("Cannot build RepoUri without a repoOwner"));
            if(_repoProvider == null)
                throw(new IllegalStateException("Cannot build RepoUri without a repoProvider"));

            switch(_repoProvider)
            {
            case GITHUB:
                if(_host != null)
                    throw(new IllegalStateException("Cannot set host for Github RepoUri"));
                _host = "github.com";
                _repoType = RepositoryType.GIT;
                break;
            case BITBUCKET:
                if(_host != null)
                    throw(new IllegalStateException("Cannot set host for Bitbucket RepoUri"));
                _host = "bitbucket.org";
                break;
            case GITLAB:
                if(_host != null)
                    throw(new IllegalStateException("Cannot set host for Gitlab RepoUri"));
                _host = "gitlab.com";
                _repoType = RepositoryType.GIT;
                break;
            case BITBUCKETSERVER:
                if(_host == null)
                    throw(new IllegalStateException("Cannot create Bitbucket Server RepoUri without a host"));
                break;
            case GITHUBENTERPRISE:
                if(_host == null)
                    throw(new IllegalStateException("Cannot create Bitbucket Server RepoUri without a host"));
                if(_repoType != null)
                    throw(new IllegalStateException("Cannot set repo type for Github Enterprise RepoUri"));
                _repoType = RepositoryType.GIT;
                break;
            default:
                return null;
            }

            RepoUri repoUri = new RepoUri();
            repoUri._repoProvider = _repoProvider;
            repoUri._host = _host;
            repoUri._scheme = "https";
            repoUri._repoName = _repoName;
            repoUri._repoOwner = _repoOwner;
            repoUri._repoType = _repoType;
            repoUri._port = _port;
            return repoUri;
        }
    }

    public static RepoUri fromString(String uri, RepositoryProvider repoProvider)
    {
        RepoUri repoUri = new RepoUri();
        URIish uriIsh = null;
        try {
            uriIsh = new URIish(uri);
        } catch(URISyntaxException use) {
            throw(new IllegalArgumentException("Invalid URI: "+uri));
        }
        repoUri._host = uriIsh.getHost();
        if("github.com".equalsIgnoreCase(repoUri._host))
            repoUri._repoProvider = RepositoryProvider.GITHUB;
        else if("gitlab.com".equalsIgnoreCase(repoUri._host))
            repoUri._repoProvider = RepositoryProvider.GITLAB;
        else if("bitbucket.org".equalsIgnoreCase(repoUri._host))
            repoUri._repoProvider = RepositoryProvider.BITBUCKET;

        String path = uriIsh.getPath();
        if(path != null && path.startsWith("/"))
            path = path.substring(1);

        repoUri._port = uriIsh.getPort();
        String scheme = uriIsh.getScheme();
        repoUri._authUser = uriIsh.getUser();
        if(scheme == null)
        {
            if(uri.startsWith("git@"))
            {
                repoUri._repoType = RepositoryType.GIT;
                scheme = "ssh";
            }
            else if(uri.startsWith("hg@"))
            {
                repoUri._repoType = RepositoryType.HG;
                scheme = "ssh";
            }
        }
        if(repoUri._repoType == null)
        {
            if(path.endsWith(".git"))
                repoUri._repoType = RepositoryType.GIT;
            else if("github.com".equalsIgnoreCase(repoUri._host))
                repoUri._repoType = RepositoryType.GIT;
            else if("gitlab.com".equalsIgnoreCase(repoUri._host))
                repoUri._repoType = RepositoryType.GIT;
            else if(repoUri._authUser != null)
            {
                if(repoUri._authUser.equalsIgnoreCase("git"))
                    repoUri._repoType = RepositoryType.GIT;
                else if(repoUri._authUser.equalsIgnoreCase("hg"))
                    repoUri._repoType = RepositoryType.HG;
            }
        }
        repoUri._scheme = scheme;

        Path pathObj = Paths.get(path);
        String repoOwner = null;
        String repoName = null;
        Path repoOwnerPath = pathObj.getName(0);
        if(repoOwnerPath != null)
            repoOwner = repoOwnerPath.toString();
        Path repoNamePath = pathObj.getName(1);
        if(repoNamePath != null)
        {
            repoName = repoNamePath.toString();
            if(repoName.endsWith(".git"))
                repoName = repoName.substring(0, repoName.length() - 4);
        }

        if(repoUri._repoProvider == null && repoProvider != null)
            repoUri._repoProvider = repoProvider;
        repoUri._repoOwner = repoOwner;
        repoUri._repoName = repoName;
        return repoUri;
    }

    public String toString()
    {
        return String.format("RepoUri[scheme=%s, host=%s, port=%d, authUser=%s, repoType=%s, repoName=%s, repoOwner=%s, repoProvider=%s",
                             _scheme,
                             _host,
                             _port,
                             _authUser,
                             _repoType,
                             _repoName,
                             _repoOwner,
                             _repoProvider);
    }

    public String toSshCloneUri()
    {
        if(_repoType == RepositoryType.GIT)
            return "git@"+_host+":"+_repoOwner+"/"+_repoName+".git";
        else
            return "ssh://hg@"+_host+"/"+_repoOwner+"/"+_repoName;
    }

    public String toHttpsCloneUri()
    {
        if(_repoType == RepositoryType.GIT)
            return "https://"+_host+"/"+_repoOwner+"/"+_repoName+".git";
        else {
            if(_authUser != null)
                return "https://"+_authUser+"@"+_host+"/"+_repoOwner+"/"+_repoName;
            else
                return "https://"+_host+"/"+_repoOwner+"/"+_repoName;
        }
    }

    public String toRepoWebUrl()
    {
        try {
            String path = "/"+_repoOwner+"/"+_repoName;
            String scheme = _scheme;
            if(_scheme == null || _scheme.equalsIgnoreCase("ssh"))
                scheme = "https";
            URI uri = new URI(scheme,
                              null,
                              getHost(),
                              getPort(),
                              path,
                              null,
                              null);
            return uri.toString();
        } catch(URISyntaxException use) {
            throw(new IllegalStateException(use));
        }
    }

    public String toCommitUrl(String commitId)
    {
        try {
            String path = "/"+_repoOwner+"/"+_repoName;
            if(_repoProvider == RepositoryProvider.GITHUB)
                path = path+"/commit/";
            else if(_repoProvider == RepositoryProvider.BITBUCKET)
                path = path+"/commits/";
            else if(_repoProvider == RepositoryProvider.GITHUBENTERPRISE)
                path = path+"/commit/";
            else if(_repoProvider == RepositoryProvider.GITLAB)
                path = path+"/commit/";
            else if(_repoProvider == RepositoryProvider.BITBUCKETSERVER)
                path = path+"/commits/";
            else
                path = path+"/commit/";
            if(commitId != null)
                path = path+commitId;
            String scheme = _scheme;
            if(_scheme == null || _scheme.equalsIgnoreCase("ssh"))
                scheme = "https";
            URI uri = new URI(scheme,
                              null,
                              getHost(),
                              getPort(),
                              path,
                              null,
                              null);
            return uri.toString();
        } catch(URISyntaxException use) {
            throw(new IllegalStateException(use));
        }
    }

    public String toCommitUrlBase()
    {
        return toCommitUrl(null);
    }

    public final String getHost() {
        return this._host;
    }

    public final String getScheme() {
        return this._scheme;
    }

    public final int getPort() {
        return this._port;
    }

    public final String getAuthUser() {
        return this._authUser;
    }

    public final RepositoryType getRepoType() {
        return this._repoType;
    }

    public final RepositoryProvider getRepoProvider() {
        return this._repoProvider;
    }

    public final String getRepoName() {
        return this._repoName;
    }

    public final String getRepoOwner() {
        return this._repoOwner;
    }
}
