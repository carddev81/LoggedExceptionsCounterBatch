REM ##############################################################################################################
REM # S T A R T                                                                                                  #
REM # The following lines are being prepended to this bat file as a way of identifying the user and time of file #
REM # execution. On run of this bat file, a LoggedExceptionsCounterBatchStart-Info.log                           #
REM # file will be created/updated for the purpose of logging this information.                                  #
REM ##############################################################################################################
@echo off
set batName=LoggedExceptionsCounterBatchStart
whoami > whoami.txt
set datestr=%date:~3,3%-%date:~7,2%-%date:~10,4%
set timef=%time:~0,2%:%time:~3,2%:%time:~6,2%
set /P user=< whoami.txt
del whoami.txt
echo -------------------------- >> %batName%-Info.log
echo Executing %batName%
echo Date: %datestr% %timef% >> %batName%-Info.log
echo User:  %user% >> %batName%-Info.log
echo on
REM ###########
REM # E N D   #
REM ###########
echo off
REM ##############################################################################################
REM # ARGUMENTS (Explained)                                                                      #
REM # Parameters and values for application:                                                     #
REM #                                                                                            #
REM # 1) Arguments: <DATE> <ENVIRONMENT>                                                         #
REM #                                                                                            #
REM #    a) <DATE> in YYYY-MM-DD format is first argument and is used for searching              #
REM #       for logs on this particular date. You can also use key word "yesterday"              #
REM #                                                                                            #
REM #    b) <ENVIRONMENT> can be either production, jccc, or test and is the second argument     #
REM #       used to tell the application for which environment logs are to be scanned.           #
REM #                                                                                            #
REM # EXAMPLE JAR COMMANDS:                                                                      #
REM #                                                                                            #
REM # EXAMPLE (without any arguments will default to run getting yesterdays logs for production) #
REM #                                                                                            #
REM #     java -Xms128M -Xmx1024M -jar @filename@                                                #
REM #                                                                                            #
REM # EXAMPLE (with DATE and environment)                                                        #
REM #                                                                                            #
REM #     java -Xms128M -Xmx1024M -jar @filename@ 2015-02-13 production                          #
REM #                                                                                            #
REM # EXAMPLE (with keyword yesterday and environment)                                           #
REM #                                                                                            #
REM #     java -Xms128M -Xmx1024M -jar @filename@ yesterday production                           #
REM ##############################################################################################
REM 2>NUL is appended to the end of the command to REDIRECT output from printing to the console window.
REM currently this program is set to run for yesterday and within production (this is the default settings)
M:\Utilities\java\jre1.8.0_381\bin\java -Xms128M -Xmx1024M -jar @filename@ @environment@ yesterday 2>NUL

