# map-reduce-impl

Word Counter Application with simple map-reduce aproach

This project is a simple word counter application which uses map reduce algorithm. Application writes the words counted to the Mongo db. In order to run the application, MongoDB should be run before the project. 

Running MongoDB:

Windows:
- Run C:\Program Files\MongoDB\Server\3.4\bin\mongod.exe
- After running mongo.exe, run C:\Program Files\MongoDB\Server\3.4\bin\mongo.exe

What is Map Reduce Algorithm?

In order to analyse big text file, we should devide problem into smaller pieces. Map job simply reads and divides word counts in a big text file and seperate it for reduce job. During the mapping process, we will have smaller pieces of big text data under the reduce directory, as you can see from picture:




The first is the map job, which takes a set of data and converts it into another set of data, where individual elements are broken down into tuples (key/value pairs). The reduce job takes the output from a map as input and combines those data tuples into a smaller set of tuples. As the sequence of the name MapReduce implies, the reduce job is always performed after the map job.

