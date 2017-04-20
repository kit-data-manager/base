#!/bin/sh

#Variable definition for accessing/storing data. The placeholder variables are replaced by the workflow service using the appropriate values for the 
#according task execution and execution environment. 
export INPUT_DIR=${data.input.dir}
export OUTPUT_DIR=${data.output.dir}
export WORKING_DIR=${working.dir}
export TEMP_DIR=${temp.dir}

#Place environment checks here, if necessary, to allow better debugging. However, if something is missing at this point, the process will fail 
#either way.  

#Now, the execution starts. The variables above should be provided to the process in a proper way depending on the kind of the process, e.g. 
#for Java processes via -DINPUT_DIR=$INPUT_DIR 
echo "This is a sample workflow task execution script." >> $OUTPUT_DIR/out.log
echo "Provided directory variables are:" >> $OUTPUT_DIR/out.log
echo "Input Dir: $INPUT_DIR" >> $OUTPUT_DIR/out.log
echo "Output Dir: $OUTPUT_DIR" >> $OUTPUT_DIR/out.log
echo "Working Dir: $WORKING_DIR" >> $OUTPUT_DIR/out.log
echo "Temp Dir: $TEMP_DIR" >> $OUTPUT_DIR/out.log

#Obtain the exit code of the process, print out a logging message for debugging, and exit the wrapper script using the exit code of the internal
#process to allow a proper handling by the workflow service. If the exit code is 0 the data ingest of everything stored in OUTPUT_DIR is triggered.
#Otherwise, the task will remain in an error state and needs user interaction.
EXIT=$?
echo "Execution finished."
exit $EXIT
