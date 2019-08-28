# um-latency-tester
Simple testing app to publish and subscribe to documents to measure latency and data integrity for UM

## Build requirements
 - Add nClient.jar from your UM instance to your local maven repo, adjusting versions as needed:
 `mvn install:install-file -Dfile=nClient.jar -DgroupId=com.softwareag -DartifactId=nClient -Dversion=9.12.0 -Dpackaging=jar -DgeneratePom=true`
 - create and deploy java service on Integration Service to extract protobuf definitions, referring to getProtoDef.frag
 
## Build process
Example is already setup with the input, so steps 1-3 are done alread
 1. Run the wM IS service, getProtoDef, with the input document and save the output to src/main/resources/protobuf as %message%.proto (e.g., AmGlDocs_Event_EventUDM.proto)
 2. Generate message java classes by running `mvn generate-sources`
 3. Update message generation to use new java classes (more below, see Protocol Buffers)
 4. Compile jar file by running `mvn package`
 
 ## Running the example
Run the script, substituting the UM realm (RNAME) and document channel (CHANNAME) as needed:
 `runPublisher.bat CHANNAME:wm/is/AmGlDocs/Event/EventUDM RNAME:nhp://uslxd10600a:9000 COUNT:1`
Paramenters
 1. rname - UM realm URL to connect with
 2. channel - channel to publish or subscribe to
 3. mode - either pub or sub or both
 4. count - optional, must be set if runtimeseconds is not set, number of documents to publish
 5. runtimeseconds - optional, must be set if count is not set, time to wait between publishes
 5. delayms - optional, time to wait between publishes
 6. size - size of payload to publish in bytes, if publishing
 7. startseq - option, sequence number to start publishing at (long)
Note:  The name for the channel should always match the value for the document property called Provider definition (e.g."wm/is/AmGlDocs/Event/EventUDM")
