# FMDominoFileWatcher
Watch dir changes to trigger IBM Domino commands and run agents

This Java addin allow to register any number of local system or network folders at OS level and receive change event.

These events launch one or more IBM Domino command each. You can configure any number of pair "folder-command".

When new files are created in the configured folders, the operating system "warns" our code that triggers a configured command, which can be different for each monitored folder.

We could configure the folders c:/filepdf/, d:/otherFolder/, so that when they receive new files, the Domino command line is:
- tell amgr run "tools/db1.nsf" 'Read PDF File'
- tell amgr run "tools/db2.nsf" 'other command'

Every request to execute the Domino command start a Thread that wait 5 second to allow new file arrive. If new files arrive, the thread is terminated and a newer thread is started. So only when there are 5 seconds without new activity the command run.  

This way if we have 10, 100, 1000 new files in a bunch of seconds there is not a storm of commands that launch the same agent.

This class, plus some collaborating companions, are developed to work on a substrate formed by a wonderful work by Andy Brunner:

https://github.com/AndyBrunner/Domino-JAddin

---------------------------------------

How to install and configure FMDominoFileWatcher

Download from here
https://github.com/fmarzolo/FMDominoFileWatcher/releases/

1) put into the Domino executable files folder expanding the prerequisite Jaddin*.zip files
2) put the FM*.class file in the same path
3) put the fmWatchCompanion.jar file in folder \Domino\jvm\lib\ext 
4) insert in Notes.ini as many pairs of lines as needed to indicate the path of the folders to be monitored and the syntax of the respective commands to be launched with the following example:
FMwatchdir_Folder=c:/temp
FMwatchdir_Command=tell amgr run "names.nsf" 'agent1'
FMwatchdir_Folder1=//nas4/public/Software
FMwatchdir_Command1=tell amgr run "tools/db2.nsf" 'agent2'
FMwatchdir_Folder2=c:/temp
FMwatchdir_Command2=tell amgr run "tools/db3.nsf" 'agent3'

Please note: the default Domino Windows service starts with LocalSystem rights and therefore maybe does not have network access, so it can not see paths other than local disks. It may require to create a user with network access, and tell Domino service to login with it, set the user name in service properties.

If you need more than 10 (default) pairs "folder-command" you can set any maximum number with the syntax {-varsMax n}, for example:
-varsMax 200

If you need more verbose logging you can specify:
-loglevel x
replacing x with an integer as:
	0: no logging (default)
	1: terse logging
	5: normal logging
	7: verbose logging
	9: trace, very verbose logging
	
If you need shorter loop between launch commands:
- waitseconds x
replacing x with a number that indicates the number of seconds to wait between loops of check threads.
Please note, these loops are free of charge for the CPU, so is not relevant for CPU to have longer or shorter time to wait. The number 0 is not valid.

4) Launch execution (pay attention to casing that in java is relevant)
load runjava JAddin FMWatcher

5) Debugging. You can set a high debug through two levels of debugging setting
debugging JAddin: load runjava JAddin FMWatcher Debug!
debugging FMWatcher: load runjava JAddin FMWatcher -debug
	(this is the same then use "load runjava JAddin FMWatcher -loglevel 9"
debugging of both: load runjava JAddin FMWatcher Debug! -debug
	(this is the same then use "load runjava JAddin FMWatcher Debug! -loglevel 9"

6) Launch at server startup
To start the application automatically when the Domino server starts, you can add notes.ini to the end of the line:
SERVERTASKS = xxxx, xxx, xxx, load runjava JAddin FMWatcher

7) To modify runtime logging level
To modify runtime the logging level you can use the following command, use the above values for x:
	tell FMWatcher -loglevel x
	
	
Caveats

It may happen that the changes to the folders to be monitored are continuous throughout the day and are never separated from each other by 15 seconds. In this case the waiting timer would never expire, and it could happen that the configured command is not launched. May be usefull trye to set shorter loop by setting "-waitseconds 5"

Contacts
Francesco Marzolo
checco.marzolo (at) gmail
If you liked this job you can give me a pepperoni pizza, which I love:

Bibliography and references for those who want to deepen

Why it is important to use the Recycle () in java/Domino 
http://www-01.ibm.com/support/docview.wss?uid=swg21097861

Source code
https://github.com/fmarzolo/FMDominoFileWatcher

Prerequisite
https://github.com/AndyBrunner/Domino-JAddin
http://abdata.ch

Take a Trip Into the Forest: A Java Primer on Maps, Trees, and Collections
https://youtu.be/Ln-meA0WXaw

Another example of using WatchService via NIO
https://www.thecoderscorner.com/team-blog/java-and-jvm/java-nio/36-watching-files-in-java-7-with-watchservice/

Open Mic Webcast: Multi-threaded designing Domino Java applications
https://www-01.ibm.com/support/docview.wss?uid=swg27037716

Rust Language, mon amour! Stanford seminar
https://www.youtube.com/watch?v=O5vzLKg7y-k