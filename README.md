# Java Monitor for OpenStack - Monitor

## What is JMO?
JMO is as a java application for monitoring the resources of a system (which includes OpenStack instances) 
and stores the logs both locally and on the Object Storage service (Swift).

JMO has two components, jmo-monitor which is the main project and jmo-client which allows to automates some operations.

JMO uses a plugin system. It ships with two plugins (one for the CPU and the other one for the RAM) but you can
easily write __your own plugin__ choosing what to log.

You can find the **client** here: github.com/Ocelot92/jmo-client

## How does it do it?
It uses a repository like structure. 
You will create (manually or with the jmo-client) a container "jmo-repository":

		jmo-repository
		├── JMO-config.properties
		├── jmo-monitor.jar
		├── plugins/
		│		├── PluginCPU.class
		│		└── PluginRAM.class
		└── scripts/
				└── PluginCPU.sh

Now when creating a VM to monitor you must initialize it with the Cloud-Init framework by generating a "init" script with jmo-client.

		Ex:
		nova boot --user-data jmo-init.sh --image myimage myinstance --flavor 3

The VMs created  will download the *jmo-monitor.jar* and all the plugins specified in the init script and then they will launch *jmo-monitor.jar*.

Once started, JMO'll loads and runs all the plugins in the **plugins** folder. After that it start writing logs, according to the plugins' rates,
and uploading them to Swift.

There are 2 kind of rate:
* Plugin's rate: which is the rate setted inside the code of the plugin itself.
* The uploading rate (**Readiness**), which specify how often JMO has to upload the logs on Swift. 

## Configuration.
Open the file *JMO-config.propertie* to set JMO's properties. Read the comments for help.

## How to write a plugin.
Writing a plugin it's easy as you can see from the source code of "plugins/PluginCPU.java" and "plugins/PluginRAM.java". You can use those as
templates.

A plugin is a Java class plus an optional script. Optional scripts are stored in the "scripts" folder.
Basically a plugin has to:
* Extend the class IfcPlugin.
* Invoke the super class's constructor (IfcPlugin) inside its own constructor by passing a "BlockingQueue<JMOMessage>" and a "String".
* Set the field rate in its constructor.
* Override the run() method and invoke the sendJMOMessage(InputStream) method inside of it.

Note: the InputStream is the log record you are sending to JMO for logging. You must format it with a date, JMO adds it to the InputStream by itself
before writing the log.

After you wrote the plugin you have to store the class file in the "plugins/" directory (and the optional script in the "scripts/" one). The next time
you'll launch jmo-monitor, it will loads the new plugin.

## How to retrieve the logs
Logs are stored in two places. You can find them locally, in the VMs, under the path: "JMO-Folder/logs/" and on Swift in the "jmo-logs" container
where they are organized per host.
 
You can retrieve the logs from Swift by using the jmo-client which allows you tospecify a range of time.

## Requirements
You need the openstack4j APIs: http://openstack4j.com/