/*
  $Id: $
  @file ProjectDb.java
  @brief Contains the ProjectDb.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.db;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.RollbackException;

import org.apache.log4j.Logger;
//import com.distelli.europa.models.*;
import com.distelli.persistence.ConvertMarker;
import com.distelli.persistence.Index;
import com.distelli.persistence.Index;
import com.distelli.persistence.PageIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.distelli.jackson.transform.TransformModule;

/**
   This class is commented out because its an example of how to use
   the persistence library
 */
public class ProjectDb
{
    // private static final Logger log = Logger.getLogger(ProjectDb.class);

    // private Index<Project> _main;

    // public static final int MAX_PROJECT_NAME_LENGTH = 32;
    // private static final ObjectMapper om = new ObjectMapper();
    // private final Pattern projectNamePattern = Pattern.compile("[a-zA-Z0-9_-]+");

    // private static TransformModule createTransforms(TransformModule module) {
    //     module.createTransform(Project.class)
    //     .put("hk", String.class,
    //          (item) -> getHashKey())
    //     .put("rk", String.class,
    //          (item) -> item.getName().toLowerCase())
    //     .put("namem", String.class, "name")
    //     .put("desc", String.class, "desc")
    //     .put("ctime", Long.class, "created");
    //     return module;
    // }

    // private static final String getHashKey()
    // {
    //     return "17a09ef8";
    // }

    // @Inject
    // protected ProjectDb(Index.Factory indexFactory, ConvertMarker.Factory convertMarkerFactory) {
    //     om.registerModule(createTransforms(new TransformModule()));
    //     _main = indexFactory.create(Project.class)
    //     .withTableName("projects.europa") //TODO: Add the prefix
    //     .withNoEncrypt("hk", "rk")
    //     .withHashKeyName("hk")
    //     .withRangeKeyName("rk")
    //     .withConvertValue(om::convertValue)
    //     .withConvertMarker(convertMarkerFactory.create("hk", "rk"))
    //     .build();
    // }

    // public void save(Project project)
    //     throws EntityExistsException
    // {
    //     if(!isValidProjectName(project.getName()))
    //         throw(new IllegalArgumentException("Invalid Project Name: "+project.getName()));
    //     _main.putItemOrThrow(project);
    // }

    // public Project queryByName(String name)
    // {
    //     return _main.getItem(getHashKey(),
    //                          name.toLowerCase());
    // }

    // public List<Project> listProjects(PageIterator pageIterator)
    // {
    //     return _main.queryItems(getHashKey(),
    //                             pageIterator).list();
    // }

    // public boolean isValidProjectName(String projectName)
    // {
    //     /**
    //        Rules:
    //        If its null its invalid
    //        If its zero-length then its invalid
    //        If it contains any other letter other than A-Za-z0-9
    //     */
    //     if(projectName == null)
    //         return false;
    //     projectName = projectName.trim();
    //     if(projectName.length() == 0 || projectName.length() > MAX_PROJECT_NAME_LENGTH)
    //         return false;
    //     //Cannot start with a dash
    //     if(projectName.startsWith("-"))
    //         return false;
    //     //Cannot end with a dash
    //     if(projectName.endsWith("-"))
    //         return false;

    //     //cannot contain periods
    //     if(projectName.contains("."))
    //         return false;

    //     Matcher m = projectNamePattern.matcher(projectName);
    //     if(!m.matches())
    //         return false;

    //     return true;
    // }
}
