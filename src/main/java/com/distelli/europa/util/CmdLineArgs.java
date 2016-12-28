/*
  $Id: $
  @file CmdLineArgs.java
  @brief Contains the CmdLineArgs.java class

  All Rights Reserved.

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.util;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.log4j.Log4j;

@Log4j
public class CmdLineArgs
{
    private LockingMap _args = null;
    private String _appName = null;
    private int _port = 3000;

    protected static final String NAME_ARG = "name";
    protected static final String PORT_ARG = "port";

    private static final class LockingMap
    {
        private HashMap<String, String> _map = null;
        public LockingMap()
        {
            _map = new HashMap<String, String>();
        }

        public synchronized String get(String key)
        {
            return _map.get(key);
        }

        public synchronized void put(String key, String value)
        {
            _map.put(key, value);
        }

        public synchronized boolean containsKey(String key)
        {
            return _map.containsKey(key);
        }
    }

    public CmdLineArgs()
    {

    }

    public CmdLineArgs(String[] args)
    {
        _args = new LockingMap();
        parseArgs(args);
    }

    /**
       Parses the command line args in args. Alg is simple:

       If the string starts with -- then its an argument.

       If the string doesn't start with -- then its a value for the
       last argument.

       If the last item was not an argument then throw an exception

       args expected:

       --arg=value

       or

       --arg value
    */
    private void parseArgs(String[] args)
    {
        String lastArg = null;
        for(String s : args)
        {
            String arg = s.trim();
            if(arg.length() == 0)
                continue;
            if(arg.trim().equals("="))
                continue;
            else if(arg.indexOf("=") > 0)
                parseCompositeArg(arg);
            else if(arg.startsWith("--"))
            {
                arg = arg.substring(2);
                _args.put(arg, null);
                lastArg = arg;
            }
            else
            {
                if(lastArg == null)
                    throw(new IllegalStateException("Unknown argument for value: "+arg));

                if(_args.get(lastArg) != null)
                    throw(new IllegalStateException("Duplicate values "+arg+" for argument: "+lastArg));

                _args.put(lastArg, arg);
            }
        }
    }

    private void parseCompositeArg(String arg)
    {
        if(!arg.startsWith("--"))
            throw(new IllegalStateException("Invalid arg: "+arg));

        String argPair = arg.substring(2);
        String[] parts = argPair.split("=");
        if(parts.length != 2)
            throw(new IllegalStateException("Invalid arg: "+arg));

        _args.put(parts[0], parts[1]);
    }

    public boolean hasOption(String arg)
    {
        if(_args.containsKey(arg))
            return true;
        return false;
    }

    public String getOption(String arg)
    {
        return _args.get(arg);
    }

    public boolean getLogToConsole()
    {
        return hasOption("log-to-console");
    }

    public String getServiceManifestPath()
    {
        return getOption("manifest");
    }

    public String getName()
    {
        return _appName;
    }

    public int getPort()
    {
        return _port;
    }
}
